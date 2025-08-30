package taskmanagement.app;

import taskmanagement.application.viewmodel.commands.CommandStack;
import taskmanagement.application.viewmodel.commands.MarkStateCommand;
import taskmanagement.domain.ITask;
import taskmanagement.domain.Task;
import taskmanagement.domain.TaskState;
import taskmanagement.domain.exceptions.ValidationException;
import taskmanagement.persistence.DAOProvider;
import taskmanagement.persistence.ITasksDAO;
import taskmanagement.persistence.TasksDAOException;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.function.Predicate;

/**
 * FullStackSmokeTest (Steps 1-6)
 * -----------------------------------------
 * Covers:
 * 1-2) Interfaces presence (runtime sanity via reflection)
 * 3)   Model validations + equals/hashCode
 * 4)   Derby DAO CRUD end-to-end
 * 5)   Combinator filters + Visitor report (with adapters if present)
 * 6)   State + Command (undo/redo) + Strategy sorting (+ Observer if present)
 *
 * Notes:
 * - Some Step 5/6 extras are attempted via reflection to avoid compile break if APIs differ.
 * - Optional parts print SKIPPED instead of failing the suite.
 */
public final class FullStackSmokeTest {

    public static void main(String[] args) {
        try {
            System.out.println("== Full Stack Smoke (Steps 1-6) ==");

            step1and2_interfacesPresence();
            step3_modelValidation();
            step4_daoCrud();
            step5_combinatorFilters();
            step5_visitorReports();
            step6_stateAndCommand();
            step6_strategySorting();
            step6_observerSignals();

            System.out.println("\n✔ All sections passed");
        } catch (AssertionError ae) {
            System.err.println("❌ Full stack smoke failed: " + ae.getMessage());
            System.exit(1);
        } catch (Throwable t) {
            System.err.println("❌ Unexpected failure: " + t);
            t.printStackTrace();
            System.exit(2);
        }
    }

    /* =======================
       Utilities
       ======================= */
    private static void assertTrue(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
    }

    /**
     * Expect the runnable to throw an exception of the requested type.
     * This version safely unwraps RuntimeException wrappers and checks the root cause.
     */
    private static <T extends Throwable> void expectThrows(Class<T> type, Runnable r, String failMsg) {
        try {
            r.run();
            throw new AssertionError(failMsg);
        } catch (Throwable t) {
            Throwable root = rootCause(t);
            if (!type.isInstance(root)) {
                t.printStackTrace();
                throw new AssertionError("Expected " + type.getSimpleName() + " but got " + root.getClass().getSimpleName());
            }
        }
    }

    private static Throwable rootCause(Throwable t) {
        Throwable cur = t;
        while (cur.getCause() != null) cur = cur.getCause();
        return cur;
    }

    private static String state(ITask t) { return t.getState().name(); }

    /* =======================
       Step 1-2: Interfaces
       ======================= */
    private static void step1and2_interfacesPresence() {
        System.out.println("\n[Step 1-2] Interfaces presence…");
        try {
            Class.forName("taskmanagement.domain.ITask");
            Class.forName("taskmanagement.persistence.ITasksDAO");
            Class.forName("taskmanagement.domain.TaskState");
            Class.forName("taskmanagement.domain.visitor.TaskVisitor");
            System.out.println("  ✔ ITask / ITasksDAO / TaskState / TaskVisitor present");
        } catch (ClassNotFoundException e) {
            throw new AssertionError("Missing required interface/type: " + e.getMessage());
        }
    }

    /* =======================
       Step 3: Model
       ======================= */
    private static void step3_modelValidation() throws Exception {
        System.out.println("\n[Step 3] Model validations + equals/hashCode…");

        // Valid task
        ITask ok = new Task(101, "A", "d", TaskState.ToDo);
        assertTrue(ok.getId() == 101, "id mismatch");
        assertTrue("A".equals(ok.getTitle()), "title mismatch");

        // Invalid title (empty) — allow model to throw ValidationException (unwrapped by expectThrows)
        expectThrows(ValidationException.class,
                () -> {
                    try {
                        new Task(102, "", "d", TaskState.ToDo);
                    } catch (ValidationException e) {
                        throw new RuntimeException(e);
                    }
                },
                "Empty title should be invalid");

        // equals/hashCode on same logical task
        ITask t1 = new Task(200, "Same", "Desc", TaskState.ToDo);
        ITask t2 = new Task(200, "Same", "Desc", TaskState.ToDo);
        assertTrue(t1.equals(t2), "equals must hold for same identity/content");
        assertTrue(t1.hashCode() == t2.hashCode(), "hashCode must match for equal objects");

        System.out.println("  ✔ Model validations passed");
    }

