package taskmanagement.ui;

import com.formdev.flatlaf.themes.FlatMacDarkLaf;
import taskmanagement.ui.chrome.WindowChrome;
import taskmanagement.ui.dialogs.AboutDialog;
import taskmanagement.ui.dialogs.ConfirmExitDialog;
import taskmanagement.ui.styles.AppTheme;
import taskmanagement.ui.views.ContentArea;
import taskmanagement.ui.widgets.HeaderBar;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowEvent;

/**
 * MainFrame
 * Borderless frame with HeaderBar (About/Close) and the main ContentArea.
 */
public final class MainFrame extends JFrame {

    private HeaderBar header;

    public MainFrame() {
        super("Task Management App");
        try { UIManager.setLookAndFeel(new FlatMacDarkLaf()); } catch (Throwable ignore) {}

        setContentPane(buildRoot());
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        WindowChrome.makeBorderlessWithRoundedCorners(this);

        setPreferredSize(new Dimension(AppTheme.APP_WIDTH, AppTheme.APP_HEIGHT));
        setBackground(AppTheme.APP_BG);
        pack();
        setLocationRelativeTo(null);
    }

    private JComponent buildRoot() {
        JPanel root = new JPanel(new BorderLayout());
        root.setOpaque(true);
        root.setBackground(AppTheme.APP_BG);

        // Header
        header = new HeaderBar();
        header.setTitleText("Task Management App");
        header.onAbout(btn -> AboutDialog.showDialog(this));
        header.onClose(btn -> {
            boolean ok = ConfirmExitDialog.confirm(this);
            if (ok) {
                dispatchEvent(new WindowEvent(MainFrame.this, WindowEvent.WINDOW_CLOSING));
            }
        });

        // Drag by header (fallback)
        installDragOn(header);
        try {
            var m = WindowChrome.class.getDeclaredMethod("installDragHandler", JFrame.class, JComponent.class);
            m.invoke(null, this, header);
        } catch (Throwable ignored) {}

        JPanel headerWrap = new JPanel(new BorderLayout());
        headerWrap.setOpaque(false);
        headerWrap.setBorder(BorderFactory.createEmptyBorder(
                AppTheme.PADDING, AppTheme.PADDING, 0, AppTheme.PADDING));
        headerWrap.add(header, BorderLayout.CENTER);
        root.add(headerWrap, BorderLayout.NORTH);

        // Body
        JPanel bodyBackground = new JPanel(new BorderLayout());
        bodyBackground.setOpaque(true);
        bodyBackground.setBackground(AppTheme.BODY_BG);
        bodyBackground.setBorder(BorderFactory.createEmptyBorder(
                AppTheme.PADDING, AppTheme.PADDING, AppTheme.PADDING, AppTheme.PADDING));
        root.add(bodyBackground, BorderLayout.CENTER);

        ContentArea content = new ContentArea();
        bodyBackground.add(content, BorderLayout.CENTER);

        installEscToClose();
        return root;
    }

    /** ESC key closes the window (same as Close). */
    private void installEscToClose() {
        JRootPane root = getRootPane();
        InputMap im = root.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        ActionMap am = root.getActionMap();
        final String key = "app-close";
        im.put(KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_ESCAPE, 0), key);
        am.put(key, new AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                boolean ok = ConfirmExitDialog.confirm(MainFrame.this);
                if (ok) {
                    dispatchEvent(new WindowEvent(MainFrame.this, WindowEvent.WINDOW_CLOSING));
                }
            }
        });
    }

    /** Fallback drag handler: drag the header to move the window. */
    private void installDragOn(JComponent handle) {
        final Point[] origin = new Point[1];
        MouseAdapter ma = new MouseAdapter() {
            @Override public void mousePressed(MouseEvent e) { origin[0] = e.getPoint(); }
            @Override public void mouseDragged(MouseEvent e) {
                if (origin[0] != null) {
                    Point p = e.getLocationOnScreen();
                    Insets ins = getInsets();
                    setLocation(p.x - origin[0].x - ins.left, p.y - origin[0].y - ins.top);
                }
            }
            @Override public void mouseReleased(MouseEvent e) { origin[0] = null; }
        };
        handle.addMouseListener(ma);
        handle.addMouseMotionListener(ma);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            MainFrame f = new MainFrame();
            f.setVisible(true);
        });
    }
}
