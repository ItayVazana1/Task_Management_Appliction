package taskmanagement.application.viewmodel.events;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Minimal observable property (Observer pattern).
 * View code can subscribe to value changes via {@link #addListener(Listener)}
 * and react when {@link #setValue(Object)} updates the value.
 *
 * @param <T> value type (may be nullable)
 */
public final class Property<T> {

    /** Listener of value changes. */
    @FunctionalInterface
    public interface Listener<T> {
        /**
         * Called when the property's value changes.
         *
         * @param oldValue previous value (nullable)
         * @param newValue new value (nullable)
         */
        void onChanged(T oldValue, T newValue);
    }

    private volatile T value;
    private final List<Listener<T>> listeners = new ArrayList<>();

    /**
     * Creates a property with an initial value.
     *
     * @param initial initial value (nullable)
     */
    public Property(T initial) {
        this.value = initial;
    }

    /** Returns the current value (nullable). */
    public T getValue() {
        return value;
    }

    /**
     * Sets a new value and notifies listeners if it actually changed
     * (by {@link Objects#equals(Object, Object)}).
     *
     * @param newValue new value (nullable)
     */
    public void setValue(T newValue) {
        T old = this.value;
        if (!Objects.equals(old, newValue)) {
            this.value = newValue;
            fireChanged(old, newValue);
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

    /** Returns an unmodifiable snapshot of current listeners (debug/testing). */
    public List<Listener<T>> getListeners() {
        return Collections.unmodifiableList(listeners);
    }

    private void fireChanged(T oldValue, T newValue) {
        // Iterate over a copy to avoid ConcurrentModification on re-entrant changes.
        List<Listener<T>> copy = List.copyOf(listeners);
        for (Listener<T> l : copy) {
            try {
                l.onChanged(oldValue, newValue);
            } catch (RuntimeException ignored) {
                // Listener exceptions must not break the chain.
            }
        }
    }
}