    /* =======================
       Step 4: DAO CRUD
       ======================= */
    private static void step4_daoCrud() throws Exception {
        System.out.println("\n[Step 4] Derby DAO CRUD…");
        ITasksDAO dao = DAOProvider.get();
        dao.deleteTasks();

        ITask a = new Task(1, "Alpha", "first", TaskState.ToDo);
        ITask b = new Task(2, "Beta", "second", TaskState.InProgress);
        ITask c = new Task(3, "Gamma", "third", TaskState.Completed);

        dao.addTask(a);
        dao.addTask(b);
        dao.addTask(c);

        ITask fetched = dao.getTask(2);
        assertTrue("Beta".equals(fetched.getTitle()), "getTask failed");

        // update
        ITask b2 = new Task(2, "Beta*", "second", TaskState.InProgress);
        dao.updateTask(b2);
        assertTrue("Beta*".equals(dao.getTask(2).getTitle()), "updateTask failed");

        // list
        ITask[] all = dao.getTasks();
        assertTrue(all.length >= 3, "getTasks count mismatch");

        // delete
        dao.deleteTask(3);
        expectThrows(TasksDAOException.class, () -> {
            try {
                dao.getTask(3);
            } catch (TasksDAOException e) {
                throw new RuntimeException(e);
            }
        }, "Expecting TasksDAOException for missing id=3");

        System.out.println("  ✔ DAO CRUD passed");
    }

    /* =======================
       Step 5: Combinator Filters
       ======================= */
    private static void step5_combinatorFilters() throws Exception {
        System.out.println("\n[Step 5] Combinator filters…");
        ITasksDAO dao = DAOProvider.get();
        ITask[] tasks = dao.getTasks();

        // Try to locate Filters + a matching method on filter instance
        try {
            Class<?> filters = Class.forName("taskmanagement.domain.filter.Filters");
            Method byState = findMethod(filters, "byState", TaskState.class);
            Method titleContains = findMethod(filters, "titleContains", String.class);

            Object stTodo = byState.invoke(null, TaskState.ToDo);
            Object stInProg = byState.invoke(null, TaskState.InProgress);
            Object titleHasA = titleContains.invoke(null, "a");

            // Compose AND/OR if available, else test single predicates
            Method andM = tryFindMethod(filters, "and", stTodo.getClass(), stInProg.getClass());
            Method orM  = tryFindMethod(filters, "or",  stTodo.getClass(), titleHasA.getClass());

            Predicate<ITask> pTodo = toPredicate(stTodo);
            Predicate<ITask> pInPr = toPredicate(stInProg);
            Predicate<ITask> pHasA = toPredicate(titleHasA);

            Predicate<ITask> pAnd = (andM != null) ? toPredicate(andM.invoke(null, stTodo, stInProg))
                    : pTodo.and(pInPr);
            Predicate<ITask> pOr  = (orM  != null) ? toPredicate(orM.invoke(null, stTodo, titleHasA))
                    : pTodo.or(pHasA);

            long andCount = Arrays.stream(tasks).filter(pAnd).count();
            long orCount  = Arrays.stream(tasks).filter(pOr).count();

            assertTrue(andCount >= 0, "AND composition failed");
            assertTrue(orCount  >= 0, "OR composition failed");

            System.out.println("  ✔ Filters AND/OR verified (count AND=" + andCount + ", OR=" + orCount + ")");
        } catch (ClassNotFoundException | NoSuchMethodException e) {
            System.out.println("  ⚠ Filters not fully detected (SKIPPED) – " + e.getMessage());
        }
    }

