package taskmanagement.domain.visitor.export;

import taskmanagement.domain.TaskState;

/**
 * Sealed interface representing exportable task nodes for use in visitors.
 * <p>
 * Only the record variants {@link ToDoTaskRec}, {@link InProgressTaskRec},
 * and {@link CompletedTaskRec} are permitted implementations.
 * </p>
 */
public sealed interface ExportNode
        permits ToDoTaskRec, InProgressTaskRec, CompletedTaskRec {

    /**
     * Returns the unique identifier of the task.
     *
     * @return task id
     */
    int id();

    /**
     * Returns the title of the task.
     *
     * @return task title (may be {@code null})
     */
    String title();

    /**
     * Returns the description of the task.
     *
     * @return task description (may be {@code null})
     */
    String description();

    /**
     * Returns the state of the task.
     *
     * @return task state (never {@code null})
     */
    TaskState state();
}
