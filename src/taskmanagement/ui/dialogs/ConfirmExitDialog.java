package taskmanagement.ui.dialogs;

import taskmanagement.ui.styles.AppTheme;
import taskmanagement.ui.util.RoundedPanel;
import taskmanagement.ui.util.UiUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

/**
 * Modal dialog that asks the user to confirm exiting the application.
 * <p>
 * Presentation-only and MVVM-safe: contains no model or DAO access.
 */
public final class ConfirmExitDialog extends JDialog {

    private JButton okButton;
    private boolean confirmed;

    /**
     * Creates a confirm-exit dialog as a modal child of the given owner.
     *
     * @param owner the parent window; may be {@code null}
     */
    public ConfirmExitDialog(Window owner) {
        super(owner, "Exit", ModalityType.APPLICATION_MODAL);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setContentPane(buildContent());
        pack();
        setResizable(false);
        setLocationRelativeTo(owner);
        getRootPane().setDefaultButton(okButton);

        var im = getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        var am = getRootPane().getActionMap();
        im.put(KeyStroke.getKeyStroke("ESCAPE"), "cancel");
        am.put("cancel", new AbstractAction() { @Override public void actionPerformed(ActionEvent e) { onCancel(); } });
        im.put(KeyStroke.getKeyStroke("ENTER"), "confirm");
        am.put("confirm", new AbstractAction() { @Override public void actionPerformed(ActionEvent e) { onConfirm(); } });
    }

    private JComponent buildContent() {
        RoundedPanel root = new RoundedPanel(AppTheme.BODY_BG, AppTheme.WINDOW_CORNER_ARC);
        root.setBorder(BorderFactory.createEmptyBorder(16, 18, 16, 18));
        root.setLayout(new BorderLayout(0, 12));

        final Color accentBg = new Color(0x3A0E12);
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

        JPanel center = new JPanel();
        center.setOpaque(false);
        center.setLayout(new BoxLayout(center, BoxLayout.Y_AXIS));
        JLabel note = new JLabel("This will close the application.");
        note.setForeground(AppTheme.MAIN_TEXT);
        note.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        note.setAlignmentX(Component.LEFT_ALIGNMENT);
        center.add(note);

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

        root.add(header, BorderLayout.NORTH);
        root.add(center, BorderLayout.CENTER);
        root.add(actions, BorderLayout.SOUTH);
        return root;
    }

    private void onConfirm() {
        confirmed = true;
        dispose();
    }

    private void onCancel() {
        confirmed = false;
        dispose();
    }

    /**
     * Shows a modal confirm-exit dialog and returns the user's choice.
     *
     * @param parent any component inside the parent window; may be {@code null}
     * @return {@code true} if the user confirmed exiting; {@code false} otherwise
     */
    public static boolean confirm(Component parent) {
        Window owner = (parent instanceof Window) ? (Window) parent : SwingUtilities.getWindowAncestor(parent);
        ConfirmExitDialog dlg = new ConfirmExitDialog(owner);
        dlg.setVisible(true);
        return dlg.confirmed;
    }
}
