package taskmanagement.domain.visitor.reports;

import taskmanagement.domain.TaskState;

import java.util.EnumMap;
import java.util.Map;

/**
 * Report data structure that aggregates the number of tasks per {@link TaskState}.
 * <p>
 * Provides both generic access methods ({@link #count(TaskState)}, {@link #inc(TaskState)})
 * and convenience accessors for individual states ({@link #todo()}, {@link #inProgress()},
 * {@link #completed()}).
 * </p>
 */
public final class ByStateCount implements Report {

    private int todo;
    private int inProgress;
    private int completed;

    /**
     * Creates a new {@code ByStateCount} with all counters initialized to zero.
     */
    public ByStateCount() {
    }

    /**
     * Creates a new {@code ByStateCount} with explicit counts.
     *
     * @param todo       initial count of tasks in {@link TaskState#ToDo}
     * @param inProgress initial count of tasks in {@link TaskState#InProgress}
     * @param completed  initial count of tasks in {@link TaskState#Completed}
     */
    public ByStateCount(int todo, int inProgress, int completed) {
        this.todo = todo;
        this.inProgress = inProgress;
        this.completed = completed;
    }

    /**
     * Increments the counter corresponding to the given state.
     *
     * @param state the task state to increment
     */
    public void inc(TaskState state) {
        switch (state) {
            case ToDo -> todo++;
            case InProgress -> inProgress++;
            case Completed -> completed++;
        }
    }

    /**
     * Returns the count for the given state.
     *
     * @param state the task state to query
     * @return number of tasks recorded in the specified state
     */
    public int count(TaskState state) {
        return switch (state) {
            case ToDo -> todo;
            case InProgress -> inProgress;
            case Completed -> completed;
        };
    }

    /**
     * Returns the count of tasks in {@link TaskState#ToDo}.
     *
     * @return number of tasks in ToDo state
     */
    public int todo() {
        return todo;
    }

    /**
     * Returns the count of tasks in {@link TaskState#InProgress}.
     *
     * @return number of tasks in InProgress state
     */
    public int inProgress() {
        return inProgress;
    }

    /**
     * Returns the count of tasks in {@link TaskState#Completed}.
     *
     * @return number of tasks in Completed state
     */
    public int completed() {
        return completed;
    }

    /**
     * Returns an immutable map view of all state counts.
     *
     * @return a map of {@link TaskState} to their corresponding counts
     */
    public Map<TaskState, Integer> asMap() {
        EnumMap<TaskState, Integer> m = new EnumMap<>(TaskState.class);
        m.put(TaskState.ToDo, todo);
        m.put(TaskState.InProgress, inProgress);
        m.put(TaskState.Completed, completed);
        return Map.copyOf(m);
    }

    /**
     * Returns a string representation of the state counts.
     *
     * @return a formatted string showing counts for each state
     */
    @Override
    public String toString() {
        return "ByStateCount{ToDo=" + todo +
                ", InProgress=" + inProgress +
                ", Completed=" + completed + '}';
    }
}
