package taskmanagement.ui.panels;

import taskmanagement.application.viewmodel.TasksViewModel;
import taskmanagement.domain.TaskState;

import javax.swing.*;
import java.awt.*;
import java.util.function.Consumer;

/**
 * Filters panel (Combinator entry). Sends filter criteria to the ViewModel.
 */
public final class FiltersPanel extends JPanel {

    private final JTextField titleLike = new JTextField(12);
    private final JComboBox<TaskState> stateCombo = new JComboBox<>(TaskState.values());

    /**
     * Constructs a filter panel bound to the given ViewModel.
     *
     * @param viewModel          the Tasks ViewModel
     * @param onAppliedCallback  callback to refresh the list once filters are applied
     */
    public FiltersPanel(TasksViewModel viewModel, Runnable onAppliedCallback) {
        super(new GridBagLayout());
        setBorder(BorderFactory.createTitledBorder("Filters"));

        stateCombo.insertItemAt(null, 0);
        stateCombo.setSelectedIndex(0);

        JButton apply = new JButton("Apply");
        apply.addActionListener(e -> {
            viewModel.applyFilters(titleLike.getText().trim(),
                    (TaskState) stateCombo.getSelectedItem());
            if (onAppliedCallback != null) onAppliedCallback.run();
        });

        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(4, 4, 4, 4);
        c.anchor = GridBagConstraints.WEST;

        int row = 0;
        c.gridx = 0; c.gridy = row; add(new JLabel("Title contains:"), c);
        c.gridx = 1; add(titleLike, c); row++;
        c.gridx = 0; c.gridy = row; add(new JLabel("State:"), c);
        c.gridx = 1; add(stateCombo, c); row++;
        c.gridx = 0; c.gridy = row; c.gridwidth = 2; add(apply, c);
    }
}
