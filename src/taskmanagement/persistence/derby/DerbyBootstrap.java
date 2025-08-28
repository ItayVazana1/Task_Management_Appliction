package taskmanagement.persistence.derby;

import java.sql.*;

public final class DerbyBootstrap {
    private DerbyBootstrap() {}

    /** Ensures Derby driver is loaded and schema exists. */
    public static void ensureSchema() throws SQLException, ClassNotFoundException {
        Class.forName(DerbyConfig.DRIVER);
        try (Connection c = DriverManager.getConnection(DerbyConfig.URL)) {
            if (!tableExists(c, DerbyConfig.TABLE_TASKS)) {
                try (Statement st = c.createStatement()) {
                    st.executeUpdate(DerbyConfig.SQL_CREATE_TASKS);
                }
            }
        }
    }

    private static boolean tableExists(Connection c, String table) throws SQLException {
        try (ResultSet rs = c.getMetaData().getTables(null, null, table.toUpperCase(), null)) {
            return rs.next();
        }
    }
}
