package taskmanagement.application.viewmodel.events;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * A simple observable property holding a value of type T.
 * View listens to value changes raised by the ViewModel.
 * <p>Threading: callers are responsible to marshal to EDT when updating UI.</p>
 * @param <T> value type
 */
public final class Property<T> {

    /**
     * Listener for property change events.
     * @param <T> value type
     */
    @FunctionalInterface
    public interface ChangeListener<T> {
        /**
         * Invoked when the property's value changes.
         * @param oldValue the previous value (may be null)
         * @param newValue the new value (may be null)
         */
        void onChanged(T oldValue, T newValue);
    }

    private final List<ChangeListener<T>> listeners = new ArrayList<>();
    private T value;

    /**
     * Creates a property with an initial value (may be null).
     * @param initial initial value
     */
    public Property(T initial) {
        this.value = initial;
    }

    /**
     * @return current value (may be null)
     */
    public T get() {
        return value;
    }

    /**
     * Sets a new value and notifies listeners if changed (by equals).
     * @param newValue new value (may be null)
     */
    public void set(T newValue) {
        T old = this.value;
        if (!Objects.equals(old, newValue)) {
            this.value = newValue;
            notifyListeners(old, newValue);
        }
    }

    /**
     * Adds a change listener (no duplicates).
     * @param listener listener to add
     */
    public void addListener(ChangeListener<T> listener) {
        if (listener == null) return;
        if (!listeners.contains(listener)) {
            listeners.add(listener);
        }
    }

    /**
     * Removes a change listener (no-op if not present).
     * @param listener listener to remove
     */
    public void removeListener(ChangeListener<T> listener) {
        listeners.remove(listener);
    }

    private void notifyListeners(T oldValue, T newValue) {
        // Iterate over a snapshot to avoid ConcurrentModification on re-entrancy
        List<ChangeListener<T>> snapshot = List.copyOf(listeners);
        for (ChangeListener<T> l : snapshot) {
            l.onChanged(oldValue, newValue);
        }
    }
}
