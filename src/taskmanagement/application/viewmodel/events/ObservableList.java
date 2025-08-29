package taskmanagement.application.viewmodel.events;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * A minimal observable list used by the ViewModel to expose task collections.
 * Not thread-safe; UI must marshal notifications to EDT when needed.
 * @param <T> element type
 */
public final class ObservableList<T> {

    /**
     * Listener for list change events.
     * Provides a minimal diff semantic: ADDED / REMOVED / CLEARED.
     * @param <T> element type
     */
    public interface ListChangeListener<T> {
        enum Kind { ADDED, REMOVED, CLEARED }

        /**
         * Called on list mutation.
         * @param kind the change kind
         * @param elements affected elements (immutable snapshot; empty for CLEARED)
         */
        void onChanged(Kind kind, List<T> elements);
    }

    private final List<T> internal = new ArrayList<>();
    private final List<ListChangeListener<T>> listeners = new ArrayList<>();

    /**
     * @return immutable snapshot of current elements.
     */
    public List<T> snapshot() {
        return Collections.unmodifiableList(new ArrayList<>(internal));
    }

    /**
     * Adds a single element and notifies listeners.
     * @param element element to add (must not be null)
     */
    public void add(T element) {
        Objects.requireNonNull(element, "element");
        internal.add(element);
        notifyListeners(ListChangeListener.Kind.ADDED, List.of(element));
    }

    /**
     * Removes a single element (by equals) and notifies if removed.
     * @param element element to remove (must not be null)
     * @return true if removed
     */
    public boolean remove(T element) {
        Objects.requireNonNull(element, "element");
        boolean removed = internal.remove(element);
        if (removed) {
            notifyListeners(ListChangeListener.Kind.REMOVED, List.of(element));
        }
        return removed;
    }

    /**
     * Clears the list and notifies listeners (CLEARED).
     */
    public void clear() {
        if (!internal.isEmpty()) {
            internal.clear();
            notifyListeners(ListChangeListener.Kind.CLEARED, List.of());
        }
    }

    /**
     * Adds a list change listener (no duplicates).
     * @param listener listener to add
     */
    public void addListener(ListChangeListener<T> listener) {
        if (listener == null) return;
        if (!listeners.contains(listener)) {
            listeners.add(listener);
        }
    }

    /**
     * Removes a list change listener.
     * @param listener listener to remove
     */
    public void removeListener(ListChangeListener<T> listener) {
        listeners.remove(listener);
    }

    private void notifyListeners(ListChangeListener.Kind kind, List<T> elements) {
        List<ListChangeListener<T>> snapshot = List.copyOf(listeners);
        List<T> els = List.copyOf(elements);
        for (ListChangeListener<T> l : snapshot) {
            l.onChanged(kind, els);
        }
    }
}
