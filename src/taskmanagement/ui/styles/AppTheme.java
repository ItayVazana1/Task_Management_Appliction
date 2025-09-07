package taskmanagement.ui.styles;

import javax.swing.*;
import javax.swing.plaf.ComboBoxUI;
import javax.swing.plaf.basic.BasicComboBoxUI;
import java.awt.*;

/**
 * Centralized, immutable theme tokens for the application UI (colors, sizes, typography).
 * <p>This class is a pure constant holder and must not be instantiated.</p>
 */
public final class AppTheme {

    /** Hidden constructor — not instantiable. */
    private AppTheme() { }

    // =========================
    // App shell (dimensions / base)
    // =========================
    /** Logical application width in pixels (initial/suggested). */
    public static final int   APP_WIDTH          = 1000;
    /** Logical application height in pixels (initial/suggested). */
    public static final int   APP_HEIGHT         = 640;
    /** Corner arc radius (px) for rounded windows/containers. */
    public static final int   WINDOW_CORNER_ARC  = 24;

    // Base colors
    /** App background color (dark). */
    public static final Color APP_BG      = new Color(0x121212);
    /** Body/content background color (dark). */
    public static final Color BODY_BG     = new Color(0x121212);
    /** Header/background color for top bars and cards. */
    public static final Color HEADER_BG   = new Color(0x1E1E1E);

    /** Global accent color for titles/emphasis (use instead of IOS_ORANGE). */
    public static final Color ACCENT_PRIMARY = Color.BLACK;
    public static final Color ACCENT_SECONDARY = new Color(0xFFFFFF);
    public static final Color MAIN_APP_TITLE = new Color(0xFFE690);
    /** Legacy accent kept for backward compatibility; prefer ACCENT_PRIMARY. */
    @Deprecated public static final Color IOS_ORANGE  = new Color(0xFFA100);

    /** Accent/danger color (iOS-like red). */
    public static final Color IOS_RED     = new Color(0x630700);
    /** Light cream color for light pills and highlights. */
    public static final Color CREAM_WHITE = new Color(0xFAFAE1);
    /** Neutral dark grey for panels and rails. */
    public static final Color DARK_GREY   = new Color(0x2B2B2B);
    /** Primary foreground text color on dark backgrounds. */
    public static final Color MAIN_TEXT   = new Color(0xFFFFFF);

    // Spacing
    /** Generic padding unit (px) for containers. */
    public static final int PADDING     = 12;
    /** Horizontal padding (px) inside header areas. */
    public static final int HEADER_HPAD = 8;
    /** Horizontal gap (px) between action buttons. */
    public static final int ACTIONS_HGAP = 28;
    /** Vertical gap (px) between stacked action buttons. */
    public static final int ACTIONS_VGAP = 8;

    // Generic/legacy buttons
    /** Default corner radius (px) for rounded buttons. */
    public static final int  BTN_RADIUS = 12;
    /** Vertical inner padding (px) for generic buttons. */
    public static final int  BTN_PAD_V  = 10;
    /** Horizontal inner padding (px) for generic buttons. */
    public static final int  BTN_PAD_H  = 18;
    /** Default font size (pt) for generic buttons. */
    public static final int  BTN_FONT   = 14;
    /** Title font size (pt) for large headings. */
    public static final int  TITLE_FONT = 36;

    // =========================
    // Control Panel (left rail) — COLORS
    // =========================
    /** Add button background color. */
    public static final Color CTRL_ADD_BG    = new Color(144, 236, 152);
    /** Edit button background color. */
    public static final Color CTRL_EDIT_BG   = new Color(255, 203, 160);
    /** Delete button background color. */
    public static final Color CTRL_DELETE_BG = new Color(248, 130, 130);

    /** Add button foreground color. */
    public static final Color CTRL_ADD_FG    = new Color(0, 50, 4);
    /** Edit button foreground color. */
    public static final Color CTRL_EDIT_FG   = new Color(115, 49, 0);
    /** Delete button foreground color. */
    public static final Color CTRL_DELETE_FG = new Color(83, 0, 0);

