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
 * {@code EmbeddedDerbyTasksDAO}
 * <br/>
 * ITasksDAO implementation over embedded Apache Derby.
 * <ul>
 *   <li>Pure persistence (no UI / no ViewModel logic).</li>
 *   <li>Honors the DAO contract — never returns {@code null} for "not found":
 *       throws {@link TasksDAOException} instead.</li>
 *   <li>Singleton instance with a single bootstrapped {@link Connection}.</li>
 * </ul>
 */
public final class EmbeddedDerbyTasksDAO implements ITasksDAO {

    /* ===== Singleton ===== */
    private static EmbeddedDerbyTasksDAO instance;

    /**
     * Returns the single DAO instance (Singleton).
     *
     * @return the singleton instance
     */
    public static synchronized EmbeddedDerbyTasksDAO getInstance() {
        if (instance == null) {
            instance = new EmbeddedDerbyTasksDAO();
        }
        return instance;
    }

    /* ===== State ===== */
    private final Connection conn;

    /**
     * Boots Derby and ensures schema is present (tables, indexes).
     * Package-private by design — use {@link #getInstance()}.
     */
    EmbeddedDerbyTasksDAO() {
        this.conn = DerbyBootstrap.bootAndEnsureSchema();
    }

    /* ===== ITasksDAO ===== */

    /** {@inheritDoc} */
    @Override
    public ITask[] getTasks() throws TasksDAOException {
        final String sql = "SELECT id, title, description, state FROM TASKS ORDER BY id";
        final List<ITask> list = new ArrayList<>();
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

    /** {@inheritDoc} */
    @Override
    public ITask getTask(int id) throws TasksDAOException {
        final String sql = "SELECT id, title, description, state FROM TASKS WHERE id=?";
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

    /** {@inheritDoc} */
    @Override
    public void addTask(ITask task) throws TasksDAOException {
        final String sql = "INSERT INTO TASKS (id, title, description, state) VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {

            // If caller passed id<=0, allocate the next id from DB.
            int idToInsert = task.getId();
            if (idToInsert <= 0) {
                idToInsert = nextId();
            }

            ps.setInt(1, idToInsert);
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

    /** {@inheritDoc} */
    @Override
    public void updateTask(ITask task) throws TasksDAOException {
        final String sql = "UPDATE TASKS SET title=?, description=?, state=? WHERE id=?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, task.getTitle());
            ps.setString(2, task.getDescription());
            ps.setString(3, task.getState().name());
            ps.setInt(4, task.getId());
            final int updated = ps.executeUpdate();
            if (updated == 0) {
                throw new TasksDAOException("Cannot update, task not found: id=" + task.getId());
            }
        } catch (SQLException e) {
            throw new TasksDAOException("updateTask failed for id=" + task.getId(), e);
        }
    }

    /**
     * Deletes all tasks from the table.
     *
     * @throws TasksDAOException if the operation fails
     */
    @Override
    public void deleteTasks() throws TasksDAOException {
        final String sql = "DELETE FROM TASKS";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new TasksDAOException("Failed to delete all tasks", e);
        }
    }

    /** {@inheritDoc} */
    @Override
    public void deleteTask(int id) throws TasksDAOException {
        final String sql = "DELETE FROM TASKS WHERE id=?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            final int deleted = ps.executeUpdate();
            if (deleted == 0) {
                throw new TasksDAOException("Cannot delete, task not found: id=" + id);
            }
        } catch (SQLException e) {
            throw new TasksDAOException("deleteTask failed for id=" + id, e);
        }
    }

    /* ===== Helpers ===== */

    private Task mapRow(ResultSet rs) throws SQLException, ValidationException {
        final int id = rs.getInt("id");
        final String title = rs.getString("title");
        final String description = rs.getString("description");
        final String stateStr = rs.getString("state");
        final TaskState state = TaskState.valueOf(stateStr);
        return new Task(id, title, description, state);
    }

    private int nextId() throws SQLException {
        final String sql = "SELECT COALESCE(MAX(id), -1) + 1 FROM TASKS";
        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            rs.next();
            return rs.getInt(1);
        }
    }

    /** Optional: call on app shutdown if you want a clean Derby shutdown. */
    public void shutdown() {
        DerbyBootstrap.shutdownQuietly(conn);
    }
}
