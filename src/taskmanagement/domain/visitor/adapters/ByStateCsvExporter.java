package taskmanagement.domain.visitor.adapters;

import taskmanagement.domain.visitor.reports.ByStateCount;
import taskmanagement.domain.visitor.reports.Report;

/**
 * Adapter that exports ByStateCount report into CSV format.
 */
public class ByStateCsvExporter implements ReportExporter {

    @Override
    public String export(Report report) {
        if (!(report instanceof ByStateCount count)) {
            throw new IllegalArgumentException("Unsupported report type: " + report);
        }
        // Simple CSV: header + row
        return "todo,inProgress,completed,total\n"
                + count.todo() + ","
                + count.inProgress() + ","
                + count.completed() + ","
                + count.total();
    }
}
