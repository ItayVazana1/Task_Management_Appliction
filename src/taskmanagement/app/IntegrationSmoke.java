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

public final class IntegrationSmoke {
    public static void main(String[] args) throws Exception {
        System.out.println("== Integration Smoke (Domain + Filters + Visitor + Derby DAO) ==");

        // 1) MODEL sanity (constructor validation)
        ITask t1 = safeTask(1, "Buy milk", "Go to store", TaskState.ToDo);
        ITask t2 = safeTask(2, "Write tests", "Visitor/Filters", TaskState.InProgress);
        ITask t3 = safeTask(3, "Ship", "Create JAR", TaskState.Completed);

        // 2) DAO bootstrap + CRUD
        ITasksDAO dao = DAOProvider.tasksDAO();
        dao.deleteTasks(); // clean start

        dao.addTask(t1);
        dao.addTask(t2);
        dao.addTask(t3);

        ITask[] all = dao.getTasks();
        assertTrue(all.length == 3, "Expected 3 tasks after insert, got " + all.length);

        // update task #2 -> Completed
        ITask t2u = safeTask(2, t2.getTitle(), t2.getDescription(), TaskState.Completed);
        dao.updateTask(t2u);

        ITask got2 = dao.getTask(2);
        assertTrue(got2 != null && got2.getState() == TaskState.Completed, "Task #2 should be Completed");

        // 3) COMBINATOR filters (using ITaskFilter)
        ITaskFilter completed = byState(TaskState.Completed);
        ITaskFilter titleHasI = titleContains("i"); // e.g., "Ship", "Write"
        ITaskFilter combo = completed.or(titleHasI);

        long matched = Arrays.stream(dao.getTasks()).filter(combo::test).count();
        System.out.println("Filter matched count = " + matched);

        // 4) VISITOR + ADAPTER exporters (using IReportExporter)
        CountByStateVisitor visitor = new CountByStateVisitor();
        for (ITask t : dao.getTasks()) {
            t.accept(visitor);
        }
        ByStateCount report = visitor.result();

        IReportExporter<ByStateCount> plain = new ByStatePlainTextExporter();
        IReportExporter<ByStateCount> csv = new ByStateCsvExporter();

        System.out.println("\n-- Visitor Report (Plain) --");
        System.out.println(plain.export(report));

        System.out.println("-- Visitor Report (CSV) --");
        System.out.println(csv.export(report));

        // 5) DELETE one + list again
        dao.deleteTask(1);
        ITask[] afterDelete = dao.getTasks();
        assertTrue(afterDelete.length == 2, "Expected 2 tasks after delete, got " + afterDelete.length);

        System.out.println("\nAll integration steps passed ✔");
    }

    // ---- helpers ----
    private static ITask safeTask(int id, String title, String desc, TaskState s) {
        try {
            return new Task(id, title, desc, s);
        } catch (ValidationException ve) {
            throw new IllegalStateException("Validation failed for task id=" + id + ": " + ve.getMessage(), ve);
        }
    }

    private static void assertTrue(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
    }
}
