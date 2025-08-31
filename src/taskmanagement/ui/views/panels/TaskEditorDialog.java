package taskmanagement.ui.views.panels;

import taskmanagement.domain.ITask;
import taskmanagement.domain.TaskState;
import taskmanagement.ui.UITheme;

import javax.swing.*;
import javax.swing.text.JTextComponent;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.util.Objects;

/**
 * Modal dialog to add or edit a task.
 * If the user presses OK, returns {@link EditorResult}; otherwise returns {@code null}.
 * <p>
 * Pure Swing (no .form). Styling aligns with the dark mock:
 * lime header, light inputs, and clear OK/Cancel buttons.
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

    /**
     * Constructs a modal editor dialog for adding or editing a task.
     * @param owner           parent window
     * @param title           window title (e.g., "Add Task" / "Edit Task")
     * @param existingOrNull  initial task values or {@code null} for new
     */
    public TaskEditorDialog(Window owner, String title, ITask existingOrNull) {
        super(owner, title, ModalityType.APPLICATION_MODAL);

        // Global defaults (safe if called again)
        UITheme.applyGlobalDefaults();

        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout(0, 0));
        getContentPane().setBackground(UITheme.BG_APP);

        // ===== Header (lime) =====
        JPanel header = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 12));
        header.setOpaque(false);
        header.add(UITheme.makeHeaderTitle(title));
        add(header, BorderLayout.NORTH);

        // ===== Form panel =====
        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        form.setBackground(UITheme.BG_CARD);
        add(form, BorderLayout.CENTER);

        // Apply input styles
        styleLightInput(titleField);
        styleTextDark(descArea);
        UITheme.styleInput(stateCombo);

        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(8, 8, 8, 8);
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 0;
        c.gridx = 0; c.gridy = 0; form.add(UITheme.makeSectionLabel("Title"), c);
        c.gridx = 1; c.weightx = 1.0; form.add(titleField, c);

        c.gridx = 0; c.gridy = 1; c.weightx = 0; form.add(UITheme.makeSectionLabel("Description"), c);
        c.gridx = 1; c.fill = GridBagConstraints.BOTH; c.weightx = 1.0; c.weighty = 1.0;
        JScrollPane descScroll = new JScrollPane(descArea);
        descScroll.getViewport().setBackground(UITheme.BG_CARD);
        descScroll.setBorder(BorderFactory.createEmptyBorder());
        form.add(descScroll, c);

        c.gridx = 0; c.gridy = 2; c.fill = GridBagConstraints.HORIZONTAL; c.weighty = 0;
        form.add(UITheme.makeSectionLabel("State"), c);
        c.gridx = 1; form.add(stateCombo, c);

        // ===== Buttons =====
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        buttons.setOpaque(false);
        JButton ok = UITheme.makeFilledButton("OK", UITheme.BTN_APPLY_BG, Color.WHITE);
        JButton cancel = UITheme.makeFilledButton("Cancel", UITheme.BTN_RESET_BG, Color.WHITE);
        buttons.add(ok);
        buttons.add(cancel);
        add(buttons, BorderLayout.SOUTH);

        // Behavior
        ok.addActionListener(e -> onOK());
        cancel.addActionListener(e -> onCancel());
        getRootPane().setDefaultButton(ok);

        // ESC → cancel
        getRootPane().registerKeyboardAction(e -> onCancel(),
                KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
                JComponent.WHEN_IN_FOCUSED_WINDOW);

        // Initial values
        if (existingOrNull != null) {
            titleField.setText(existingOrNull.getTitle());
            descArea.setText(existingOrNull.getDescription());
            stateCombo.setSelectedItem(existingOrNull.getState());
        } else {
            stateCombo.setSelectedItem(TaskState.ToDo);
        }

        pack();
        setMinimumSize(new Dimension(Math.max(getWidth(), 520), Math.max(getHeight(), 420)));
        setLocationRelativeTo(owner);
    }

    /** Shows dialog (modal) and returns the user's result or {@code null}. */
    public EditorResult showDialog() {
        setVisible(true);
        return result;
    }

    // ===== Actions =====

    private void onOK() {
        String title = titleField.getText().trim();
        String desc = descArea.getText().trim();
        TaskState st = (TaskState) stateCombo.getSelectedItem();
        if (title.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Title is required.", "Validation",
                    JOptionPane.WARNING_MESSAGE);
            titleField.requestFocusInWindow();
            return;
        }
        result = new EditorResult(title, desc, st);
        dispose();
    }

    private void onCancel() {
        result = null;
        dispose();
    }

    // ===== Styling helpers (dialog-local) =====

    /** Inputs on the mock right panel are light; reuse theme token + padding. */
    private static void styleLightInput(JComponent comp) {
        UITheme.styleInput(comp);
        comp.setFont(comp.getFont().deriveFont(Font.PLAIN, 14f));
    }

    /** Dark text component (editor area) aligned to the card background. */
    private static void styleTextDark(JTextComponent tc) {
        UITheme.styleTextDark(tc);
        tc.setFont(tc.getFont().deriveFont(Font.PLAIN, 14f));
        // Only JTextArea supports wrapping APIs:
        if (tc instanceof JTextArea ta) {
            ta.setLineWrap(true);
            ta.setWrapStyleWord(true);
        }
    }
}
