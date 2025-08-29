package taskmanagement.application.viewmodel.sort;

import taskmanagement.domain.ITask;
import taskmanagement.domain.TaskState;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

/**
 * Sorts tasks by lifecycle state (ToDo < InProgress < Completed), then by title.
 */
public final class SortByState implements SortStrategy {

    // explicit order instead of ordinal, for clarity and future-proofing
    private static int rank(TaskState s) {
        if (s == null) return Integer.MAX_VALUE; // push nulls to the end
        return switch (s) {
            case ToDo -> 0;
            case InProgress -> 1;
            case Completed -> 2;
        };
    }

    private static final Comparator<ITask> CMP =
            Comparator.comparingInt((ITask t) -> rank(t.getState()))
                    .thenComparing(t -> safe(t.getTitle()).toLowerCase())
                    .thenComparingInt(ITask::getId);

    @Override
    public String displayName() {
        return "State (ToDo→Completed)";
    }

    @Override
    public List<ITask> sort(List<ITask> items) {
        Objects.requireNonNull(items, "items");
        List<ITask> copy = new ArrayList<>(items);
        copy.sort(CMP);
        return copy;
    }

    private static String safe(String s) { return (s == null) ? "" : s; }
}
