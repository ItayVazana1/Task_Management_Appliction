package taskmanagement.domain.filter;

import taskmanagement.domain.TaskState;

/**
 * Ready-made task filters (Combinator building blocks).
 * All methods are null-safe and trimmed/lower-cased where applicable.
 */
public final class Filters {
    private Filters() {}

    /** Title contains substring (case-insensitive). */
    public static ITaskFilter titleContains(String needle) {
        final String n = safeLower(needle);
        return t -> {
            String title = t.getTitle();
            return title != null && title.toLowerCase().contains(n);
        };
    }

    /** Description contains substring (case-insensitive). */
    public static ITaskFilter descriptionContains(String needle) {
        final String n = safeLower(needle);
        return t -> {
            String desc = t.getDescription();
            return desc != null && desc.toLowerCase().contains(n);
        };
    }

    /** Exact id match. */
    public static ITaskFilter idEquals(int id) {
        return t -> t.getId() == id;
    }

    /** State equals specific value. */
    public static ITaskFilter stateIs(TaskState state) {
        return t -> t.getState() == state;
    }

    /** Alias required by smoke/tests: same as {@link #stateIs(TaskState)}. */
    public static ITaskFilter byState(TaskState state) {
        return stateIs(state);
    }

    /* ---------- helpers ---------- */

    private static String safeLower(String s) {
        return (s == null) ? "" : s.trim().toLowerCase();
    }

    /** Utility to negate a filter (aliases ITaskFilter.not). */
    public static ITaskFilter not(ITaskFilter f) {
        return ITaskFilter.not(f);
    }

    /** Match-all (useful as a neutral element). */
    public static ITaskFilter all() {
        return ITaskFilter.all();
    }
}
