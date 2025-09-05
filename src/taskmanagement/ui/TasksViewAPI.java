package taskmanagement.ui;

import taskmanagement.application.viewmodel.events.Property;
import taskmanagement.domain.ITask;
import taskmanagement.domain.TaskState;
import taskmanagement.domain.filter.ITaskFilter;
import taskmanagement.application.viewmodel.ExportFormat;

import java.nio.file.Path;
import java.util.List;

/**
 * View-side API for Tasks MVVM.
 * The Swing View depends ONLY on this interface.
 * No model/DAO types or logic should leak into the View.
 *
 * Responsibilities expected by the UI:
 *  - Expose observable tasks list (Observer).
 *  - CRUD operations (add/update/delete/deleteAll).
 *  - State change (markState).
 *  - Filtering via Combinator (applyFilter / clearFilter).
 *  - Report export via Visitor (exportReport).
 *
 * All methods must be UI-thread safe to invoke (caller is the EDT).
 */
public interface TasksViewAPI {

    // ---------------------------------------------------------------------
    // Observable data the UI binds to
    // ---------------------------------------------------------------------

    /**
     * Observable list of tasks after applying the current filter.
     * The UI binds its JTable model to this property and re-renders when it fires change events.
     */
    Property<List<ITask>> tasksProperty();

    // ---------------------------------------------------------------------
    // CRUD
    // ---------------------------------------------------------------------

    /**
     * Add a new task.
     * @param title       non-null/non-blank
     * @param description nullable/optional (as per model validation)
     * @param state       non-null (ToDo/InProgress/Completed)
     * @throws ViewModelException on validation/persistence error
     */
    void addTask(String title, String description, TaskState state) throws ViewModelException;

    /**
     * Update an existing task by id.
     * @param id          existing task id
     * @param title       non-null/non-blank
     * @param description nullable/optional
     * @param state       non-null
     * @throws ViewModelException on validation/persistence error or if id not found
     */
    void updateTask(int id, String title, String description, TaskState state) throws ViewModelException;

    /**
     * Delete a single task by id.
     * @param id existing task id
     * @throws ViewModelException if id not found or on persistence error
     */
    void deleteTask(int id) throws ViewModelException;

    /**
     * Delete all tasks (used by ControlPanel bulk action).
     * @throws ViewModelException on persistence error
     */
    void deleteAllTasks() throws ViewModelException;

    // ---------------------------------------------------------------------
    // State change
    // ---------------------------------------------------------------------

    /**
     * Mark task lifecycle state (State pattern).
     * @param id       task id
     * @param newState target state
     * @throws ViewModelException on invalid transition or persistence error
     */
    void markState(int id, TaskState newState) throws ViewModelException;

    // ---------------------------------------------------------------------
    // Filtering (Combinator)
    // ---------------------------------------------------------------------

    /**
     * Apply a composable filter (e.g., by title AND state).
     * The implementation should update {@link #tasksProperty()} accordingly and notify observers.
     * @param filter non-null combinator filter
     * @throws ViewModelException when filter application fails
     */
    void applyFilter(ITaskFilter filter) throws ViewModelException;

    /**
     * Clear any active filter, restoring the full tasks list.
     * @throws ViewModelException if refresh fails
     */
    void clearFilter() throws ViewModelException;

    // ---------------------------------------------------------------------
    // Reporting / Export (Visitor + Adapters)
    // ---------------------------------------------------------------------

    /**
     * Export a report using the Visitor-based reporting implemented in the domain.
     * The format is provided by {@link ExportFormat} and the file path is absolute or user-chosen.
     * @param format export format (e.g., CSV, TXT)
     * @param path   target file path
     * @throws ViewModelException on IO or reporting failures
     */
    void exportReport(ExportFormat format, Path path) throws ViewModelException;
}
