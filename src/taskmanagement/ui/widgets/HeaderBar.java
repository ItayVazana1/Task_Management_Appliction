package taskmanagement.ui.widgets;

import taskmanagement.ui.styles.AppTheme;
import taskmanagement.ui.util.UiUtils;

import javax.swing.*;
import java.awt.*;
import java.util.function.Consumer;

/**
 * HeaderBar
 * Left:  app icon + title label (~60%)
 * Right: pill buttons (About / Close) (~40%)
 *
 * Uses AppTheme tokens for colors/sizing and UiUtils.styleHeaderPillButton(...)
 * for the rounded header buttons.
 */
public class HeaderBar extends JPanel {

    private final JLabel  titleLabel;
    private final JButton aboutButton;
    private final JButton closeButton;

    private Consumer<JButton> aboutHandler;
    private Consumer<JButton> closeHandler;

    public HeaderBar() {
        setOpaque(true);
        setBackground(AppTheme.HEADER_BG);
        setLayout(new GridBagLayout());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridy = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(AppTheme.HEADER_HPAD, AppTheme.HEADER_HPAD, AppTheme.HEADER_HPAD, AppTheme.HEADER_HPAD);

        // ---- Left (icon + title ~60%) ----
        JPanel leftPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        leftPanel.setOpaque(false);

        Icon appIcon = UiUtils.loadRasterIcon("/taskmanagement/ui/resources/tasks_mng.png", 40, 40);
        if (appIcon != null) {
            JLabel iconLabel = new JLabel(appIcon);
            leftPanel.add(iconLabel);
        }

        titleLabel = new JLabel("Task Management App");
        titleLabel.setForeground(AppTheme.ACCENT_SECONDARY);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, AppTheme.TITLE_FONT));
        titleLabel.setHorizontalAlignment(SwingConstants.LEFT);

        leftPanel.add(titleLabel);

        gbc.gridx = 0;
        gbc.weightx = 0.60;
        add(leftPanel, gbc);

        // ---- Right (actions ~40%) ----
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, AppTheme.ACTIONS_HGAP, AppTheme.ACTIONS_VGAP));
        actions.setOpaque(false);

        Icon infoIcon  = UiUtils.loadRasterIcon("/taskmanagement/ui/resources/information.png", 40, 40);
        Icon closeIcon = UiUtils.loadRasterIcon("/taskmanagement/ui/resources/exit.png", 50, 50);

        aboutButton = new JButton("", infoIcon);
        UiUtils.styleHeaderPillButton(aboutButton, AppTheme.HB_ABOUT_BG, AppTheme.HB_ABOUT_FG);
        aboutButton.addActionListener(e -> {
            if (aboutHandler != null) aboutHandler.accept(aboutButton);
        });

        closeButton = new JButton("", closeIcon);
        UiUtils.styleHeaderPillButton(closeButton, AppTheme.HB_CLOSE_BG, AppTheme.HB_CLOSE_FG);
        closeButton.addActionListener(e -> {
            if (closeHandler != null) closeHandler.accept(closeButton);
        });

        actions.add(aboutButton);
        actions.add(closeButton);

        gbc.gridx = 1;
        gbc.weightx = 0.40;
        add(actions, gbc);
    }

    // ---------------------------------------------------------------------
    // Public API
    // ---------------------------------------------------------------------

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
        this.aboutHandler = handler;
    }

    /** Register a click handler for the Close button. */
    public void onClose(Consumer<JButton> handler) {
        if (handler == null) return;
        for (var l : closeButton.getActionListeners()) {
            closeButton.removeActionListener(l);
        }
        closeButton.addActionListener(e -> handler.accept(closeButton));
        this.closeHandler = handler;
    }
}
