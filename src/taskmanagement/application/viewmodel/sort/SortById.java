package taskmanagement.application.viewmodel.sort;

import taskmanagement.domain.ITask;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

/**
 * Default sorting by ID (acts like the "empty/default" option).
 * Pure: returns a new sorted snapshot without mutating input.
 */
public final class SortById implements SortStrategy {

    @Override
    public String displayName() {
        return "ID (Default)";
    }

    @Override
    public List<ITask> sort(List<ITask> items) {
        Objects.requireNonNull(items, "items");
        List<ITask> copy = new ArrayList<>(items);
        copy.sort(Comparator.comparingInt(ITask::getId));
        return copy;
    }

    @Override
    public String toString() {
        // למקרה שמציגים ישירות את האובייקט ברנדרר דיפולטי
        return displayName();
    }
}
