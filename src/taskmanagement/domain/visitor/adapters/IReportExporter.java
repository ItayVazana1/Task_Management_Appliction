package taskmanagement.domain.visitor.adapters;

import taskmanagement.domain.visitor.reports.Report;

/**
 * Adapter interface for exporting a {@link Report} into a textual representation.
 *
 * @param <T> the specific type of {@link Report} supported by this exporter
 */
public interface IReportExporter<T extends Report> {

    /**
     * Exports the given report into a textual format.
     *
     * @param report the report to export (must not be {@code null})
     * @return string content representing the exported report
     * @throws IllegalArgumentException if {@code report} is {@code null}
     *                                  or if the exporter does not support the report type
     */
    String export(T report);
}
