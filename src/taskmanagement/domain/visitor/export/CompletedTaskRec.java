package taskmanagement.domain.visitor.export;

import taskmanagement.domain.TaskState;

/**
 * Export record representing a task in the {@link TaskState#Completed} state.
 * <p>
 * Used by export visitors to handle tasks with state {@code Completed}.
 * </p>
 *
 * @param id          the task id
 * @param title       the task title
 * @param description the task description
 */
public record CompletedTaskRec(int id, String title, String description)
        implements ExportNode {

    /**
     * Returns the associated state for this export record.
     *
     * @return {@link TaskState#Completed}
     */
    @Override
    public TaskState state() {
        return TaskState.Completed;
    }
}