    /** Refresh button background color (teal). */
    public static final Color CTRL_REFRESH_BG = new Color(0x2EC4B6);
    /** Refresh button foreground color. */
    public static final Color CTRL_REFRESH_FG = new Color(0x003E36);

    /** Disk Cleanup button background color (deep lilac). */
    public static final Color CTRL_CLEANUP_BG = new Color(0x6C5CE7);
    /** Disk Cleanup button foreground color. */
    public static final Color CTRL_CLEANUP_FG = new Color(0xC2BDF3);

    /** Neutral foreground for controls placed on dark backgrounds. */
    public static final Color CTRL_ON_DARK_FG = MAIN_TEXT;

    // =========================
    // Control Panel (left rail) — SIZES & TYPOGRAPHY
    // =========================
    /** Square control block size (px) used for rail buttons. */
    public static final int    CTRL_BLOCK_SIZE = 80;
    /** Icon size (px) inside a control block. */
    public static final int    CTRL_ICON_SIZE  = 25;
    /** Corner radius (px) for control blocks. */
    public static final int    CTRL_CORNER_RAD = 12;
    /** Label font size (pt) under control icons. */
    public static final float  CTRL_FONT_SIZE  = 12f;
    /** Default bold font for control buttons. */
    public static final Font   CTRL_BUTTON_FONT = new Font("Segoe UI", Font.BOLD, 12);

    // =========================
    // ToolBox (right column) — CONTAINER TOKENS
    // =========================
    /** Uniform padding (px) inside toolbox containers. */
    public static final int   TB_PAD            = 8;
    /** Vertical gap (px) between toolbox rows. */
    public static final int   TB_GAP            = 8;
    /** Smaller gap for tight rows (e.g., 3-button rows to avoid clipping). */
    public static final int   TB_GAP_SM         = 6;
    /** Corner radius (px) for toolbox rounded containers. */
    public static final int   TB_CORNER_RADIUS  = 12;

    /** Toolbox large title font. */
    public static final Font  TB_TITLE_FONT_LG  = new Font("Segoe UI", Font.BOLD, 16);
    /** Toolbox label font. */
    public static final Font  TB_LABEL_FONT_LG  = new Font("Segoe UI", Font.PLAIN, 14);
    /** Toolbox radio/checkbox font. */
    public static final Font  TB_RADIO_FONT     = new Font("Segoe UI", Font.PLAIN, 13);

    /** Preferred field width (px) to keep right column near ~20%. */
    public static final int   TB_FIELD_WIDTH    = 160;
    /** Preferred field height (px). */
    public static final int   TB_FIELD_HEIGHT   = 34;

    /** Toolbox foreground text color. */
    public static final Color TB_TEXT_FG        = MAIN_TEXT;
    /** Toolbox field background color. */
    public static final Color TB_FIELD_BG       = new Color(0x30, 0x30, 0x30);
    /** Toolbox field border color. */
    public static final Color TB_FIELD_BORDER   = new Color(0x4A, 0x4A, 0x4A);

    /** Neutral panel background color (used by rounded panels). */
    public static final Color PANEL_BG          = DARK_GREY;

    // =========================
    // ToolBox (right column) — BUTTON TOKENS
    // =========================

    // Export (primary)
    /** Export button preferred width (px). */
    public static final int   TB_EXPORT_W       = 150;
    /** Export button preferred height (px). */
    public static final int   TB_EXPORT_H       = 70;
    /** Export button corner radius (px). */
    public static final int   TB_EXPORT_RADIUS  = 12;
    /** Export button icon size (px). */
    public static final int   TB_EXPORT_ICON    = 35;
    /** Export button font. */
    public static final Font  TB_EXPORT_FONT    = new Font("Segoe UI", Font.BOLD, 17);
    /** Export button background color. */
    public static final Color TB_EXPORT_BG      = new Color(136, 198, 228);
    /** Export button foreground color. */
    public static final Color TB_EXPORT_FG      = new Color(14, 26, 76);

