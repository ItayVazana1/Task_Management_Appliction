package taskmanagement.persistence.derby;

/**
 * DerbyConfig
 * -----------
 * Central place for Derby URL construction and constants.
 * Keeps URLs separated for boot/create/shutdown phases.
 */
public final class DerbyConfig {

    private DerbyConfig() { }

    /** Database name (relative). You can change it if you want a different file name. */
    public static final String DB_NAME = "tasksdb";

    /** Derby embedded driver FQCN. */
    public static final String DRIVER_CLASS = "org.apache.derby.jdbc.EmbeddedDriver";

    /**
     * URL for creating the DB if it doesn't exist.
     * Example: jdbc:derby:tasksdb;create=true
     */
    public static String urlCreate() {
        return "jdbc:derby:" + DB_NAME + ";create=true";
    }

    /**
     * URL for booting an existing DB (no create flag).
     * Example: jdbc:derby:tasksdb
     */
    public static String urlBoot() {
        return "jdbc:derby:" + DB_NAME;
    }

    /**
     * URL for shutting down the whole Derby system.
     * For embedded Derby the shutdown usually throws a SQL exception by design.
     */
    public static String urlShutdown() {
        return "jdbc:derby:;shutdown=true";
    }

    /** Tables / schema constants */
    public static final String TABLE_TASKS = "tasks";
}
