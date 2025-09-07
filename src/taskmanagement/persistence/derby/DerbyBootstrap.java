package taskmanagement.persistence.derby;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Utility class responsible for booting the embedded Derby database,
 * creating it if necessary, and ensuring that the required schema exists.
 * <p>
 * This class is final and cannot be instantiated.
 */
public final class DerbyBootstrap {

    /**
     * Private constructor to prevent instantiation.
     */
    private DerbyBootstrap() {
    }

    /**
     * Boots the embedded Derby database, creates it if it does not already exist,
     * ensures that the required schema is present, and returns an open
     * {@link Connection}.
     *
     * @return an open {@link Connection} to the embedded Derby database
     * @throws IllegalStateException if the Derby driver cannot be loaded
     *                               or the database cannot be booted
     */
    public static Connection bootAndEnsureSchema() {
        try {
            Class.forName(DerbyConfig.DRIVER_CLASS);
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException("Derby driver not found: " + DerbyConfig.DRIVER_CLASS, e);
        }

        try (Connection ignored = DriverManager.getConnection(DerbyConfig.urlCreate())) {
            // Attempt to create the database if it does not exist
        } catch (SQLException createEx) {
            // Ignore exceptions if database already exists
        }

        try {
            Connection conn = DriverManager.getConnection(DerbyConfig.urlBoot());
            ensureSchema(conn); // Ensure required tables are present
            return conn;
        } catch (SQLException bootEx) {
            throw new IllegalStateException("Failed to boot Derby DB", bootEx);
        }
    }

    /**
     * Ensures the required tables exist in the database.
     * Creates missing tables if they are not present.
     *
     * @param conn an open {@link Connection} to the database
     * @throws SQLException if a database access error occurs
     */
    private static void ensureSchema(Connection conn) throws SQLException {
        if (!tableExists(conn, DerbyConfig.TABLE_TASKS)) {
            try (Statement st = conn.createStatement()) {
                st.executeUpdate(
                        "CREATE TABLE " + DerbyConfig.TABLE_TASKS + " (" +
                                "  id INT PRIMARY KEY," +
                                "  title VARCHAR(255) NOT NULL," +
                                "  description VARCHAR(2000) NOT NULL," +
                                "  state VARCHAR(32) NOT NULL" +
                                ")"
                );
            }
        }
    }

    /**
     * Checks whether a table with the specified name exists in the database.
     *
     * @param conn  an open {@link Connection} to the database
     * @param table the table name to check
     * @return {@code true} if the table exists, {@code false} otherwise
     * @throws SQLException if a database access error occurs
     */
    private static boolean tableExists(Connection conn, String table) throws SQLException {
        DatabaseMetaData meta = conn.getMetaData();
        try (ResultSet rs = meta.getTables(null, null, table.toUpperCase(), null)) {
            return rs.next();
        }
    }

    /**
     * Attempts to shut down the embedded Derby database and closes
     * the given {@link Connection}. Any shutdown-related exceptions
     * are ignored as Derby throws an exception when shutdown succeeds.
     *
     * @param conn an open {@link Connection} to close, may be {@code null}
     */
    public static void shutdownQuietly(Connection conn) {
        if (conn != null) {
            try {
                conn.close();
            } catch (SQLException ignored) {
                // ignore close failures
            }
        }
        try {
            DriverManager.getConnection(DerbyConfig.urlShutdown());
        } catch (SQLException ignored) {
            // Derby throws an exception on successful shutdown
        }
    }
}
