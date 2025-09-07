package taskmanagement.application.viewmodel.sort;

import taskmanagement.domain.ITask;
import taskmanagement.domain.TaskState;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

/**
 * Sorting strategy that orders tasks by their lifecycle state in the sequence:
 * <ul>
 *   <li>{@link TaskState#ToDo}</li>
 *   <li>{@link TaskState#InProgress}</li>
 *   <li>{@link TaskState#Completed}</li>
 * </ul>
 * <p>
 * Within the same state, tasks are further ordered by case-insensitive title,
 * and finally by task ID to ensure stable ordering.
 * </p>
 */
public final class SortByState implements SortStrategy {

    /**
     * Provides an explicit rank for each {@link TaskState}.
     * Null values are ranked last for safety.
     *
     * @param state the task state
     * @return integer rank (lower values come first)
     */
    private static int rank(TaskState state) {
        if (state == null) {
            return Integer.MAX_VALUE;
        }
        return switch (state) {
            case ToDo -> 0;
            case InProgress -> 1;
            case Completed -> 2;
        };
    }

    /**
     * Comparator used to order tasks by state, then title, then id.
     */
    private static final Comparator<ITask> CMP =
            Comparator
                    .comparingInt((ITask t) -> rank(t.getState()))
                    .thenComparing(t -> safe(t.getTitle()).toLowerCase())
                    .thenComparingInt(ITask::getId);

    /**
     * Returns the human-readable display name of this sorting strategy.
     *
     * @return the display name for this strategy
     */
    @Override
    public String displayName() {
        return "State (ToDo→Completed)";
    }

    /**
     * Returns a new list of tasks sorted by state (ToDo &lt; InProgress &lt; Completed),
     * then by case-insensitive title, then by id.
     *
     * @param items the list of tasks to be sorted (must not be {@code null})
     * @return a new list of sorted tasks
     * @throws NullPointerException if {@code items} is {@code null}
     */
    @Override
    public List<ITask> sort(List<ITask> items) {
        Objects.requireNonNull(items, "items");
        List<ITask> copy = new ArrayList<>(items);
        copy.sort(CMP);
        return copy;
    }

    /**
     * Returns a safe, non-null string for comparison.
     *
     * @param s the string to check
     * @return the original string if non-null, or an empty string if {@code null}
     */
    private static String safe(String s) {
        return (s == null) ? "" : s;
    }
}
