package taskmanagement.ui.dialogs;

import taskmanagement.ui.styles.AppTheme;
import taskmanagement.ui.util.RoundedPanel;
import taskmanagement.ui.util.UiUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

/**
 * ConfirmExitDialog
 * -----------------
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

        // Keyboard shortcuts
        var im = getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        var am = getRootPane().getActionMap();
        im.put(KeyStroke.getKeyStroke("ESCAPE"), "cancel");
        am.put("cancel", new AbstractAction() { @Override public void actionPerformed(ActionEvent e) { onCancel(); } });
        im.put(KeyStroke.getKeyStroke("ENTER"), "confirm");
        am.put("confirm", new AbstractAction() { @Override public void actionPerformed(ActionEvent e) { onConfirm(); } });
    }

    // ---------------------------------------------------------------------
    // UI
    // ---------------------------------------------------------------------

    private JComponent buildContent() {
        // Root container
        RoundedPanel root = new RoundedPanel(AppTheme.BODY_BG, AppTheme.WINDOW_CORNER_ARC);
        root.setBorder(BorderFactory.createEmptyBorder(16, 18, 16, 18));
        root.setLayout(new BorderLayout(0, 12));

        // Header (accent)
        final Color accentBg = new Color(0x3A0E12); // deep warning red-brown
        final Color accentFg = new Color(0xFFECEC);
        RoundedPanel header = new RoundedPanel(accentBg, AppTheme.WINDOW_CORNER_ARC);
        header.setLayout(new BorderLayout(10, 8));
        header.setOpaque(true);
        header.setBorder(BorderFactory.createEmptyBorder(10, 12, 10, 12));

        Icon warnIcon = UiUtils.loadRasterIcon("/taskmanagement/ui/resources/warning.png", 22, 22);
        if (warnIcon != null) {
            header.add(new JLabel(warnIcon), BorderLayout.WEST);
        }

        JLabel title = new JLabel("Exit application?");
        title.setFont(new Font("Segoe UI", Font.BOLD, 16));
        title.setForeground(accentFg);

        JLabel subtitle = new JLabel("Unsaved work may be lost.");
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        subtitle.setForeground(new Color(0xFFDADA));

        JPanel titles = new JPanel();
        titles.setOpaque(false);
        titles.setLayout(new BoxLayout(titles, BoxLayout.Y_AXIS));
        titles.add(title);
        titles.add(Box.createVerticalStrut(2));
        titles.add(subtitle);
        header.add(titles, BorderLayout.CENTER);

        // Body text (optional extra note)
        JPanel center = new JPanel();
        center.setOpaque(false);
        center.setLayout(new BoxLayout(center, BoxLayout.Y_AXIS));
        JLabel note = new JLabel("This will close the application.");
        note.setForeground(AppTheme.MAIN_TEXT);
        note.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        note.setAlignmentX(Component.LEFT_ALIGNMENT);
        center.add(note);

        // Actions
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        actions.setOpaque(false);

        JButton cancel = new JButton("Cancel");
        UiUtils.styleStableHoverButton(cancel, AppTheme.DARK_GREY, AppTheme.MAIN_TEXT);
        cancel.addActionListener(e -> onCancel());

        okButton = new JButton("Exit");
        UiUtils.styleStableHoverButton(okButton, AppTheme.IOS_RED, Color.WHITE);
        okButton.addActionListener(e -> onConfirm());

        actions.add(cancel);
        actions.add(okButton);

        // Assemble
        root.add(header, BorderLayout.NORTH);
        root.add(center, BorderLayout.CENTER);
        root.add(actions, BorderLayout.SOUTH);
        return root;
    }

    // ---------------------------------------------------------------------
    // Behavior
    // ---------------------------------------------------------------------

    private void onConfirm() {
        confirmed = true;
        dispose();
    }

    private void onCancel() {
        confirmed = false;
        dispose();
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
