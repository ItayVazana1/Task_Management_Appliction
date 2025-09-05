package taskmanagement.ui;

import javax.swing.*;
import javax.swing.border.LineBorder;
import javax.swing.text.JTextComponent;
import java.awt.*;
import java.awt.image.BufferedImage;

/**
 * Centralized dark UI theme mapped to the new mock.
 * <p>
 * Usage:
 * <ul>
 *   <li>Call {@link #applyGlobalDefaults()} once on startup (EDT).</li>
 *   <li>Use helpers: {@link #styleActionButton(AbstractButton, Color)} for the big left buttons,
 *       {@link #makeFilledButton(String, Color, Color)} for Apply/Reset/Export/About,
 *       {@link #styleInput(JComponent)} for fields, and {@link #whiteSquareIcon(int)} as an icon placeholder.</li>
 * </ul>
 */
public final class UITheme {

    private UITheme() {}

    // ===== Palette (mock-accurate) =====
    /** App background (very dark). */
    public static final Color BG_APP       = new Color(0x1A1A1A);
    /** Cards/viewport background (slightly lighter than app). */
    public static final Color BG_CARD      = new Color(0x1C1C1C);

    /** Primary text on dark backgrounds. */
    public static final Color FG_PRIMARY   = Color.WHITE;
    /** Muted/secondary text. */
    public static final Color FG_MUTED     = new Color(0xD9D9D9);

    /** Brand lime for the header title. */
    public static final Color BRAND_LIME   = new Color(0xA6FF4D);

    /** Table header background + grid color. */
    public static final Color TABLE_HEADER = new Color(0x4C4C4C);
    public static final Color TABLE_GRID   = new Color(0x6B6B6B);

    /** Inputs (text field / combo) background. */
    public static final Color INPUT_BG     = new Color(0xB3B3B3);

    // Left action buttons
    public static final Color BTN_ADD_BG      = new Color(0x009246); // green
    public static final Color BTN_EDIT_BG     = new Color(0x808080); // grey
    public static final Color BTN_DELETE_BG   = new Color(0x8B0000); // dark red

    // Right side buttons
    public static final Color BTN_APPLY_BG    = new Color(0x00BFFF); // light blue
    public static final Color BTN_RESET_BG    = new Color(0x000000); // black
    public static final Color BTN_EXPORT_BG   = new Color(0xD9D9D9); // light grey
    public static final Color BTN_ABOUT_BG    = new Color(0x0066CC); // dark blue

    // State pills
    public static final Color PILL_TODO_BG        = new Color(0xFF3333); // red
    public static final Color PILL_INPROGRESS_BG  = new Color(0xFFD633); // yellow
    public static final Color PILL_COMPLETED_BG   = new Color(0x66CC33); // green

    // ===== Apply global LaF/defaults =====
    /**
     * Apply global Look&Feel and UI defaults matching the mock.
     * Tries FlatLaf Dark if present; otherwise falls back to system LaF.
     * Safe to call multiple times.
     */
    public static void applyGlobalDefaults() {
        try {
            Class<?> flatDark = Class.forName("com.formdev.flatlaf.FlatDarkLaf");
            LookAndFeel laf = (LookAndFeel) flatDark.getDeclaredConstructor().newInstance();
            UIManager.setLookAndFeel(laf);
        } catch (Throwable ignore) {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignored) { /* ignore */ }
        }

        // Global colors
        UIManager.put("Panel.background", BG_APP);
        UIManager.put("Label.foreground", FG_PRIMARY);

        // Table
        UIManager.put("Table.background", BG_CARD);
        UIManager.put("Table.foreground", FG_PRIMARY);
        UIManager.put("Table.gridColor", TABLE_GRID);
        UIManager.put("TableHeader.background", TABLE_HEADER);
        UIManager.put("TableHeader.foreground", FG_PRIMARY);

        // Inputs
        UIManager.put("TextField.background", INPUT_BG);
        UIManager.put("TextField.foreground", Color.BLACK);
        UIManager.put("ComboBox.background", INPUT_BG);
        UIManager.put("ComboBox.foreground", Color.BLACK);
        UIManager.put("TextArea.background", BG_CARD);
        UIManager.put("TextArea.foreground", FG_PRIMARY);

        // Menus / dialogs (dark)
        UIManager.put("MenuBar.background", BG_CARD);
        UIManager.put("MenuBar.foreground", FG_PRIMARY);
        UIManager.put("Menu.background", BG_CARD);
        UIManager.put("Menu.foreground", FG_PRIMARY);
        UIManager.put("MenuItem.background", BG_CARD);
        UIManager.put("MenuItem.foreground", FG_PRIMARY);
        UIManager.put("OptionPane.background", BG_CARD);
        UIManager.put("OptionPane.messageForeground", FG_PRIMARY);

