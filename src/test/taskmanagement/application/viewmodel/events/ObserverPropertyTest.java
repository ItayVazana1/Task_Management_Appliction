package taskmanagement.application.viewmodel.events;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.*;

/**
 * JUnit 4 test suite for the ViewModel observer utilities
 * {@code Property<T>} and {@code ObservableList<T>}.
 * <p>
 * Verifies listener notification semantics, conditional updates,
 * multi-listener behavior, listener removal, and robustness in the
 * presence of listener exceptions.
 */
public final class ObserverPropertyTest {

    private Property<String> prop;
    private ObservableList<Integer> olist;

    /**
     * Initializes the property and observable list before each test.
     */
    @Before
    public void setUp() {
        prop = new Property<>("A");
        olist = new ObservableList<>();
    }

    /**
     * Ensures {@code Property#setValue} always notifies listeners,
     * including when the new value equals the current value.
     */
    @Test
    public void property_setValue_alwaysNotifies_evenIfEqual() {
        final AtomicInteger calls = new AtomicInteger(0);
        final List<String> last = new ArrayList<>(2);

        Property.Listener<String> l = (oldV, newV) -> {
            calls.incrementAndGet();
            last.clear();
            last.add(oldV);
            last.add(newV);
        };

        prop.addListener(l);

        prop.setValue("B");
        assertEquals(1, calls.get());
        assertEquals(List.of("A", "B"), last);

        prop.setValue("B");
        assertEquals(2, calls.get());
        assertEquals(List.of("B", "B"), last);
    }

    /**
     * Ensures {@code Property#setValueIfChanged} notifies listeners
     * only when the value actually changes.
     */
    @Test
    public void property_setValueIfChanged_notifiesOnlyWhenDifferent() {
        final AtomicInteger calls = new AtomicInteger(0);
        prop.addListener((oldV, newV) -> calls.incrementAndGet());

        prop.setValueIfChanged("A");
        assertEquals(0, calls.get());

        prop.setValueIfChanged("Z");
        assertEquals(1, calls.get());

        prop.setValueIfChanged("Z");
        assertEquals(1, calls.get());
    }

    /**
     * Ensures {@code Property#fireChange()} notifies listeners using the
     * current value as both old and new.
     */
    @Test
    public void property_fireChange_notifiesWithSameValue() {
        final List<String> pairs = new ArrayList<>();
        prop.addListener((oldV, newV) -> pairs.add(oldV + "→" + newV));

        prop.fireChange();
        assertEquals(1, pairs.size());
        assertEquals("A→A", pairs.get(0));
    }

    /**
     * Verifies that removing a listener prevents further notifications.
     */
    @Test
    public void property_removeListener_noFurtherNotifications() {
        final AtomicInteger calls = new AtomicInteger();
        Property.Listener<String> l = (o, n) -> calls.incrementAndGet();
        prop.addListener(l);

        prop.setValue("X");
        assertEquals(1, calls.get());

        prop.removeListener(l);
        prop.setValue("Y");
        assertEquals(1, calls.get());
    }

    /**
     * Verifies multiple listeners are invoked even if one throws,
     * and that exceptions from one listener do not prevent others.
     */
    @Test
    public void property_multipleListeners_allAreCalled_evenIfOneThrows() {
        final AtomicInteger calls = new AtomicInteger();

        prop.addListener((o, n) -> calls.incrementAndGet());
        prop.addListener((o, n) -> { throw new RuntimeException("boom"); });
        prop.addListener((o, n) -> calls.incrementAndGet());

        prop.setValue("B");
        assertEquals(2, calls.get());
    }

    /**
     * Ensures {@code ObservableList#set} notifies listeners only when the
     * snapshot content changes.
     */
    @Test
    public void olist_set_notifiesOnlyOnRealChange() {
        final AtomicInteger calls = new AtomicInteger(0);
        final List<List<Integer>> snapshots = new ArrayList<>();
        olist.addListener(newSnap -> {
            calls.incrementAndGet();
            snapshots.add(newSnap);
        });

        olist.set(List.of());
        assertEquals(0, calls.get());

        olist.set(List.of(1, 2, 3));
        assertEquals(1, calls.get());
        assertEquals(List.of(1, 2, 3), snapshots.get(0));

        olist.set(List.of(1, 2, 3));
        assertEquals(1, calls.get());

        olist.set(List.of(1, 2, 3, 4));
        assertEquals(2, calls.get());
        assertEquals(List.of(1, 2, 3, 4), snapshots.get(1));
    }

    /**
     * Ensures {@code ObservableList#clear} notifies only if the list
     * was previously non-empty.
     */
    @Test
    public void olist_clear_notifiesOnlyIfWasNonEmpty() {
        final AtomicInteger calls = new AtomicInteger();
        olist.addListener(newSnap -> calls.incrementAndGet());

        olist.clear();
        assertEquals(0, calls.get());

        olist.set(List.of(9));
        assertEquals(1, calls.get());
        olist.clear();
        assertEquals(2, calls.get());
    }

    /**
     * Verifies adding and removing listeners affects notification delivery.
     */
    @Test
    public void olist_addRemoveListeners() {
        final AtomicInteger calls = new AtomicInteger();
        ObservableList.Listener<Integer> l = newSnap -> calls.incrementAndGet();

        olist.addListener(l);
        olist.set(List.of(1));
        assertEquals(1, calls.get());

        olist.removeListener(l);
        olist.set(List.of(2));
        assertEquals(1, calls.get());
    }

    /**
     * Verifies multiple list listeners are invoked even if one throws.
     */
    @Test
    public void olist_multipleListeners_allAreCalled_evenIfOneThrows() {
        final AtomicInteger calls = new AtomicInteger();
        olist.addListener(newSnap -> calls.incrementAndGet());
        olist.addListener(newSnap -> { throw new RuntimeException("boom"); });
        olist.addListener(newSnap -> calls.incrementAndGet());

        olist.set(List.of(1, 2));
        assertEquals(2, calls.get());
    }

    /**
     * Small helper that compares lists by content using {@link Objects#equals(Object, Object)}.
     *
     * @param a first list
     * @param b second list
     * @param <T> element type
     * @return {@code true} if equal by content, otherwise {@code false}
     */
    private static <T> boolean equalLists(List<T> a, List<T> b) {
        return Objects.equals(a, b);
    }
}
