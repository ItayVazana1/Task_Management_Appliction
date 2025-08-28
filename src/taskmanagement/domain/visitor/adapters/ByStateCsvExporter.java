package taskmanagement.domain.visitor.adapters;

import taskmanagement.domain.visitor.reports.ByStateCount;

/**
 * CSV exporter for {@link ByStateCount}.
 * Format:
 * state,count
 * ToDo,<n>
 * InProgress,<n>
 * Completed,<n>
 * Total,<n>
 */
public final class ByStateCsvExporter implements IReportExporter<ByStateCount> {

    @Override
    public String export(ByStateCount report) {
        if (report == null) {
            throw new IllegalArgumentException("report is null");
        }
        int total = report.todo() + report.inProgress() + report.completed();

        String nl = System.lineSeparator();
        StringBuilder sb = new StringBuilder();
        sb.append("state,count").append(nl);
        sb.append("ToDo,").append(report.todo()).append(nl);
        sb.append("InProgress,").append(report.inProgress()).append(nl);
        sb.append("Completed,").append(report.completed()).append(nl);
        sb.append("Total,").append(total).append(nl);
        return sb.toString();
    }
}
