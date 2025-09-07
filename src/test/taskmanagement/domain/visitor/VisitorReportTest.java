package taskmanagement.domain.visitor;

import org.junit.Assert;
import org.junit.Test;

import taskmanagement.domain.Task;
import taskmanagement.domain.TaskState;

// Flat exporters (route via Task.accept(visitor))
import taskmanagement.domain.visitor.export.CsvFlatTaskVisitor;
import taskmanagement.domain.visitor.export.PlainTextFlatTaskVisitor;

// Counting visitor + concrete report + exporters
import taskmanagement.domain.visitor.reports.Report;          // marker
import taskmanagement.domain.visitor.reports.ByStateCount;    // concrete
import taskmanagement.domain.visitor.adapters.IReportExporter;
import taskmanagement.domain.visitor.adapters.ByStateCsvExporter;
import taskmanagement.domain.visitor.adapters.ByStatePlainTextExporter;

/**
 * VisitorReportTest
 * -----------------
 * 1) CSV/PlainText export via Task.accept(visitor).
 * 2) Count-by-state using CountByStateVisitor and export the concrete report.
 */
public class VisitorReportTest {

    @Test
    public void csv_export_via_accept() {
        Task t1 = new Task(1, "Write tests", "DAO CRUD", TaskState.ToDo);
        Task t2 = new Task(2, "Wire UI",     "MVVM binding", TaskState.InProgress);
        Task t3 = new Task(3, "Polish UX",   "Dark theme",   TaskState.Completed);

        CsvFlatTaskVisitor csv = new CsvFlatTaskVisitor();
        t1.accept(csv);
        t2.accept(csv);
        t3.accept(csv);
        csv.complete();

        String out = csv.result();
        Assert.assertTrue(out.startsWith("id,title,description,state\n"));
        Assert.assertTrue(out.contains("1,\"Write tests\",\"DAO CRUD\",ToDo"));
        Assert.assertTrue(out.contains("2,\"Wire UI\",\"MVVM binding\",InProgress"));
        Assert.assertTrue(out.contains("3,\"Polish UX\",\"Dark theme\",Completed"));
    }

    @Test
    public void plaintext_export_via_accept() {
        Task t1 = new Task(10, "A", "a", TaskState.ToDo);
        Task t2 = new Task(11, "B", "b", TaskState.InProgress);
        Task t3 = new Task(12, "C", "c", TaskState.Completed);

        PlainTextFlatTaskVisitor txt = new PlainTextFlatTaskVisitor();
        t1.accept(txt);
        t2.accept(txt);
        t3.accept(txt);
        txt.complete();

        String out = txt.result();
        Assert.assertTrue(out.contains("Tasks Export"));
        Assert.assertTrue(out.contains("ID: 10"));
        Assert.assertTrue(out.contains("Title: A"));
        Assert.assertTrue(out.contains("State: ToDo"));
        Assert.assertTrue(out.contains("State: InProgress"));
        Assert.assertTrue(out.contains("State: Completed"));
    }

    @Test
    public void by_state_count_and_exporters() {
        CountByStateVisitor counter = new CountByStateVisitor();

        // Count by visiting domain tasks (accept routes to the proper record via pattern matching)
        new Task(21, "T1", "x", TaskState.ToDo).accept(counter);
        new Task(22, "T2", "y", TaskState.ToDo).accept(counter);
        new Task(23, "T3", "z", TaskState.InProgress).accept(counter);
        new Task(24, "T4", "w", TaskState.Completed).accept(counter);
        new Task(25, "T5", "q", TaskState.Completed).accept(counter);

        // Report is a marker interface; cast to concrete implementation to assert counts
        Report rep = counter.report(); // or counter.result() — both exist in your code
        ByStateCount byState = (ByStateCount) rep;

        Assert.assertEquals(2, byState.count(TaskState.ToDo));
        Assert.assertEquals(1, byState.count(TaskState.InProgress));
        Assert.assertEquals(2, byState.count(TaskState.Completed));

        // Exporters are generic; use concrete type to avoid -Xlint:unchecked warnings
        IReportExporter<ByStateCount> csv = new ByStateCsvExporter();
        String csvOut = csv.export(byState);
        Assert.assertTrue(csvOut.startsWith("state,count"));
        Assert.assertTrue(csvOut.contains("ToDo,2"));
        Assert.assertTrue(csvOut.contains("InProgress,1"));
        Assert.assertTrue(csvOut.contains("Completed,2"));

        IReportExporter<ByStateCount> txt = new ByStatePlainTextExporter();
        String txtOut = txt.export(byState);
        Assert.assertTrue(txtOut.contains("Tasks by state"));
        Assert.assertTrue(txtOut.contains("ToDo: 2"));
        Assert.assertTrue(txtOut.contains("InProgress: 1"));
        Assert.assertTrue(txtOut.contains("Completed: 2"));
    }
}
