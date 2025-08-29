package taskmanagement.domain.filter;

import taskmanagement.domain.TaskState;

/**
 * Static factories for common Task filters.
 */
public final class Filters {
    private Filters() {}

    public static ITaskFilter byState(TaskState state) {
        return t -> t.getState() == state;
    }

    public static ITaskFilter byTitleContainsIgnoreCase(String needle) {
        final String n = needle == null ? "" : needle.trim().toLowerCase();
        return t -> t.getTitle() != null && t.getTitle().toLowerCase().contains(n);
    }

    public static ITaskFilter byDescriptionContainsIgnoreCase(String needle) {
        final String n = needle == null ? "" : needle.trim().toLowerCase();
        return t -> t.getDescription() != null && t.getDescription().toLowerCase().contains(n);
    }
}
