package taskmanagement.domain.visitor.adapters;

import taskmanagement.domain.visitor.reports.ByStateCount;
import taskmanagement.domain.visitor.reports.Report;

/**
 * Adapter that exports ByStateCount report into plain text format.
 */
public class ByStatePlainTextExporter implements IReportExporter {

    @Override
    public String export(Report report) {
        if (!(report instanceof ByStateCount count)) {
            throw new IllegalArgumentException("Unsupported report type: " + report);
        }
        return "Tasks Report (Plain Text)\n"
                + "------------------------\n"
                + "ToDo:       " + count.todo() + "\n"
                + "InProgress: " + count.inProgress() + "\n"
                + "Completed:  " + count.completed() + "\n"
                + "Total:      " + count.total();
    }
}
