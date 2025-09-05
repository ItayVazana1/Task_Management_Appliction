package ui_test;

import com.formdev.flatlaf.themes.FlatMacDarkLaf;
import ui_test.chrome.WindowChrome;
import ui_test.styles.AppTheme;
import ui_test.views.ContentArea;
import ui_test.widgets.HeaderBar;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowEvent;

/**
 * MainFrame (fixed)
 * - Borderless with rounded corners (WindowChrome)
 * - Opaque app background behind everything (no desktop peeking)
 * - HeaderBar (About / Close) + ContentArea (3 columns)
 * - Drag window by header, ESC closes
 *
 * Comments in English only.
 */
public final class MainFrame extends JFrame {

    private HeaderBar header;

    public MainFrame() {
        super("Task Management App");

        // LAF (safe)
        try { UIManager.setLookAndFeel(new FlatMacDarkLaf()); } catch (Throwable ignore) {}

        // Build UI tree
        setContentPane(buildRoot());
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        // Borderless + rounded corners
        WindowChrome.makeBorderlessWithRoundedCorners(this);

        // Size & show
        setPreferredSize(new Dimension(AppTheme.APP_WIDTH, AppTheme.APP_HEIGHT));
        setBackground(AppTheme.APP_BG);
        pack();
        setLocationRelativeTo(null);
    }

    /** Root: opaque background + header on top + padded body with ContentArea. */
    private JComponent buildRoot() {
        // Opaque root that paints the global app background
        JPanel root = new JPanel(new BorderLayout());
        root.setOpaque(true);
        root.setBackground(AppTheme.APP_BG);

        // ---------- Header ----------
        header = new HeaderBar();
        // אם יש לך setter לטקסט—תשאיר; אחרת אפשר להסיר את השורה
        try {
            java.lang.reflect.Method m = HeaderBar.class.getMethod("setTitleText", String.class);
            m.invoke(header, "Task Management App");
        } catch (Throwable ignored) {
            // no-op if method doesn't exist
        }

        // Actions
        try {
            java.lang.reflect.Method onAbout = HeaderBar.class.getMethod("onAbout", java.util.function.Consumer.class);
            onAbout.invoke(header, (java.util.function.Consumer<JButton>) (btn) -> showAboutDialog());
            java.lang.reflect.Method onClose = HeaderBar.class.getMethod("onClose", java.util.function.Consumer.class);
            onClose.invoke(header, (java.util.function.Consumer<JButton>) (btn) ->
                    dispatchEvent(new WindowEvent(MainFrame.this, WindowEvent.WINDOW_CLOSING)));
        } catch (Throwable ignored) {
            // fallback: if these APIs לא קיימים אצלך, אפשר להשאיר בלי חיווט כאן
        }

        // Drag by header (fallback-safe)
        installDragOn(header);
        // אם יש מתודה ב-WindowChrome להתקנת גרירה, ננסה גם אותה בלי לשבור אם אין
        try {
            var m = WindowChrome.class.getDeclaredMethod("installDragHandler", JFrame.class, JComponent.class);
            m.invoke(null, this, header);
        } catch (Throwable ignored) {}

        // Header padding (כמו בסקרינשוט)
        JPanel headerWrap = new JPanel(new BorderLayout());
        headerWrap.setOpaque(false);
        headerWrap.setBorder(BorderFactory.createEmptyBorder(
                AppTheme.PADDING, AppTheme.PADDING, 0, AppTheme.PADDING));
        headerWrap.add(header, BorderLayout.CENTER);
        root.add(headerWrap, BorderLayout.NORTH);

        // ---------- Body ----------
        // Pane אטום שמחזיק את כל התוכן (כדי שלא תראה את הדסקטופ)
        JPanel bodyBackground = new JPanel(new BorderLayout());
        bodyBackground.setOpaque(true);
        bodyBackground.setBackground(AppTheme.BODY_BG);
        bodyBackground.setBorder(BorderFactory.createEmptyBorder(
                AppTheme.PADDING, AppTheme.PADDING, AppTheme.PADDING, AppTheme.PADDING));
        root.add(bodyBackground, BorderLayout.CENTER);

        // ה־ContentArea שלך (3 עמודות) – הפאנלים הפנימיים יכולים להיות שקופים/מעוגלים, וזה בסדר
        ContentArea content = new ContentArea();
        bodyBackground.add(content, BorderLayout.CENTER);

        // ESC to close
        installEscToClose();

        return root;
    }

    /** Simple About dialog. */
    private void showAboutDialog() {
        String html =
                "<html><div style='font-family:Segoe UI, Arial; font-size:12px;'>"
                        + "<div style='font-size:16px; font-weight:bold; margin-bottom:6px;'>Task Management App</div>"
                        + "<div>Version 1.0</div>"
                        + "<div style='margin-top:8px;'>A simple Swing app for managing tasks.</div>"
                        + "<div style='color:#888; margin-top:10px;'>© 2025</div>"
                        + "</div></html>";
        JLabel msg = new JLabel(html);
        msg.setBorder(BorderFactory.createEmptyBorder(10, 14, 10, 14));
        JOptionPane.showMessageDialog(this, msg, "About", JOptionPane.INFORMATION_MESSAGE);
    }

    /** ESC key closes the window (same as Close). */
    private void installEscToClose() {
        JRootPane root = getRootPane();
        InputMap im = root.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        ActionMap am = root.getActionMap();
        final String key = "app-close";
        im.put(KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_ESCAPE, 0), key);
        am.put(key, new AbstractAction() {
            @Override public void actionPerformed(java.awt.event.ActionEvent e) {
                dispatchEvent(new WindowEvent(MainFrame.this, WindowEvent.WINDOW_CLOSING));
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
