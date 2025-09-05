package taskmanagement.application.viewmodel.commands;

import taskmanagement.domain.ITask;
import taskmanagement.persistence.ITasksDAO;
import taskmanagement.persistence.TasksDAOException;

/**
 * Command: add a task. Undo removes the created row by its id.
 */
public final class AddTaskCommand implements Command {

    private final ITasksDAO dao;
    private final ITask taskToAdd;
    private Integer createdId; // set after execute()

    /**
     * @param dao       tasks DAO
     * @param taskToAdd task instance to persist (validated by domain model)
     */
    public AddTaskCommand(ITasksDAO dao, ITask taskToAdd) {
        this.dao = dao;
        this.taskToAdd = taskToAdd;
    }

    @Override
    public String name() { return "Add Task"; }

    @Override
    public void execute() throws TasksDAOException {
        dao.addTask(taskToAdd);
        // if DAO assigns id during insert, it must reflect into taskToAdd.getId()
        createdId = taskToAdd.getId();
    }

    @Override
    public void undo() throws TasksDAOException {
        if (createdId != null) {
            dao.deleteTask(createdId);
        }
    }
}
