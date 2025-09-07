package taskmanagement.ui.dialogs;

import taskmanagement.domain.TaskState;
import taskmanagement.ui.styles.AppTheme;
import taskmanagement.ui.util.RoundedPanel;
import taskmanagement.ui.util.UiUtils;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.Objects;
import java.util.Optional;

/**
 * Modal dialog for creating or editing a task.
 * <p>
 * Presentation-only and MVVM-safe: collects input and returns it to the caller
 * without accessing the model or DAO.
 */
public final class TaskEditorDialog extends JDialog {

    /**
     * Mode of the editor.
     */
    public enum Mode { ADD, EDIT }

    /**
     * Initial values for edit mode.
     *
     * @param id          task identifier
     * @param title       task title
     * @param description task description
     * @param state       task state
     */
    public static record Prefill(int id, String title, String description, TaskState state) { }

    /**
     * Result returned when the user confirms.
     *
     * @param id          task identifier (may be {@code null} in add mode)
     * @param title       task title
     * @param description task description
     * @param state       task state
     */
    public static record EditorResult(Integer id, String title, String description, TaskState state) { }

    private final Mode mode;
    private final Prefill prefill;

    private final JTextField titleField      = new JTextField(28);
    private final JTextArea  descriptionArea = new JTextArea(6, 28);
    private final JComboBox<TaskState> stateCombo = new JComboBox<>(TaskState.values());

    private final JLabel descCounter = new JLabel("0 / 500");
    private boolean confirmed = false;

    /**
     * Constructs the task editor dialog.
     *
     * @param owner   parent window
     * @param mode    editor mode (ADD or EDIT)
     * @param prefill initial values used only for EDIT mode
     */
    private TaskEditorDialog(Window owner, Mode mode, Prefill prefill) {
        super(owner, mode == Mode.ADD ? "Add Task" : "Edit Task", ModalityType.APPLICATION_MODAL);
        this.mode = mode;
        this.prefill = prefill;

        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setResizable(false);
        setContentPane(buildContent());
        pack();
        setLocationRelativeTo(owner);

        getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
                .put(KeyStroke.getKeyStroke("ESCAPE"), "cancel");
        getRootPane().getActionMap().put("cancel", new AbstractAction() {
            @Override public void actionPerformed(ActionEvent e) { onCancel(); }
        });
        getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
                .put(KeyStroke.getKeyStroke("ctrl ENTER"), "confirm");
        getRootPane().getActionMap().put("confirm", new AbstractAction() {
            @Override public void actionPerformed(ActionEvent e) { onConfirm(); }
        });
    }

    private JComponent buildContent() {
        final RoundedPanel root = new RoundedPanel(AppTheme.PANEL_BG, AppTheme.WINDOW_CORNER_ARC);
        root.setLayout(new BorderLayout(0, 12));
        root.setBorder(new EmptyBorder(16, 18, 16, 18));

        final Color accentBg = (mode == Mode.ADD) ? new Color(0x154734) : new Color(0x1F2A44);
        final Color accentFg = new Color(0xEAF2FF);

        final RoundedPanel header = new RoundedPanel(accentBg, AppTheme.WINDOW_CORNER_ARC);
        header.setLayout(new BorderLayout(10, 0));
        header.setBorder(new EmptyBorder(10, 12, 10, 12));
        header.setOpaque(true);

        Icon hdrIcon = UiUtils.loadRasterIcon(
                mode == Mode.ADD
                        ? "/taskmanagement/ui/resources/add.png"
                        : "/taskmanagement/ui/resources/edit.png",
                22, 22
        );
        if (hdrIcon != null) {
            header.add(new JLabel(hdrIcon), BorderLayout.WEST);
        }

        JLabel hdrTitle = new JLabel(mode == Mode.ADD ? "Add New Task" : "Edit Task");
        hdrTitle.setFont(new Font("Segoe UI", Font.BOLD, 16));
        hdrTitle.setForeground(accentFg);

        JLabel hdrSub = new JLabel(mode == Mode.ADD ? "Create a new task" : "Update existing task details");
        hdrSub.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        hdrSub.setForeground(new Color(0xCFE0FF));

        JPanel titles = new JPanel();
        titles.setOpaque(false);
        titles.setLayout(new BoxLayout(titles, BoxLayout.Y_AXIS));
        titles.add(hdrTitle);
        titles.add(Box.createVerticalStrut(2));
        titles.add(hdrSub);
        header.add(titles, BorderLayout.CENTER);

        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);
        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(6, 6, 6, 6);
        gc.fill = GridBagConstraints.HORIZONTAL;
        gc.gridx = 0; gc.gridy = 0; gc.weightx = 0;

