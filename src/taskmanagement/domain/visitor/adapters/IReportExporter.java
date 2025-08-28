package taskmanagement.domain.visitor.adapters;

import taskmanagement.domain.visitor.reports.Report;

/**
 * Adapter interface to export a Report into a textual representation.
 */
public interface IReportExporter {
    /**
     * Exports the given report into a textual format.
     * @param report non-null report
     * @return string content representing the report
     * @throws IllegalArgumentException if the exporter does not support the report type
     */
    String export(Report report);
}
