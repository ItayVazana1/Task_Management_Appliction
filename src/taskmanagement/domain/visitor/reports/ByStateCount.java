package taskmanagement.domain.visitor.reports;

/**
 * Immutable report summarizing how many tasks are in each state.
 * Implemented as a Java record (as required by the specification).
 */
public record ByStateCount(int todo, int inProgress, int completed) implements Report {

    /**
     * @return total number of tasks counted
     */
    public int total() {
        return todo + inProgress + completed;
    }
}
