package taskmanagement.persistence.derby;

import java.nio.file.Path;
import java.nio.file.Paths;

public final class DerbyConfig {
    public static final Path DB_DIR = Paths.get("data", "tasksdb");

    /** Normal boot – DO NOT use create=true here. */
    public static String urlBoot() {
        return "jdbc:derby:" + DB_DIR.toString().replace('\\','/');
    }

    /** Create path – only when the DB does not exist. */
    public static String urlCreate() {
        return urlBoot() + ";create=true";
    }

    /** Clean shutdown. */
    public static String urlShutdown() {
        return urlBoot() + ";shutdown=true";
    }

    private DerbyConfig() { }
}
