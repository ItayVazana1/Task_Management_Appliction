package taskmanagement.persistence.derby;

/**
 * Central Derby configuration (embedded mode).
 * Keeps JDBC driver, URL and SQL DDL constants.
 */
public final class DerbyConfig {
    private DerbyConfig() { }

    /** Embedded driver class name. */
    public static final String DRIVER = "org.apache.derby.jdbc.EmbeddedDriver";

    /** DB location under ./data; create if missing. */
    public static final String URL = "jdbc:derby:data/tasksdb;create=true";

    /** Table names. */
    public static final String TABLE_TASKS = "TASKS";

    /** Minimal schema for tasks (IDs managed by the app). */
    public static final String SQL_CREATE_TASKS =
            "CREATE TABLE " + TABLE_TASKS + " (" +
                    "  ID INT PRIMARY KEY, " +
                    "  TITLE VARCHAR(200) NOT NULL, " +
                    "  DESCRIPTION VARCHAR(2000) NOT NULL, " +
                    "  STATE VARCHAR(32) NOT NULL" +
                    ")";
}
