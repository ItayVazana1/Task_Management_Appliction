package taskmanagement.application.viewmodel.commands;

import taskmanagement.domain.ITask;
import taskmanagement.persistence.DAOProvider;
import taskmanagement.persistence.ITasksDAO;
import taskmanagement.persistence.TasksDAOException;

/**
 * Updates a task; undo restores the previous snapshot.
 */
public final class UpdateTaskCommand implements Command {

    private final ITask beforeSnapshot;
    private final ITask afterSnapshot;

    /**
     * @param beforeSnapshot snapshot to restore on undo
     * @param afterSnapshot snapshot to apply on execute
     */
    public UpdateTaskCommand(ITask beforeSnapshot, ITask afterSnapshot) {
        this.beforeSnapshot = beforeSnapshot;
        this.afterSnapshot = afterSnapshot;
    }

    @Override
    public String name() {
        return "Update Task";
    }

    @Override
    public void execute() throws TasksDAOException {
        ITasksDAO dao = DAOProvider.get();
        dao.updateTask(afterSnapshot);
    }

    @Override
    public void undo() throws TasksDAOException {
        ITasksDAO dao = DAOProvider.get();
        dao.updateTask(beforeSnapshot);
    }
}
