package taskmanagement.domain.visitor;

import taskmanagement.domain.ITask;
import taskmanagement.domain.visitor.reports.ByStateCount;

/**
 * TaskVisitor that counts tasks by their state and produces a ByStateCount report.
 */
public class CountByStateVisitor implements ITaskVisitor {

    private int todo;
    private int inProgress;
    private int completed;

    @Override
    public void visit(ITask task) {
        if (task == null || task.getState() == null) {
            return;
        }
        // Pattern matching switch over TaskState
        switch (task.getState()) {
            case ToDo -> todo++;
            case InProgress -> inProgress++;
            case Completed -> completed++;
        }
    }

    /**
     * @return an immutable ByStateCount record based on all visited tasks
     */
    public ByStateCount result() {
        return new ByStateCount(todo, inProgress, completed);
    }
}
