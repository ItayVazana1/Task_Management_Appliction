package taskmanagement.persistence;

import taskmanagement.persistence.derby.EmbeddedDerbyTasksDAO;

/**
 * Central provider for concrete DAO implementations.
 */
public final class DAOProvider {
    private DAOProvider() { }

    public static ITasksDAO tasksDAO() {
        // Singleton DAO (one connection per process)
        return EmbeddedDerbyTasksDAO.getInstance();
    }
}
