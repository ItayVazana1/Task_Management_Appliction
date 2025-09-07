package taskmanagement.ui.dialogs;

import taskmanagement.ui.styles.AppTheme;
import taskmanagement.ui.util.RoundedPanel;
import taskmanagement.ui.util.UiUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.net.URI;

/**
 * Modal “About” dialog for the application.
 * <p>
 * Presentation-only (MVVM-safe): contains no DAO or model access.
 */
public final class AboutDialog extends JDialog {

    private JButton okButton;

    /**
     * Creates a modal About dialog.
     *
     * @param owner the owner window; may be {@code null}
     */
    public AboutDialog(Window owner) {
        super(owner, "About", ModalityType.APPLICATION_MODAL);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setContentPane(buildContent());
        pack();
        setResizable(false);
        setLocationRelativeTo(owner);
        getRootPane().setDefaultButton(okButton);

        var im = getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        var am = getRootPane().getActionMap();
        im.put(KeyStroke.getKeyStroke("ESCAPE"), "close");
        am.put("close", new AbstractAction() {
            @Override public void actionPerformed(ActionEvent e) { dispose(); }
        });
    }

    private JComponent buildContent() {
        final RoundedPanel root = new RoundedPanel(AppTheme.PANEL_BG, AppTheme.TB_CORNER_RADIUS);
        root.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));
        root.setLayout(new BorderLayout(0, 12));

        Icon infoIcon = UiUtils.loadRasterIcon(
                "/taskmanagement/ui/resources/tasks_mng.png", 40, 40);

        JLabel title = new JLabel("Tasks Management Application");
        title.setFont(new Font("Segoe UI", Font.BOLD, 18));
        title.setForeground(AppTheme.MAIN_TEXT);

        JLabel subtitle = new JLabel("Version 1.0 · Swing · MVVM · Derby Embedded");
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        subtitle.setForeground(new Color(0x888888));

        JPanel titles = new JPanel();
        titles.setOpaque(false);
        titles.setLayout(new BoxLayout(titles, BoxLayout.Y_AXIS));
        titles.add(title);
        titles.add(Box.createVerticalStrut(4));
        titles.add(subtitle);

        JPanel header = new JPanel(new BorderLayout(10, 0));
        header.setOpaque(false);
        if (infoIcon != null) {
            header.add(new JLabel(infoIcon), BorderLayout.WEST);
        }
        header.add(titles, BorderLayout.CENTER);

        JPanel body = new JPanel();
        body.setOpaque(false);
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));

        JLabel description = new JLabel(
                "<html>" +
                        "<p>A lightweight desktop app to add, edit, and organize tasks.</p>" +
                        "<p><b>Architecture:</b><br>" +
                        "• Strict MVVM separation<br>" +
                        "• Derby embedded database<br>" +
                        "• Swing UI (responsive)</p>" +
                        "</html>"
        );
        description.setForeground(AppTheme.MAIN_TEXT);
        description.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        description.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel madeByTitle = new JLabel("Made by:");
        madeByTitle.setForeground(AppTheme.MAIN_TEXT);
        madeByTitle.setFont(new Font("Segoe UI", Font.BOLD, 12));
        madeByTitle.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel authorsList = buildOrderedAuthors();
        authorsList.setAlignmentX(Component.LEFT_ALIGNMENT);

        body.add(description);
        body.add(Box.createVerticalStrut(8));
        body.add(madeByTitle);
        body.add(Box.createVerticalStrut(4));
        body.add(authorsList);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        actions.setOpaque(false);

        okButton = new JButton("OK");
        okButton.setMnemonic('O');
        okButton.getAccessibleContext().setAccessibleName("OK");
        UiUtils.styleStableHoverButton(okButton, new Color(0x2F7BFF), Color.WHITE);
        okButton.addActionListener(e -> dispose());

        actions.add(okButton);

        root.add(header, BorderLayout.NORTH);
        root.add(body, BorderLayout.CENTER);
        root.add(actions, BorderLayout.SOUTH);

        return root;
    }

    private JPanel buildOrderedAuthors() {
        JPanel list = new JPanel();
        list.setOpaque(false);
        list.setLayout(new BoxLayout(list, BoxLayout.Y_AXIS));

        JPanel row1 = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        row1.setOpaque(false);
        JLabel a1Label = new JLabel("1. Itay Vaznan  ");
        a1Label.setForeground(AppTheme.MAIN_TEXT);
        a1Label.setFont(new Font("Segoe UI", Font.BOLD, 12));
        row1.add(a1Label);
        row1.add(buildLinksPanel());
        row1.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel row2 = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        row2.setOpaque(false);
        JLabel a2Label = new JLabel("2. Yuval Benzaquen  ");
        a2Label.setForeground(AppTheme.MAIN_TEXT);
        a2Label.setFont(new Font("Segoe UI", Font.BOLD, 12));
        row2.add(a2Label);
        row2.setAlignmentX(Component.LEFT_ALIGNMENT);

        list.add(row1);
        list.add(Box.createVerticalStrut(4));
        list.add(row2);
        return list;
    }

    private JPanel buildLinksPanel() {
        JPanel links = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        links.setOpaque(false);

        links.add(makeIconLink(
                UiUtils.loadRasterIcon("/taskmanagement/ui/resources/linkedin.png", 24, 24),
                "LinkedIn",
                "https://www.linkedin.com/in/itayvazana/"));

        links.add(makeIconLink(
                UiUtils.loadRasterIcon("/taskmanagement/ui/resources/github.png", 24, 24),
                "GitHub",
                "https://github.com/ItayVazana1"));

        links.add(makeIconLink(
                UiUtils.loadRasterIcon("/taskmanagement/ui/resources/email.png", 24, 24),
                "Email",
                "mailto:itay.vazana.b@gmail.com"));

        return links;
    }

    private JComponent makeIconLink(Icon icon, String label, String url) {
        JLabel comp;
        if (icon != null) {
            comp = new JLabel(icon);
            comp.setToolTipText(label);
        } else {
            comp = new JLabel("<html><u>   " + label + "   </u></html>");
            comp.setForeground(new Color(0x2F7BFF));
            comp.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            comp.setToolTipText(label);
        }
        comp.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        comp.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) { openLink(url); }
        });
        return comp;
    }

    private void openLink(String url) {
        try {
            if (!Desktop.isDesktopSupported()) {
                throw new UnsupportedOperationException("Desktop API not supported");
            }
            Desktop desktop = Desktop.getDesktop();
            URI uri = new URI(url);
            String scheme = uri.getScheme();

            if ("mailto".equalsIgnoreCase(scheme)) {
                if (desktop.isSupported(Desktop.Action.MAIL)) {
                    desktop.mail(uri);
                } else {
                    throw new UnsupportedOperationException("MAIL action not supported");
                }
            } else {
                if (desktop.isSupported(Desktop.Action.BROWSE)) {
                    desktop.browse(uri);
                } else {
                    throw new UnsupportedOperationException("BROWSE action not supported");
                }
            }
        } catch (Exception ex) {
            try {
                String value = url.startsWith("mailto:") ? url.substring("mailto:".length()) : url;
                Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(value), null);
                JOptionPane.showMessageDialog(this,
                        "Couldn't open the default app.\nCopied to clipboard: " + value,
                        "Open Link", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ignore) {
                // intentionally ignored
            }
        }
    }

    /**
     * Shows the modal About dialog.
     *
     * @param parent a component within the parent window; may be {@code null}
     */
    public static void showDialog(Component parent) {
        Window owner = parent instanceof Window ? (Window) parent : SwingUtilities.getWindowAncestor(parent);
        AboutDialog dlg = new AboutDialog(owner);
        dlg.setVisible(true);
    }
}
