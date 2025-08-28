package taskmanagement.persistence.derby;

import taskmanagement.persistence.TasksDAOException;

import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.file.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.stream.Stream;

/**
 * Robust, idempotent boot for Embedded Derby:
 * - Do NOT pre-create the DB directory (Derby must create it on ;create=true).
 * - If DB dir exists without a valid Derby database (no service.properties), clean or instruct.
 * - Prevent ghost locks from IDE Database viewer.
 */
public final class DerbyBootstrap {
    private static final String MARKER_FILE = "service.properties"; // Derby's DB marker

    private DerbyBootstrap() {}

    /** If db.lck exists but isn’t held, delete it; if held by another process, fail fast. */
    public static void ensureNotBootedByAnotherProcess() {
        Path lock = DerbyConfig.DB_DIR.resolve("db.lck");
        if (!Files.exists(lock)) return;
        try (FileChannel ch = FileChannel.open(lock, StandardOpenOption.WRITE)) {
            FileLock fl = ch.tryLock();
            if (fl != null) {
                try { fl.close(); } finally { Files.deleteIfExists(lock); }
            } else {
                throw new TasksDAOException(
                        "Database is in use by another process (IDE Database viewer?). Disconnect it and retry.");
            }
        } catch (IOException | java.nio.channels.OverlappingFileLockException e) {
            throw new TasksDAOException("Database appears to be locked by another process.", e);
        }
    }

    /**
     * Idempotent boot:
     * 1) Try normal boot (no create).
     * 2) If not found (XJ004), ensure parent dir exists, make sure DB dir is absent or clean,
     *    then create via ;create=true and build schema.
     */
    public static Connection bootAndEnsureSchema() {
        ensureNotBootedByAnotherProcess();

        // 1) Try normal boot (no ;create=true)
        try {
            Connection c = DriverManager.getConnection(DerbyConfig.urlBoot());
            ensureSchema(c);
            return c;
        } catch (SQLException bootEx) {
            if (!"XJ004".equals(bootEx.getSQLState())) {
                throw new TasksDAOException("Failed to boot Derby database.", bootEx);
            }
            // XJ004: DB not found -> create it properly
        }

        // 2) Prepare for creation: ensure parent exists, and DB_DIR is either absent or a clean empty dir.
        try {
            Files.createDirectories(DerbyConfig.DB_DIR.getParent()); // parent only!

            if (Files.exists(DerbyConfig.DB_DIR)) {
                Path marker = DerbyConfig.DB_DIR.resolve(MARKER_FILE);
                if (Files.exists(marker)) {
                    // Looks like a real Derby DB but boot failed for another reason -> escalate
                    throw new TasksDAOException("Derby DB folder exists but failed to boot. " +
                            "Disconnect IDE viewer or remove the folder if it's corrupted: " + DerbyConfig.DB_DIR);
                } else {
                    // Not a valid Derby DB: if empty -> delete; if not empty -> instruct the user
                    if (isDirectoryEmpty(DerbyConfig.DB_DIR)) {
                        Files.delete(DerbyConfig.DB_DIR); // let Derby create it
                    } else {
                        throw new TasksDAOException("Folder exists but is not a Derby DB and not empty: " +
                                DerbyConfig.DB_DIR + ". Please remove or empty this folder and rerun.");
                    }
                }
            }

            // Now actually create the database (directory must NOT exist here)
            Connection c = DriverManager.getConnection(DerbyConfig.urlCreate());
            ensureSchema(c);
            return c;

        } catch (IOException io) {
            throw new TasksDAOException("Failed preparing Derby directories.", io);
        } catch (SQLException createEx) {
            // If we still get XBM0J here, the dir exists – likely a race; give a clear message.
            if ("XBM0J".equals(createEx.getSQLState())) {
                throw new TasksDAOException("Create failed: target directory already exists. " +
                        "Delete '" + DerbyConfig.DB_DIR + "' (or ensure it's empty) and rerun.", createEx);
            }
            throw new TasksDAOException("Failed to create Derby database.", createEx);
        }
    }

    private static boolean isDirectoryEmpty(Path dir) throws IOException {
        try (Stream<Path> s = Files.list(dir)) {
            return !s.findFirst().isPresent();
        }
    }

    private static void ensureSchema(Connection c) {
        try (Statement st = c.createStatement()) {
            st.executeUpdate("""
                CREATE TABLE tasks(
                  id INT PRIMARY KEY,
                  title VARCHAR(255) NOT NULL,
                  description VARCHAR(1024),
                  state VARCHAR(32) NOT NULL
                )
            """);
        } catch (SQLException ex) {
            // X0Y32 = object already exists -> OK
            if (!"X0Y32".equals(ex.getSQLState())) {
                throw new TasksDAOException("Schema init failed.", ex);
            }
        }
    }

    /** Clean Derby shutdown. XJ015/08006 are expected on shutdown. */
    public static void shutdownQuietly() {
        try {
            DriverManager.getConnection(DerbyConfig.urlShutdown());
        } catch (SQLException e) {
            String s = e.getSQLState();
            if (!"XJ015".equals(s) && !"08006".equals(s)) e.printStackTrace();
        }
    }
}
