package taskmanagement.application.viewmodel.sort;

import taskmanagement.domain.ITask;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

/**
 * Sorts tasks by title (case-insensitive), then by id to stabilize order.
 */
public final class SortByTitle implements SortStrategy {

    private static final Comparator<ITask> CMP =
            Comparator
                    .comparing((ITask t) -> safe(t.getTitle()).toLowerCase())
                    .thenComparingInt(ITask::getId);

    /** {@inheritDoc} */
    @Override
    public String displayName() {
        return "Title (A→Z)";
    }

    /** {@inheritDoc} */
    @Override
    public List<ITask> sort(List<ITask> items) {
        Objects.requireNonNull(items, "items");
        List<ITask> copy = new ArrayList<>(items);
        copy.sort(CMP);
        return copy;
    }

    private static String safe(String s) {
        return (s == null) ? "" : s;
    }
}
