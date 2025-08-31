package taskmanagement.domain.visitor.export;

import taskmanagement.domain.TaskState;

/**
 * Export node variants (records) used for pattern-matching in export visitors.
 * This is a sealed interface that restricts the allowed record types.
 */
public sealed interface ExportNode
        permits ToDoTaskRec, InProgressTaskRec, CompletedTaskRec {

    /**
     * @return unique identifier of the task
     */
    int id();

    /**
     * @return title of the task
     */
    String title();

    /**
     * @return description of the task
     */
    String description();

    /**
     * @return state of the task
     */
    TaskState state();
}