    // Undo/Redo mini buttons (distinct per button)
    /** Undo button background color. */
    public static final Color TB_UNDO_BG        = new Color(0x3A3A3A);
    /** Undo button foreground color. */
    public static final Color TB_UNDO_FG        = MAIN_TEXT;
    /** Redo button background color. */
    public static final Color TB_REDO_BG        = new Color(0x3A3A3A);
    /** Redo button foreground color. */
    public static final Color TB_REDO_FG        = MAIN_TEXT;

    // Advance / Mark-as icon+text buttons (distinct per button)
    /** Advance button background color. */
    public static final Color TB_ADVANCE_BG     = new Color(0x2F3B26); // subtle green tint
    /** Advance button foreground color. */
    public static final Color TB_ADVANCE_FG     = new Color(0xCFF5C0);
    /** Mark-as button background color. */
    public static final Color TB_MARK_BG        = new Color(0x2B3442); // subtle blue tint
    /** Mark-as button foreground color. */
    public static final Color TB_MARK_FG        = new Color(0xC8E3FF);
    /** Generic icon size (px) for ToolBox action buttons (Advance/Mark-as/etc.). */
    public static final int   TB_ACTION_ICON    = 28;

    // Sort row (Apply/Reset) — distinct colors
    /** Sort Apply button background color. */
    public static final Color TB_SORT_APPLY_BG  = new Color(0x3C5D2A);
    /** Sort Apply button foreground color. */
    public static final Color TB_SORT_APPLY_FG  = new Color(0xD9F7C6);
    /** Sort Reset button background color. */
    public static final Color TB_SORT_RESET_BG  = new Color(0x5D2A2A);
    /** Sort Reset button foreground color. */
    public static final Color TB_SORT_RESET_FG  = new Color(0xFAD4D4);

    // Filter row (Apply/Reset/Show Filtered) — distinct colors
    /** Filter Apply button background color. */
    public static final Color TB_FILTER_APPLY_BG = new Color(0x3C5D2A);
    /** Filter Apply button foreground color. */
    public static final Color TB_FILTER_APPLY_FG = new Color(0xD9F7C6);
    /** Filter Reset button background color. */
    public static final Color TB_FILTER_RESET_BG = new Color(0x5D2A2A);
    /** Filter Reset button foreground color. */
    public static final Color TB_FILTER_RESET_FG = new Color(0xFAD4D4);

    /** Show-Filtered toggle border color (OFF state). */
    public static final Color TB_SHOW_BORDER      = TB_FIELD_BORDER;
    /** Show-Filtered toggle foreground color (OFF state). */
    public static final Color TB_SHOW_FG          = TB_TEXT_FG;
    /** Show-Filtered toggle background color (ON state). */
    public static final Color TB_SHOW_SELECTED_BG = new Color(0x2B3E5A);
    /** Show-Filtered toggle foreground color (ON state). */
    public static final Color TB_SHOW_SELECTED_FG = new Color(0xD6E8FF);

    // =========================
    // Header action buttons (About / Close)
    // =========================
    /** Header button fixed height (px). */
    public static final int   HB_BTN_HEIGHT = 38;
    /** Header button minimum width (px). */
    public static final int   HB_BTN_MIN_W  = 110;
    /** Header button corner radius (px). */
    public static final int   HB_BTN_RADIUS = 12;
    /** Header button font. */
    public static final Font  HB_BTN_FONT   = new Font("Segoe UI", Font.BOLD, 14);
    /** About button background color. */
    public static final Color HB_ABOUT_BG   = CREAM_WHITE;
    /** About button foreground color. */
    public static final Color HB_ABOUT_FG   = Color.BLACK;
    /** Close button background color. */
    public static final Color HB_CLOSE_BG   = IOS_RED;
    /** Close button foreground color. */
    public static final Color HB_CLOSE_FG   = Color.WHITE;

