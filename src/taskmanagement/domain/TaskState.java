package taskmanagement.domain;

/**
 * Task lifecycle states implemented as a behavioral enum (State pattern).
 * <p>
 * Keeping an enum preserves binary/source compatibility across the app,
 * while each constant provides its own transition rules and "next" step.
 */
public enum TaskState {

    /**
     * Initial state. Allowed transitions:
     * - ToDo -> InProgress
     * - ToDo -> ToDo (idempotent)
     */
    ToDo {
        @Override
        public boolean canTransitionTo(TaskState target) {
            return target == ToDo || target == InProgress;
        }

        @Override
        public TaskState next() {
            return InProgress;
        }
    },

    /**
     * Active work state. Allowed transitions:
     * - InProgress -> Completed
     * - InProgress -> InProgress (idempotent)
     * - InProgress -> ToDo (rollback allowed)
     */
    InProgress {
        @Override
        public boolean canTransitionTo(TaskState target) {
            return target == InProgress || target == Completed || target == ToDo;
        }

        @Override
        public TaskState next() {
            return Completed;
        }
    },

    /**
     * Terminal state. Allowed transitions:
     * - Completed -> Completed (idempotent)
     * Other transitions are disallowed by default.
     */
    Completed {
        @Override
        public boolean canTransitionTo(TaskState target) {
            return target == Completed;
        }

        @Override
        public TaskState next() {
            return Completed;
        }
    };

    /**
     * Whether a transition from this state to {@code target} is allowed.
     * Implemented per constant (polymorphic behavior).
     */
    public abstract boolean canTransitionTo(TaskState target);

    /**
     * The "next" state in a forward-only scenario.
     * For terminal states, returns itself.
     */
    public abstract TaskState next();
}
