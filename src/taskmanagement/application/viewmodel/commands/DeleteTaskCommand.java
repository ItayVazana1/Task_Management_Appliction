package taskmanagement.application.viewmodel.commands;

import taskmanagement.domain.ITask;
import taskmanagement.persistence.DAOProvider;
import taskmanagement.persistence.ITasksDAO;
import taskmanagement.persistence.TasksDAOException;

/**
 * Deletes a task; undo re-inserts the deleted snapshot.
 */
public final class DeleteTaskCommand implements Command {

    private final ITask deletedSnapshot;

    /**
     * @param deletedSnapshot full snapshot of the task to delete
     */
    public DeleteTaskCommand(ITask deletedSnapshot) {
        this.deletedSnapshot = deletedSnapshot;
    }

    @Override
    public String name() {
        return "Delete Task";
    }

    @Override
    public void execute() throws TasksDAOException {
        ITasksDAO dao = DAOProvider.get();
        dao.deleteTask(deletedSnapshot.getId());
    }

    @Override
    public void undo() throws TasksDAOException {
        ITasksDAO dao = DAOProvider.get();
        dao.addTask(deletedSnapshot);
    }
}