    // =========================
    // Global UI defaults (remove OS blue accent)
    // =========================

    /**
     * Apply global Swing defaults so the OS blue accent disappears.
     * Call once on EDT early in startup (before creating components).
     */
    public static void applyAccentDefaults() {
        // General selection colors (lists, popups, tables, text components)
        UIManager.put("List.selectionBackground", SELECTION_BG);
        UIManager.put("List.selectionForeground", SELECTION_FG);
        UIManager.put("Table.selectionBackground", SELECTION_BG);
        UIManager.put("Table.selectionForeground", SELECTION_FG);
        UIManager.put("Tree.selectionBackground", SELECTION_BG);
        UIManager.put("Tree.selectionForeground", SELECTION_FG);
        UIManager.put("TextField.selectionBackground", SELECTION_BG);
        UIManager.put("TextField.selectionForeground", SELECTION_FG);
        UIManager.put("TextArea.selectionBackground", SELECTION_BG);
        UIManager.put("TextArea.selectionForeground", SELECTION_FG);

        // ComboBox popup/selection + arrow button base (no blue)
        UIManager.put("ComboBox.selectionBackground", SELECTION_BG);
        UIManager.put("ComboBox.selectionForeground", SELECTION_FG);
        UIManager.put("ComboBox.background", TB_FIELD_BG);
        UIManager.put("ComboBox.foreground", TB_TEXT_FG);
        UIManager.put("ComboBox.buttonBackground", TB_FIELD_BG);
        UIManager.put("ComboBox.buttonShadow", ACCENT_PRIMARY);
        UIManager.put("ComboBox.buttonDarkShadow", ACCENT_PRIMARY);

        // CheckBox – replace blue check with a custom accent icon
        UIManager.put("CheckBox.icon", new AccentCheckBoxIcon());
    }

    /** Selection background/foreground used across lists, popups, fields. */
    public static final Color SELECTION_BG = new Color(0x2B2B2B);
    public static final Color SELECTION_FG = MAIN_TEXT;

    /**
     * Provide a flat ComboBox UI with a neutral arrow (black accent), no bright blue.
     * Use: combo.setUI(AppTheme.flatComboUI());
     */
    public static ComboBoxUI flatComboUI() {
        return new BasicComboBoxUI() {
            @Override
            protected JButton createArrowButton() {
                JButton b = new JButton("▾");
                b.setBorder(BorderFactory.createEmptyBorder(2, 8, 2, 8));
                b.setFocusable(false);
                b.setOpaque(true);
                b.setBackground(TB_FIELD_BG);
                b.setForeground(ACCENT_PRIMARY);
                return b;
            }
        };
    }

    /** Minimal accent-colored checkbox icon used to override OS blue. */
    public static final class AccentCheckBoxIcon implements Icon {
        private static final int SZ = 18, ARC = 4;
        @Override public int getIconWidth()  { return SZ; }
        @Override public int getIconHeight() { return SZ; }
        @Override public void paintIcon(Component c, Graphics g, int x, int y) {
            AbstractButton b = (AbstractButton) c;
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            // box
            g2.setColor(TB_FIELD_BG);
            g2.fillRoundRect(x, y, SZ, SZ, ARC, ARC);
            g2.setColor(TB_FIELD_BORDER);
            g2.drawRoundRect(x, y, SZ, SZ, ARC, ARC);

            // check mark when selected
            if (b.isSelected()) {
                g2.setStroke(new BasicStroke(2.2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                g2.setColor(ACCENT_SECONDARY);
                int x1 = x + 4,  y1 = y + 9;
                int x2 = x + 8,  y2 = y + 13;
                int x3 = x + 14, y3 = y + 5;
                g2.drawLine(x1, y1, x2, y2);
                g2.drawLine(x2, y2, x3, y3);
            }
            g2.dispose();
        }
    }
}
