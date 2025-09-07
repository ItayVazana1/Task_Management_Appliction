package taskmanagement.ui.widgets;

import taskmanagement.ui.styles.AppTheme;
import taskmanagement.ui.util.UiUtils;

import javax.swing.*;
import java.awt.*;
import java.util.function.Consumer;

/**
 * Header bar component with a left-aligned application icon and title, and right-aligned
 * pill buttons (About / Close). Colors, spacing, and typography are sourced from
 * {@link AppTheme}, and rounded buttons are styled via {@link UiUtils#styleHeaderPillButton(JButton, Color, Color)}.
 */
public class HeaderBar extends JPanel {

    private final JLabel  titleLabel;
    private final JButton aboutButton;
    private final JButton closeButton;

    private Consumer<JButton> aboutHandler;
    private Consumer<JButton> closeHandler;

    /**
     * Constructs a header bar with icon/title on the left and About/Close buttons on the right.
     */
    public HeaderBar() {
        setOpaque(true);
        setBackground(AppTheme.HEADER_BG);
        setLayout(new GridBagLayout());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridy = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(AppTheme.HEADER_HPAD, AppTheme.HEADER_HPAD, AppTheme.HEADER_HPAD, AppTheme.HEADER_HPAD);

        JPanel leftPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        leftPanel.setOpaque(false);

        Icon appIcon = UiUtils.loadRasterIcon("/taskmanagement/ui/resources/tasks_mng.png", 40, 40);
        if (appIcon != null) {
            JLabel iconLabel = new JLabel(appIcon);
            leftPanel.add(iconLabel);
        }

        titleLabel = new JLabel("Task Management App");
        titleLabel.setForeground(AppTheme.MAIN_APP_TITLE);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, AppTheme.TITLE_FONT));
        titleLabel.setHorizontalAlignment(SwingConstants.LEFT);
        leftPanel.add(titleLabel);

        gbc.gridx = 0;
        gbc.weightx = 0.60;
        add(leftPanel, gbc);

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

    /**
     * Sets the title text displayed on the left side of the header.
     *
     * @param text the new title text; {@code null} is treated as an empty string
     */
    public void setTitleText(String text) {
        titleLabel.setText(text != null ? text : "");
    }

    /**
     * Registers a click handler for the About button, replacing any existing listeners.
     *
     * @param handler a consumer that receives the About {@link JButton}; ignored if {@code null}
     */
    public void onAbout(Consumer<JButton> handler) {
        if (handler == null) return;
        for (var l : aboutButton.getActionListeners()) {
            aboutButton.removeActionListener(l);
        }
        aboutButton.addActionListener(e -> handler.accept(aboutButton));
        this.aboutHandler = handler;
    }

    /**
     * Registers a click handler for the Close button, replacing any existing listeners.
     *
     * @param handler a consumer that receives the Close {@link JButton}; ignored if {@code null}
     */
    public void onClose(Consumer<JButton> handler) {
        if (handler == null) return;
        for (var l : closeButton.getActionListeners()) {
            closeButton.removeActionListener(l);
        }
        closeButton.addActionListener(e -> handler.accept(closeButton));
        this.closeHandler = handler;
    }
}
