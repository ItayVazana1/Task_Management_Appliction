package taskmanagement.application.viewmodel.events;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Observable list wrapper for UI binding.
 * <p>
 * This class does not implement the full {@link java.util.List} interface.
 * Instead, it manages an internal list and exposes methods for replacing,
 * clearing, and observing changes to the list as a whole.
 * Useful for scenarios such as refreshing a UI table when the list changes.
 * </p>
 *
 * @param <T> the element type of the list
 */
public final class ObservableList<T> {

    /**
     * Listener notified when the list reference or contents are changed.
     *
     * @param <T> element type
     */
    @FunctionalInterface
    public interface Listener<T> {
        /**
         * Invoked when the list has been updated.
         *
         * @param newSnapshot an immutable snapshot of the updated list
         */
        void onListChanged(List<T> newSnapshot);
    }

    private List<T> data = List.of();
    private final List<Listener<T>> listeners = new ArrayList<>();

    /**
     * Returns an immutable snapshot of the current list contents.
     *
     * @return the current immutable list
     */
    public List<T> get() {
        return data;
    }

    /**
     * Replaces the entire list with the provided items and notifies listeners.
     *
     * @param items the new list contents; if {@code null}, the list becomes empty
     */
    public void set(List<T> items) {
        List<T> newSnap = (items == null) ? List.of() : List.copyOf(items);
        if (!Objects.equals(this.data, newSnap)) {
            this.data = newSnap;
            fireChanged();
        }
    }

    /**
     * Clears the list contents and notifies listeners if it was not already empty.
     */
    public void clear() {
        if (!data.isEmpty()) {
            this.data = List.of();
            fireChanged();
        }
    }

    /**
     * Adds a listener to be notified when the list changes.
     * Duplicate additions are ignored.
     *
     * @param l the listener to add (must not be {@code null})
     */
    public void addListener(Listener<T> l) {
        Objects.requireNonNull(l, "listener must not be null");
        if (!listeners.contains(l)) {
            listeners.add(l);
        }
    }

    /**
     * Removes a listener if present.
     *
     * @param l the listener to remove
     */
    public void removeListener(Listener<T> l) {
        listeners.remove(l);
    }

    /**
     * Returns an immutable snapshot of the current listeners.
     *
     * @return an unmodifiable list of listeners
     */
    public List<Listener<T>> getListeners() {
        return Collections.unmodifiableList(listeners);
    }

    /**
     * Notifies all registered listeners of the current list contents.
     * <p>
     * Each listener receives the same immutable snapshot of the data.
     * Runtime exceptions from one listener do not prevent notification of others.
     * </p>
     */
    private void fireChanged() {
        List<Listener<T>> copy = List.copyOf(listeners);
        List<T> snap = data; // immutable snapshot
        for (Listener<T> l : copy) {
            try {
                l.onListChanged(snap);
            } catch (RuntimeException ex) {
                // exception suppressed to allow remaining listeners to be notified
            }
        }
    }
}
