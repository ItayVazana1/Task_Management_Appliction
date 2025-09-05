package taskmanagement.ui.dialogs;

import taskmanagement.ui.styles.AppTheme;
import taskmanagement.ui.util.RoundedPanel;
import taskmanagement.ui.util.UiUtils;

import javax.swing.*;
import java.awt.*;

/**
 * Modal "About" dialog for the application.
 * Presentation-only (MVVM-safe): no DAO/model access.
 */
public final class AboutDialog extends JDialog {

    private JButton okButton;

    /**
     * Creates the About dialog as a modal child of the given owner.
     *
     * @param owner the parent window; may be null
     */
    public AboutDialog(Window owner) {
        super(owner, "About", ModalityType.APPLICATION_MODAL);
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
        // Rounded container using existing helper + theme tokens
        RoundedPanel root = new RoundedPanel(AppTheme.PANEL_BG, AppTheme.TB_CORNER_RADIUS);
        root.setBorder(BorderFactory.createEmptyBorder(16, 18, 16, 18));
        root.setLayout(new BorderLayout(12, 12));

        // Optional icon (if resource exists)
        Icon infoIcon = UiUtils.loadRasterIcon("/ui_test/resources/information.png", 40, 40);

        // Title + subtitle
        JLabel title = new JLabel("Tasks Management Application");
        title.setForeground(AppTheme.MAIN_TEXT);
        title.setFont(new Font("Segoe UI", Font.BOLD, 18));

        JLabel subtitle = new JLabel("Version 1.0 · Built with Swing & MVVM");
        subtitle.setForeground(new Color(0xCCCCCC));
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 13));

        // Top line with icon (if any)
        JPanel north = new JPanel(new BorderLayout(10, 0));
        north.setOpaque(false);
        if (infoIcon != null) {
            JLabel iconLabel = new JLabel(infoIcon);
            north.add(iconLabel, BorderLayout.WEST);
        }
        JPanel titles = new JPanel();
        titles.setOpaque(false);
        titles.setLayout(new BoxLayout(titles, BoxLayout.Y_AXIS));
        title.setAlignmentX(Component.LEFT_ALIGNMENT);
        subtitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        titles.add(title);
        titles.add(Box.createVerticalStrut(4));
        titles.add(subtitle);
        north.add(titles, BorderLayout.CENTER);

        // Body text
        JLabel body = new JLabel("<html>" +
                "Simple desktop app to add, edit and organize tasks.<br>" +
                "Architecture: MVVM · Derby Embedded DB · Swing UI." +
                "</html>");
        body.setForeground(AppTheme.MAIN_TEXT);

        JPanel center = new JPanel(new BorderLayout());
        center.setOpaque(false);
        center.add(body, BorderLayout.CENTER);

        // Actions
        JPanel south = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        south.setOpaque(false);

        okButton = new JButton("OK");
        // Use existing stable hover styling (no custom inventions)
        UiUtils.styleStableHoverButton(okButton, AppTheme.HB_ABOUT_BG, AppTheme.HB_ABOUT_FG);
        okButton.addActionListener(e -> dispose());

        south.add(okButton);

        root.add(north, BorderLayout.NORTH);
        root.add(center, BorderLayout.CENTER);
        root.add(south, BorderLayout.SOUTH);
        return root;
    }

    // ---------------------------------------------------------------------
    // API
    // ---------------------------------------------------------------------

    /**
     * Show the modal About dialog.
     *
     * @param parent a component inside the parent window; may be null
     */
    public static void showDialog(Component parent) {
        Window owner = parent instanceof Window ? (Window) parent : SwingUtilities.getWindowAncestor(parent);
        AboutDialog dlg = new AboutDialog(owner);
        dlg.setVisible(true);
    }
}
