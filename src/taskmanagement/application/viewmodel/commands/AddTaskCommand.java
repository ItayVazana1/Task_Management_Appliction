package taskmanagement.application.viewmodel.commands;

import taskmanagement.domain.ITask;
import taskmanagement.persistence.ITasksDAO;
import taskmanagement.persistence.TasksDAOException;

import java.util.Objects;

/**
 * Command: add a task.
 * <p>Execute: persists the task via DAO and captures the effective id written back to the task.</p>
 * <p>Undo: deletes the created row by the captured id.</p>
 */
public final class AddTaskCommand implements Command {

    private final ITasksDAO dao;
    private final ITask taskToAdd;
    private Integer createdId; // set after execute()

    /**
     * Creates a new AddTaskCommand.
     *
     * @param dao       tasks DAO (must not be {@code null})
     * @param taskToAdd task instance to persist (must not be {@code null})
     */
    public AddTaskCommand(ITasksDAO dao, ITask taskToAdd) {
        this.dao = Objects.requireNonNull(dao, "dao");
        this.taskToAdd = Objects.requireNonNull(taskToAdd, "taskToAdd");
    }

    /** {@inheritDoc} */
    @Override
    public String name() { return "Add Task"; }

    /** {@inheritDoc} */
    @Override
    public void execute() throws TasksDAOException {
        dao.addTask(taskToAdd);
        // DAO must reflect the effective id into taskToAdd.getId()
        createdId = taskToAdd.getId();
        if (createdId == null || createdId < 0) {
            throw new TasksDAOException("DAO did not assign a valid id to the added task (got: " + createdId + ")");
        }
    }

    /** {@inheritDoc} */
    @Override
    public void undo() throws TasksDAOException {
        if (createdId == null || createdId <= 0) {
            throw new TasksDAOException("Cannot undo add: missing valid createdId");
        }
        dao.deleteTask(createdId);
    }
}
