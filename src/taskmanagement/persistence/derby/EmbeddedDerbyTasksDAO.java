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
 * {@code EmbeddedDerbyTasksDAO}
 * <p>
 * An {@link ITasksDAO} implementation over embedded Apache Derby (Single connection, Singleton).
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

    /**
     * Retrieves all tasks ordered by id.
     *
     * @return an array of tasks (never {@code null})
     * @throws TasksDAOException on SQL error or mapping failure
     */
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

    /**
     * Retrieves a single task by id.
     *
     * @param id the task id
     * @return the task (never {@code null})
     * @throws TasksDAOException if not found or on SQL/mapping error
     */
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

    /**
     * Inserts a new task. If {@code task.getId() <= 0} a new id is allocated from DB.
     * The effective id is written back into the given task object (when the implementation allows).
     *
     * @param task the task to insert (must not be {@code null})
     * @throws TasksDAOException on SQL error or duplicate key
     */
    @Override
    public void addTask(ITask task) throws TasksDAOException {
        Objects.requireNonNull(task, "task");

        // We explicitly insert the id (no auto-increment in schema).
        // If caller provided id <= 0, allocate the next id first.
        int idToInsert = task.getId();
        try {
            if (idToInsert <= 0) {
                idToInsert = nextId();
            }
        } catch (SQLException e) {
            throw new TasksDAOException("Failed to allocate next id", e);
        }

        final String sql = "INSERT INTO TASKS (id, title, description, state) VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idToInsert);
            ps.setString(2, task.getTitle());
            ps.setString(3, task.getDescription());
            ps.setString(4, task.getState().name());
            ps.executeUpdate();

            // IMPORTANT: write the effective id back into the task object
            // (needed for Command undo/redo logic, e.g., undo Add → delete by id)
            if (task instanceof Task t) {
                if (t.getId() != idToInsert) {
                    t.setId(idToInsert); // Task#setId should validate id > 0
                }
            } else {
                // If a different ITask implementation is used, we require it to carry the id already.
                // Otherwise, undo/redo may not work correctly.
                if (task.getId() <= 0) {
                    throw new TasksDAOException(
                            "Unsupported task implementation; cannot assign generated id to " + task.getClass().getName());
                }
            }

        } catch (SQLException e) {
            // Derby duplicate key SQLState is 23505
            if ("23505".equals(e.getSQLState())) {
                throw new TasksDAOException("Task id already exists: id=" + idToInsert, e);
            }
            throw new TasksDAOException("addTask failed for id=" + idToInsert, e);
        }
    }

    /**
     * Updates an existing task by id.
     *
     * @param task the task with new values
     * @throws TasksDAOException if task not found or on SQL error
     */
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
     * Deletes all tasks.
     *
     * @throws TasksDAOException on SQL error
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

    /**
     * Deletes a single task by id.
     *
     * @param id the task id
     * @throws TasksDAOException if not found or on SQL error
     */
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

    /**
     * Maps a single row into a domain {@link Task}.
     *
     * @param rs the current result set row
     * @return mapped {@link Task}
     * @throws SQLException        on JDBC error
     * @throws ValidationException if domain validation fails
     */
    private Task mapRow(ResultSet rs) throws SQLException, ValidationException {
        final int id = rs.getInt("id");
        final String title = rs.getString("title");
        final String description = rs.getString("description");
        final String stateStr = rs.getString("state");
        final TaskState state = TaskState.valueOf(stateStr);
        return new Task(id, title, description, state);
    }

    /**
     * Allocates the next available id from the TASKS table.
     * <p>Starts from {@code 1} when the table is empty (IDs are always positive).</p>
     *
     * @return the next id (always {@code >= 1})
     * @throws SQLException on SQL error
     */
    private int nextId() throws SQLException {
        final String sql = "SELECT COALESCE(MAX(id), 0) + 1 FROM TASKS"; // start from 1
        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            rs.next();
            return rs.getInt(1);
        }
    }

    /**
     * Optional: call on app shutdown to close Derby.
     */
    public void shutdown() {
        DerbyBootstrap.shutdownQuietly(conn);
    }
}
