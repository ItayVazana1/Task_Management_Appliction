package taskmanagement.ui.styles;

import java.awt.*;

/**
 * Central theme tokens (colors, sizes).
 */
public final class AppTheme {
    private AppTheme() {}

    // App dimensions
    public static final int  APP_WIDTH  = 1000;
    public static final int  APP_HEIGHT = 640;

    // Window shape
    public static final int WINDOW_CORNER_ARC = 24;

    // Colors (Dark theme + iOS accents)
    public static final Color APP_BG      = new Color(0x121212);
    public static final Color BODY_BG     = new Color(0x121212);
    public static final Color HEADER_BG   = new Color(0x1E1E1E);
    public static final Color IOS_ORANGE  = new Color(0xFFC846);
    public static final Color IOS_RED     = new Color(0x9E0D00);
    public static final Color CREAM_WHITE = new Color(0xFAFAE1);
    public static final Color DARK_GREY   = new Color(0x2B2B2B);
    public static final Color MAIN_TEXT   = new Color(0xFFFFFF);

    // Spacing
    public static final int PADDING = 12;
    public static final int HEADER_HPAD = 8;
    public static final int ACTIONS_HGAP = 28;
    public static final int ACTIONS_VGAP = 8;

    // Buttons (legacy/general)
    public static final int BTN_RADIUS = 12;
    public static final int BTN_PAD_V  = 10;
    public static final int BTN_PAD_H  = 18;
    public static final int BTN_FONT   = 14;
    public static final int TITLE_FONT = 36;

    // --- ControlPanel (left rail) theme tokens ---
    public static final Color CTRL_ADD_BG    = new Color(144, 236, 152);
    public static final Color CTRL_EDIT_BG   = new Color(255, 203, 160);
    public static final Color CTRL_DELETE_BG = new Color(248, 130, 130);
    public static final Color CTRL_ADD_FG    = new Color(0, 50, 4);
    public static final Color CTRL_EDIT_FG   = new Color(115, 49, 0);
    public static final Color CTRL_DELETE_FG = new Color(83, 0, 0);

    public static final int   CTRL_BLOCK_SIZE   = 80;   // square button size
    public static final int   CTRL_ICON_SIZE    = 25;   // icon size inside button
    public static final int   CTRL_CORNER_RAD   = 12;   // corner radius for buttons
    public static final float CTRL_FONT_SIZE    = 14f;  // label under icon
    public static final Font  CTRL_BUTTON_FONT  = new Font("Segoe UI", Font.BOLD, 12);

    // Unified on-dark text for controls (added for compatibility)
    public static final Color CTRL_ON_DARK_FG = MAIN_TEXT;

    // --- ToolBox (layout/typography/colors) ---
    public static final int   TB_PAD               = 8;     // uniform padding
    public static final int   TB_GAP               = 8;     // vertical gaps
    public static final int   TB_CORNER_RADIUS     = 12;    // container rounding (if applicable)

    // Typography (larger)
    public static final java.awt.Font TB_TITLE_FONT_LG =
            new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 16);
    public static final java.awt.Font TB_LABEL_FONT_LG =
            new java.awt.Font("Segoe UI", java.awt.Font.PLAIN, 14);
    public static final java.awt.Font TB_RADIO_FONT =
            new java.awt.Font("Segoe UI", java.awt.Font.PLAIN, 13);

    // Field sizing
    public static final int TB_FIELD_WIDTH  = 160; // tuned to keep right column at ~20%
    public static final int TB_FIELD_HEIGHT = 34;

    // Colors (dark UI friendly)
    public static final java.awt.Color TB_TEXT_FG      = MAIN_TEXT;
    public static final java.awt.Color TB_FIELD_BG     = new java.awt.Color(0x30, 0x30, 0x30);
    public static final java.awt.Color TB_FIELD_BORDER = new java.awt.Color(0x4A, 0x4A, 0x4A);

    // Export button (primary-ish look)
    public static final int            TB_EXPORT_W     = 150;
    public static final int            TB_EXPORT_H     = 70;
    public static final int            TB_EXPORT_RADIUS= 12;
    public static final int            TB_EXPORT_ICON  = 35;
    public static final java.awt.Font  TB_EXPORT_FONT  =
            new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 17);

    // Colors for Export button
    public static final java.awt.Color TB_EXPORT_BG    = new java.awt.Color(136, 198, 228);
    public static final java.awt.Color TB_EXPORT_FG    = new java.awt.Color(14, 26, 76);

    // Neutral panel bg (used by some rounded panels)
    public static final Color PANEL_BG = DARK_GREY;

    // --- Header buttons (About / Close) — NEW ---
    public static final int   HB_BTN_HEIGHT   = 38;
    public static final int   HB_BTN_MIN_W    = 110;
    public static final int   HB_BTN_RADIUS   = 12;
    public static final Font  HB_BTN_FONT     = new Font("Segoe UI", Font.BOLD, 14);

    public static final Color HB_ABOUT_BG     = CREAM_WHITE; // light pill
    public static final Color HB_ABOUT_FG     = Color.BLACK;

    public static final Color HB_CLOSE_BG     = IOS_RED;     // red pill
    public static final Color HB_CLOSE_FG     = Color.WHITE;
}
