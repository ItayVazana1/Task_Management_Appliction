package taskmanagement.domain.filter;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;

import taskmanagement.domain.ITask;
import taskmanagement.domain.TaskState;

/**
 * A factory/utility for building composable TaskFilter instances.
 * <p>Implements the Combinator pattern (AND/OR/NOT) and common predicates.</p>
 */
public final class Filters {

    private Filters() { /* utility class */ }

    /**
     * A filter that always matches.
     */
    public static ITaskFilter any() {
        return t -> true;
    }

    /**
     * A filter that never matches.
     */
    public static ITaskFilter none() {
        return t -> false;
    }

    /**
     * Builds a filter from a java.util.function.Predicate over ITask.
     * @param predicate predicate (non-null)
     * @return TaskFilter wrapping the predicate
     */
    public static ITaskFilter of(Predicate<ITask> predicate) {
        Objects.requireNonNull(predicate, "predicate");
        return predicate::test;
    }

    /**
     * Matches tasks by their state.
     * @param state non-null state to match
     * @return a filter for the given state
     */
    public static ITaskFilter byState(TaskState state) {
        Objects.requireNonNull(state, "state");
        return t -> t != null && state.equals(t.getState());
    }

    /**
     * Matches tasks whose title contains the given token.
     * @param token non-empty token
     * @param ignoreCase true to ignore case
     * @return a filter for title containment
     */
    public static ITaskFilter titleContains(String token, boolean ignoreCase) {
        final String normalized = normalizeToken(token);
        return t -> contains(getOrEmpty(t.getTitle()), normalized, ignoreCase);
    }

    /**
     * Matches tasks whose description contains the given token.
     * @param token non-empty token
     * @param ignoreCase true to ignore case
     * @return a filter for description containment
     */
    public static ITaskFilter descriptionContains(String token, boolean ignoreCase) {
        final String normalized = normalizeToken(token);
        return t -> contains(getOrEmpty(t.getDescription()), normalized, ignoreCase);
    }

    /**
     * Logical AND over multiple filters. Returns {@link #any()} if array is empty.
     * @param filters non-null array (elements non-null)
     * @return combined AND filter
     */
    public static ITaskFilter and(ITaskFilter... filters) {
        if (filters == null || filters.length == 0) {
            return any();
        }
        return t -> {
            for (ITaskFilter f : filters) {
                if (f == null || !f.test(t)) return false;
            }
            return true;
        };
    }

    /**
     * Logical OR over multiple filters. Returns {@link #none()} if array is empty.
     * @param filters non-null array (elements non-null)
     * @return combined OR filter
     */
    public static ITaskFilter or(ITaskFilter... filters) {
        if (filters == null || filters.length == 0) {
            return none();
        }
        return t -> {
            for (ITaskFilter f : filters) {
                if (f != null && f.test(t)) return true;
            }
            return false;
        };
    }

    /**
     * Negates a filter.
     * @param filter non-null filter
     * @return negated filter
     */
    public static ITaskFilter not(ITaskFilter filter) {
        Objects.requireNonNull(filter, "filter");
        return filter.negate();
    }

    /**
     * Applies the filter to the given array and returns a new array with matches.
     * (DAO returns arrays לפי הדרישה, לכן כאן מחזירים מערך.)
     * @param tasks array of tasks (non-null)
     * @param filter filter to apply (non-null)
     * @return new array containing only matching tasks
     */
    public static ITask[] apply(ITask[] tasks, ITaskFilter filter) {
        Objects.requireNonNull(tasks, "tasks");
        Objects.requireNonNull(filter, "filter");
        List<ITask> out = new ArrayList<>();
        for (ITask t : tasks) {
            if (t != null && filter.test(t)) {
                out.add(t);
            }
        }
        return out.toArray(ITask[]::new);
        // Java 24: method reference array constructor supported
    }

    // ---------- helpers ----------

    private static String getOrEmpty(String s) {
        return s == null ? "" : s;
    }

    private static boolean contains(String text, String token, boolean ignoreCase) {
        if (ignoreCase) {
            return text.toLowerCase().contains(token.toLowerCase());
        }
        return text.contains(token);
    }

    private static String normalizeToken(String token) {
        if (token == null || token.trim().isEmpty()) {
            throw new IllegalArgumentException("token must be non-empty");
        }
        return token.trim();
    }
}
