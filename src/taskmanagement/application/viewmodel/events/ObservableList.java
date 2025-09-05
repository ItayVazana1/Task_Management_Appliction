package taskmanagement.application.viewmodel.events;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Very small observable list wrapper for UI binding.
 * <p>
 * Unlike a full list implementation, this wrapper keeps an internal list and
 * exposes set/clear/replace methods that fire a single "list changed" event.
 * Use this for simple table refreshes.
 *
 * @param <T> element type
 */
public final class ObservableList<T> {

    /** Listener notified when the list reference or contents change as a whole. */
    @FunctionalInterface
    public interface Listener<T> {
        /**
         * Called when the list is replaced/cleared/updated.
         *
         * @param newSnapshot immutable snapshot after the change
         */
        void onListChanged(List<T> newSnapshot);
    }

    private List<T> data = List.of();
    private final List<Listener<T>> listeners = new ArrayList<>();

    /** Returns an immutable snapshot of current elements. */
    public List<T> get() {
        return data;
    }

    /** Replaces the entire content and notifies listeners. */
    public void set(List<T> items) {
        List<T> newSnap = (items == null) ? List.of() : List.copyOf(items);
        if (!Objects.equals(this.data, newSnap)) {
            this.data = newSnap;
            fireChanged();
        }
    }

    /** Clears the content and notifies listeners if non-empty. */
    public void clear() {
        if (!data.isEmpty()) {
            this.data = List.of();
            fireChanged();
        }
    }

    /** Adds a listener; no-op if already added. */
    public void addListener(Listener<T> l) {
        Objects.requireNonNull(l, "listener must not be null");
        if (!listeners.contains(l)) {
            listeners.add(l);
        }
    }

    /** Removes a listener; no-op if not present. */
    public void removeListener(Listener<T> l) {
        listeners.remove(l);
    }

    /** Current listeners (unmodifiable snapshot). */
    public List<Listener<T>> getListeners() {
        return Collections.unmodifiableList(listeners);
    }

    private void fireChanged() {
        List<Listener<T>> copy = List.copyOf(listeners);
        List<T> snap = data; // already immutable
        for (Listener<T> l : copy) {
            try {
                l.onListChanged(snap);
            } catch (RuntimeException ex) {
                // swallow to keep others notified; consider logging if policy exists
            }
        }
    }
}
