package taskmanagement.ui.views.panels;

import taskmanagement.domain.ITask;
import taskmanagement.domain.TaskState;
import taskmanagement.ui.UITheme;

import javax.swing.*;
import java.awt.*;
import java.util.Objects;

/**
 * Modal dialog to add or edit a task.
 * If the user presses OK, returns {@link EditorResult}; otherwise returns {@code null}.
 */
public final class TaskEditorDialog extends JDialog {

    /** Immutable value object carrying the user's edits. */
    public static final class EditorResult {
        private final String title;
        private final String description;
        private final TaskState state;
        public EditorResult(String title, String description, TaskState state) {
            this.title = Objects.requireNonNull(title);
            this.description = Objects.requireNonNull(description);
            this.state = Objects.requireNonNull(state);
        }
        public String title() { return title; }
        public String description() { return description; }
        public TaskState state() { return state; }
    }

    private final JTextField titleField = new JTextField(28);
    private final JTextArea descArea = new JTextArea(8, 28);
    private final JComboBox<TaskState> stateCombo =
            new JComboBox<>(new DefaultComboBoxModel<>(TaskState.values()));
    private EditorResult result;

    public TaskEditorDialog(Window owner, String title, ITask existingOrNull) {
        super(owner, title, ModalityType.APPLICATION_MODAL);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());
        setBackground(UITheme.BASE_800);

        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        form.setBackground(UITheme.BASE_800);

        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(6, 6, 6, 6);
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridx = 0; c.gridy = 0; form.add(new JLabel("Title:"), c);
        c.gridx = 1; form.add(titleField, c);
        c.gridx = 0; c.gridy = 1; form.add(new JLabel("Description:"), c);
        c.gridx = 1; c.fill = GridBagConstraints.BOTH; form.add(new JScrollPane(descArea), c);
        c.gridx = 0; c.gridy = 2; c.fill = GridBagConstraints.HORIZONTAL; form.add(new JLabel("State:"), c);
        c.gridx = 1; form.add(stateCombo, c);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton ok = new JButton("OK");
        UITheme.styleFilterButton(ok);
        JButton cancel = new JButton("Cancel");
        ok.addActionListener(e -> onOK());
        cancel.addActionListener(e -> onCancel());
        buttons.add(ok); buttons.add(cancel);

        add(form, BorderLayout.CENTER);
        add(buttons, BorderLayout.SOUTH);

        if (existingOrNull != null) {
            titleField.setText(existingOrNull.getTitle());
            descArea.setText(existingOrNull.getDescription());
            stateCombo.setSelectedItem(existingOrNull.getState());
        } else {
            stateCombo.setSelectedItem(TaskState.ToDo);
        }

        pack();
        setLocationRelativeTo(owner);
    }

    /** Shows dialog (modal) and returns the user's result or {@code null}. */
    public EditorResult showDialog() {
        setVisible(true);
        return result;
    }

    private void onOK() {
        String title = titleField.getText().trim();
        String desc = descArea.getText().trim();
        TaskState st = (TaskState) stateCombo.getSelectedItem();
        if (title.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Title is required.", "Validation",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }
        result = new EditorResult(title, desc, st);
        dispose();
    }

    private void onCancel() {
        result = null;
        dispose();
    }
}