        // Scrollbars
        UIManager.put("ScrollBar.thumb", TABLE_HEADER);
        UIManager.put("ScrollBar.track", BG_APP);
    }

    // ===== Typography =====
    /** Large header label (lime, bold). */
    public static JLabel makeHeaderTitle(String text) {
        JLabel l = new JLabel(text);
        l.setForeground(BRAND_LIME);
        l.setFont(l.getFont().deriveFont(Font.BOLD, 36f));
        return l;
    }

    /** Section label used in the right panel (bold). */
    public static JLabel makeSectionLabel(String text) {
        JLabel l = new JLabel(text);
        l.setForeground(FG_PRIMARY);
        l.setFont(l.getFont().deriveFont(Font.BOLD, 14f));
        l.setBorder(BorderFactory.createEmptyBorder(8, 8, 4, 8));
        return l;
    }

    // ===== Inputs =====
    /** Styles text field / combo to match the right-side mock controls. */
    public static void styleInput(JComponent comp) {
        comp.setBackground(INPUT_BG);
        comp.setForeground(Color.BLACK);
        comp.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        comp.setAlignmentX(Component.LEFT_ALIGNMENT);
    }

    /**
     * Styles Swing text components in dark areas (e.g., editor dialog).
     * If the component is a JTextArea, line wrapping and word wrapping are enabled.
     */
    public static void styleTextDark(JTextComponent tc) {
        tc.setBackground(BG_CARD);
        tc.setForeground(FG_PRIMARY);
        tc.setCaretColor(FG_PRIMARY);
        tc.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(TABLE_GRID, 1, true),
                BorderFactory.createEmptyBorder(8, 8, 8, 8)
        ));
        // Enable wrapping only for JTextArea
        if (tc instanceof JTextArea ta) {
            ta.setLineWrap(true);
            ta.setWrapStyleWord(true);
        }
    }

    // ===== Buttons =====
    /**
     * Big rounded action button (left bar) with white border and solid background.
     * Use together with {@link #whiteSquareIcon(int)} as placeholder.
     */
    public static void styleActionButton(AbstractButton b, Color bg) {
        b.setOpaque(true);
        b.setBackground(bg);
        b.setForeground(Color.WHITE);
        b.setFocusPainted(false);
        b.setBorder(BorderFactory.createLineBorder(Color.WHITE, 2, true));
        b.setFont(b.getFont().deriveFont(Font.BOLD, 16f));
        b.setPreferredSize(new Dimension(140, 110)); // matches the mock proportions
        b.setHorizontalTextPosition(SwingConstants.CENTER);
        b.setVerticalTextPosition(SwingConstants.BOTTOM);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }

    /**
     * Filled rectangular button with default accent (lime background, black text).
     * Used for actions when no explicit colors are passed.
     */
    public static JButton makeFilledButton(String text) {
        return makeFilledButton(text, BRAND_LIME, Color.BLACK);
    }


    /** Filled rectangular button used on the right panel (Apply/Reset/Export/About). */
    public static JButton makeFilledButton(String text, Color bg, Color fg) {
        JButton b = new JButton(text);
        b.setBackground(bg);
        b.setForeground(fg);
        b.setFocusPainted(false);
        b.setBorder(BorderFactory.createLineBorder(Color.WHITE, 2, true));
        b.setAlignmentX(Component.LEFT_ALIGNMENT);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return b;
    }

    /** Small white rounded-square icon used as a temporary placeholder. */
    public static Icon whiteSquareIcon(int size) {
        BufferedImage img = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setColor(Color.WHITE);
        int arc = Math.max(6, size / 4);
        g.fillRoundRect(0, 0, size, size, arc, arc);
        g.dispose();
        return new ImageIcon(img);
    }

    // ===== Pills (for State column, if you choose to build them as labels) =====
    public static JLabel makePill(String text, Color bg, Color fg) {
        JLabel pill = new JLabel(text, SwingConstants.CENTER);
        pill.setOpaque(true);
        pill.setBackground(bg);
        pill.setForeground(fg);
        pill.setFont(pill.getFont().deriveFont(Font.BOLD, 12f));
        pill.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(bg.darker(), 1, true),
                BorderFactory.createEmptyBorder(6, 10, 6, 10)
        ));
        return pill;
    }
}
