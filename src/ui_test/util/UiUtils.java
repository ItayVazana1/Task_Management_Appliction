package ui_test.util;

import ui_test.styles.AppTheme;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;

/**
 * UI utilities: stable hover buttons, color helpers, custom borders,
 * and small Swing helpers. This class keeps legacy helpers intact and
 * adds new helpers in an append-only manner.
 */
public final class UiUtils {
    private UiUtils() {}

    // ----------------------------------------------------------------
    // Legacy helpers (kept intact)
    // ----------------------------------------------------------------

    /** Stable-size modern button (no layout shift on hover/press). */
    public static void styleStableHoverButton(JButton b, Color baseBg, Color baseFg) {
        b.setFocusPainted(false);
        b.setRolloverEnabled(true);
        b.setContentAreaFilled(true);
        b.setOpaque(true);
        b.setBackground(baseBg);
        b.setForeground(baseFg);
        b.setFont(b.getFont().deriveFont(Font.BOLD, (float) AppTheme.BTN_FONT));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        // Constant border (rounded + fixed padding) — does not change per state
        Border constantBorder = BorderFactory.createCompoundBorder(
                new RoundedMatteShadowBorder(new Color(0,0,0,60), AppTheme.BTN_RADIUS, 1),
                BorderFactory.createEmptyBorder(AppTheme.BTN_PAD_V, AppTheme.BTN_PAD_H,
                        AppTheme.BTN_PAD_V, AppTheme.BTN_PAD_H)
        );
        b.setBorder(constantBorder);

        // Precompute hover/press colors (background/foreground only)
        final Color hoverBg  = shiftForContrast(baseBg, 0.40f);
        final Color hoverFg  = bestTextFor(hoverBg, baseFg);
        final Color pressBg  = shiftForContrast(baseBg, 0.55f);
        final Color pressFg  = bestTextFor(pressBg, baseFg);

        b.getModel().addChangeListener(e -> {
            ButtonModel m = (ButtonModel) e.getSource();
            if (m.isPressed()) {
                b.setBackground(pressBg);
                b.setForeground(pressFg);
            } else if (m.isRollover()) {
                b.setBackground(hoverBg);
                b.setForeground(hoverFg);
            } else {
                b.setBackground(baseBg);
                b.setForeground(baseFg);
            }
        });
    }

    // ---------- Color helpers ----------

    /** Shift color toward light or dark depending on luminance (for hover/press). */
    public static Color shiftForContrast(Color c, float ratio) {
        float lum = luminance(c);
        return (lum >= 0.5f) ? blend(c, Color.BLACK, ratio) : blend(c, Color.WHITE, ratio);
    }

    /** Choose readable text color for a given background. */
    public static Color bestTextFor(Color bg, Color preferred) {
        if (contrastRatio(bg, preferred) >= 4.0) return preferred;
        Color alt1 = Color.WHITE, alt2 = Color.BLACK;
        double bestC = contrastRatio(bg, preferred);
        Color best = preferred;
        double c1 = contrastRatio(bg, alt1);
        if (c1 > bestC) { best = alt1; bestC = c1; }
        double c2 = contrastRatio(bg, alt2);
        if (c2 > bestC) { best = alt2; }
        return best;
    }

    /** Linear blend between two colors. t in [0..1]. */
    public static Color blend(Color a, Color b, float t) {
        t = Math.max(0f, Math.min(1f, t));
        int r = Math.round(a.getRed()   + (b.getRed()   - a.getRed())   * t);
        int g = Math.round(a.getGreen() + (b.getGreen() - a.getGreen()) * t);
        int bl= Math.round(a.getBlue()  + (b.getBlue()  - a.getBlue())  * t);
        int al= Math.round(a.getAlpha() + (b.getAlpha() - a.getAlpha()) * t);
        return new Color(r, g, bl, al);
    }

    /** Relative luminance (sRGB) approx. */
    public static float luminance(Color c) {
        float r = srgbToLin(c.getRed() / 255f);
        float g = srgbToLin(c.getGreen() / 255f);
        float b = srgbToLin(c.getBlue() / 255f);
        return (float) (0.2126 * r + 0.7152 * g + 0.0722 * b);
    }

