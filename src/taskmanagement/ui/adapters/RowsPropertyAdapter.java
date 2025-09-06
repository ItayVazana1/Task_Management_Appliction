package taskmanagement.ui.adapters;

import taskmanagement.application.viewmodel.TasksViewModel.RowDTO;
import taskmanagement.application.viewmodel.events.Property;
import taskmanagement.domain.ITask;

import java.util.*;

/**
 * RowsPropertyAdapter
 * <p>Bridges a {@code Property&lt;List&lt;RowDTO&gt;&gt;} exposed by the ViewModel to a
 * {@code Property&lt;List&lt;ITask&gt;&gt;} consumable by UI widgets. It guarantees that:
 * <ul>
 *   <li>Each VM update results in a NEW {@code List&lt;ITask&gt;} instance, ensuring that
 *       UI listeners are notified and repaint (including Edit scenarios).</li>
 *   <li>Per-id proxies are cached so selection/focus can be preserved across refreshes.
 *       Existing proxies are updated in-place via {@link UiTaskProxy#updateFrom(RowDTO)}.</li>
 *   <li>Eviction removes proxies for rows that disappeared.</li>
 * </ul>
 */
public final class RowsPropertyAdapter {

    private final Property<List<RowDTO>> vmRows;
    private final Property<List<ITask>> uiRows;

    /** Cache proxies by task id to keep selection stable after refresh. */
    private final Map<Integer, UiTaskProxy> cache = new HashMap<>();

    /**
     * Creates the adapter and starts listening to VM changes immediately.
     * @param vmRows the ViewModel property with row snapshots (non-null)
     * @throws NullPointerException if {@code vmRows} is null
     */
    public RowsPropertyAdapter(final Property<List<RowDTO>> vmRows) {
        this.vmRows = Objects.requireNonNull(vmRows, "vmRows");
        this.uiRows = new Property<>(List.of());

        // Initial snapshot.
        rebuild(this.vmRows.getValue());

        // Subscribe to VM changes and rebuild the UI list each time.
        this.vmRows.addListener((oldValue, newValue) -> rebuild(newValue));
    }

    /**
     * Returns the UI-facing property to bind against in views.
     * Typical usage: {@code api.tasksProperty().addListener(...)}.
     * @return an observable property of immutable {@code List&lt;ITask&gt;}
     */
    public Property<List<ITask>> asProperty() {
        return uiRows;
    }

    /**
     * Gets the current adapted UI list (never null).
     * @return current UI list
     */
    public List<ITask> getCurrentUiRows() {
        final List<ITask> v = uiRows.getValue();
        return v != null ? v : List.of();
    }

    /**
     * Rebuilds the UI list from the latest VM rows. Always sets a NEW list instance
     * on {@link #uiRows} to ensure listeners fire even when the logical size doesn't change
     * (e.g., Edit updates).
     * @param rows latest rows from the ViewModel (may be null or empty)
     */
    private void rebuild(final List<RowDTO> rows) {
        if (rows == null || rows.isEmpty()) {
            cache.clear();
            uiRows.setValue(List.of());
            return;
        }

        final List<ITask> fresh = new ArrayList<>(rows.size());
        final Set<Integer> present = new HashSet<>(rows.size());

        for (RowDTO r : rows) {
            final int id = r.id();
            present.add(id);

            UiTaskProxy p = cache.get(id);
            if (p == null) {
                p = new UiTaskProxy(r);
                cache.put(id, p);
            } else {
                p.updateFrom(r); // Critical for EDIT to show immediately
            }
            fresh.add(p);
        }

        // Evict proxies that are no longer present.
        cache.keySet().removeIf(id -> !present.contains(id));

        // Publish an unmodifiable NEW list instance to trigger UI listeners.
        uiRows.setValue(List.copyOf(fresh));
    }
}
