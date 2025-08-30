package taskmanagement.persistence.derby;

import java.sql.*;

/**
 * DerbyBootstrap
 * --------------
 * Loads Derby embedded driver, boots/creates the DB, and ensures schema.
 */
public final class DerbyBootstrap {

    private DerbyBootstrap() { }

    /**
     * Boot Derby, create DB & schema if needed, and return an open Connection.
     */
    public static Connection bootAndEnsureSchema() {
        try {
            Class.forName(DerbyConfig.DRIVER_CLASS);
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException("Derby driver not found: " + DerbyConfig.DRIVER_CLASS, e);
        }

        try {
            // Create DB if needed
            try (Connection ignored = DriverManager.getConnection(DerbyConfig.urlCreate())) {
                // just creating; the connection will be closed by try-with-resources
            }
        } catch (SQLException createEx) {
            // If DB already exists, Derby may still succeed without throwing; if it throws for some reason,
            // we will continue to boot normally.
        }

        try {
            Connection conn = DriverManager.getConnection(DerbyConfig.urlBoot());
            ensureSchema(conn);
            return conn;
        } catch (SQLException bootEx) {
            throw new IllegalStateException("Failed to boot Derby DB", bootEx);
        }
    }

    /**
     * Ensure required tables exist. Creates them if missing.
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

    private static boolean tableExists(Connection conn, String table) throws SQLException {
        DatabaseMetaData meta = conn.getMetaData();
        try (ResultSet rs = meta.getTables(null, null, table.toUpperCase(), null)) {
            return rs.next();
        }
    }

    /**
     * Attempt to shutdown Derby and close the given connection quietly.
     */
    public static void shutdownQuietly(Connection conn) {
        if (conn != null) {
            try { conn.close(); } catch (SQLException ignored) {}
        }
        try {
            DriverManager.getConnection(DerbyConfig.urlShutdown());
        } catch (SQLException ignored) {
            // Derby throws an exception on successful shutdown in embedded mode – ignore by design.
        }
    }
}
