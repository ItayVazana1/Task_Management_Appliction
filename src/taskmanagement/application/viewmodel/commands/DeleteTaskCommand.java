package taskmanagement.application.viewmodel.commands;

import taskmanagement.domain.ITask;
import taskmanagement.persistence.ITasksDAO;
import taskmanagement.persistence.TasksDAOException;

/**
 * Command: delete a task. Undo re-inserts the deleted snapshot.
 */
public final class DeleteTaskCommand implements Command {

    private final ITasksDAO dao;
    private final ITask deletedSnapshot;

    /**
     * @param dao             tasks DAO
     * @param deletedSnapshot full snapshot of the task being deleted (used to restore on undo)
     */
    public DeleteTaskCommand(ITasksDAO dao, ITask deletedSnapshot) {
        this.dao = dao;
        this.deletedSnapshot = deletedSnapshot;
    }

    @Override
    public String name() { return "Delete Task"; }

    @Override
    public void execute() throws TasksDAOException {
        dao.deleteTask(deletedSnapshot.getId());
    }

    @Override
    public void undo() throws TasksDAOException {
        dao.addTask(deletedSnapshot);
    }
}
