package taskmanagement.application.viewmodel.commands;

import taskmanagement.domain.ITask;
import taskmanagement.domain.TaskState;
import taskmanagement.domain.exceptions.ValidationException;
import taskmanagement.persistence.DAOProvider;
import taskmanagement.persistence.ITasksDAO;
import taskmanagement.persistence.TasksDAOException;

/**
 * Command: mark task state with lifecycle enforcement.
 * Recommended fix (A): validate transition in execute() before DAO update.
 */
public final class MarkStateCommand implements Command {

    public interface TaskFactory {
        ITask copyWithState(ITask source, TaskState target) throws ValidationException;
    }

    private final ITask beforeSnapshot;
    private final ITask afterSnapshot;
    private final TaskState targetState;

    public MarkStateCommand(ITask beforeSnapshot, TaskState targetState, TaskFactory factory)
            throws ValidationException {
        if (beforeSnapshot == null) {
            throw new IllegalArgumentException("beforeSnapshot must not be null");
        }
        if (targetState == null) {
            throw new IllegalArgumentException("targetState must not be null");
        }
        if (factory == null) {
            throw new IllegalArgumentException("factory must not be null");
        }
        this.beforeSnapshot = beforeSnapshot;
        this.targetState = targetState;
        this.afterSnapshot = factory.copyWithState(beforeSnapshot, targetState);
    }

    @Override
    public String name() {
        return "Mark Task State";
    }

    @Override
    public void execute() throws TasksDAOException, ValidationException {
        // ✅ Enforce lifecycle rule here (prevents bypass via “new Task with target state”)
        if (!beforeSnapshot.getState().canTransitionTo(targetState)) {
            throw new ValidationException("Illegal state transition: "
                    + beforeSnapshot.getState() + " -> " + targetState);
        }
        ITasksDAO dao = DAOProvider.get();
        dao.updateTask(afterSnapshot);
    }

    @Override
    public void undo() throws TasksDAOException {
        ITasksDAO dao = DAOProvider.get();
        dao.updateTask(beforeSnapshot);
    }
}


