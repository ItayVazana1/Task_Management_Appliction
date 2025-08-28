package taskmanagement.domain.visitor.adapters;

import taskmanagement.domain.visitor.reports.ByStateCount;

/**
 * Plain text exporter for {@link ByStateCount}.
 */
public final class ByStatePlainTextExporter implements IReportExporter<ByStateCount> {

    @Override
    public String export(ByStateCount report) {
        if (report == null) {
            throw new IllegalArgumentException("report is null");
        }
        int total = report.todo() + report.inProgress() + report.completed();

        String nl = System.lineSeparator();
        StringBuilder sb = new StringBuilder();
        sb.append("Tasks by state").append(nl);
        sb.append("ToDo: ").append(report.todo()).append(nl);
        sb.append("InProgress: ").append(report.inProgress()).append(nl);
        sb.append("Completed: ").append(report.completed()).append(nl);
        sb.append("Total: ").append(total).append(nl);
        return sb.toString();
    }
}
