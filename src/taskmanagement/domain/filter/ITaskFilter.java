package taskmanagement.domain.filter;

import taskmanagement.domain.ITask;

import java.util.Objects;

/**
 * Combinator predicate for tasks.
 */
@FunctionalInterface
public interface ITaskFilter {
    boolean test(ITask task);

    default ITaskFilter and(ITaskFilter other) {
        Objects.requireNonNull(other);
        return t -> this.test(t) && other.test(t);
    }

    default ITaskFilter or(ITaskFilter other) {
        Objects.requireNonNull(other);
        return t -> this.test(t) || other.test(t);
    }

    default ITaskFilter negate() {
        return t -> !this.test(t);
    }

    static ITaskFilter any() { return t -> true; }
}
