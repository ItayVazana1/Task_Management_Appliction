package taskmanagement.ui.views;

import taskmanagement.ui.UITheme;

import javax.swing.*;
import java.awt.*;

/**
 * Simple About dialog (pure Java, no .form).
 * Shows app info + developers list.
 */
public final class AboutDialog extends JDialog {

    // ===== Developer info (edit here as needed) =====
    private static final String DEV1_NAME = "Developer One";
    private static final String DEV1_LINKEDIN = "https://linkedin.com/in/devone";
    private static final String DEV1_GITHUB = "https://github.com/devone";

    private static final String DEV2_NAME = "Developer Two";
    private static final String DEV2_LINKEDIN = "https://linkedin.com/in/devtwo";
    private static final String DEV2_GITHUB = "https://github.com/devtwo";

    public AboutDialog(Window owner) {
        super(owner, "About", ModalityType.APPLICATION_MODAL);

        UITheme.applyGlobalDefaults();
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        JPanel p = new JPanel(new BorderLayout());
        p.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));
        p.setBackground(UITheme.BG_CARD);

        // Title
        JLabel title = UITheme.makeHeaderTitle("Tasks Management Application");

        // App info
        JLabel info = new JLabel("<html>MVVM + DAO (Derby) + Design Patterns<br/>© 2025</html>");
        info.setForeground(UITheme.FG_PRIMARY);
        info.setBorder(BorderFactory.createEmptyBorder(12, 0, 12, 0));

        // Developer list
        String devsHtml = """
            <html>
              <b>Developers:</b><br/>
              %s - <a href='%s'>LinkedIn</a> | <a href='%s'>GitHub</a><br/>
              %s - <a href='%s'>LinkedIn</a> | <a href='%s'>GitHub</a>
            </html>
            """.formatted(
                DEV1_NAME, DEV1_LINKEDIN, DEV1_GITHUB,
                DEV2_NAME, DEV2_LINKEDIN, DEV2_GITHUB
        );
        JLabel devs = new JLabel(devsHtml);
        devs.setForeground(UITheme.FG_PRIMARY);

        // OK button
        JButton ok = UITheme.makeFilledButton("OK", UITheme.BTN_APPLY_BG, Color.WHITE);
        ok.addActionListener(e -> dispose());
        JPanel south = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        south.setOpaque(false);
        south.add(ok);

        // Layout
        p.add(title, BorderLayout.NORTH);
        p.add(info, BorderLayout.CENTER);
        p.add(devs, BorderLayout.SOUTH);
        p.add(south, BorderLayout.PAGE_END);

        setContentPane(p);
        pack();
        setMinimumSize(new Dimension(Math.max(480, getWidth()), Math.max(280, getHeight())));
        setLocationRelativeTo(owner);
    }
}
