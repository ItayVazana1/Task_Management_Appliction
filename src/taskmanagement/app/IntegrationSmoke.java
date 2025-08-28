package taskmanagement.app;

import taskmanagement.domain.ITask;
import taskmanagement.domain.Task;
import taskmanagement.domain.TaskState;
import taskmanagement.domain.exceptions.ValidationException;

// Combinator filters
import taskmanagement.domain.filter.ITaskFilter;
import static taskmanagement.domain.filter.Filters.*;

// Visitor + reports + adapters
import taskmanagement.domain.visitor.CountByStateVisitor;
import taskmanagement.domain.visitor.reports.ByStateCount;
import taskmanagement.domain.visitor.adapters.ByStateCsvExporter;
import taskmanagement.domain.visitor.adapters.ByStatePlainTextExporter;
import taskmanagement.domain.visitor.adapters.IReportExporter;

// DAO
import taskmanagement.persistence.DAOProvider;
import taskmanagement.persistence.ITasksDAO;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

/**
 * Comprehensive integration smoke:
 * - Model validation
 * - DAO CRUD and edge cases
 * - Filters (Combinator)
 * - Visitor reports + adapters
 */
public final class IntegrationSmoke {

    public static void main(String[] args) throws Exception {
        System.out.println("== Integration Smoke (Domain + Filters + Visitor + Derby DAO) ==");

        // MODEL sanity checks
        testModelValidation();

        // DAO CRUD (including edge cases)
        ITasksDAO dao = DAOProvider.tasksDAO();
        testDaoCrud(dao);

        // Filters (Combinator)
        testFilters(dao);

        // Visitor + Adapters
        testVisitorAndAdapters(dao);

        System.out.println("\nAll integration steps passed ✔");
    }

    private static void testModelValidation() {
        try {
            new Task(99, "", "desc", TaskState.ToDo);
            throw new AssertionError("Empty title should have failed");
        } catch (ValidationException expected) { }
        try {
            new Task(-1, "Negative id", "desc", TaskState.ToDo);
            throw new AssertionError("Negative id should have failed");
        } catch (ValidationException expected) { }
    }

    private static void testDaoCrud(ITasksDAO dao) {
        dao.deleteTasks(); // start clean

        ITask t1 = safeTask(1, "Buy milk", "Go to store", TaskState.ToDo);
        ITask t2 = safeTask(2, "Write tests", "Visitor/Filters", TaskState.InProgress);
        ITask t3 = safeTask(3, "Ship", "Create JAR", TaskState.Completed);

        dao.addTask(t1);
        dao.addTask(t2);
        dao.addTask(t3);

        ITask[] all = dao.getTasks();
        assertTrue(all.length == 3, "Expected 3 tasks after insert, got " + all.length);

        // Update + verify
        ITask t2u = safeTask(2, "Write tests", "Visitor/Filters", TaskState.Completed);
        dao.updateTask(t2u);
        assertTrue(dao.getTask(2).getState() == TaskState.Completed, "Task #2 should be Completed");

        // Delete single + verify
        dao.deleteTask(1);
        assertTrue(dao.getTasks().length == 2, "Expected 2 tasks after delete, got " + dao.getTasks().length);

        // Delete all + verify
        dao.deleteTasks();
        assertTrue(dao.getTasks().length == 0, "Expected 0 tasks after deleteAll");
    }

    private static void testFilters(ITasksDAO dao) {
        dao.deleteTasks();
        dao.addTask(safeTask(10, "Alpha", "first", TaskState.ToDo));
        dao.addTask(safeTask(11, "Beta", "second", TaskState.Completed));
        dao.addTask(safeTask(12, "Gamma", "third", TaskState.Completed));
        dao.addTask(safeTask(13, "Delta", "fourth", TaskState.InProgress));

        ITaskFilter completed = byState(TaskState.Completed);
        ITaskFilter titleHasA = titleContains("a");

        long completedCount = Arrays.stream(dao.getTasks()).filter(completed::test).count();
        long combinedCount = Arrays.stream(dao.getTasks()).filter(completed.and(titleHasA)::test).count();

        assertTrue(completedCount == 2, "Expected 2 completed tasks");
        assertTrue(combinedCount >= 1, "Expected at least one Completed+title contains 'a'");
    }

    private static void testVisitorAndAdapters(ITasksDAO dao) {
        CountByStateVisitor visitor = new CountByStateVisitor();
        for (ITask t : dao.getTasks()) t.accept(visitor);
        ByStateCount report = visitor.result();

        IReportExporter<ByStateCount> plain = new ByStatePlainTextExporter();
        IReportExporter<ByStateCount> csv = new ByStateCsvExporter();

        System.out.println("\n-- Visitor Report (Plain) --");
        System.out.println(plain.export(report));

        System.out.println("-- Visitor Report (CSV) --");
        System.out.println(csv.export(report));

        // Basic assertion
        assertTrue(report.total() == dao.getTasks().length, "Visitor total should equal DAO count");
    }

    // ---- helpers ----
    private static ITask safeTask(int id, String title, String desc, TaskState s) {
        try {
            return new Task(id, title, desc, s);
        } catch (ValidationException ve) {
            throw new IllegalStateException("Validation failed for task id=" + id, ve);
        }
    }

    private static void assertTrue(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
    }
}
