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

/**
 * EmbeddedDerbyTasksDAO
 * ---------------------
 * ITasksDAO implementation over embedded Apache Derby.
 * - Pure persistence (no UI).
 * - Honors the DAO contract: never returns null for "not found" → throws TasksDAOException.
 * - Uses a single bootstrapped Connection (Singleton DAO instance).
 */
public final class EmbeddedDerbyTasksDAO implements ITasksDAO {

    /* ===== Singleton ===== */
    private static EmbeddedDerbyTasksDAO instance;

    public static synchronized EmbeddedDerbyTasksDAO getInstance() {
        if (instance == null) {
            instance = new EmbeddedDerbyTasksDAO();
        }
        return instance;
    }

    /* ===== State ===== */
    private final Connection conn;

    private EmbeddedDerbyTasksDAO() {
        this.conn = DerbyBootstrap.bootAndEnsureSchema();
    }

    /* ===== ITasksDAO ===== */

    @Override
    public ITask[] getTasks() throws TasksDAOException {
        final String sql = "SELECT id, title, description, state FROM " + DerbyConfig.TABLE_TASKS + " ORDER BY id";
        List<ITask> list = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapRow(rs));
            }
            return list.toArray(new ITask[0]);
        } catch (SQLException | ValidationException e) {
            throw new TasksDAOException("Failed to fetch tasks", e);
        }
    }

    @Override
    public ITask getTask(int id) throws TasksDAOException {
        final String sql = "SELECT id, title, description, state FROM " + DerbyConfig.TABLE_TASKS + " WHERE id=?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    throw new TasksDAOException("Task not found: id=" + id);
                }
                return mapRow(rs);
            }
        } catch (SQLException | ValidationException e) {
            throw new TasksDAOException("getTask failed for id=" + id, e);
        }
    }

    @Override
    public void addTask(ITask task) throws TasksDAOException {
        final String sql = "INSERT INTO " + DerbyConfig.TABLE_TASKS + " (id, title, description, state) VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, task.getId());
            ps.setString(2, task.getTitle());
            ps.setString(3, task.getDescription());
            ps.setString(4, task.getState().name());
            ps.executeUpdate();
        } catch (SQLException e) {
            // Derby duplicate key SQLState is 23505
            if ("23505".equals(e.getSQLState())) {
                throw new TasksDAOException("Task id already exists: id=" + task.getId(), e);
            }
            throw new TasksDAOException("addTask failed for id=" + task.getId(), e);
        }
    }

    @Override
    public void updateTask(ITask task) throws TasksDAOException {
        final String sql = "UPDATE " + DerbyConfig.TABLE_TASKS + " SET title=?, description=?, state=? WHERE id=?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, task.getTitle());
            ps.setString(2, task.getDescription());
            ps.setString(3, task.getState().name());
            ps.setInt(4, task.getId());
            int updated = ps.executeUpdate();
            if (updated == 0) {
                throw new TasksDAOException("Cannot update, task not found: id=" + task.getId());
            }
        } catch (SQLException e) {
            throw new TasksDAOException("updateTask failed for id=" + task.getId(), e);
        }
    }

    @Override
    public void deleteTasks() throws TasksDAOException {
        final String sql = "DELETE FROM " + DerbyConfig.TABLE_TASKS;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new TasksDAOException("Failed to delete all tasks", e);
        }
    }

    @Override
    public void deleteTask(int id) throws TasksDAOException {
        final String sql = "DELETE FROM " + DerbyConfig.TABLE_TASKS + " WHERE id=?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            int deleted = ps.executeUpdate();
            if (deleted == 0) {
                throw new TasksDAOException("Cannot delete, task not found: id=" + id);
            }
        } catch (SQLException e) {
            throw new TasksDAOException("deleteTask failed for id=" + id, e);
        }
    }

    /* ===== Helpers ===== */

    /**
     * Maps current ResultSet row to a validated Task.
     * May throw ValidationException if DB contains invalid data.
     */
    private Task mapRow(ResultSet rs) throws SQLException, ValidationException {
        int id = rs.getInt("id");
        String title = rs.getString("title");
        String description = rs.getString("description");
        String stateStr = rs.getString("state");
        TaskState state = TaskState.valueOf(stateStr);
        return new Task(id, title, description, state);
    }

    /** Optional: call on app shutdown if you want a clean Derby shutdown. */
    public void shutdown() {
        DerbyBootstrap.shutdownQuietly(conn);
    }
}
