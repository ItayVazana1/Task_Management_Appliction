package taskmanagement.app;

import taskmanagement.domain.ITask;
import taskmanagement.domain.Task;
import taskmanagement.domain.TaskState;
import taskmanagement.domain.exceptions.ValidationException;
import taskmanagement.domain.filter.ITaskFilter;
import taskmanagement.domain.visitor.CountByStateVisitor;
import taskmanagement.domain.visitor.reports.ByStateCount;
import taskmanagement.domain.visitor.adapters.ByStateCsvExporter;
import taskmanagement.domain.visitor.adapters.ByStatePlainTextExporter;
import taskmanagement.domain.visitor.adapters.IReportExporter;
import taskmanagement.persistence.TasksDAOException;
import taskmanagement.persistence.derby.EmbeddedDerbyTasksDAO;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Comparator;

import static taskmanagement.domain.filter.Filters.*;

public final class FullStackSmokeTest {

    public static void main(String[] args) {
        System.out.println("== Full Stack Smoke Test ==");

        try (EmbeddedDerbyTasksDAO dao = EmbeddedDerbyTasksDAO.getInstance()) {
            // 0) Clean slate
            dao.deleteTasks();

            // 1) Create sample tasks (validations happen inside Task)
            ITask t1 = new Task(1, "Write spec", "Requirements + style", TaskState.ToDo);
            ITask t2 = new Task(2, "Implement DAO", "Derby embedded CRUD", TaskState.InProgress);
            ITask t3 = new Task(3, "Wire Visitor", "Counts by state + exporters", TaskState.Completed);
            ITask t4 = new Task(4, "Filters", "Combinator AND/OR", TaskState.InProgress);

            // 2) Insert
            dao.addTask(t1);
            dao.addTask(t2);
            dao.addTask(t3);
            dao.addTask(t4);

            // 3) Read all (order by id)
            ITask[] all = dao.getTasks();
            require(all.length == 4, "Expected 4 tasks after insert, got " + all.length);
            require(all[0].getId() == 1 && all[3].getId() == 4, "Tasks not ordered by id as expected");

            // 4) Read one
            ITask got2 = dao.getTask(2);
            require(got2 != null && got2.getTitle().contains("DAO"), "getTask(2) failed");

            // 5) Update
            Task updated2 = new Task(2, "Implement DAO", "CRUD + errors mapping", TaskState.Completed);
            dao.updateTask(updated2);
            ITask got2b = dao.getTask(2);
            require(got2b.getState() == TaskState.Completed, "updateTask(2) didn't persist new state");

            // 6) Combinator filters
            //    (title contains 'i' case-insensitive) AND (state == InProgress)
            ITaskFilter inProgress = byState(TaskState.InProgress);
            ITaskFilter titleHasI = byTitleContainsIgnoreCase("i");
            ITask[] filtered = Arrays.stream(dao.getTasks())
                    .filter(inProgress.and(titleHasI)::test)
                    .toArray(ITask[]::new);
            // t4 is InProgress + has 'i' in title "Filters"
            require(filtered.length == 1 && filtered[0].getId() == 4, "Combinator AND failed");

            // 7) Visitor report (count by state)
            CountByStateVisitor visitor = new CountByStateVisitor();
            for (ITask t : dao.getTasks()) {
                t.accept(visitor);
            }
            ByStateCount report = visitor.result();
            // Now we have: ToDo:1 (t1), InProgress:1 (t4), Completed:2 (t2,t3)
            require(report.count(TaskState.ToDo) == 1, "Visitor count ToDo mismatch");
            require(report.count(TaskState.InProgress) == 1, "Visitor count InProgress mismatch");
            require(report.count(TaskState.Completed) == 2, "Visitor count Completed mismatch");

            // 8) Adapters (exporters)
            Path outDir = Path.of("build-smoke");
            Files.createDirectories(outDir);
            Path txt = outDir.resolve("by_state.txt");
            Path csv = outDir.resolve("by_state.csv");

            IReportExporter<ByStateCount> txtExporter = new ByStatePlainTextExporter();
            IReportExporter<ByStateCount> csvExporter = new ByStateCsvExporter();

            Files.writeString(txt, txtExporter.export(report));
            Files.writeString(csv, csvExporter.export(report));

            require(Files.size(txt) > 0, "PlainText export is empty");
            require(Files.size(csv) > 0, "CSV export is empty");

            // 9) Delete one
            dao.deleteTask(3);
            require(dao.getTask(3) == null, "deleteTask(3) failed");

            // 10) Final listing (sorted by title using Strategy later; here just check DB state)
            ITask[] finalAll = dao.getTasks();
            require(finalAll.length == 3, "Expected 3 tasks after delete, got " + finalAll.length);

            // Optional: deterministic order for printing
            Arrays.sort(finalAll, Comparator.comparingInt(ITask::getId));
            System.out.println("-- Remaining tasks --");
            for (ITask t : finalAll) {
                System.out.printf("%d | %s | %s | %s%n",
                        t.getId(), t.getTitle(), t.getDescription(), t.getState());
            }

            System.out.println("\n✔ Full Stack Smoke Test passed");
            System.out.println("Exports: " + txt.toAbsolutePath() + " , " + csv.toAbsolutePath());
        } catch (ValidationException ve) {
            fail("Validation failed: " + ve.getMessage(), ve);
        } catch (TasksDAOException de) {
            fail("DAO error: " + de.getMessage(), de);
        } catch (Exception e) {
            fail("Unexpected error: " + e, e);
        }
    }

    /* ---------- tiny helpers ---------- */

    private static void require(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
    }

    private static void fail(String msg, Throwable t) {
        System.err.println("✖ " + msg);
        if (t != null) t.printStackTrace(System.err);
        System.exit(1);
    }
}
