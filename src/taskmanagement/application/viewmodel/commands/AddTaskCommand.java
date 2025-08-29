package taskmanagement.application.viewmodel.commands;

import taskmanagement.domain.ITask;
import taskmanagement.persistence.DAOProvider;
import taskmanagement.persistence.ITasksDAO;
import taskmanagement.persistence.TasksDAOException;

/**
 * Adds a task; undo removes it.
 */
public final class AddTaskCommand implements Command {

    private final ITask task;
    private Integer createdId;

    /**
     * @param task task to add
     */
    public AddTaskCommand(ITask task) {
        this.task = task;
    }

    @Override
    public String name() {
        return "Add Task";
    }

    @Override
    public void execute() throws TasksDAOException {
        ITasksDAO dao = DAOProvider.get();
        dao.addTask(task);
        createdId = task.getId();
    }

    @Override
    public void undo() throws TasksDAOException {
        if (createdId == null) {
            return;
        }
        ITasksDAO dao = DAOProvider.get();
        dao.deleteTask(createdId);
    }
}
