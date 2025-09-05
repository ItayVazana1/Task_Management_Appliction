package ui_test.widgets;

import ui_test.styles.AppTheme;
import ui_test.util.UiUtils;

import javax.swing.*;
import java.awt.*;
import java.util.function.Consumer;

/**
 * HeaderBar
 * Left:  title label (~60%)
 * Right: two rounded "pill" buttons (About / Close) (~40%)
 *
 * - Uses AppTheme for colors/typography/sizing.
 * - Uses UiUtils.styleHeaderPillButton(...) for pill styling (rounded, hover/press).
 * - Exposes handler registration: onAbout(...) / onClose(...).
 * - Title can be set via setTitleText(...).
 *
 * Comments in English only.
 */
public class HeaderBar extends JPanel {

    private final JLabel  titleLabel;
    private final JButton aboutButton;
    private final JButton closeButton;

    public HeaderBar() {
        setOpaque(true);
        setBackground(AppTheme.HEADER_BG);
        setLayout(new GridBagLayout());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridy = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(AppTheme.HEADER_HPAD, AppTheme.HEADER_HPAD, AppTheme.HEADER_HPAD, AppTheme.HEADER_HPAD);

        // ---- Left (title ~60%) ----
        titleLabel = new JLabel("Task Management App");
        titleLabel.setForeground(AppTheme.IOS_ORANGE);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, AppTheme.TITLE_FONT));
        titleLabel.setHorizontalAlignment(SwingConstants.LEFT);

        gbc.gridx = 0;
        gbc.weightx = 0.60;
        add(titleLabel, gbc);

        // ---- Right (actions ~40%) ----
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, AppTheme.ACTIONS_HGAP, AppTheme.ACTIONS_VGAP));
        actions.setOpaque(false);

        // Optional icons (if exist under resources)
        Icon infoIcon  = UiUtils.loadRasterIcon("/ui_test/resources/information.png", 40, 40);
        Icon closeIcon = UiUtils.loadRasterIcon("/ui_test/resources/exit.png", 50, 50);

        aboutButton = new JButton("", infoIcon);
        UiUtils.styleHeaderPillButton(aboutButton, AppTheme.HB_ABOUT_BG, AppTheme.HB_ABOUT_FG);
        aboutButton.setActionCommand("ABOUT");

        closeButton = new JButton("", closeIcon);
        UiUtils.styleHeaderPillButton(closeButton, AppTheme.HB_CLOSE_BG, AppTheme.HB_CLOSE_FG);
        closeButton.setActionCommand("CLOSE");

        actions.add(aboutButton);
        actions.add(closeButton);

        gbc.gridx = 1;
        gbc.weightx = 0.40;
        add(actions, gbc);
    }

    // --- Public API ---

    /** Change the title text on the left side. */
    public void setTitleText(String text) {
        titleLabel.setText(text != null ? text : "");
    }

    /** Register a click handler for the About button. */
    public void onAbout(Consumer<JButton> handler) {
        if (handler == null) return;
        for (var l : aboutButton.getActionListeners()) {
            aboutButton.removeActionListener(l);
        }
        aboutButton.addActionListener(e -> handler.accept(aboutButton));
    }

    /** Register a click handler for the Close button. */
    public void onClose(Consumer<JButton> handler) {
        if (handler == null) return;
        for (var l : closeButton.getActionListeners()) {
            closeButton.removeActionListener(l);
        }
        closeButton.addActionListener(e -> handler.accept(closeButton));
    }

    // Accessors if external controllers need raw components
    public JLabel getTitleLabel()   { return titleLabel; }
    public JButton getAboutButton() { return aboutButton; }
    public JButton getCloseButton() { return closeButton; }
}
