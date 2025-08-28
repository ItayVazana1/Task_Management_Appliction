package taskmanagement.persistence;

import taskmanagement.persistence.derby.EmbeddedDerbyTasksDAO;

/** Central accessor for the concrete DAO instance. */
public final class DAOProvider {
    private DAOProvider() {}

    public static ITasksDAO tasksDAO() throws TasksDAOException {
        return EmbeddedDerbyTasksDAO.getInstance();
    }
}