        JLabel titleLbl = new JLabel("Title:");
        titleLbl.setForeground(AppTheme.MAIN_TEXT);
        form.add(titleLbl, gc);

        gc.gridx = 1; gc.weightx = 1.0;
        UiUtils.styleTextFieldForDarkCentered(titleField);
        titleField.setHorizontalAlignment(SwingConstants.LEFT);
        form.add(titleField, gc);

        gc.gridx = 0; gc.gridy++; gc.weightx = 0;
        JLabel descLbl = new JLabel("Description:");
        descLbl.setForeground(AppTheme.MAIN_TEXT);
        form.add(descLbl, gc);

        gc.gridx = 1; gc.weightx = 1.0;
        UiUtils.styleTextArea(descriptionArea);
        descriptionArea.setLineWrap(true);
        descriptionArea.setWrapStyleWord(true);
        descriptionArea.getDocument().addDocumentListener((UiUtils.simpleDocListener(e -> {
            int len = descriptionArea.getText().length();
            if (len > 500) {
                descriptionArea.setText(descriptionArea.getText().substring(0, 500));
                len = 500;
            }
            descCounter.setText(len + " / 500");
        })));
        JScrollPane sp = new JScrollPane(descriptionArea);
        sp.setBorder(BorderFactory.createEmptyBorder());
        sp.setOpaque(false);
        sp.getViewport().setOpaque(false);
        form.add(sp, gc);

        gc.gridx = 1; gc.gridy++; gc.weightx = 1.0;
        descCounter.setForeground(new Color(0x8CA0B3));
        descCounter.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        descCounter.setHorizontalAlignment(SwingConstants.RIGHT);
        form.add(descCounter, gc);

        gc.gridx = 0; gc.gridy++; gc.weightx = 0;
        JLabel stateLbl = new JLabel("State:");
        stateLbl.setForeground(AppTheme.MAIN_TEXT);
        form.add(stateLbl, gc);

        gc.gridx = 1; gc.weightx = 1.0;
        stateCombo.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        form.add(stateCombo, gc);

        if (mode == Mode.EDIT && prefill != null) {
            titleField.setText(Objects.toString(prefill.title(), ""));
            descriptionArea.setText(Objects.toString(prefill.description(), ""));
            stateCombo.setSelectedItem(prefill.state());
            descCounter.setText(Math.min(descriptionArea.getText().length(), 500) + " / 500");
        } else {
            stateCombo.setSelectedItem(TaskState.ToDo);
        }

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        actions.setOpaque(false);

        final JButton cancelButton = new JButton("Cancel");
        UiUtils.styleStableHoverButton(cancelButton, new Color(0x3B3B3B), AppTheme.MAIN_TEXT);
        cancelButton.addActionListener(e -> onCancel());

        final JButton okButton = new JButton(mode == Mode.ADD ? "Add" : "Save");
        Color primaryBg = (mode == Mode.ADD) ? new Color(0x2E8B57) : new Color(0x2F7BFF);
        UiUtils.styleStableHoverButton(okButton, primaryBg, Color.WHITE);
        okButton.addActionListener(e -> onConfirm());

        actions.add(cancelButton);
        actions.add(okButton);

        root.add(header, BorderLayout.NORTH);
        root.add(form, BorderLayout.CENTER);
        root.add(actions, BorderLayout.SOUTH);

        getRootPane().setDefaultButton(okButton);
        titleField.requestFocusInWindow();

        return root;
    }

    private void onConfirm() {
        final String title = titleField.getText().trim();
        if (title.isEmpty()) {
            JOptionPane.showMessageDialog(
                    this,
                    "Title cannot be empty.",
                    "Validation Error",
                    JOptionPane.WARNING_MESSAGE
            );
            titleField.requestFocusInWindow();
            return;
        }
        confirmed = true;
        dispose();
    }

    private void onCancel() {
        confirmed = false;
        dispose();
    }

    /**
     * Displays the dialog modally and returns the user input if confirmed.
     *
     * @param parent  parent component (for centering)
     * @param mode    dialog mode (ADD or EDIT)
     * @param prefill optional prefilled values for EDIT mode
     * @return an {@link Optional} containing {@link EditorResult} if the user confirmed; otherwise empty
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
