package taskmanagement.persistence.derby;

/**
 * Provides constants and utility methods for configuring
 * the embedded Apache Derby database used by the application.
 * <p>
 * This class centralizes the database name, driver class,
 * and connection URL construction for create, boot, and shutdown phases.
 * It cannot be instantiated.
 */
public final class DerbyConfig {

    /**
     * The relative name of the embedded Derby database.
     */
    public static final String DB_NAME = "tasksdb";

    /**
     * The fully qualified class name of the embedded Derby JDBC driver.
     */
    public static final String DRIVER_CLASS = "org.apache.derby.jdbc.EmbeddedDriver";

    /**
     * The name of the tasks table in the database schema.
     */
    public static final String TABLE_TASKS = "tasks";

    /**
     * Private constructor to prevent instantiation.
     */
    private DerbyConfig() {
    }

    /**
     * Builds the JDBC URL for creating the database if it does not already exist.
     *
     * @return a JDBC URL string for creating the Derby database
     */
    public static String urlCreate() {
        return "jdbc:derby:" + DB_NAME + ";create=true";
    }

    /**
     * Builds the JDBC URL for booting an existing Derby database.
     *
     * @return a JDBC URL string for booting the Derby database
     */
    public static String urlBoot() {
        return "jdbc:derby:" + DB_NAME;
    }

    /**
     * Builds the JDBC URL for shutting down the entire embedded Derby system.
     * Note: a successful shutdown typically throws a {@link java.sql.SQLException}.
     *
     * @return a JDBC URL string for shutting down Derby
     */
    public static String urlShutdown() {
        return "jdbc:derby:;shutdown=true";
    }
}
