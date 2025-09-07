package taskmanagement.application.viewmodel.sort;

import taskmanagement.domain.ITask;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

/**
 * Sorting strategy that orders tasks by their title in case-insensitive
 * lexicographic order. If titles are identical, tasks are ordered by ID
 * to ensure a stable ordering.
 */
public final class SortByTitle implements SortStrategy {

    /**
     * Comparator that orders tasks by title (case-insensitive),
     * then by task ID as a tiebreaker.
     */
    private static final Comparator<ITask> CMP =
            Comparator
                    .comparing((ITask t) -> safe(t.getTitle()).toLowerCase())
                    .thenComparingInt(ITask::getId);

    /**
     * Returns the human-readable display name of this sorting strategy.
     *
     * @return the display name for this strategy
     */
    @Override
    public String displayName() {
        return "Title (A→Z)";
    }

    /**
     * Returns a new list of tasks sorted by title (case-insensitive),
     * then by ID.
     *
     * @param items the list of tasks to sort (must not be {@code null})
     * @return a new list containing the tasks in sorted order
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
     * @return the original string if non-null, otherwise an empty string
     */
    private static String safe(String s) {
        return (s == null) ? "" : s;
    }
}
