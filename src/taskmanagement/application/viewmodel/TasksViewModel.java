package taskmanagement.application.viewmodel;

import taskmanagement.domain.ITask;
import taskmanagement.domain.Task;
import taskmanagement.domain.TaskState;
import taskmanagement.domain.exceptions.ValidationException;
import taskmanagement.persistence.ITasksDAO;
import taskmanagement.persistence.TasksDAOException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * TasksViewModel
 * --------------
 * MVVM ViewModel that mediates between the DAO-backed model and Swing views.
 * Exposes read-only snapshots for UI tables and imperative commands to mutate
 * the task store. No Swing code is allowed here.
 */
public final class TasksViewModel {

    private final ITasksDAO dao;

    // Cached lists for UI rendering (simple approach; could be replaced by ObservableList)
    private List<ITask> all = new ArrayList<>();
    private List<ITask> filtered = new ArrayList<>();

    // Current filter criteria
    private String titleContains = "";
    private TaskState stateEquals = null;

    /**
     * Constructs a ViewModel bound to the provided DAO.
     * @param dao tasks DAO (must not be null)
     */
    public TasksViewModel(ITasksDAO dao) {
        this.dao = Objects.requireNonNull(dao, "dao is required");
        reloadSafe();
    }

    // -------------------- Data loading & filtering --------------------

    /**
     * Reload tasks from the DAO and re-apply current filters (swallows DAO errors).
     */
    private void reloadSafe() {
        try {
            ITask[] arr = dao.getTasks();
            List<ITask> tmp = new ArrayList<>();
            if (arr != null) {
                Collections.addAll(tmp, arr);
            }
            this.all = List.copyOf(tmp);
            reapplyFilters();
        } catch (TasksDAOException e) {
            // In MVVM we don't throw checked exceptions to the UI layer.
            // Minimal handling here; you can later surface this via a status Property if you add one.
            System.err.println("DAO error (getTasks): " + e.getMessage());
            this.all = List.of();
            this.filtered = List.of();
        }
    }

    /**
     * Returns an immutable snapshot of the currently filtered tasks.
     */
    public List<ITask> getTasksSnapshot() {
        return List.copyOf(filtered);
    }

    /**
     * Finds a task by id in the cached list or via DAO as a fallback.
     */
    public Optional<ITask> findTaskById(int id) {
        for (ITask t : all) {
            if (t.getId() == id) return Optional.of(t);
        }
        try {
            return Optional.ofNullable(dao.getTask(id));
        } catch (TasksDAOException e) {
            System.err.println("DAO error (getTask): " + e.getMessage());
            return Optional.empty();
        }
    }

    // -------------------- Commands (no checked throws to UI) --------------------

    /**
     * Adds a task through the DAO and refreshes the cache.
     */
    public void addTask(ITask task) {
        Objects.requireNonNull(task, "task is required");
        try {
            dao.addTask(task);
        } catch (TasksDAOException e) {
            System.err.println("DAO error (addTask): " + e.getMessage());
        }
        reloadSafe();
    }

    /**
     * Updates a task through the DAO and refreshes the cache.
     */
    public void updateTask(ITask task) {
        Objects.requireNonNull(task, "task is required");
        try {
            dao.updateTask(task);
        } catch (TasksDAOException e) {
            System.err.println("DAO error (updateTask): " + e.getMessage());
        }
        reloadSafe();
    }

    /**
     * Deletes a task by id and refreshes the cache.
     */
    public void deleteTask(int id) {
        try {
            dao.deleteTask(id);
        } catch (TasksDAOException e) {
            System.err.println("DAO error (deleteTask): " + e.getMessage());
        }
        reloadSafe();
    }

    /**
     * Marks a task state by id and refreshes the cache.
     * Swallows ValidationException from the domain (illegal transition, etc.).
     */
    public void markTaskState(int id, TaskState newState) {
        Objects.requireNonNull(newState, "newState is required");
        Optional<ITask> opt = findTaskById(id);
        if (opt.isEmpty()) return;

        ITask t = opt.get();
        try {
            Task updated = new Task(t.getId(), t.getTitle(), t.getDescription(), newState);
            dao.updateTask(updated);
        } catch (ValidationException ve) {
            System.err.println("Validation error (markTaskState): " + ve.getMessage());
            return; // do not reload; nothing changed
        } catch (TasksDAOException de) {
            System.err.println("DAO error (updateTask after mark): " + de.getMessage());
        }
        reloadSafe();
    }

    // -------------------- Filtering --------------------

    /**
     * Applies filters and updates the filtered snapshot.
     * @param titleLike substring or empty; case-insensitive
     * @param state desired state or null to ignore
     */
    public void applyFilters(String titleLike, TaskState state) {
        this.titleContains = titleLike == null ? "" : titleLike;
        this.stateEquals = state;
        reapplyFilters();
    }

    private void reapplyFilters() {
        String needle = titleContains == null ? "" : titleContains.trim().toLowerCase();
        List<ITask> out = new ArrayList<>();
        for (ITask t : all) {
            boolean ok = true;
            if (!needle.isEmpty()) {
                ok &= t.getTitle() != null && t.getTitle().toLowerCase().contains(needle);
            }
            if (stateEquals != null) {
                ok &= t.getState() == stateEquals;
            }
            if (ok) out.add(t);
        }
        this.filtered = List.copyOf(out);
    }

    // -------------------- Reporting --------------------

    /**
     * Generates a simple plain-text report with task counts by state.
     * (Domain may also provide Visitor-based reports; this keeps UI independent.)
     */
    public String generatePlainTextReport() {
        int todo = 0, inProg = 0, done = 0;
        for (ITask t : all) {
            if (t.getState() == TaskState.ToDo) todo++;
            else if (t.getState() == TaskState.InProgress) inProg++;
            else if (t.getState() == TaskState.Completed) done++;
        }
        int total = all.size();
        return "Tasks Report\n"
                + "------------\n"
                + "Total     : " + total + "\n"
                + "ToDo      : " + todo + "\n"
                + "InProgress: " + inProg + "\n"
                + "Completed : " + done + "\n";
    }
}
