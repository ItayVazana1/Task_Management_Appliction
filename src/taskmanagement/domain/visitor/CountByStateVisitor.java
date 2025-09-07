package taskmanagement.domain.visitor;

import taskmanagement.domain.ITask;
import taskmanagement.domain.TaskState;
import taskmanagement.domain.visitor.export.CompletedTaskRec;
import taskmanagement.domain.visitor.export.InProgressTaskRec;
import taskmanagement.domain.visitor.export.ToDoTaskRec;
import taskmanagement.domain.visitor.reports.ByStateCount;

/**
 * Visitor that counts tasks by their {@link TaskState} and produces
 * a {@link ByStateCount} report.
 * <p>
 * Supports record-based visits (Visitor + Records + Pattern Matching).
 * Can also be used directly with {@link ITask} or {@link TaskState}.
 * </p>
 */
public final class CountByStateVisitor implements TaskVisitor {

    private int todo;
    private int inProgress;
    private int completed;

    /**
     * Resets all counters to zero.
     * Useful when reusing the same visitor instance.
     */
    public void reset() {
        todo = inProgress = completed = 0;
    }

    /**
     * Counts a task by delegating to its {@link ITask#accept(TaskVisitor)} method.
     *
     * @param task the task to count (nullable; ignored if {@code null})
     */
    public void visit(ITask task) {
        if (task != null) {
            task.accept(this);
        }
    }

    /**
     * Increments counters directly based on a {@link TaskState}.
     *
     * @param s the task state (nullable; ignored if {@code null})
     */
    public void visit(TaskState s) {
        if (s == null) return;
        switch (s) {
            case ToDo -> todo++;
            case InProgress -> inProgress++;
            case Completed -> completed++;
        }
    }

    @Override
    public void visit(ToDoTaskRec node) {
        todo++;
    }

    @Override
    public void visit(InProgressTaskRec node) {
        inProgress++;
    }

    @Override
    public void visit(CompletedTaskRec node) {
        completed++;
    }

    @Override
    public void complete() {
        // no-op, placeholder for future extensions
    }

    /**
     * Returns the current counts as a {@link ByStateCount} report.
     *
     * @return report with counts for ToDo, InProgress, and Completed states
     */
    public ByStateCount result() {
        return new ByStateCount(todo, inProgress, completed);
    }

    /**
     * Alias for {@link #result()}.
     *
     * @return report with counts for ToDo, InProgress, and Completed states
     */
    public ByStateCount report() {
        return result();
    }

    /**
     * Alias for {@link #result()}.
     *
     * @return report with counts for ToDo, InProgress, and Completed states
     */
    public ByStateCount getReport() {
        return result();
    }
}
