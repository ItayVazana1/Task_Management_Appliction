package taskmanagement.application.viewmodel.events;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.*;

/**
 * ObserverPropertyTest
 * --------------------
 * Unit tests for the simple Observer utilities used by the ViewModel layer:
 *  • Property<T> – value change notifications
 *  • ObservableList<T> – list snapshot change notifications
 *
 * The tests verify:
 *  • Listener is invoked on change (and for Property#setValue even if equal)
 *  • Conditional notifications for Property#setValueIfChanged
 *  • fireChange() notifies without changing value
 *  • Add/remove listeners and multiple listeners
 *  • Robustness against listener exceptions (others still receive events)
 */
public final class ObserverPropertyTest {

    // ===== Fixtures =====
    private Property<String> prop;
    private ObservableList<Integer> olist;

    @Before
    public void setUp() {
        prop = new Property<>("A");
        olist = new ObservableList<>();
    }

    // ===== Property<T> tests =====

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

        // initial -> "B" (different)
        prop.setValue("B");
        assertEquals(1, calls.get());
        assertEquals(List.of("A", "B"), last);

        // "B" -> "B" (equal) SHOULD STILL notify (per class contract)
        prop.setValue("B");
        assertEquals(2, calls.get());
        assertEquals(List.of("B", "B"), last);
    }

    @Test
    public void property_setValueIfChanged_notifiesOnlyWhenDifferent() {
        final AtomicInteger calls = new AtomicInteger(0);
        prop.addListener((oldV, newV) -> calls.incrementAndGet());

        // current = "A" (from setUp)
        prop.setValueIfChanged("A");  // equal -> no notify
        assertEquals(0, calls.get());

        prop.setValueIfChanged("Z");  // different -> notify
        assertEquals(1, calls.get());

        prop.setValueIfChanged("Z");  // equal again -> no notify
        assertEquals(1, calls.get());
    }

    @Test
    public void property_fireChange_notifiesWithSameValue() {
        final List<String> pairs = new ArrayList<>();
        prop.addListener((oldV, newV) -> pairs.add(oldV + "→" + newV));

        prop.fireChange(); // old == new == "A"
        assertEquals(1, pairs.size());
        assertEquals("A→A", pairs.get(0));
    }

    @Test
    public void property_removeListener_noFurtherNotifications() {
        final AtomicInteger calls = new AtomicInteger();
        Property.Listener<String> l = (o, n) -> calls.incrementAndGet();
        prop.addListener(l);

        prop.setValue("X");
        assertEquals(1, calls.get());

        prop.removeListener(l);
        prop.setValue("Y");
        assertEquals(1, calls.get()); // unchanged
    }

    @Test
    public void property_multipleListeners_allAreCalled_evenIfOneThrows() {
        final AtomicInteger calls = new AtomicInteger();

        prop.addListener((o, n) -> calls.incrementAndGet());
        prop.addListener((o, n) -> { throw new RuntimeException("boom"); });
        prop.addListener((o, n) -> calls.incrementAndGet());

        // Should not fail and both non-throwing listeners should be counted
        prop.setValue("B");
        assertEquals(2, calls.get());
    }

    // ===== ObservableList<T> tests =====

    @Test
    public void olist_set_notifiesOnlyOnRealChange() {
        final AtomicInteger calls = new AtomicInteger(0);
        final List<List<Integer>> snapshots = new ArrayList<>();
        olist.addListener(newSnap -> {
            calls.incrementAndGet();
            snapshots.add(newSnap);
        });

        // start empty; set empty -> no change, no notify
        olist.set(List.of());
        assertEquals(0, calls.get());

        // set [1,2,3] -> notify
        olist.set(List.of(1, 2, 3));
        assertEquals(1, calls.get());
        assertEquals(List.of(1, 2, 3), snapshots.get(0));

        // set equal content -> no notify
        olist.set(List.of(1, 2, 3));
        assertEquals(1, calls.get());

        // set different -> notify
        olist.set(List.of(1, 2, 3, 4));
        assertEquals(2, calls.get());
        assertEquals(List.of(1, 2, 3, 4), snapshots.get(1));
    }

    @Test
    public void olist_clear_notifiesOnlyIfWasNonEmpty() {
        final AtomicInteger calls = new AtomicInteger();
        olist.addListener(newSnap -> calls.incrementAndGet());

        // clear empty -> no notify
        olist.clear();
        assertEquals(0, calls.get());

        // set then clear -> one notify for set + one for clear
        olist.set(List.of(9));
        assertEquals(1, calls.get());
        olist.clear();
        assertEquals(2, calls.get());
    }

    @Test
    public void olist_addRemoveListeners() {
        final AtomicInteger calls = new AtomicInteger();
        ObservableList.Listener<Integer> l = newSnap -> calls.incrementAndGet();

        olist.addListener(l);
        olist.set(List.of(1));
        assertEquals(1, calls.get());

        olist.removeListener(l);
        olist.set(List.of(2));
        assertEquals(1, calls.get()); // unchanged
    }

    @Test
    public void olist_multipleListeners_allAreCalled_evenIfOneThrows() {
        final AtomicInteger calls = new AtomicInteger();
        olist.addListener(newSnap -> calls.incrementAndGet());
        olist.addListener(newSnap -> { throw new RuntimeException("boom"); });
        olist.addListener(newSnap -> calls.incrementAndGet());

        olist.set(List.of(1, 2));
        assertEquals(2, calls.get());
    }

    // ===== small sanity helper =====
    private static <T> boolean equalLists(List<T> a, List<T> b) {
        return Objects.equals(a, b);
    }
}
