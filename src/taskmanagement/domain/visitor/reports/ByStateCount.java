package taskmanagement.domain.visitor.reports;

import taskmanagement.domain.TaskState;

import java.util.EnumMap;
import java.util.Map;

/**
 * Aggregated counts per TaskState for reporting (Visitor result).
 * Provides both generic API (count/inc) and convenience accessors (todo/inProgress/completed)
 * so existing exporters continue to compile.
 */
public final class ByStateCount implements Report {
    private int todo;
    private int inProgress;
    private int completed;

    /** Default constructor (all zero). */
    public ByStateCount() {
        // all zero by default
    }

    /** Convenience constructor for direct assignment. */
    public ByStateCount(int todo, int inProgress, int completed) {
        this.todo = todo;
        this.inProgress = inProgress;
        this.completed = completed;
    }

    /** Increment the counter for a given state. */
    public void inc(TaskState state) {
        switch (state) {
            case ToDo -> todo++;
            case InProgress -> inProgress++;
            case Completed -> completed++;
        }
    }

    /** Return the count for the given state. */
    public int count(TaskState state) {
        return switch (state) {
            case ToDo -> todo;
            case InProgress -> inProgress;
            case Completed -> completed;
        };
    }

    /** Convenience accessors (kept for existing exporters). */
    public int todo() { return todo; }
    public int inProgress() { return inProgress; }
    public int completed() { return completed; }

    /** Optional: immutable view (useful for other exporters). */
    public Map<TaskState, Integer> asMap() {
        EnumMap<TaskState, Integer> m = new EnumMap<>(TaskState.class);
        m.put(TaskState.ToDo, todo);
        m.put(TaskState.InProgress, inProgress);
        m.put(TaskState.Completed, completed);
        return Map.copyOf(m);
    }

    @Override
    public String toString() {
        return "ByStateCount{ToDo=" + todo +
                ", InProgress=" + inProgress +
                ", Completed=" + completed + '}';
    }
}
