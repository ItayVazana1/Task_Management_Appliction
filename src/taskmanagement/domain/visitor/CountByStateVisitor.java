package taskmanagement.domain.visitor;

import taskmanagement.domain.ITask;
import taskmanagement.domain.TaskState;
import taskmanagement.domain.visitor.reports.ByStateCount;

/**
 * Visitor that counts tasks by their state.
 * Implements the interface method visit(ITask) to match ITaskVisitor.
 */
public final class CountByStateVisitor implements TaskVisitor {
    private final ByStateCount acc = new ByStateCount();

    @Override
    public void visit(ITask task) {
        TaskState s = task.getState();
        acc.inc(s);
    }

    public ByStateCount result() {
        return acc;
    }
}
