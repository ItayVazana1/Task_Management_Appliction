package taskmanagement.domain.visitor.export;

import taskmanagement.domain.TaskState;

/**
 * Record representing a task in "InProgress" state for export visitors.
 */
public record InProgressTaskRec(int id, String title, String description)
        implements ExportNode {

    @Override
    public TaskState state() {
        return TaskState.InProgress;
    }
}
