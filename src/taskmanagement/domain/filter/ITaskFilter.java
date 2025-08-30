package taskmanagement.domain.filter;

import taskmanagement.domain.ITask;

/**
 * Functional filter over tasks (Combinator pattern).
 * Provides default AND/OR and static NOT/ALL combinators.
 */
@FunctionalInterface
public interface ITaskFilter {
    boolean test(ITask task);

    /** Logical AND composition. */
    default ITaskFilter and(ITaskFilter other) {
        return t -> this.test(t) && other.test(t);
    }

    /** Logical OR composition. */
    default ITaskFilter or(ITaskFilter other) {
        return t -> this.test(t) || other.test(t);
    }

    /** Logical NOT. */
    static ITaskFilter not(ITaskFilter f) {
        return t -> !f.test(t);
    }

    /** Match-all (useful as a neutral element). */
    static ITaskFilter all() {
        return t -> true;
    }
}
