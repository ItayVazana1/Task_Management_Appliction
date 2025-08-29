package taskmanagement.persistence;

import taskmanagement.persistence.derby.EmbeddedDerbyTasksDAO;

/**
 * Central provider for the application's Data Access Object (DAO).
 * <p>
 * Ensures a single {@link ITasksDAO} instance is used (Singleton pattern).
 */
public final class DAOProvider {

    private DAOProvider() {
        // utility class, prevent instantiation
    }

    /**
     * Returns the singleton DAO implementation.
     *
     * @return global {@link ITasksDAO} instance backed by Embedded Derby
     */
    public static ITasksDAO get() {
        return EmbeddedDerbyTasksDAO.getInstance();
    }
}
