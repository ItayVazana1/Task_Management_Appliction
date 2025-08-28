package taskmanagement.domain.filter;

import taskmanagement.domain.ITask;

/**
 * Functional filter for tasks (Combinator pattern).
 * <p>Implementations return true if a task matches the filter.</p>
 */
@FunctionalInterface
public interface TaskFilter {
    /**
     * Tests whether the given task matches this filter.
     * @param task task to test (non-null)
     * @return true if the task matches
     */
    boolean test(ITask task);

    /**
     * Returns a filter that is the logical AND of this filter and the other.
     * @param other another filter (non-null)
     * @return combined AND filter
     */
    default TaskFilter and(TaskFilter other) {
        if (other == null) throw new IllegalArgumentException("other must be non-null");
        return t -> this.test(t) && other.test(t);
    }

    /**
     * Returns a filter that is the logical OR of this filter and the other.
     * @param other another filter (non-null)
     * @return combined OR filter
     */
    default TaskFilter or(TaskFilter other) {
        if (other == null) throw new IllegalArgumentException("other must be non-null");
        return t -> this.test(t) || other.test(t);
    }

    /**
     * Returns a filter that negates this filter.
     * @return negated filter
     */
    default TaskFilter negate() {
        return t -> !this.test(t);
    }
}
