package taskmanagement.domain.filter;

import taskmanagement.domain.TaskState;

/**
 * Utility class providing ready-made task filters as building blocks
 * for Combinator composition.
 * <p>
 * All methods are null-safe. Strings are trimmed and compared
 * in lowercase where applicable.
 * </p>
 */
public final class Filters {

    private Filters() { }

    /**
     * Returns a filter that matches tasks whose title contains the given substring,
     * case-insensitive.
     *
     * @param needle the substring to search for (nullable, treated as empty)
     * @return a task filter matching by title
     */
    public static ITaskFilter titleContains(String needle) {
        final String n = safeLower(needle);
        return t -> {
            String title = t.getTitle();
            return title != null && title.toLowerCase().contains(n);
        };
    }

    /**
     * Returns a filter that matches tasks whose description contains the given substring,
     * case-insensitive.
     *
     * @param needle the substring to search for (nullable, treated as empty)
     * @return a task filter matching by description
     */
    public static ITaskFilter descriptionContains(String needle) {
        final String n = safeLower(needle);
        return t -> {
            String desc = t.getDescription();
            return desc != null && desc.toLowerCase().contains(n);
        };
    }

    /**
     * Returns a filter that matches tasks with the specified id.
     *
     * @param id the task id
     * @return a task filter matching by id
     */
    public static ITaskFilter idEquals(int id) {
        return t -> t.getId() == id;
    }

    /**
     * Returns a filter that matches tasks in the specified state.
     *
     * @param state the target state (nullable)
     * @return a task filter matching by state
     */
    public static ITaskFilter stateIs(TaskState state) {
        return t -> t.getState() == state;
    }

    /**
     * Alias for {@link #stateIs(TaskState)}.
     *
     * @param state the target state (nullable)
     * @return a task filter matching by state
     */
    public static ITaskFilter byState(TaskState state) {
        return stateIs(state);
    }

    /**
     * Returns a filter that negates the given filter.
     *
     * @param f the filter to negate
     * @return negated filter
     */
    public static ITaskFilter not(ITaskFilter f) {
        return ITaskFilter.not(f);
    }

    /**
     * Returns a filter that matches all tasks.
     *
     * @return match-all filter
     */
    public static ITaskFilter all() {
        return ITaskFilter.all();
    }

    private static String safeLower(String s) {
        return (s == null) ? "" : s.trim().toLowerCase();
    }
}
