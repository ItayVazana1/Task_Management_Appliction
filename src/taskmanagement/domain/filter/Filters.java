package taskmanagement.domain.filter;

import taskmanagement.domain.ITask;
import taskmanagement.domain.TaskState;

/**
 * Factory methods for common task filters (Combinator).
 */
public final class Filters {
    private Filters() { }

    /** Matches any task. */
    public static ITaskFilter any() {
        return t -> true;
    }

    /** Matches no task. */
    public static ITaskFilter none() {
        return t -> false;
    }

    /** Filter by exact state. */
    public static ITaskFilter byState(TaskState state) {
        return t -> t != null && t.getState() == state;
    }

    /** Filter by title containing token (case-insensitive by default). */
    public static ITaskFilter titleContains(String token) {
        return titleContains(token, true);
    }

    /** Filter by title containing token with control over case-sensitivity. */
    public static ITaskFilter titleContains(String token, boolean ignoreCase) {
        final String needle = token == null ? "" : token.trim();
        return t -> contains(getOrEmpty(t.getTitle()), needle, ignoreCase);
    }

    /** Filter by description containing token (case-insensitive by default). */
    public static ITaskFilter descriptionContains(String token) {
        return descriptionContains(token, true);
    }

    /** Filter by description containing token with control over case-sensitivity. */
    public static ITaskFilter descriptionContains(String token, boolean ignoreCase) {
        final String needle = token == null ? "" : token.trim();
        return t -> contains(getOrEmpty(t.getDescription()), needle, ignoreCase);
    }

    /** Filter by exact id. */
    public static ITaskFilter idEquals(int id) {
        return t -> t != null && t.getId() == id;
    }

    // ---- helpers ----

    static boolean contains(String haystack, String needle, boolean ignoreCase) {
        if (ignoreCase) {
            return haystack.toLowerCase().contains(needle.toLowerCase());
        }
        return haystack.contains(needle);
    }

    static String getOrEmpty(String s) {
        return (s == null) ? "" : s;
    }
}
