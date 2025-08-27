package taskmanagement.persistence;

import taskmanagement.domain.ITask;

/**
 * Data Access Object API for tasks (embedded Derby implementation will follow).
 */
public interface ITasksDAO {

    /**
     * Retrieves all tasks.
     * @return an array of tasks (never null; may be empty)
     * @throws TasksDAOException on persistence errors
     */
    ITask[] getTasks() throws TasksDAOException;

    /**
     * Retrieves a single task by id.
     * @param id the task id
     * @return the task, or null if not found
     * @throws TasksDAOException on persistence errors
     */
    ITask getTask(int id) throws TasksDAOException;

    /**
     * Adds a new task.
     * @param task the task to add
     * @throws TasksDAOException on persistence errors or validation failures
     */
    void addTask(ITask task) throws TasksDAOException;

    /**
     * Updates an existing task.
     * @param task the task to update
     * @throws TasksDAOException on persistence errors or if the task doesn't exist
     */
    void updateTask(ITask task) throws TasksDAOException;

    /**
     * Deletes all tasks.
     * @throws TasksDAOException on persistence errors
     */
    void deleteTasks() throws TasksDAOException;

    /**
     * Deletes a specific task by id.
     * @param id the task id
     * @throws TasksDAOException on persistence errors
     */
    void deleteTask(int id) throws TasksDAOException;
}