    public static float srgbToLin(float c) {
        return (c <= 0.04045f) ? (c / 12.92f) : (float) Math.pow((c + 0.055f) / 1.055f, 2.4);
    }

    /** WCAG-ish contrast ratio approximation. */
    public static double contrastRatio(Color a, Color b) {
        double la = luminance(a) + 0.05;
        double lb = luminance(b) + 0.05;
        return (Math.max(la, lb) / Math.min(la, lb));
    }

    // ---------- Borders ----------

    /** Rounded matte border with a soft bottom shadow (no external libs). */
    public static final class RoundedMatteShadowBorder extends javax.swing.border.AbstractBorder {
        private final Color shadow;
        private final int arc;
        private final int depth; // shadow thickness
        public RoundedMatteShadowBorder(Color shadow, int arc, int depth) {
            this.shadow = shadow; this.arc = arc; this.depth = Math.max(1, depth);
        }
        @Override public Insets getBorderInsets(Component c) { return new Insets(4, 8, 4 + depth, 8); }
        @Override public boolean isBorderOpaque() { return false; }
        @Override
        public void paintBorder(Component c, Graphics g, int x, int y, int w, int h) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(shadow);
            g2.fillRoundRect(x, y + h - depth + 1, w - 1, depth, arc, arc);
            g2.dispose();
        }
    }

    /** Align component center horizontally (for BoxLayout Y_AXIS stacks). */
    public static void centerHoriz(JComponent c) {
        c.setAlignmentX(Component.CENTER_ALIGNMENT);
    }

    /** Create a square JButton with icon (top) and text (bottom), styled for dark backgrounds. */
    public static JButton createSquareActionButton(
            String text,
            Icon icon,
            Color bg,
            Color fg,
            int size,
            int cornerRadius,
            float fontSize
    ) {
        JButton b = new JButton(text);
        b.setHorizontalTextPosition(SwingConstants.CENTER);
        b.setVerticalTextPosition(SwingConstants.BOTTOM);
        b.setFocusPainted(false);
        b.setBorderPainted(false);
        b.setContentAreaFilled(true);
        b.setOpaque(true);
        b.setBackground(bg);
        b.setForeground(fg);
        b.setFont(b.getFont().deriveFont(Font.PLAIN, fontSize));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        Dimension d = new Dimension(size, size);
        b.setPreferredSize(d);
        b.setMinimumSize(d);
        b.setMaximumSize(d);

        if (icon != null) {
            b.setIcon(icon);
        } else {
            b.setIcon(makeDotIcon(Math.max(10, Math.min(22, size / 2)), fg));
        }

        // FlatLaf hints (safe no-ops on other LAFs)
        b.putClientProperty("JButton.buttonType", "roundRect");
        b.putClientProperty("JComponent.roundRect", true);
        b.putClientProperty("JComponent.arc", cornerRadius);

        addDarkHoverAndPress(b, bg);
        return b;
    }

    /** Attach dark-theme hover/pressed color behavior. */
    public static void addDarkHoverAndPress(AbstractButton b, Color base) {
        final Color hover = darken(base, 0.06f);
        final Color press = darken(base, 0.12f);
        b.addChangeListener(e -> {
            ButtonModel m = b.getModel();
            if (m.isPressed()) {
                b.setBackground(press);
            } else if (m.isRollover()) {
                b.setBackground(hover);
            } else {
                b.setBackground(base);
            }
        });
    }

    /** Darken a color by factor in [0..1]. */
    public static Color darken(Color c, float factor) {
        factor = Math.max(0f, Math.min(1f, factor));
        int r = Math.max(0, (int) (c.getRed()   * (1f - factor)));
        int g = Math.max(0, (int) (c.getGreen() * (1f - factor)));
        int b = Math.max(0, (int) (c.getBlue()  * (1f - factor)));
        return new Color(r, g, b, c.getAlpha());
    }

    /** Load PNG (or any raster) icon from classpath and scale to w×h. Path example: "/icons/add.png". */
    public static Icon loadRasterIcon(String classpath, int w, int h) {
        try {
            java.net.URL url = UiUtils.class.getResource(classpath);
            if (url == null) return null;
            Image img = new ImageIcon(url).getImage().getScaledInstance(w, h, Image.SCALE_SMOOTH);
            return new ImageIcon(img);
        } catch (Exception ignore) {
            return null;
        }
    }

    /** Try loading SVG with FlatLaf Extras; returns null if not available. */
    public static Icon tryLoadSvgIcon(String classpath, int w, int h) {
        try {
            Class<?> svgIconCls = Class.forName("com.formdev.flatlaf.extras.FlatSVGIcon");
            return (Icon) svgIconCls
                    .getConstructor(String.class, int.class, int.class)
                    .newInstance(classpath, w, h);
        } catch (Throwable t) {
            return null;
        }
    }

    /** Small fallback icon (filled circle). */
    public static Icon makeDotIcon(int size, Color color) {
        return new Icon() {
            @Override public void paintIcon(Component c, Graphics g, int x, int y) {
                g.setColor(color);
                int s = Math.min(size, getIconWidth());
                g.fillOval(x + (getIconWidth() - s) / 2, y + (getIconHeight() - s) / 2, s, s);
            }
            @Override public int getIconWidth()  { return size; }
            @Override public int getIconHeight() { return size; }
        };
    }

    // ----------------------------------------------------------------
    // Append-only helpers for ToolBox / modern widgets
    // ----------------------------------------------------------------

    /** Style a JTextField for dark panels using AppTheme tokens (centered). */
    public static void styleTextFieldForDarkCentered(JTextField field) {
        field.putClientProperty("JTextField.showClearButton", true); // FlatLaf hint (safe no-op)
        field.setForeground(AppTheme.TB_TEXT_FG);
        field.setBackground(AppTheme.TB_FIELD_BG);
        field.setCaretColor(AppTheme.TB_TEXT_FG);
        field.setSelectionColor(darken(AppTheme.TB_FIELD_BG, 0.25f));
        field.setSelectedTextColor(AppTheme.TB_TEXT_FG);
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(AppTheme.TB_FIELD_BORDER),
                BorderFactory.createEmptyBorder(6, 8, 6, 8)
        ));
        field.setHorizontalAlignment(SwingConstants.CENTER);
        Dimension d = new Dimension(AppTheme.TB_FIELD_WIDTH, AppTheme.TB_FIELD_HEIGHT);
        field.setPreferredSize(d);
        field.setMinimumSize(d);
    }

    /** Style a ToolBox title label centered. */
    public static void styleToolBoxTitleCentered(JLabel label) {
        label.setOpaque(false);
        label.setForeground(AppTheme.TB_TEXT_FG);
        if (AppTheme.TB_TITLE_FONT_LG != null) label.setFont(AppTheme.TB_TITLE_FONT_LG);
        label.setHorizontalAlignment(SwingConstants.CENTER);
    }

    /** Style a ToolBox regular label centered. */
    public static void styleToolBoxLabelCentered(JLabel label) {
        label.setOpaque(false);
        label.setForeground(AppTheme.TB_TEXT_FG);
        if (AppTheme.TB_LABEL_FONT_LG != null) label.setFont(AppTheme.TB_LABEL_FONT_LG);
        label.setHorizontalAlignment(SwingConstants.CENTER);
    }

    /** Helper: simple FlowLayout wrapper to center a single child. */
    public static JPanel flowCenter(JComponent child) {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        p.setOpaque(false);
        p.add(child);
        return p;
    }

    /**
     * Create a primary rounded button with icon (icon left, text right),
     * painted by us to avoid LAF artifacts. Non-breaking helper.
     */
    public static JButton createPrimaryIconButton(
            String text,
            Icon icon,
            int width,
            int height,
            int cornerRadius,
            java.awt.Font font,
            java.awt.Color bg,
            java.awt.Color fg
    ) {
        JButton b = new JButton(text, icon) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getBackground());
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), cornerRadius, cornerRadius);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        // text/icon layout
        b.setHorizontalTextPosition(SwingConstants.RIGHT);
        b.setVerticalTextPosition(SwingConstants.CENTER);
        b.setIconTextGap(8);

        // remove default background (we paint ourselves)
        b.setContentAreaFilled(false);
        b.setOpaque(false);

        // visuals
        b.setBackground(bg);
        b.setForeground(fg);
        if (font != null) b.setFont(font);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.setBorder(BorderFactory.createEmptyBorder(6, 12, 6, 12));

        // size
        Dimension d = new Dimension(width, height);
        b.setPreferredSize(d);
        b.setMinimumSize(d);
        b.setMaximumSize(d);

        // hover/press using existing util
        addDarkHoverAndPress(b, bg);
        return b;
    }

    /**
     * Create a rounded square button (size×size) that paints its own background,
     * with an icon on top and text at the bottom. This avoids LAF default background
     * rectangles around the button and keeps a clean rounded look.
     *
     * Signature matches existing calls from ControlPanel.
     */
    public static JButton createPaintedRoundedIconButton(
            String text,
            Icon icon,
            Color bg,
            Color fg,
            int size,
            int cornerRadius,
            float fontSize
    ) {
        JButton b = new JButton(text, icon) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                // paint custom rounded background
                g2.setColor(getBackground());
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), cornerRadius, cornerRadius);
                g2.dispose();
                // then let Swing paint icon+text
                super.paintComponent(g);
            }
        };

        // We draw the background ourselves
        b.setContentAreaFilled(false);
        b.setOpaque(false);

        b.setBackground(bg);
        b.setForeground(fg);

        // Font: prefer AppTheme.CTRL_BUTTON_FONT if defined; otherwise use passed size
        try {
            if (ui_test.styles.AppTheme.CTRL_BUTTON_FONT != null) {
                b.setFont(ui_test.styles.AppTheme.CTRL_BUTTON_FONT.deriveFont(fontSize));
            } else {
                b.setFont(b.getFont().deriveFont(Font.PLAIN, fontSize));
            }
        } catch (Throwable ignore) {
            b.setFont(b.getFont().deriveFont(Font.PLAIN, fontSize));
        }

        // Layout: icon on top, text at bottom (centered)
        b.setHorizontalTextPosition(SwingConstants.CENTER);
        b.setVerticalTextPosition(SwingConstants.BOTTOM);

        // Fixed square size
        Dimension d = new Dimension(size, size);
        b.setPreferredSize(d);
        b.setMinimumSize(d);
        b.setMaximumSize(d);

        // Keep hover/press feedback using existing util
        addDarkHoverAndPress(b, bg);

        return b;
    }

    // ----------------------------------------------------------------
    // NEW: Header pill buttons (About / Close)
    // ----------------------------------------------------------------

    /**
     * Style a compact rounded header button (pill-like) with stable hover/press.
     * Keeps layout stable (no border size changes); paints background ourselves.
     */
    public static void styleHeaderPillButton(JButton b, Color bg, Color fg) {
        b.setFocusPainted(false);
        b.setRolloverEnabled(true);
        b.setContentAreaFilled(false); // we paint the background
        b.setOpaque(false);
        b.setForeground(fg);
        if (AppTheme.HB_BTN_FONT != null) b.setFont(AppTheme.HB_BTN_FONT);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.setHorizontalTextPosition(SwingConstants.RIGHT);
        b.setIconTextGap(8);

        // Size hints (pill min width)
        Dimension d = new Dimension(AppTheme.HB_BTN_MIN_W, AppTheme.HB_BTN_HEIGHT);
        b.setPreferredSize(d);
        b.setMinimumSize(d);

        // Basic padding
        b.setBorder(BorderFactory.createEmptyBorder(6, 12, 6, 12));

        // Interactive colors
        final Color base  = bg;
        final Color hover = shiftForContrast(base, 0.12f);
        final Color press = shiftForContrast(base, 0.22f);

        b.setBackground(base);
        b.getModel().addChangeListener(e -> {
            ButtonModel m = b.getModel();
            if (m.isPressed())      b.setBackground(press);
            else if (m.isRollover())b.setBackground(hover);
            else                    b.setBackground(base);
        });

        // Paint rounded background
        b.setUI(new javax.swing.plaf.basic.BasicButtonUI() {
            @Override public void paint(Graphics g, JComponent c) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(b.getBackground());
                g2.fillRoundRect(0, 0, c.getWidth(), c.getHeight(), AppTheme.HB_BTN_RADIUS, AppTheme.HB_BTN_RADIUS);
                g2.dispose();
                super.paint(g, c);
            }
        });
    }

}
