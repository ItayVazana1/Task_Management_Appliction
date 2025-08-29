package taskmanagement.domain;

/**
 * Lifecycle states for a Task.
 */
public enum TaskState {
    ToDo,
    InProgress,
    Completed;

    /**
     * @param target the desired next state
     * @return true if the transition is allowed by the lifecycle rules
     */
    public boolean canTransitionTo(TaskState target) {
        if (target == null) return false;
        return switch (this) {
            case ToDo -> target == InProgress || target == ToDo;
            case InProgress -> target == Completed || target == InProgress;
            case Completed -> target == Completed; // Completed is terminal (idempotent only)
        };
    }
}
