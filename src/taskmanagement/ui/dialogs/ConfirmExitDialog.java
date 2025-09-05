package taskmanagement.ui.dialogs;

import taskmanagement.ui.styles.AppTheme;
import taskmanagement.ui.util.RoundedPanel;
import taskmanagement.ui.util.UiUtils;

import javax.swing.*;
import java.awt.*;

/**
 * ConfirmExitDialog
 * Modal dialog asking the user to confirm exiting the application.
 * View-only; no model/DAO access (MVVM-safe).
 */
public final class ConfirmExitDialog extends JDialog {

    private JButton okButton;
    private boolean confirmed;

    /**
     * Creates the confirm-exit dialog as a modal child of the given owner.
     *
     * @param owner the parent window; may be null
     */
    public ConfirmExitDialog(Window owner) {
        super(owner, "Exit", ModalityType.APPLICATION_MODAL);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setContentPane(buildContent());
        pack();
        setResizable(false);
        setLocationRelativeTo(owner);
        getRootPane().setDefaultButton(okButton);
    }

    // ---------------------------------------------------------------------
    // UI
    // ---------------------------------------------------------------------

    private JComponent buildContent() {
        // Rounded container: use the existing RoundedPanel(Color, int)
        RoundedPanel root = new RoundedPanel(AppTheme.BODY_BG, AppTheme.WINDOW_CORNER_ARC);
        root.setBorder(BorderFactory.createEmptyBorder(16, 18, 16, 18));
        root.setLayout(new BorderLayout(0, 12));

        // Title + note
        JLabel title = new JLabel("Exit application?");
        title.setFont(title.getFont().deriveFont(Font.BOLD, 16f));
        title.setForeground(AppTheme.MAIN_TEXT);

        JLabel note = new JLabel("Unsaved work may be lost.");
        note.setForeground(AppTheme.CREAM_WHITE);

        JPanel center = new JPanel();
        center.setOpaque(false);
        center.setLayout(new BoxLayout(center, BoxLayout.Y_AXIS));
        title.setAlignmentX(Component.LEFT_ALIGNMENT);
        note.setAlignmentX(Component.LEFT_ALIGNMENT);
        center.add(title);
        center.add(Box.createVerticalStrut(6));
        center.add(note);

        // Actions (use only existing UiUtils styling helpers)
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        actions.setOpaque(false);

        okButton = new JButton("Exit");
        UiUtils.styleStableHoverButton(okButton, AppTheme.IOS_RED, AppTheme.MAIN_TEXT);

        JButton cancel = new JButton("Cancel");
        UiUtils.styleStableHoverButton(cancel, AppTheme.DARK_GREY, AppTheme.MAIN_TEXT);

        okButton.addActionListener(e -> {
            confirmed = true;
            dispose();
        });
        cancel.addActionListener(e -> {
            confirmed = false;
            dispose();
        });

        actions.add(cancel);
        actions.add(okButton);

        root.add(center, BorderLayout.CENTER);
        root.add(actions, BorderLayout.SOUTH);
        return root;
    }

    // ---------------------------------------------------------------------
    // API
    // ---------------------------------------------------------------------

    /**
     * Show a modal confirm-exit dialog and return the user's choice.
     *
     * @param parent any component inside the parent window; may be null
     * @return true if user confirmed exiting; false otherwise
     */
    public static boolean confirm(Component parent) {
        Window owner = (parent instanceof Window) ? (Window) parent : SwingUtilities.getWindowAncestor(parent);
        ConfirmExitDialog dlg = new ConfirmExitDialog(owner);
        dlg.setVisible(true);
        return dlg.confirmed;
    }
}
