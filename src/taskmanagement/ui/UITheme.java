package taskmanagement.ui;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.LineBorder;
import javax.swing.text.JTextComponent;
import java.awt.*;

/**
 * Centralized dark UI theme for the application.
 * <p>
 * Usage:
 * <ul>
 *   <li>Call {@link #applyGlobalDefaults()} once on startup (before creating Swing components).</li>
 *   <li>Use {@link #stylePrimaryButton(AbstractButton, Color, Color)} or the helpers
 *   {@link #styleAddButton(AbstractButton)}, {@link #styleDeleteButton(AbstractButton)},
 *   {@link #styleFilterButton(AbstractButton)} for consistent buttons.</li>
 *   <li>Use the helper methods for labels, text fields and status “pills”.</li>
 * </ul>
 */
public final class UITheme {

    private UITheme() { }

    // ===== Palette (dark) =====
    /** App background (very dark navy). */
    public static final Color BASE_900 = new Color(0x0E1722);
    /** Panels/cards background. */
    public static final Color BASE_800 = new Color(0x1A334A);
    /** Lighter panel/table header. */
    public static final Color BASE_700 = new Color(0x244661);

    /** Primary text on dark backgrounds. */
    public static final Color TEXT_PRIMARY = new Color(0xE8EEF5);
    /** Secondary/disabled text. */
    public static final Color TEXT_SECONDARY = new Color(0xB6C6D9);

    // Buttons
    public static final Color BTN_ADD_BG     = new Color(0x2E7D32); // green 700-ish
    public static final Color BTN_DELETE_BG  = new Color(0xC62828); // red 700-ish
    public static final Color BTN_FILTER_BG  = new Color(0x1565C0); // blue 800-ish
    public static final Color BTN_FG         = TEXT_PRIMARY;

    // Accents (optional)
    public static final Color ACCENT_YELLOW  = new Color(0xFFCA28);
    public static final Color ACCENT_CYAN    = new Color(0x26C6DA);

    // Borders
    public static final Color BORDER_SOFT    = new Color(0x2B435B);

    /**
     * Apply global LaF and defaults.
     * Tries FlatLaf Dark if present on classpath; otherwise falls back to system LaF.
     * Safe to call multiple times (idempotent).
     */
    public static void applyGlobalDefaults() {
        try {
            // Try FlatLaf Dark if available (the project already ships flatlaf jar)
            Class<?> flatDark = Class.forName("com.formdev.flatlaf.FlatDarkLaf");
            LookAndFeel laf = (LookAndFeel) flatDark.getDeclaredConstructor().newInstance();
            UIManager.setLookAndFeel(laf);
        } catch (Throwable ignore) {
            // Fallback to system LaF
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignored) { /* ignore */ }
        }

        UIManager.put("Panel.background", BASE_900);
        UIManager.put("Table.background", BASE_900);
        UIManager.put("Table.foreground", TEXT_PRIMARY);
        UIManager.put("Table.gridColor", BORDER_SOFT);
        UIManager.put("TableHeader.background", BASE_700);
        UIManager.put("TableHeader.foreground", TEXT_PRIMARY);
        UIManager.put("Label.foreground", TEXT_PRIMARY);
        UIManager.put("TextField.background", BASE_800);
        UIManager.put("TextField.foreground", TEXT_PRIMARY);
        UIManager.put("TextArea.background", BASE_800);
        UIManager.put("TextArea.foreground", TEXT_PRIMARY);
        UIManager.put("ComboBox.background", BASE_800);
        UIManager.put("ComboBox.foreground", TEXT_PRIMARY);
        UIManager.put("MenuBar.background", BASE_800);
        UIManager.put("MenuBar.foreground", TEXT_PRIMARY);
        UIManager.put("Menu.background", BASE_800);
        UIManager.put("Menu.foreground", TEXT_PRIMARY);
        UIManager.put("MenuItem.background", BASE_800);
        UIManager.put("MenuItem.foreground", TEXT_PRIMARY);
        UIManager.put("OptionPane.background", BASE_800);
        UIManager.put("OptionPane.messageForeground", TEXT_PRIMARY);
        UIManager.put("ScrollBar.thumb", BASE_700);
        UIManager.put("ScrollBar.track", BASE_900);
    }

    // ===== Buttons =====

    /**
     * Styles a button with unified shape, background and foreground.
     * @param btn button to style
     * @param bg  background color
     * @param fg  foreground (text/icon) color
     */
    public static void stylePrimaryButton(AbstractButton btn, Color bg, Color fg) {
        btn.setOpaque(true);
        btn.setBackground(bg);
        btn.setForeground(fg);
        btn.setFont(btn.getFont().deriveFont(Font.BOLD));
        btn.setBorder(roundedBorder(bg.darker(), 10));
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        // Some Look&Feels (FlatLaf) respect this client property for a nicer shape
        btn.putClientProperty("JButton.buttonType", "roundRect");
        btn.setMargin(new Insets(6, 12, 6, 12));
    }

    /** Convenience: “Add” button styling (green). */
    public static void styleAddButton(AbstractButton btn) {
        stylePrimaryButton(btn, BTN_ADD_BG, BTN_FG);
    }

    /** Convenience: “Delete” button styling (red). */
    public static void styleDeleteButton(AbstractButton btn) {
        stylePrimaryButton(btn, BTN_DELETE_BG, BTN_FG);
    }

    /** Convenience: “Filter/OK/Apply” button styling (blue). */
    public static void styleFilterButton(AbstractButton btn) {
        stylePrimaryButton(btn, BTN_FILTER_BG, BTN_FG);
    }

    // ===== Labels / Text / Misc =====

    /** Large title label (bold, bigger font). */
    public static JLabel makeTitleLabel(String text) {
        JLabel l = new JLabel(text);
        l.setForeground(TEXT_PRIMARY);
        l.setFont(l.getFont().deriveFont(Font.BOLD, 18f));
        return l;
    }

    /** Styles text components to match the dark theme. */
    public static void styleText(JTextComponent tc) {
        tc.setBackground(BASE_800);
        tc.setForeground(TEXT_PRIMARY);
        tc.setCaretColor(TEXT_PRIMARY);
        tc.setBorder(paddedBorder(8, BASE_800));
    }

    /**
     * Creates a small rounded “pill” label (useful for status badges).
     * @param text the text to display
     * @param bg   background color
     * @param fg   foreground color
     */
    public static JLabel makeStatusPill(String text, Color bg, Color fg) {
        JLabel pill = new JLabel(text);
        pill.setOpaque(true);
        pill.setBackground(bg);
        pill.setForeground(fg);
        pill.setBorder(roundedBorder(bg.darker(), 12));
        pill.setFont(pill.getFont().deriveFont(Font.BOLD, 12f));
        pill.setHorizontalAlignment(SwingConstants.CENTER);
        return pill;
    }

    // ===== Borders helpers =====

    /** Rounded line border with padding. */
    private static Border roundedBorder(Color line, int arc) {
        return BorderFactory.createCompoundBorder(
                new LineBorder(line, 1, true),
                BorderFactory.createEmptyBorder(6, 10, 6, 10)
        );
    }

    /** Empty padding border colored (for consistency with dark bg). */
    private static Border paddedBorder(int pad, Color bg) {
        return BorderFactory.createCompoundBorder(
                new LineBorder(BORDER_SOFT, 1, true),
                BorderFactory.createEmptyBorder(pad, pad, pad, pad)
        );
    }
}
