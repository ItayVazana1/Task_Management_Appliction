package taskmanagement.ui.views.panels;

import taskmanagement.domain.TaskState;
import taskmanagement.ui.UITheme;

import javax.swing.*;
import java.awt.*;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * FiltersPanel
 * ------------
 * Right-side panel for filtering (title contains + state) and sorting.
 * Emits callbacks to the owning frame; does not reference the ViewModel.
 *
 * Design:
 *  - Fixed control sizes for layout stability (fields/combos: 220x36; buttons: 140x40)
 *  - ComboBoxes use prototype display values so width won't depend on current value
 *  - Styled consistently via UITheme helpers
 */
public final class FiltersPanel {

    private static final Dimension INPUT_SIZE = new Dimension(220, 36);
    private static final Dimension BTN_SIZE   = new Dimension(140, 40);

    private final JPanel root = new JPanel(new GridBagLayout());
    private final JTextField titleFilter = new JTextField();

    private final JComboBox<TaskState> stateFilter =
            new JComboBox<>(new DefaultComboBoxModel<>(TaskState.values()));

    private final JComboBox<String> sortBy =
            new JComboBox<>(new String[]{"id", "title", "state"});

    private final BiConsumer<String, TaskState> onApply;
    private final Consumer<String> onSort;

    /**
     * @param onApply      callback for applying filters (title, stateOrNull)
     * @param onSortChanged callback for sort key changes ("id" | "title" | "state")
     */
    public FiltersPanel(BiConsumer<String, TaskState> onApply, Consumer<String> onSortChanged) {
        this.onApply = Objects.requireNonNull(onApply, "onApply");
        this.onSort = Objects.requireNonNull(onSortChanged, "onSortChanged");
        build();
    }

    /** @return the panel component to add into layouts */
    public JComponent getComponent() {
        return root;
    }

    // ===== Build UI =====

    private void build() {
        UITheme.applyGlobalDefaults();

        root.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        root.setBackground(UITheme.BG_CARD);

        // Inputs styling & fixed sizes
        UITheme.styleInput(titleFilter);
        titleFilter.setPreferredSize(INPUT_SIZE);
        titleFilter.setMinimumSize(INPUT_SIZE);
        titleFilter.setToolTipText("Filter tasks by title (contains)");

        UITheme.styleInput(stateFilter);
        stateFilter.setPreferredSize(INPUT_SIZE);
        stateFilter.setMinimumSize(INPUT_SIZE);
        stateFilter.setMaximumRowCount(6); // nicer dropdown height

        UITheme.styleInput(sortBy);
        sortBy.setPreferredSize(INPUT_SIZE);
        sortBy.setMinimumSize(INPUT_SIZE);
        sortBy.setToolTipText("Sort the list");

        // Allow "All" state (null) with readable renderer
        stateFilter.insertItemAt(null, 0);
        stateFilter.setSelectedIndex(0);
        stateFilter.setRenderer(new DefaultListCellRenderer() {
            @Override public Component getListCellRendererComponent(
                    JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                String text = (value == null) ? "All" : ((TaskState) value).name();
                return super.getListCellRendererComponent(list, text, index, isSelected, cellHasFocus);
            }
        });

        // Prototype display values → stable width regardless of current selection
        // Longest TaskState string for our enum set:
        stateFilter.setPrototypeDisplayValue(TaskState.InProgress);
        // Longest sort key among our options:
        sortBy.setPrototypeDisplayValue("state");

        // Fire sort callback whenever selection changes (no separate button)
        sortBy.setSelectedItem("id");
        sortBy.addActionListener(e -> onSort.accept((String) sortBy.getSelectedItem()));

        // Buttons
        JButton apply = UITheme.makeFilledButton("Apply");
        apply.setPreferredSize(BTN_SIZE);
        apply.setMinimumSize(BTN_SIZE);
        apply.setToolTipText("Apply filters");
        apply.addActionListener(e -> {
            String text = titleFilter.getText().trim();
            TaskState st = (TaskState) stateFilter.getSelectedItem(); // may be null ("All")
            onApply.accept(text, st);
        });

        JButton reset = UITheme.makeFilledButton("Reset");
        reset.setPreferredSize(BTN_SIZE);
        reset.setMinimumSize(BTN_SIZE);
        reset.setToolTipText("Reset filters");
        reset.addActionListener(e -> {
            titleFilter.setText("");
            stateFilter.setSelectedIndex(0);
            sortBy.setSelectedItem("id");
            onApply.accept("", null);
            onSort.accept("id");
        });

        // Enter in title field → Apply
        titleFilter.addActionListener(e -> apply.doClick());

        // Layout (GridBag)
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(8, 8, 8, 8);
        c.gridy = 0;
        c.gridx = 0;
        c.anchor = GridBagConstraints.LINE_START;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1.0;

        // Title
        root.add(UITheme.makeSectionLabel("Title contains"), c);
        c.gridy++;
        root.add(titleFilter, c);

        // State
        c.gridy++;
        root.add(UITheme.makeSectionLabel("State"), c);
        c.gridy++;
        root.add(stateFilter, c);

        // Sort
        c.gridy++;
        root.add(UITheme.makeSectionLabel("Sort by"), c);
        c.gridy++;
        root.add(sortBy, c);

        // Buttons row
        JPanel buttons = new JPanel(new GridLayout(1, 2, 12, 0));
        buttons.setOpaque(false);
        buttons.add(apply);
        buttons.add(reset);

        c.gridy++;
        root.add(buttons, c);
    }
}
