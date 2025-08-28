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
 * Embedded Derby implementation of {@link ITasksDAO}.
 * <p>
 * Pattern notes:
 * <ul>
 *   <li><b>DAO</b> — encapsulates all DB access for tasks.</li>
 *   <li><b>Singleton</b> — single shared instance via {@link #getInstance()}.</li>
 * </ul>
 * All checked/validation/data errors are wrapped in {@link TasksDAOException}.
 */
public final class EmbeddedDerbyTasksDAO implements ITasksDAO {

    /** Singleton holder. */
    private static volatile EmbeddedDerbyTasksDAO instance;

    /** Private C'tor — initializes Derby schema if missing. */
    private EmbeddedDerbyTasksDAO() throws TasksDAOException {
        try {
            DerbyBootstrap.ensureSchema();
        } catch (Exception e) {
            throw new TasksDAOException("Failed to initialize Derby schema", e);
        }
    }

    /**
     * Global accessor (Singleton, double-checked locking).
     *
     * @return the single DAO instance
     * @throws TasksDAOException if initialization fails
     */
    public static EmbeddedDerbyTasksDAO getInstance() throws TasksDAOException {
        if (instance == null) {
            synchronized (EmbeddedDerbyTasksDAO.class) {
                if (instance == null) {
                    instance = new EmbeddedDerbyTasksDAO();
                }
            }
        }
        return instance;
    }

    /** Opens a new JDBC connection to the embedded Derby DB. */
    private Connection conn() throws SQLException {
        return DriverManager.getConnection(DerbyConfig.URL);
    }

    // ----------------------------------------------------
    // ITasksDAO implementation
    // ----------------------------------------------------

    /** {@inheritDoc} */
    @Override
    public ITask[] getTasks() throws TasksDAOException {
        final String sql = "SELECT ID, TITLE, DESCRIPTION, STATE FROM " + DerbyConfig.TABLE_TASKS + " ORDER BY ID";
        final List<ITask> list = new ArrayList<>();
        try (Connection c = conn();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapRow(rs));
            }
            return list.toArray(new ITask[0]);
        } catch (SQLException | ValidationException e) {
            throw new TasksDAOException("getTasks failed", e);
        }
    }

    /** {@inheritDoc} */
    @Override
    public ITask getTask(int id) throws TasksDAOException {
        final String sql = "SELECT ID, TITLE, DESCRIPTION, STATE FROM " + DerbyConfig.TABLE_TASKS + " WHERE ID=?";
        try (Connection c = conn(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapRow(rs) : null;
            }
        } catch (SQLException | ValidationException e) {
            throw new TasksDAOException("getTask failed id=" + id, e);
        }
    }

    /** {@inheritDoc} */
    @Override
    public void addTask(ITask task) throws TasksDAOException {
        final String sql = "INSERT INTO " + DerbyConfig.TABLE_TASKS + " (ID, TITLE, DESCRIPTION, STATE) VALUES (?,?,?,?)";
        try (Connection c = conn(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, task.getId());
            ps.setString(2, task.getTitle());
            ps.setString(3, task.getDescription());
            ps.setString(4, task.getState().name());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new TasksDAOException("addTask failed id=" + task.getId(), e);
        }
    }

    /** {@inheritDoc} */
    @Override
    public void updateTask(ITask task) throws TasksDAOException {
        final String sql = "UPDATE " + DerbyConfig.TABLE_TASKS + " SET TITLE=?, DESCRIPTION=?, STATE=? WHERE ID=?";
        try (Connection c = conn(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, task.getTitle());
            ps.setString(2, task.getDescription());
            ps.setString(3, task.getState().name());
            ps.setInt(4, task.getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new TasksDAOException("updateTask failed id=" + task.getId(), e);
        }
    }

    /** {@inheritDoc} */
    @Override
    public void deleteTasks() throws TasksDAOException {
        final String sql = "DELETE FROM " + DerbyConfig.TABLE_TASKS;
        try (Connection c = conn(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new TasksDAOException("deleteTasks failed", e);
        }
    }

    /** {@inheritDoc} */
    @Override
    public void deleteTask(int id) throws TasksDAOException {
        final String sql = "DELETE FROM " + DerbyConfig.TABLE_TASKS + " WHERE ID=?";
        try (Connection c = conn(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new TasksDAOException("deleteTask failed id=" + id, e);
        }
    }

    // ----------------------------------------------------
    // Helpers
    // ----------------------------------------------------

    /**
     * Maps the current {@link ResultSet} row to a domain {@link ITask}.
     * Wraps invalid enum values as {@link SQLException} and lets domain validation bubble as {@link ValidationException}.
     */
    private static ITask mapRow(ResultSet rs) throws SQLException, ValidationException {
        final int id = rs.getInt("ID");
        final String title = rs.getString("TITLE");
        final String desc = rs.getString("DESCRIPTION");

        final String stateStr = rs.getString("STATE");
        final TaskState state;
        try {
            state = TaskState.valueOf(stateStr);
        } catch (IllegalArgumentException iae) {
            // Defensive: corrupted/unknown enum value in DB
            throw new SQLException("Invalid STATE enum value in DB: " + stateStr, iae);
        }
        // Task constructor/setters may validate and throw ValidationException (checked)
        return new Task(id, title, desc, state);
    }
}
