package taskmanagement.persistence;

import taskmanagement.domain.ITask;

/**
 * Data-Access interface for tasks.
 * Must be implemented exactly as defined in the requirements.
 */
public interface ITasksDAO {
    ITask[] getTasks();
    ITask getTask(int id);
    void addTask(ITask task);
    void updateTask(ITask task);
    void deleteTasks();
    void deleteTask(int id);
}
