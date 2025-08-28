package taskmanagement.persistence.derby;

import taskmanagement.domain.ITask;
import taskmanagement.domain.Task;
import taskmanagement.domain.TaskState;
import taskmanagement.domain.exceptions.ValidationException;
import taskmanagement.persistence.ITasksDAO;
import taskmanagement.persistence.TasksDAOException;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Embedded Derby DAO (Singleton).
 * Maintains a single Connection per process and adds a JVM shutdown hook to close DB cleanly.
 */
public final class EmbeddedDerbyTasksDAO implements ITasksDAO, AutoCloseable {

    private static volatile EmbeddedDerbyTasksDAO INSTANCE;
    private final Connection conn;

    private EmbeddedDerbyTasksDAO() {
        this.conn = DerbyBootstrap.bootAndEnsureSchema();
        Runtime.getRuntime().addShutdownHook(new Thread(DerbyBootstrap::shutdownQuietly, "DerbyShutdown"));
    }

    /** Singleton accessor. */
    public static EmbeddedDerbyTasksDAO getInstance() {
        if (INSTANCE == null) {
            synchronized (EmbeddedDerbyTasksDAO.class) {
                if (INSTANCE == null) {
                    INSTANCE = new EmbeddedDerbyTasksDAO();
                }
            }
        }
        return INSTANCE;
    }

    @Override
    public ITask[] getTasks() throws TasksDAOException {
        final String sql = "SELECT id, title, description, state FROM tasks ORDER BY id";
        final List<ITask> list = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(mapRow(rs));
            return list.toArray(ITask[]::new);
        } catch (SQLException e) {
            throw new TasksDAOException("getTasks failed", e);
        }
    }

    @Override
    public ITask getTask(int id) throws TasksDAOException {
        final String sql = "SELECT id, title, description, state FROM tasks WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapRow(rs) : null;
            }
        } catch (SQLException e) {
            throw new TasksDAOException("getTask failed for id=" + id, e);
        }
    }

    @Override
    public void addTask(ITask task) throws TasksDAOException {
        Objects.requireNonNull(task, "task");
        final String sql = "INSERT INTO tasks(id, title, description, state) VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            bindTask(ps, task);
            final int n = ps.executeUpdate();
            if (n != 1) throw new TasksDAOException("addTask affected " + n + " rows");
        } catch (SQLException e) {
            // Derby duplicate key
            if ("23505".equals(e.getSQLState())) {
                throw new TasksDAOException("addTask failed: id " + task.getId() + " already exists", e);
            }
            throw new TasksDAOException("addTask failed for id=" + task.getId(), e);
        }
    }

    @Override
    public void updateTask(ITask task) throws TasksDAOException {
        Objects.requireNonNull(task, "task");
        final String sql = "UPDATE tasks SET title = ?, description = ?, state = ? WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, task.getTitle());
            ps.setString(2, task.getDescription());
            ps.setString(3, task.getState().name());
            ps.setInt(4, task.getId());
            final int n = ps.executeUpdate();
            if (n != 1) {
                throw new TasksDAOException("updateTask updated " + n + " rows (expected 1). id=" + task.getId());
            }
        } catch (SQLException e) {
            throw new TasksDAOException("updateTask failed for id=" + task.getId(), e);
        }
    }

    @Override
    public void deleteTasks() throws TasksDAOException {
        final String sql = "DELETE FROM tasks";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new TasksDAOException("deleteTasks failed", e);
        }
    }

    @Override
    public void deleteTask(int id) throws TasksDAOException {
        final String sql = "DELETE FROM tasks WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            final int n = ps.executeUpdate();
            if (n != 1) {
                throw new TasksDAOException("deleteTask removed " + n + " rows (expected 1). id=" + id);
            }
        } catch (SQLException e) {
            throw new TasksDAOException("deleteTask failed for id=" + id, e);
        }
    }

    // ---------- helpers ----------

    private static void bindTask(PreparedStatement ps, ITask t) throws SQLException {
        ps.setInt(1, t.getId());
        ps.setString(2, t.getTitle());
        ps.setString(3, t.getDescription());
        ps.setString(4, t.getState().name()); // store enum name exactly
    }

    private static ITask mapRow(ResultSet rs) {
        try {
            final int id = rs.getInt("id");
            final String title = rs.getString("title");
            final String desc = rs.getString("description");
            final String stateStr = rs.getString("state");
            final TaskState state;
            try {
                state = TaskState.valueOf(stateStr);
            } catch (IllegalArgumentException iae) {
                // Defensive: corrupted/unknown enum value in DB
                throw new SQLException("Invalid STATE enum value in DB: " + stateStr, iae);
            }
            return new Task(id, title, desc, state);
        } catch (SQLException e) {
            throw new TasksDAOException("Failed mapping row", e);
        } catch (ValidationException ve) {
            throw new TasksDAOException("Domain validation failed while mapping DB row", ve);
        }
    }

    @Override
    public void close() {
        try { if (conn != null && !conn.isClosed()) conn.close(); }
        catch (SQLException ignore) { }
    }
}
