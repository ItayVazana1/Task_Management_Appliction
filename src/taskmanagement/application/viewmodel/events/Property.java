package taskmanagement.application.viewmodel.events;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Observable property implementing the Observer pattern.
 * <p>
 * Listeners can subscribe via {@link #addListener(Listener)} and will be notified
 * whenever the property's value is set or explicitly refreshed.
 * </p>
 * <p>
 * Important: {@link #setValue(Object)} always notifies listeners,
 * even if the old and new values are equal according to {@link Objects#equals(Object, Object)}.
 * This ensures in-place updates (e.g., mutable objects) still trigger a refresh.
 * </p>
 *
 * @param <T> the type of the property's value (nullable)
 */
public final class Property<T> {

    /**
     * Listener interface for observing property changes.
     *
     * @param <T> the type of the observed value
     */
    @FunctionalInterface
    public interface Listener<T> {
        /**
         * Called when the property's value changes.
         *
         * @param oldValue the previous value (nullable)
         * @param newValue the new value (nullable)
         */
        void onChanged(T oldValue, T newValue);
    }

    private volatile T value;
    private final List<Listener<T>> listeners = new CopyOnWriteArrayList<>();

    /**
     * Constructs a new property with the given initial value.
     *
     * @param initial the initial value (nullable)
     */
    public Property(T initial) {
        this.value = initial;
    }

    /**
     * Returns the current value of this property.
     *
     * @return the current value (nullable)
     */
    public T getValue() {
        return value;
    }

    /**
     * Sets a new value and always notifies listeners, regardless of equality.
     *
     * @param newValue the new value (nullable)
     */
    public void setValue(T newValue) {
        T old = this.value;
        this.value = newValue;
        fireChanged(old, newValue);
    }

    /**
     * Sets a new value and notifies listeners only if the value has changed
     * according to {@link Objects#equals(Object, Object)}.
     *
     * @param newValue the new value (nullable)
     */
    public void setValueIfChanged(T newValue) {
        T old = this.value;
        if (!Objects.equals(old, newValue)) {
            this.value = newValue;
            fireChanged(old, newValue);
        }
    }

    /**
     * Notifies listeners of a change without modifying the current value.
     * Useful when the current value is mutable and has been updated in place.
     */
    public void fireChange() {
        fireChanged(this.value, this.value);
    }

    /**
     * Registers a new listener to be notified of property changes.
     * Duplicate additions are ignored.
     *
     * @param l the listener to add (must not be {@code null})
     */
    public void addListener(Listener<T> l) {
        listeners.add(Objects.requireNonNull(l, "listener must not be null"));
    }

    /**
     * Removes a previously registered listener.
     * Does nothing if the listener is not registered.
     *
     * @param l the listener to remove
     */
    public void removeListener(Listener<T> l) {
        listeners.remove(l);
    }

    /**
     * Notifies all registered listeners of the property change.
     *
     * @param oldValue the old value (nullable)
     * @param newValue the new value (nullable)
     */
    private void fireChanged(T oldValue, T newValue) {
        for (Listener<T> l : listeners) {
            try {
                l.onChanged(oldValue, newValue);
            } catch (RuntimeException ignored) {
                // Listener exceptions are suppressed to avoid breaking the chain.
            }
        }
    }
}
