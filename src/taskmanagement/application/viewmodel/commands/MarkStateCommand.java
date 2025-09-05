package taskmanagement.application.viewmodel.commands;

import taskmanagement.domain.ITask;
import taskmanagement.domain.TaskState;
import taskmanagement.domain.exceptions.ValidationException;
import taskmanagement.persistence.ITasksDAO;
import taskmanagement.persistence.TasksDAOException;

/**
 * Command: change a task's lifecycle state.
 * Executes as an update; undo restores previous snapshot.
 */
public final class MarkStateCommand implements Command {

    /** Factory for creating a new immutable snapshot with a different state. */
    @FunctionalInterface
    public interface TaskFactory {
        ITask copyWithState(ITask src, TaskState newState) throws ValidationException;
    }

    private final ITasksDAO dao;
    private final ITask beforeSnapshot;
    private final TaskState targetState;
    private final TaskFactory factory;

    private ITask afterSnapshot; // lazily created at execute()

    /**
     * @param dao            tasks DAO
     * @param beforeSnapshot full snapshot of current entity
     * @param targetState    state to apply
     * @param factory        creates a new snapshot based on beforeSnapshot & targetState
     */
    public MarkStateCommand(ITasksDAO dao, ITask beforeSnapshot, TaskState targetState, TaskFactory factory) {
        this.dao = dao;
        this.beforeSnapshot = beforeSnapshot;
        this.targetState = targetState;
        this.factory = factory;
    }

    @Override
    public String name() { return "Mark State: " + targetState; }

    @Override
    public void execute() throws TasksDAOException, ValidationException {
        if (afterSnapshot == null) {
            afterSnapshot = factory.copyWithState(beforeSnapshot, targetState);
        }
        dao.updateTask(afterSnapshot);
    }

    @Override
    public void undo() throws TasksDAOException {
        dao.updateTask(beforeSnapshot);
    }
}
