package taskmanagement.ui.adapters;

import taskmanagement.application.viewmodel.TasksViewModel.RowDTO;
import taskmanagement.domain.ITask;
import taskmanagement.domain.TaskState;
import taskmanagement.domain.visitor.TaskVisitor;

import java.util.Objects;

/**
 * UiTaskProxy
 * <p>A lightweight UI-side proxy that implements {@link ITask} and mirrors a single
 * view-model row ({@link RowDTO}). It is selection-stable in the UI: keep the same proxy
 * instance per task id and call {@link #updateFrom(RowDTO)} when the ViewModel publishes
 * new snapshots.</p>
 *
 * <p>This class is UI-only (no DAO/model logic).</p>
 */
public final class UiTaskProxy implements ITask {

    private int id;
    private String title;
    private String description;
    private TaskState state;

    /**
     * Creates a proxy from a VM row snapshot.
     * @param row non-null row DTO coming from the ViewModel
     * @throws NullPointerException if {@code row} is null
     */
    public UiTaskProxy(final RowDTO row) {
        updateFrom(row);
    }

    /**
     * Updates this proxy from a fresh VM row snapshot. All fields are overwritten.
     * Safe to call frequently on refresh.
     * @param row non-null row DTO coming from the ViewModel
     * @throws NullPointerException if {@code row} is null
     */
    public void updateFrom(final RowDTO row) {
        Objects.requireNonNull(row, "row");
        this.id = row.id();
        this.title = row.title();
        this.description = row.description();

        // Defensive mapping from row.state() to TaskState (fallback to ToDo on failure).
        TaskState mapped;
        try {
            final String s = String.valueOf(row.state());
            mapped = TaskState.valueOf(s.trim());
        } catch (Exception ex) {
            mapped = TaskState.ToDo;
        }
        this.state = mapped;
    }

    /** {@inheritDoc} */
    @Override public int getId() { return id; }
    /** {@inheritDoc} */
    @Override public String getTitle() { return title; }
    /** {@inheritDoc} */
    @Override public String getDescription() { return description; }
    /** {@inheritDoc} */
    @Override public TaskState getState() { return state; }

    /**
     * UI proxies do not participate in domain visitors. No-op by design.
     * @param v the visitor
     */
    @Override public void accept(final TaskVisitor v) { /* no-op */ }

    @Override
    public String toString() {
        return "UiTaskProxy{id=" + id + ", title='" + title + "', state=" + state + "}";
    }
}
