package taskmanagement.domain.visitor.adapters;

import taskmanagement.domain.visitor.reports.Report;

/**
 * Adapter interface to export a Report into a textual representation.
 * @param <T> specific Report type supported by this exporter
 */
public interface IReportExporter<T extends Report> {
    /**
     * Exports the given report into a textual format.
     * @param report non-null report of the supported type
     * @return string content representing the report
     * @throws IllegalArgumentException if the exporter does not support the report type
     */
    String export(T report);
}
