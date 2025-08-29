package taskmanagement.persistence;

import taskmanagement.domain.ITask;

/**
 * Data Access Object (DAO) interface for tasks.
 */
public interface ITasksDAO {
    ITask[] getTasks() throws TasksDAOException;
    ITask getTask(int id) throws TasksDAOException;
    void addTask(ITask task) throws TasksDAOException;
    void updateTask(ITask task) throws TasksDAOException;
    void deleteTasks() throws TasksDAOException;
    void deleteTask(int id) throws TasksDAOException;
}
