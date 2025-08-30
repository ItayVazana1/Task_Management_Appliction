package taskmanagement.ui.panels;

import taskmanagement.domain.ITask;
import taskmanagement.domain.Task;
import taskmanagement.domain.TaskState;

import javax.swing.*;
import java.awt.*;
import java.util.Optional;

/**
 * Modal dialog for adding or editing a task.
 * Returns an Optional<ITask> to the caller.
 */
public final class TaskEditorDialog extends JDialog {

    private final JTextField title = new JTextField(20);
    private final JTextArea description = new JTextArea(6, 20);
    private final JComboBox<TaskState> state = new JComboBox<>(TaskState.values());
    private Optional<ITask> result = Optional.empty();

    /**
     * Creates a modal editor dialog.
     *
     * @param owner    window owner
     * @param existing existing task to edit, or null for creating a new one
     */
    public TaskEditorDialog(Window owner, ITask existing) {
        super(owner, "Task Editor", ModalityType.APPLICATION_MODAL);
        setLayout(new BorderLayout());

        JPanel form = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(4, 4, 4, 4);
        c.anchor = GridBagConstraints.WEST;

        int row = 0;
        c.gridx = 0; c.gridy = row; form.add(new JLabel("Title:"), c);
        c.gridx = 1; form.add(title, c); row++;
        c.gridx = 0; c.gridy = row; form.add(new JLabel("Description:"), c);
        c.gridx = 1; form.add(new JScrollPane(description), c); row++;
        c.gridx = 0; c.gridy = row; form.add(new JLabel("State:"), c);
        c.gridx = 1; form.add(state, c);

        add(form, BorderLayout.CENTER);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton ok = new JButton("OK");
        JButton cancel = new JButton("Cancel");
        buttons.add(ok);
        buttons.add(cancel);
        add(buttons, BorderLayout.SOUTH);

        if (existing != null) {
            title.setText(existing.getTitle());
            description.setText(existing.getDescription());
            state.setSelectedItem(existing.getState());
        } else {
            state.setSelectedItem(TaskState.ToDo);
        }

        ok.addActionListener(e -> {
            try {
                int id = existing == null ? 0 : existing.getId();
                Task t = new Task(id, title.getText(), description.getText(),
                        (TaskState) state.getSelectedItem());
                result = Optional.of(t);
                dispose();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(),
                        "Validation", JOptionPane.WARNING_MESSAGE);
            }
        });
        cancel.addActionListener(e -> dispose());

        pack();
        setLocationRelativeTo(owner);
    }

    /**
     * Returns the dialog result if the user pressed OK and validation passed.
     *
     * @return Optional of created/updated task
     */
    public Optional<ITask> getResult() {
        return result;
    }
}
