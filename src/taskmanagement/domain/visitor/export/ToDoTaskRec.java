package taskmanagement.domain.visitor.export;

import taskmanagement.domain.TaskState;

/**
 * Record representing a task in "ToDo" state for export visitors.
 */
public record ToDoTaskRec(int id, String title, String description)
        implements ExportNode {

    @Override
    public TaskState state() {
        return TaskState.ToDo;
    }
}
