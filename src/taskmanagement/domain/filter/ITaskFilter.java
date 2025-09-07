package taskmanagement.domain.filter;

import taskmanagement.domain.ITask;

/**
 * Functional filter over tasks implementing the Combinator pattern.
 * <p>
 * Filters can be combined using logical operators:
 * <ul>
 *   <li>{@link #and(ITaskFilter)} – logical AND composition</li>
 *   <li>{@link #or(ITaskFilter)} – logical OR composition</li>
 *   <li>{@link #not(ITaskFilter)} – static logical NOT</li>
 *   <li>{@link #all()} – static match-all filter</li>
 * </ul>
 * </p>
 */
@FunctionalInterface
public interface ITaskFilter {

    /**
     * Tests whether a given task matches the filter.
     *
     * @param task the task to test (must not be {@code null})
     * @return {@code true} if the task matches, otherwise {@code false}
     */
    boolean test(ITask task);

    /**
     * Returns a composed filter representing the logical AND of this filter
     * and another filter.
     *
     * @param other the other filter (must not be {@code null})
     * @return composed filter that matches when both filters match
     */
    default ITaskFilter and(ITaskFilter other) {
        return t -> this.test(t) && other.test(t);
    }

    /**
     * Returns a composed filter representing the logical OR of this filter
     * and another filter.
     *
     * @param other the other filter (must not be {@code null})
     * @return composed filter that matches when either filter matches
     */
    default ITaskFilter or(ITaskFilter other) {
        return t -> this.test(t) || other.test(t);
    }

    /**
     * Returns a filter representing the logical NOT of the given filter.
     *
     * @param f the filter to negate (must not be {@code null})
     * @return filter that matches when the given filter does not match
     */
    static ITaskFilter not(ITaskFilter f) {
        return t -> !f.test(t);
    }

    /**
     * Returns a filter that matches all tasks.
     *
     * @return match-all filter
     */
    static ITaskFilter all() {
        return t -> true;
    }
}
