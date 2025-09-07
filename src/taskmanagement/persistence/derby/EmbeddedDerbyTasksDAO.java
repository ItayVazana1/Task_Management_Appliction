package taskmanagement.persistence.derby;

import taskmanagement.domain.ITask;
import taskmanagement.domain.Task;
import taskmanagement.domain.TaskState;
import taskmanagement.domain.exceptions.ValidationException;
import taskmanagement.persistence.ITasksDAO;
import taskmanagement.persistence.TasksDAOException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Data Access Object (DAO) implementation backed by embedded Apache Derby.
 * <p>
 * This implementation maintains a single connection created by {@link DerbyBootstrap}
 * and follows the Singleton pattern via {@link #getInstance()}.
 * It performs only persistence logic and adheres to the {@link ITasksDAO} contract.
 */
public final class EmbeddedDerbyTasksDAO implements ITasksDAO {

    private static EmbeddedDerbyTasksDAO instance;

    /**
     * Returns the singleton instance of this DAO.
     *
     * @return the singleton {@code EmbeddedDerbyTasksDAO} instance
     */
    public static synchronized EmbeddedDerbyTasksDAO getInstance() {
        if (instance == null) {
            instance = new EmbeddedDerbyTasksDAO();
        }
        return instance;
    }

    private final Connection conn;

    EmbeddedDerbyTasksDAO() {
        this.conn = DerbyBootstrap.bootAndEnsureSchema();
    }

    /**
     * Retrieves all tasks ordered by ID.
     *
     * @return an array containing all tasks (never {@code null})
     * @throws TasksDAOException if a database or mapping error occurs
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
     * Retrieves a single task by its ID.
     *
     * @param id the task ID
     * @return the task matching the given ID
     * @throws TasksDAOException if the task is not found or an error occurs
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
     * Inserts a new task. If {@code task.getId() <= 0}, a new ID is allocated and
     * written back into the task object when possible.
     *
     * @param task the task to insert (must not be {@code null})
     * @throws TasksDAOException if a database error or duplicate key occurs
     */
    @Override
    public void addTask(ITask task) throws TasksDAOException {
        Objects.requireNonNull(task, "task");

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

            if (task instanceof Task t) {
                if (t.getId() != idToInsert) {
                    t.setId(idToInsert);
                }
            } else if (task.getId() <= 0) {
                throw new TasksDAOException(
                        "Unsupported task implementation; cannot assign generated id to " + task.getClass().getName());
            }

        } catch (SQLException e) {
            if ("23505".equals(e.getSQLState())) { // Derby duplicate key SQLState
                throw new TasksDAOException("Task id already exists: id=" + idToInsert, e);
            }
            throw new TasksDAOException("addTask failed for id=" + idToInsert, e);
        }
    }

    /**
     * Updates an existing task by its ID.
     *
     * @param task the task containing updated values
     * @throws TasksDAOException if the task is not found or a database error occurs
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
     * @throws TasksDAOException if a database error occurs
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
     * Deletes a single task by its ID.
     *
     * @param id the task ID
     * @throws TasksDAOException if the task is not found or a database error occurs
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

    private Task mapRow(ResultSet rs) throws SQLException, ValidationException {
        final int id = rs.getInt("id");
        final String title = rs.getString("title");
        final String description = rs.getString("description");
        final String stateStr = rs.getString("state");
        final TaskState state = TaskState.valueOf(stateStr);
        return new Task(id, title, description, state);
    }

    private int nextId() throws SQLException {
        final String sql = "SELECT COALESCE(MAX(id), 0) + 1 FROM TASKS";
        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            rs.next();
            return rs.getInt(1);
        }
    }

    /**
     * Shuts down the embedded Derby database quietly and closes the underlying connection.
     */
    public void shutdown() {
        DerbyBootstrap.shutdownQuietly(conn);
    }
}
