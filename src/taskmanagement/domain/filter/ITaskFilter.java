package taskmanagement.domain.filter;

import taskmanagement.domain.ITask;

/**
 * Functional filter over tasks (Combinator pattern).
 */
@FunctionalInterface
public interface ITaskFilter {
    /**
     * Tests whether the given task matches this filter.
     * @param task non-null task
     * @return true if matches
     */
    boolean test(ITask task);

    /**
     * Logical AND combinator.
     */
    default ITaskFilter and(ITaskFilter other) {
        return t -> this.test(t) && other.test(t);
    }

    /**
     * Logical OR combinator.
     */
    default ITaskFilter or(ITaskFilter other) {
        return t -> this.test(t) || other.test(t);
    }

    /**
     * Logical NOT combinator.
     */
    default ITaskFilter negate() {
        return t -> !this.test(t);
    }
}
