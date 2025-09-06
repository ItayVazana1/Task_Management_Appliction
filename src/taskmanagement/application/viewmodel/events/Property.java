package taskmanagement.application.viewmodel.events;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Minimal observable property (Observer pattern).
 * <p>UI can subscribe via {@link #addListener(Listener)} and will be notified
 * on any {@link #setValue(Object)} or {@link #fireChange()}.</p>
 *
 * <p><b>Important:</b> {@link #setValue(Object)} <i>always</i> notifies listeners,
 * even if {@link Objects#equals(Object, Object)} returns true. This supports
 * in-place updates (e.g., row proxies updated internally) where old/new lists
 * may be equal by value but still require a UI refresh.</p>
 *
 * @param <T> value type (nullable)
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
    // CopyOnWriteArrayList: safe iteration during concurrent adds/removes and re-entrant notifications.
    private final List<Listener<T>> listeners = new CopyOnWriteArrayList<>();

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
     * Sets a new value and <b>always</b> notifies listeners.
     * Use {@link #setValueIfChanged(Object)} if you prefer conditional notification.
     *
     * @param newValue new value (nullable)
     */
    public void setValue(T newValue) {
        T old = this.value;
        this.value = newValue;
        fireChanged(old, newValue);
    }

    /**
     * Sets a new value and notifies listeners only if {@code !Objects.equals(old, newValue)}.
     *
     * @param newValue new value (nullable)
     */
    public void setValueIfChanged(T newValue) {
        T old = this.value;
        if (!Objects.equals(old, newValue)) {
            this.value = newValue;
            fireChanged(old, newValue);
        }
    }

    /**
     * Notifies listeners without changing the current value.
     * Useful after in-place mutations of the value.
     */
    public void fireChange() {
        fireChanged(this.value, this.value);
    }

    /** Adds a listener; no-op if already added. */
    public void addListener(Listener<T> l) {
        listeners.add(Objects.requireNonNull(l, "listener must not be null"));
    }

    /** Removes a listener; no-op if not present. */
    public void removeListener(Listener<T> l) {
        listeners.remove(l);
    }

    private void fireChanged(T oldValue, T newValue) {
        for (Listener<T> l : listeners) {
            try {
                l.onChanged(oldValue, newValue);
            } catch (RuntimeException ignored) {
                // Listener exceptions must not break the chain.
            }
        }
    }
}
