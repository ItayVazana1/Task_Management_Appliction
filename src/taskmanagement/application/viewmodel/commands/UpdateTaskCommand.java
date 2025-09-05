package taskmanagement.application.viewmodel.commands;

import taskmanagement.domain.ITask;
import taskmanagement.persistence.ITasksDAO;
import taskmanagement.persistence.TasksDAOException;

/**
 * Command: update an existing task. Undo restores the "before" snapshot.
 */
public final class UpdateTaskCommand implements Command {

    private final ITasksDAO dao;
    private final ITask beforeSnapshot;
    private final ITask afterSnapshot;

    /**
     * @param dao            tasks DAO
     * @param beforeSnapshot full snapshot of the entity as it exists before the update
     * @param afterSnapshot  full snapshot to persist during execute()
     */
    public UpdateTaskCommand(ITasksDAO dao, ITask beforeSnapshot, ITask afterSnapshot) {
        this.dao = dao;
        this.beforeSnapshot = beforeSnapshot;
        this.afterSnapshot = afterSnapshot;
    }

    @Override
    public String name() { return "Update Task"; }

    @Override
    public void execute() throws TasksDAOException {
        dao.updateTask(afterSnapshot);
    }

    @Override
    public void undo() throws TasksDAOException {
        dao.updateTask(beforeSnapshot);
    }
}
