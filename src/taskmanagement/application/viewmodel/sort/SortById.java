package taskmanagement.application.viewmodel.sort;

import taskmanagement.domain.ITask;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

/**
 * Sorting strategy that orders tasks by their unique identifier.
 * <p>
 * This strategy serves as the default (identity-based) ordering
 * for {@link ITask} collections.
 * </p>
 */
public final class SortById implements SortStrategy {

    /**
     * Returns the human-readable display name of this sorting strategy.
     *
     * @return the display name for this strategy
     */
    @Override
    public String displayName() {
        return "ID (Default)";
    }

    /**
     * Returns a new list of tasks sorted by their {@code id} in ascending order.
     * The input list is not modified.
     *
     * @param items the list of tasks to be sorted (must not be {@code null})
     * @return a new list containing the tasks sorted by {@code id}
     * @throws NullPointerException if {@code items} is {@code null}
     */
    @Override
    public List<ITask> sort(List<ITask> items) {
        Objects.requireNonNull(items, "items");
        List<ITask> copy = new ArrayList<>(items);
        copy.sort(Comparator.comparingInt(ITask::getId));
        return copy;
    }

    /**
     * Returns the string representation of this sorting strategy,
     * which is the same as its display name.
     *
     * @return the display name string
     */
    @Override
    public String toString() {
        return displayName();
    }
}