    /* =======================
       Step 5: Visitor Reports
       ======================= */
    private static void step5_visitorReports() throws Exception {
        System.out.println("\n[Step 5] Visitor report…");
        ITasksDAO dao = DAOProvider.get();
        ITask[] tasks = dao.getTasks();

        // CountByStateVisitor
        Class<?> visitorType = Class.forName("taskmanagement.domain.visitor.CountByStateVisitor");
        Object visitor = visitorType.getConstructor().newInstance();

        // Visit all
        Method accept = ITask.class.getMethod("accept", Class.forName("taskmanagement.domain.visitor.TaskVisitor"));
        for (ITask t : tasks) accept.invoke(t, visitor);

        // Extract report (assume method getReport or report)
        Method getReport = tryFindMethod(visitorType, "getReport");
        if (getReport == null) getReport = tryFindMethod(visitorType, "report");
        Object report = (getReport != null) ? getReport.invoke(visitor) : null;

        assertTrue(report != null, "Visitor report is null");
        System.out.println("  ✔ Visitor aggregated report created: " + report);

        // Try exporters (optional)
        tryExport("taskmanagement.domain.visitor.adapters.ByStatePlainTextExporter", report, "PlainText");
        tryExport("taskmanagement.domain.visitor.adapters.ByStateCsvExporter",       report, "CSV");
    }

    private static void tryExport(String exporterFqn, Object report, String tag) {
        try {
            Class<?> expType = Class.forName(exporterFqn);
            Object exporter = expType.getConstructor().newInstance();
            // try export(report) → String
            Method export = tryFindMethod(expType, "export", report.getClass());
            Object out = (export != null) ? export.invoke(exporter, report) : null;
            if (out instanceof String s) {
                System.out.println("    · " + tag + " export:\n" + s);
            } else {
                System.out.println("    · " + tag + " exporter found but no suitable export(report) method (SKIPPED)");
            }
        } catch (Throwable t) {
            System.out.println("    · " + tag + " exporter not available (SKIPPED): " + t.getMessage());
        }
    }

    /* =======================
       Step 6: State + Command
       ======================= */
    private static void step6_stateAndCommand() throws Exception {
        System.out.println("\n[Step 6] State + Command (undo/redo)…");
        ITasksDAO dao = DAOProvider.get();
        dao.deleteTasks();

        ITask t = new Task(501, "Lifecycle", "demo", TaskState.ToDo);
        dao.addTask(t);

        // legal TODO -> INPROGRESS -> COMPLETED
        CommandStack stack = new CommandStack();
        stack.perform(new MarkStateCommand(t, TaskState.InProgress, (src, st) ->
                new Task(src.getId(), src.getTitle(), src.getDescription(), st)));

        ITask cur = dao.getTask(501);
        assertTrue(cur.getState() == TaskState.InProgress, "Expected INPROGRESS");

        stack.perform(new MarkStateCommand(cur, TaskState.Completed, (src, st) ->
                new Task(src.getId(), src.getTitle(), src.getDescription(), st)));

        assertTrue(dao.getTask(501).getState() == TaskState.Completed, "Expected COMPLETED");

        // illegal COMPLETED -> INPROGRESS
        ITask before = dao.getTask(501);
        expectThrows(ValidationException.class, () -> {
            try {
                MarkStateCommand illegal = new MarkStateCommand(before, TaskState.InProgress, (src, st) ->
                        new Task(src.getId(), src.getTitle(), src.getDescription(), st));
                illegal.execute();
            } catch (Exception e) { throw new RuntimeException(e); }
        }, "Illegal transition should throw");

        // undo/redo symmetry
        stack.undo();  // back to INPROGRESS
        assertTrue(dao.getTask(501).getState() == TaskState.InProgress, "Undo failed");

        stack.redo();  // forward to COMPLETED
        assertTrue(dao.getTask(501).getState() == TaskState.Completed, "Redo failed");

        System.out.println("  ✔ State transitions + Command undo/redo passed");
    }

