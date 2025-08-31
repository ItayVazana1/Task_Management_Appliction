package taskmanagement.domain.visitor.export;

import taskmanagement.domain.TaskState;

/**
 * Record representing a task in "Completed" state for export visitors.
 */
public record CompletedTaskRec(int id, String title, String description)
        implements ExportNode {

    @Override
    public TaskState state() {
        return TaskState.Completed;
    }
}
