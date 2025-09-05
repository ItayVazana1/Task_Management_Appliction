package taskmanagement.ui.dialogs;

import taskmanagement.ui.styles.AppTheme;
import taskmanagement.ui.util.RoundedPanel;
import taskmanagement.ui.util.UiUtils;
import taskmanagement.domain.TaskState;

import javax.swing.*;
import java.awt.*;
import java.util.Optional;

/**
 * TaskEditorDialog
 * ----------------
 * Modal dialog for creating or editing a task.
 * The dialog is View-only (MVVM-safe): it collects input and returns it to the caller.
 * The caller is responsible for interacting with the ViewModel (create/update).
 */
public final class TaskEditorDialog extends JDialog {

    /** Mode of the editor (Add or Edit). */
    public enum Mode { ADD, EDIT }

    /** Initial values for edit mode. */
    public static record Prefill(int id, String title, String description, TaskState state) {}

    /** Result of user confirmation. */
    public static record EditorResult(Integer id, String title, String description, TaskState state) {}

    private final Mode mode;
    private final Prefill prefill;
    private final JTextField titleField;
    private final JTextArea descriptionArea;
    private final JComboBox<TaskState> stateCombo;
    private boolean confirmed;

    /**
     * Constructs the task editor dialog.
     *
     * @param owner   parent window
     * @param mode    whether this is ADD or EDIT
     * @param prefill initial values (only used for EDIT)
     */
    private TaskEditorDialog(Window owner, Mode mode, Prefill prefill) {
        super(owner, mode == Mode.ADD ? "Add Task" : "Edit Task", ModalityType.APPLICATION_MODAL);
        this.mode = mode;
        this.prefill = prefill;
        this.titleField = new JTextField(20);
        this.descriptionArea = new JTextArea(5, 20);
        this.stateCombo = new JComboBox<>(TaskState.values());
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setContentPane(buildContent());
        pack();
        setResizable(false);
        setLocationRelativeTo(owner);
        getRootPane().setDefaultButton(okButton);
    }

    private JButton okButton;

    /**
     * Builds the UI content using project helpers.
     */
    private JComponent buildContent() {
        RoundedPanel root = new RoundedPanel(AppTheme.PANEL_BG, AppTheme.WINDOW_CORNER_ARC);
        root.setLayout(new BorderLayout(0, 12));
        root.setBorder(BorderFactory.createEmptyBorder(16, 18, 16, 18));

        // Title label
        JLabel header = new JLabel(mode == Mode.ADD ? "Add New Task" : "Edit Task");
        header.setFont(AppTheme.CTRL_BUTTON_FONT.deriveFont(Font.BOLD, 16f));
        header.setForeground(AppTheme.MAIN_TEXT);

        JPanel form = new JPanel();
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        form.setOpaque(false);

        // Title field
        UiUtils.styleTextFieldForDarkCentered(titleField);
        form.add(new JLabel("Title:"));
        form.add(titleField);
        form.add(Box.createVerticalStrut(8));

        // Description field
        UiUtils.styleTextArea(descriptionArea);
        form.add(new JLabel("Description:"));
        form.add(new JScrollPane(descriptionArea));
        form.add(Box.createVerticalStrut(8));

        // State combo
        form.add(new JLabel("State:"));
        form.add(stateCombo);

        // Prefill if editing
        if (mode == Mode.EDIT && prefill != null) {
            titleField.setText(prefill.title());
            descriptionArea.setText(prefill.description());
            stateCombo.setSelectedItem(prefill.state());
        }

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        actions.setOpaque(false);

        okButton = new JButton(mode == Mode.ADD ? "Add" : "Save");
        UiUtils.styleStableHoverButton(okButton, AppTheme.TB_EXPORT_FG, AppTheme.MAIN_TEXT);

        JButton cancelButton = new JButton("Cancel");
        UiUtils.styleStableHoverButton(cancelButton, AppTheme.DARK_GREY, AppTheme.MAIN_TEXT);

        okButton.addActionListener(e -> {
            if (titleField.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(
                        this,
                        "Title cannot be empty",
                        "Validation Error",
                        JOptionPane.WARNING_MESSAGE
                );
                return;
            }
            confirmed = true;
            dispose();
        });

        cancelButton.addActionListener(e -> {
            confirmed = false;
            dispose();
        });

        actions.add(cancelButton);
        actions.add(okButton);

        root.add(header, BorderLayout.NORTH);
        root.add(form, BorderLayout.CENTER);
        root.add(actions, BorderLayout.SOUTH);
        return root;
    }

    /**
     * Displays the dialog modally and returns the user input if confirmed.
     *
     * @param parent   parent component (for centering)
     * @param mode     dialog mode (ADD or EDIT)
     * @param prefill  optional prefilled values for EDIT mode
     * @return Optional containing EditorResult if user confirmed, otherwise empty
     */
    public static Optional<EditorResult> showDialog(Component parent, Mode mode, Prefill prefill) {
        Window owner = (parent instanceof Window) ? (Window) parent : SwingUtilities.getWindowAncestor(parent);
        TaskEditorDialog dlg = new TaskEditorDialog(owner, mode, prefill);
        dlg.setVisible(true);

        if (!dlg.confirmed) {
            return Optional.empty();
        }
        return Optional.of(new EditorResult(
                dlg.prefill != null ? dlg.prefill.id() : null,
                dlg.titleField.getText().trim(),
                dlg.descriptionArea.getText().trim(),
                (TaskState) dlg.stateCombo.getSelectedItem()
        ));
    }
}