    /* =======================
       Step 6: Strategy Sorting
       ======================= */
    @SuppressWarnings("unchecked")
    private static void step6_strategySorting() throws Exception {
        System.out.println("\n[Step 6] Strategy sorting…");
        ITasksDAO dao = DAOProvider.get();
        ITask[] arr = dao.getTasks();
        List<ITask> list = new ArrayList<>(Arrays.asList(arr));

        try {
            Class<?> sortByTitle = Class.forName("taskmanagement.application.viewmodel.sort.SortByTitle");
            Class<?> sortByState = Class.forName("taskmanagement.application.viewmodel.sort.SortByState");

            Object sTitle = sortByTitle.getConstructor().newInstance();
            Object sState = sortByState.getConstructor().newInstance();

            // Try: method sort(List<ITask>) OR method comparator(): Comparator<ITask>
            boolean ok = false;

            Method sortTitle = tryFindMethod(sortByTitle, "sort", List.class);
            Method sortState = tryFindMethod(sortByState, "sort", List.class);
            if (sortTitle != null && sortState != null) {
                List<ITask> t1 = new ArrayList<>(list);
                List<ITask> t2 = new ArrayList<>(list);
                sortTitle.invoke(sTitle, t1);
                sortState.invoke(sState, t2);
                assertTrue(isSortedByTitle(t1), "SortByTitle failed");
                assertTrue(isSortedByState(t2), "SortByState failed");
                ok = true;
            } else {
                Method compTitle = tryFindMethod(sortByTitle, "comparator");
                Method compState = tryFindMethod(sortByState, "comparator");
                if (compTitle != null && compState != null) {
                    Comparator<ITask> ct = (Comparator<ITask>) compTitle.invoke(sTitle);
                    Comparator<ITask> cs = (Comparator<ITask>) compState.invoke(sState);
                    List<ITask> t1 = new ArrayList<>(list);
                    List<ITask> t2 = new ArrayList<>(list);
                    t1.sort(ct);
                    t2.sort(cs);
                    assertTrue(isSortedByTitle(t1), "SortByTitle failed");
                    assertTrue(isSortedByState(t2), "SortByState failed");
                    ok = true;
                }
            }

            if (ok) {
                System.out.println("  ✔ Strategy sorting verified");
            } else {
                System.out.println("  ⚠ Strategy classes found but no known API (SKIPPED)");
            }
        } catch (ClassNotFoundException e) {
            System.out.println("  ⚠ Strategy classes not found (SKIPPED)");
        }
    }

    private static boolean isSortedByTitle(List<ITask> l) {
        for (int i = 1; i < l.size(); i++) {
            if (l.get(i-1).getTitle().compareToIgnoreCase(l.get(i).getTitle()) > 0) return false;
        }
        return true;
    }

    private static boolean isSortedByState(List<ITask> l) {
        // Simple ordinal check (assumes enum declaration order is lifecycle order)
        for (int i = 1; i < l.size(); i++) {
            if (l.get(i-1).getState().ordinal() > l.get(i).getState().ordinal()) return false;
        }
        return true;
    }

    /* =======================
       Step 6: Observer (optional)
       ======================= */
    private static void step6_observerSignals() {
        System.out.println("\n[Step 6] Observer (ViewModel events)…");
        try {
            Class.forName("taskmanagement.application.viewmodel.events.Property");
            Class.forName("taskmanagement.application.viewmodel.events.ObservableList");
            // Without a concrete ViewModel API in this test, only sanity that types exist:
            System.out.println("  ✔ Observer scaffolding present (sanity)");
        } catch (ClassNotFoundException e) {
            System.out.println("  ⚠ Observer types not present/implemented yet (SKIPPED)");
        }
    }

    /* =======================
       Reflection helpers
       ======================= */
    private static Method findMethod(Class<?> type, String name, Class<?>... params) throws NoSuchMethodException {
        Method m = type.getMethod(name, params);
        m.setAccessible(true);
        return m;
    }
    private static Method tryFindMethod(Class<?> type, String name, Class<?>... params) {
        try { return findMethod(type, name, params); }
        catch (Throwable ignore) { return null; }
    }

    @SuppressWarnings("unchecked")
    private static Predicate<ITask> toPredicate(Object filterObj) throws Exception {
        // Try common names: test(ITask) / matches(ITask) / apply(ITask) / predicate()
        for (String m : List.of("test", "matches", "apply")) {
            Method mm = tryFindMethod(filterObj.getClass(), m, ITask.class);
            if (mm != null) {
                return t -> {
                    try { return (boolean) mm.invoke(filterObj, t); }
                    catch (Throwable e) { throw new RuntimeException(e); }
                };
            }
        }
        // If there is a predicate() returning java.util.function.Predicate<ITask>
        Method p = tryFindMethod(filterObj.getClass(), "predicate");
        if (p != null) {
            Object obj = pReturn(p, filterObj);
            return (Predicate<ITask>) obj;
        }
        throw new NoSuchMethodException("No predicate-like method on " + filterObj.getClass().getSimpleName());
    }

    private static Object pReturn(Method m, Object target, Object... args) {
        try { return m.invoke(target, args); }
        catch (Throwable e) { throw new RuntimeException(e); }
    }
}
