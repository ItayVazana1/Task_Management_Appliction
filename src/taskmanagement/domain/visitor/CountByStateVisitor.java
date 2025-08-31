package taskmanagement.domain.visitor;

import taskmanagement.domain.ITask;
import taskmanagement.domain.TaskState;
import taskmanagement.domain.visitor.export.CompletedTaskRec;
import taskmanagement.domain.visitor.export.InProgressTaskRec;
import taskmanagement.domain.visitor.export.ToDoTaskRec;
import taskmanagement.domain.visitor.reports.ByStateCount;

/**
 * Visitor that counts tasks by state and returns a ByStateCount report.
 * Implements record-based visits (Visitor + Records + Pattern Matching).
 */
public final class CountByStateVisitor implements TaskVisitor {

    private int todo;
    private int inProgress;
    private int completed;

    /** Reset counters (optional for reuse). */
    public void reset() {
        todo = inProgress = completed = 0;
    }

    /** Convenience bridge: allow old-style calls by delegating to accept(this). */
    public void visit(ITask task) {
        if (task != null) {
            task.accept(this); // this triggers the record-specific visit(...)
        }
    }

    /** Optional convenience: count directly from TaskState (useful in ViewModel if needed). */
    public void visit(TaskState s) {
        if (s == null) return;
        switch (s) {
            case ToDo -> todo++;
            case InProgress -> inProgress++;
            case Completed -> completed++;
        }
    }

    // ===== Record-based visits (required by TaskVisitor) =====

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
        // no-op (hook for future use)
    }

    // ===== Report accessors =====

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
