package taskmanagement.application.viewmodel;

import taskmanagement.domain.ITask;
import taskmanagement.domain.Task;
import taskmanagement.domain.TaskState;
import taskmanagement.persistence.ITasksDAO;
import taskmanagement.persistence.TasksDAOException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * MVVM ViewModel for the Tasks screen.
 * <p>
 * Responsibilities:
 * <ul>
 *   <li>Mediate between the UI and the DAO-backed model.</li>
 *   <li>Expose read-only task snapshots suitable for the UI (no domain leakage).</li>
 *   <li>Provide imperative commands for CRUD and state changes.</li>
 *   <li>Enforce basic validation (null/empty checks) and call the DAO.</li>
 * </ul>
 *
 * <h2>Notes</h2>
 * - The UI never touches {@link ITask} or {@link Task} directly.
 * - After each mutation, {@link #reload()} is called so the UI sees generated IDs and the latest state.
 */
public final class TasksViewModel {

    /** DAO dependency (model). */
    private final ITasksDAO dao;

    /** Cached snapshot of tasks for UI consumption (latest reload). */
    private List<ITask> snapshot = Collections.emptyList();

    /**
     * Immutable UI row. Keeps the UI decoupled from domain types.
     *
     * @param id          task id
     * @param title       title
     * @param description description
     * @param state       state as string (e.g., "ToDo")
     */
    public record RowDTO(int id, String title, String description, String state) { }

    /**
     * Constructs the ViewModel with a DAO instance.
     *
     * @param dao tasks DAO (must not be null)
     */
    public TasksViewModel(ITasksDAO dao) {
        this.dao = Objects.requireNonNull(dao, "ITasksDAO must not be null");
    }

    // =====================
    // Query side (read)
    // =====================

    /**
     * Reloads tasks from the DAO into an internal snapshot.
     *
     * @throws TasksDAOException if the DAO read fails
     */
    public void reload() throws TasksDAOException {
        ITask[] arr = dao.getTasks();
        this.snapshot = (arr == null) ? List.of() : Arrays.asList(arr);
    }

    /**
     * Returns a UI-friendly snapshot of tasks.
     * <p>
     * Call {@link #reload()} before this if you want fresh data.
     *
     * @return immutable list of {@link RowDTO}
     */
    public List<RowDTO> getRows() {
        List<RowDTO> out = new ArrayList<>(snapshot.size());
        for (ITask t : snapshot) {
            out.add(new RowDTO(
                    t.getId(),
                    safe(t.getTitle()),
                    safe(t.getDescription()),
                    (t.getState() == null) ? "" : t.getState().name()
            ));
        }
        return Collections.unmodifiableList(out);
    }

    // =====================
    // Command side (write)
    // =====================

    /**
     * Adds a new task.
     *
     * @param title       non-empty title
     * @param description nullable/optional description
     * @param state       non-null state
     * @throws Exception wraps {@link TasksDAOException} or validation exceptions from the domain model
     */
    public void addTask(String title, String description, TaskState state) throws Exception {
        validateTitle(title);
        Objects.requireNonNull(state, "state must not be null");

        // id=0 (or negative) indicates "not yet persisted"; DAO should assign a real id.
        Task toAdd = new Task(0, title, description, state);
        dao.addTask(toAdd);
        reload();
    }

    /**
     * Updates an existing task by id.
     *
     * @param id          existing task id
     * @param title       non-empty title
     * @param description nullable/optional description
     * @param state       non-null state
     * @throws Exception wraps {@link TasksDAOException} or validation exceptions from the domain model
     */
    public void updateTask(int id, String title, String description, TaskState state) throws Exception {
        validateId(id);
        validateTitle(title);
        Objects.requireNonNull(state, "state must not be null");

        Task updated = new Task(id, title, description, state);
        dao.updateTask(updated);
        reload();
    }

    /**
     * Deletes a task by id.
     *
     * @param id existing task id
     * @throws TasksDAOException if the DAO operation fails
     */
    public void deleteTask(int id) throws TasksDAOException {
        validateId(id);
        dao.deleteTask(id);      // IMPORTANT: using the real ID fixes "Invalid id for delete: 0"
        reload();
    }

    /**
     * Marks task state by id.
     *
     * @param id    existing task id
     * @param state non-null state
     * @throws Exception wraps {@link TasksDAOException} or validation exceptions
     */
    public void markState(int id, TaskState state) throws Exception {
        validateId(id);
        Objects.requireNonNull(state, "state must not be null");

        // Option A: fetch existing task, update only state, persist.
        ITask original = dao.getTask(id);
        if (original == null) {
            throw new TasksDAOException("Task not found: id=" + id);
        }
        Task updated = new Task(
                original.getId(),
                safe(original.getTitle()),
                safe(original.getDescription()),
                state
        );
        dao.updateTask(updated);
        reload();
    }

    // =====================
    // Helpers & validation
    // =====================

    private static String safe(String s) {
        return (s == null) ? "" : s;
    }

    private static void validateTitle(String title) {
        if (title == null || title.trim().isEmpty()) {
            throw new IllegalArgumentException("title must not be empty");
        }
    }

    private static void validateId(int id) {
        if (id < 0) {
            throw new IllegalArgumentException("invalid id: " + id);
        }
    }
}
