package taskmanagement.persistence;

import taskmanagement.persistence.derby.EmbeddedDerbyTasksDAO;

/**
 * Provides centralized access to the application's {@link ITasksDAO}.
 * <p>
 * This class ensures that only a single {@link ITasksDAO} instance is used
 * throughout the application (Singleton pattern).
 * </p>
 */
public final class DAOProvider {

    /**
     * Private constructor to prevent instantiation of this utility class.
     */
    private DAOProvider() {
    }

    /**
     * Returns the global singleton instance of the {@link ITasksDAO}.
     * <p>
     * The returned DAO is backed by an embedded Derby implementation.
     * </p>
     *
     * @return the singleton {@link ITasksDAO} instance
     */
    public static ITasksDAO get() {
        return EmbeddedDerbyTasksDAO.getInstance();
    }
}
