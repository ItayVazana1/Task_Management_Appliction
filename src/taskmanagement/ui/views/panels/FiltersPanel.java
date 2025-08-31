package taskmanagement.ui.views.panels;

import taskmanagement.domain.TaskState;
import taskmanagement.ui.UITheme;

import javax.swing.*;
import java.awt.*;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * Top bar for filtering (title contains + state) and sorting.
 * Emits callbacks to the owning frame; does not know the ViewModel.
 */
public final class FiltersPanel {

    private final JPanel root = new JPanel(new GridBagLayout());
    private final JTextField titleFilter = new JTextField(24);
    private final JComboBox<TaskState> stateFilter =
            new JComboBox<>(new DefaultComboBoxModel<>(TaskState.values()));
    private final JComboBox<String> sortBy =
            new JComboBox<>(new String[]{"id", "title", "state"});

    private final BiConsumer<String, TaskState> onApply;
    private final Consumer<String> onSort;

    public FiltersPanel(BiConsumer<String, TaskState> onApply, Consumer<String> onSortChanged) {
        this.onApply = Objects.requireNonNull(onApply, "onApply");
        this.onSort = Objects.requireNonNull(onSortChanged, "onSortChanged");
        build();
    }

    public JComponent getComponent() {
        return root;
    }

    private void build() {
        root.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        root.setBackground(UITheme.BASE_800);

        int col = 0;
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(4, 4, 4, 4);
        c.gridy = 0;

        // Title filter
        c.gridx = col++;
        root.add(new JLabel("Title contains:"), c);
        c.gridx = col++;
        titleFilter.setToolTipText("Type text and click Apply");
        root.add(titleFilter, c);

        // State filter
        c.gridx = col++;
        root.add(new JLabel("State:"), c);
        c.gridx = col++;
        stateFilter.insertItemAt(null, 0);
        stateFilter.setSelectedIndex(0);
        root.add(stateFilter, c);

        // Sort by
        c.gridx = col++;
        root.add(new JLabel("Sort by:"), c);
        c.gridx = col++;
        sortBy.setSelectedItem("id");
        root.add(sortBy, c);

        // Buttons
        JButton apply = new JButton("Apply");
        UITheme.styleFilterButton(apply);
        apply.addActionListener(e -> {
            String text = titleFilter.getText().trim();
            TaskState st = (TaskState) stateFilter.getSelectedItem(); // can be null
            onApply.accept(text, st);
        });

        JButton clear = new JButton("Clear");
        clear.addActionListener(e -> {
            titleFilter.setText("");
            stateFilter.setSelectedIndex(0);
            onApply.accept("", null);
        });

        JButton setSort = new JButton("Set Sort");
        setSort.addActionListener(e -> onSort.accept((String) sortBy.getSelectedItem()));

        c.gridx = col++;
        root.add(apply, c);
        c.gridx = col++;
        root.add(clear, c);
        c.gridx = col++;
        root.add(setSort, c);
    }
}
