package taskmanagement.ui.adapters;

import taskmanagement.application.viewmodel.TasksViewModel.RowDTO;
import taskmanagement.application.viewmodel.events.Property;
import taskmanagement.domain.ITask;

import java.util.*;

/**
 * Adapter that converts a {@code Property<List<RowDTO>>} from the ViewModel
 * into a {@code Property<List<ITask>>} suitable for UI widgets.
 * <p>
 * The adapter emits a new immutable list instance on every ViewModel update to
 * ensure UI listeners are notified, and maintains per-id proxy objects to
 * preserve selection and focus across refreshes.
 */
public final class RowsPropertyAdapter {

    private final Property<List<RowDTO>> vmRows;
    private final Property<List<ITask>> uiRows;

    /** Cache proxies by task id to keep selection stable after refresh. */
    private final Map<Integer, UiTaskProxy> cache = new HashMap<>();

    /**
     * Creates the adapter and starts listening to ViewModel changes.
     *
     * @param vmRows the ViewModel property that exposes row snapshots; must not be {@code null}
     * @throws NullPointerException if {@code vmRows} is {@code null}
     */
    public RowsPropertyAdapter(final Property<List<RowDTO>> vmRows) {
        this.vmRows = Objects.requireNonNull(vmRows, "vmRows");
        this.uiRows = new Property<>(List.of());
        rebuild(this.vmRows.getValue());
        this.vmRows.addListener((oldValue, newValue) -> rebuild(newValue));
    }

    /**
     * Returns the UI-facing property for binding in views.
     *
     * @return an observable property of an immutable {@code List<ITask>}
     */
    public Property<List<ITask>> asProperty() {
        return uiRows;
    }

    /**
     * Returns the current adapted UI list.
     *
     * @return the current UI list; never {@code null}
     */
    public List<ITask> getCurrentUiRows() {
        final List<ITask> v = uiRows.getValue();
        return v != null ? v : List.of();
    }

    /**
     * Rebuilds the UI list from the latest ViewModel rows and publishes a new list instance.
     *
     * @param rows latest rows from the ViewModel; may be {@code null} or empty
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
                p.updateFrom(r); // keep existing proxy so selection/focus survive edits
            }
            fresh.add(p);
        }

        cache.keySet().removeIf(id -> !present.contains(id));
        uiRows.setValue(List.copyOf(fresh));
    }
}
