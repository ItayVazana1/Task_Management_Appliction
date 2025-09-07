package taskmanagement.application.viewmodel.sort;

import taskmanagement.domain.ITask;

import java.util.List;

/**
 * Defines a strategy interface for sorting collections of {@link ITask}.
 * <p>
 * Implementations determine the specific ordering (e.g., by ID, title, or state).
 * Strategies must be pure: the input list must not be mutated; instead,
 * a new sorted snapshot should be returned.
 * </p>
 */
public interface SortStrategy {

    /**
     * Returns a short, human-readable label for this strategy,
     * suitable for display in UI controls such as combo boxes or menus.
     *
     * @return the display name of the strategy
     */
    String displayName();

    /**
     * Produces a new list of tasks sorted according to this strategy.
     * The input list must not be modified.
     *
     * @param items the source list of tasks (must not be {@code null})
     * @return a new list containing the tasks in sorted order
     * @throws NullPointerException if {@code items} is {@code null}
     */
    List<ITask> sort(List<ITask> items);
}
