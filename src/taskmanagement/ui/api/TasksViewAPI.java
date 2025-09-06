package taskmanagement.ui.api;

import taskmanagement.application.viewmodel.TasksViewModel;
import taskmanagement.application.viewmodel.sort.SortStrategy;
import taskmanagement.domain.ITask;
import taskmanagement.domain.TaskState;
import taskmanagement.domain.filter.ITaskFilter;
import taskmanagement.domain.visitor.reports.ByStateCount;
import taskmanagement.application.viewmodel.ExportFormat;
import taskmanagement.application.viewmodel.events.Property;

import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

/**
 * TasksViewAPI
 * ------------
 * UI-facing abstraction over the {@link TasksViewModel}.
 * <p>
 * The Swing layer must depend only on this interface (never on the DAO or model directly).
 * Methods return {@code Void} (or DTOs) to keep the view free from checked exceptions.
 */
public interface TasksViewAPI {

    // ---------------------------------------------------------------------
    // Properties
    // ---------------------------------------------------------------------

    /**
     * @return observable list of all tasks (unfiltered), suitable for UI binding.
     */
    Property<List<ITask>> tasksProperty();

    /**
     * @return observable list of tasks after applying the current filter.
     */
    Property<List<ITask>> filteredTasksProperty();

    /**
     * @return observable flag indicating whether an undo operation is currently possible.
     */
    Property<Boolean> canUndoProperty();

    /**
     * @return observable flag indicating whether a redo operation is currently possible.
     */
    Property<Boolean> canRedoProperty();

    // ---------------------------------------------------------------------
    // Core Operations
    // ---------------------------------------------------------------------

    /**
     * Reloads tasks from the persistence layer into the ViewModel.
     * @return {@code null}
     */
    Void reload();

    /**
     * Deletes all tasks (the UI should handle confirmation).
     * @return {@code null}
     */
    Void deleteAll();

    /**
     * Deletes a set of tasks by their identifiers.
     * @param ids task ids to delete
     * @return {@code null}
     */
    Void deleteTasks(int... ids);

    /**
     * Advances a task to its next legal {@link TaskState}.
     * @param id task id
     * @return {@code null}
     */
    Void advanceState(int id);

    /**
     * Marks a task with an explicit target {@link TaskState}.
     * @param id    task id
     * @param state target state
     * @return {@code null}
     */
    Void markState(int id, TaskState state);

    /**
     * Adds a new task using a UI-level {@link ITask} proxy.
     * Implementations extract title/description/state as needed.
     * @param task UI proxy carrying task data
     * @return {@code null}
     */
    Void addTask(ITask task);

    /**
     * Updates an existing task using a UI-level {@link ITask} proxy.
     * Implementations use {@code task.getId()} as the key.
     * @param task UI proxy carrying updated task data
     * @return {@code null}
     */
    Void updateTask(ITask task);

    /**
     * Performs an undo operation if available.
     * @return {@code null}
     */
    Void undo();

    /**
     * Performs a redo operation if available.
     * @return {@code null}
     */
    Void redo();

    // ---------------------------------------------------------------------
    // Filtering / Sorting
    // ---------------------------------------------------------------------

    /**
     * Applies a composed filter (AND/OR combinator) to the tasks view.
     Implementations should update {@link #filteredTasksProperty()}.
     * @param filter combinator filter to apply
     * @return {@code null}
     */
    Void setFilter(ITaskFilter filter);

    /**
     * Clears the active filter.
     * @return {@code null}
     */
    Void clearFilter();

    /**
     * Sets (or clears with {@code null}) the sorting strategy used for presentation.
     * @param strategy sorting strategy, or {@code null} to clear
     * @return {@code null}
     */
    Void setSortStrategy(SortStrategy strategy);

    // ---------------------------------------------------------------------
    // Reporting
    // ---------------------------------------------------------------------

    /**
     * Computes counts of tasks by state (optionally on the filtered subset).
     * @param useFiltered if {@code true}, compute on filtered tasks; otherwise on all tasks
     * @return a {@link ByStateCount} report DTO
     */
    ByStateCount getCountsByState(boolean useFiltered);

    /**
     * Exports tasks to the given path in the requested format.
     * @param path        target file path
     * @param format      export format (e.g., CSV/TXT)
     * @param useFiltered whether to export the filtered subset
     * @param ids         optional explicit list of ids to export (may be {@code null} or empty)
     * @return {@code null}
     */
    Void exportTasks(Path path, ExportFormat format, boolean useFiltered, List<Integer> ids);

    /**
     * Exports a "count by state" report.
     * @param path        target file path
     * @param format      export format (e.g., CSV/TXT)
     * @param useFiltered whether to export the filtered subset
     * @param ids         optional explicit list of ids to include (may be {@code null} or empty)
     * @return {@code null}
     */
    Void exportByStateReport(Path path, ExportFormat format, boolean useFiltered, List<Integer> ids);

    // ---------------------------------------------------------------------
    // Lookup for dialogs
    // ---------------------------------------------------------------------

    /**
     * Finds a task row by its id for dialog prefill.
     * @param id task id
     * @return optional {@link TasksViewModel.RowDTO} if found
     */
    Optional<TasksViewModel.RowDTO> findRowById(int id);
}
