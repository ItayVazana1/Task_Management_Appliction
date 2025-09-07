package taskmanagement.domain.visitor.adapters;

import taskmanagement.domain.visitor.reports.ByStateCount;

/**
 * CSV exporter for {@link ByStateCount} reports.
 * <p>
 * Output format:
 * <pre>
 * state,count
 * ToDo,&lt;n&gt;
 * InProgress,&lt;n&gt;
 * Completed,&lt;n&gt;
 * Total,&lt;n&gt;
 * </pre>
 * </p>
 */
public final class ByStateCsvExporter implements IReportExporter<ByStateCount> {

    /**
     * Exports a {@link ByStateCount} report to CSV format.
     *
     * @param report the report to export (must not be {@code null})
     * @return CSV string representation of the report
     * @throws IllegalArgumentException if {@code report} is {@code null}
     */
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
