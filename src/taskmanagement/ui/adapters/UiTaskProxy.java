package taskmanagement.ui.adapters;

import taskmanagement.application.viewmodel.TasksViewModel.RowDTO;
import taskmanagement.domain.ITask;
import taskmanagement.domain.TaskState;
import taskmanagement.domain.visitor.TaskVisitor;

import java.util.Objects;

/**
 * UI-side proxy that implements {@link ITask} and mirrors a single
 * {@link RowDTO} published by the {@code TasksViewModel}.
 * <p>
 * Instances are intended to be selection-stable in the UI: reuse the same proxy
 * per task id and call {@link #updateFrom(RowDTO)} when new snapshots arrive.
 * This class contains no DAO or domain logic.
 */
public final class UiTaskProxy implements ITask {

    private int id;
    private String title;
    private String description;
    private TaskState state;

    /**
     * Constructs a proxy from a ViewModel row snapshot.
     *
     * @param row non-null row DTO from the ViewModel
     * @throws NullPointerException if {@code row} is {@code null}
     */
    public UiTaskProxy(final RowDTO row) {
        updateFrom(row);
    }

    /**
     * Updates this proxy with values from a ViewModel row snapshot.
     * All fields are overwritten.
     *
     * @param row non-null row DTO from the ViewModel
     * @throws NullPointerException if {@code row} is {@code null}
     */
    public void updateFrom(final RowDTO row) {
        Objects.requireNonNull(row, "row");
        this.id = row.id();
        this.title = row.title();
        this.description = row.description();

        // Defensive mapping from row.state() to TaskState with a safe fallback.
        TaskState mapped;
        try {
            final String s = String.valueOf(row.state());
            mapped = TaskState.valueOf(s.trim());
        } catch (Exception ex) {
            mapped = TaskState.ToDo;
        }
        this.state = mapped;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getId() {
        return id;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getTitle() {
        return title;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getDescription() {
        return description;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TaskState getState() {
        return state;
    }

    /**
     * No-op for UI proxies; domain visitors are not applied on the UI layer.
     *
     * @param v the visitor instance (ignored)
     */
    @Override
    public void accept(final TaskVisitor v) { /* intentionally no-op */ }

    /**
     * Returns a concise textual representation for debugging.
     *
     * @return a string containing id, title, and state
     */
    @Override
    public String toString() {
        return "UiTaskProxy{id=" + id + ", title='" + title + "', state=" + state + "}";
    }
}
