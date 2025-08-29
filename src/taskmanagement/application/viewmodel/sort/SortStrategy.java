package taskmanagement.application.viewmodel.sort;

import taskmanagement.domain.ITask;

import java.util.List;

/**
 * Strategy for sorting task lists. Implementations define the order.
 */
public interface SortStrategy {

    /**
     * A short label suitable for UI controls (combo-box, menu, etc.).
     * @return display name of the strategy
     */
    String displayName();

    /**
     * Returns a new sorted snapshot (does not mutate the input list).
     * @param items source items (never null)
     * @return sorted copy
     */
    List<ITask> sort(List<ITask> items);
}
