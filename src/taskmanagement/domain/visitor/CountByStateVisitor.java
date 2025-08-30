package taskmanagement.domain.visitor;

import taskmanagement.domain.ITask;
import taskmanagement.domain.TaskState;
import taskmanagement.domain.visitor.reports.ByStateCount;

/**
 * Visitor that counts tasks by state and returns a ByStateCount report.
 * - Never returns null (even with 0 tasks).
 * - Exposes multiple accessors (result/report/getReport) for compatibility.
 */
public final class CountByStateVisitor implements TaskVisitor {

    private int todo;
    private int inProgress;
    private int completed;

    /** Reset counters (optional for reuse). */
    public void reset() {
        todo = inProgress = completed = 0;
    }

    @Override
    public void visit(ITask task) {
        TaskState s = task.getState();
        if (s == TaskState.ToDo) {
            todo++;
        } else if (s == TaskState.InProgress) {
            inProgress++;
        } else if (s == TaskState.Completed) {
            completed++;
        }
    }

    /** Primary accessor used by reports/adapters. */
    public ByStateCount result() {
        return new ByStateCount(todo, inProgress, completed);
    }

    /** Alias accessor for compatibility. */
    public ByStateCount report() {
        return result();
    }

    /** Alias accessor for compatibility. */
    public ByStateCount getReport() {
        return result();
    }
}
