package taskmanagement.domain;

/**
 * Task lifecycle states implemented as a behavioral enum (State pattern).
 * <p>
 * Each constant defines its own transition rules and "next" step.
 * Allowed transitions:
 * <ul>
 *   <li>ToDo → InProgress or ToDo (idempotent)</li>
 *   <li>InProgress → Completed or InProgress (idempotent)</li>
 *   <li>Completed → Completed (idempotent only)</li>
 * </ul>
 * </p>
 */
public enum TaskState {

    /**
     * Initial state for newly created tasks.
     */
    ToDo {
        /** {@inheritDoc} */
        @Override
        public boolean canTransitionTo(TaskState target) {
            return target == ToDo || target == InProgress;
        }

        /** {@inheritDoc} */
        @Override
        public TaskState next() {
            return InProgress;
        }
    },

    /**
     * Active work state.
     * Rollback to {@link #ToDo} is not allowed.
     */
    InProgress {
        /** {@inheritDoc} */
        @Override
        public boolean canTransitionTo(TaskState target) {
            return target == InProgress || target == Completed;
        }

        /** {@inheritDoc} */
        @Override
        public TaskState next() {
            return Completed;
        }
    },

    /**
     * Terminal state for finished tasks.
     */
    Completed {
        /** {@inheritDoc} */
        @Override
        public boolean canTransitionTo(TaskState target) {
            return target == Completed;
        }

        /** {@inheritDoc} */
        @Override
        public TaskState next() {
            return Completed;
        }
    };

    /**
     * Checks if a transition from this state to the given target state is allowed.
     *
     * @param target the target state
     * @return {@code true} if the transition is allowed, otherwise {@code false}
     */
    public abstract boolean canTransitionTo(TaskState target);

    /**
     * Returns the "next" state in the lifecycle.
     * <p>
     * For terminal states, returns itself.
     * </p>
     *
     * @return the next state
     */
    public abstract TaskState next();
}
