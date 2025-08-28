package taskmanagement.domain.visitor.reports;

/**
 * Aggregated counts of tasks by state.
 */
public record ByStateCount(int todo, int inProgress, int completed) implements Report {
    public ByStateCount {
        if (todo < 0 || inProgress < 0 || completed < 0) {
            throw new IllegalArgumentException("counts must be non-negative");
        }
    }
    /** Convenience total. */
    public int total() { return todo + inProgress + completed; }
}
