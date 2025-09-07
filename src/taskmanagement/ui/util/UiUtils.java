package taskmanagement.ui.util;

import taskmanagement.ui.styles.AppTheme;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;

import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.util.function.Consumer;

/**
 * Swing utilities for styling widgets in a consistent, theme-aware way.
 * <p>
 * This class is append-only: legacy helpers remain intact and new helpers
 * are added without breaking existing signatures.
 * </p>
 */
public final class UiUtils {
    private UiUtils() {}

    /**
     * Styles a {@link JButton} to keep stable size on hover/press (no layout shift)
     * while providing simple color feedback.
     *
     * @param b      the button to style
     * @param baseBg base background color
     * @param baseFg base foreground (text/icon) color
     */
    public static void styleStableHoverButton(JButton b, Color baseBg, Color baseFg) {
        b.setFocusPainted(false);
        b.setRolloverEnabled(true);
        b.setContentAreaFilled(true);
        b.setOpaque(true);
        b.setBackground(baseBg);
        b.setForeground(baseFg);
        b.setFont(b.getFont().deriveFont(Font.BOLD, (float) AppTheme.BTN_FONT));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        Border constantBorder = BorderFactory.createCompoundBorder(
                new RoundedMatteShadowBorder(new Color(0, 0, 0, 60), AppTheme.BTN_RADIUS, 1),
                BorderFactory.createEmptyBorder(AppTheme.BTN_PAD_V, AppTheme.BTN_PAD_H,
                        AppTheme.BTN_PAD_V, AppTheme.BTN_PAD_H)
        );
        b.setBorder(constantBorder);
        final Color hoverBg = shiftForContrast(baseBg, 0.40f);
        final Color hoverFg = bestTextFor(hoverBg, baseFg);
        final Color pressBg = shiftForContrast(baseBg, 0.55f);
        final Color pressFg = bestTextFor(pressBg, baseFg);
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

    /**
     * Shifts a color toward light or dark depending on its luminance.
     *
     * @param c     the base color
     * @param ratio blend ratio in [0..1]
     * @return a color blended toward black or white to increase contrast
     */
    public static Color shiftForContrast(Color c, float ratio) {
        float lum = luminance(c);
        return (lum >= 0.5f) ? blend(c, Color.BLACK, ratio) : blend(c, Color.WHITE, ratio);
    }

    /**
     * Chooses a readable text color for a given background.
     *
     * @param bg        background color
     * @param preferred preferred foreground
     * @return the most readable color among preferred/white/black
     */
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

    /**
     * Linear blend between two colors.
     *
     * @param a first color
     * @param b second color
     * @param t blend factor in [0..1]
     * @return blended color
     */
    public static Color blend(Color a, Color b, float t) {
        t = Math.max(0f, Math.min(1f, t));
        int r = Math.round(a.getRed() + (b.getRed() - a.getRed()) * t);
        int g = Math.round(a.getGreen() + (b.getGreen() - a.getGreen()) * t);
        int bl = Math.round(a.getBlue() + (b.getBlue() - a.getBlue()) * t);
        int al = Math.round(a.getAlpha() + (b.getAlpha() - a.getAlpha()) * t);
        return new Color(r, g, bl, al);
    }

    /**
     * Approximates sRGB relative luminance.
     *
     * @param c color
     * @return luminance in [0..1]
     */
    public static float luminance(Color c) {
        float r = srgbToLin(c.getRed() / 255f);
        float g = srgbToLin(c.getGreen() / 255f);
        float b = srgbToLin(c.getBlue() / 255f);
        return (float) (0.2126 * r + 0.7152 * g + 0.0722 * b);
    }

    /**
     * Converts sRGB component to linear space.
     *
     * @param c component value in [0..1]
     * @return linearized component
     */
    public static float srgbToLin(float c) {
        return (c <= 0.04045f) ? (c / 12.92f) : (float) Math.pow((c + 0.055f) / 1.055f, 2.4);
    }

    /**
     * Computes a WCAG-like contrast ratio.
     *
     * @param a first color
     * @param b second color
     * @return contrast ratio (>=1)
     */
    public static double contrastRatio(Color a, Color b) {
        double la = luminance(a) + 0.05;
        double lb = luminance(b) + 0.05;
        return (Math.max(la, lb) / Math.min(la, lb));
    }

    /**
     * Rounded matte border with a soft bottom shadow (no external libraries).
     */
    public static final class RoundedMatteShadowBorder extends javax.swing.border.AbstractBorder {
        private final Color shadow;
        private final int arc;
        private final int depth;

        /**
         * Creates a new rounded border with a matte shadow at the bottom edge.
         *
         * @param shadow shadow color
         * @param arc    corner arc radius
         * @param depth  shadow thickness in pixels (min 1)
         */
        public RoundedMatteShadowBorder(Color shadow, int arc, int depth) {
            this.shadow = shadow; this.arc = arc; this.depth = Math.max(1, depth);
        }

        /** {@inheritDoc} */
        @Override public Insets getBorderInsets(Component c) { return new Insets(4, 8, 4 + depth, 8); }

        /** {@inheritDoc} */
        @Override public boolean isBorderOpaque() { return false; }

        /** {@inheritDoc} */
        @Override
        public void paintBorder(Component c, Graphics g, int x, int y, int w, int h) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(shadow);
            g2.fillRoundRect(x, y + h - depth + 1, w - 1, depth, arc, arc);
            g2.dispose();
        }
    }

    /**
     * Aligns a component horizontally to the center (useful for BoxLayout Y_AXIS stacks).
     *
     * @param c component to center
     */
    public static void centerHoriz(JComponent c) {
        c.setAlignmentX(Component.CENTER_ALIGNMENT);
    }

    /**
     * Creates a square {@link JButton} with icon (top) and text (bottom), styled for dark backgrounds.
     * Legacy signature used by ControlPanel.
     *
     * @param text         button text
     * @param icon         button icon (nullable)
     * @param bg           background color
     * @param fg           foreground color
     * @param size         square size in pixels
     * @param cornerRadius corner radius
     * @param fontSize     label font size
     * @return configured button
     */
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
        b.putClientProperty("JButton.buttonType", "roundRect");
        b.putClientProperty("JComponent.roundRect", true);
        b.putClientProperty("JComponent.arc", cornerRadius);
        addDarkHoverAndPress(b, bg);
        return b;
    }

    /**
     * Attaches hover/pressed background behavior suitable for dark themes.
     *
     * @param b    button to style
     * @param base base background color
     */
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

    /**
     * Darkens a color by a given factor.
     *
     * @param c      base color
     * @param factor darkening factor in [0..1]
     * @return darkened color
     */
    public static Color darken(Color c, float factor) {
        factor = Math.max(0f, Math.min(1f, factor));
        int r = Math.max(0, (int) (c.getRed() * (1f - factor)));
        int g = Math.max(0, (int) (c.getGreen() * (1f - factor)));
        int b = Math.max(0, (int) (c.getBlue() * (1f - factor)));
        return new Color(r, g, b, c.getAlpha());
    }

    /**
     * Loads a raster icon from the classpath and scales it.
     *
     * @param classpath resource path (e.g., "/icons/add.png")
     * @param w         target width
     * @param h         target height
     * @return an {@link Icon} or {@code null} if not found or failed
     */
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

    /**
     * Attempts to load an SVG icon using FlatLaf Extras (if present on the classpath).
     *
     * @param classpath SVG resource path
     * @param w         target width
     * @param h         target height
     * @return an {@link Icon} or {@code null} when FlatLaf Extras is unavailable
     */
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

    /**
     * Creates a small fallback icon (filled circle).
     *
     * @param size  icon size
     * @param color fill color
     * @return an {@link Icon} instance
     */
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

    /**
     * Creates a {@link DocumentListener} that invokes the same {@link Runnable} for all events.
     *
     * @param r action to execute
     * @return a document listener
     */
    public static DocumentListener simpleDocListener(Runnable r) {
        return new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e)  { r.run(); }
            @Override public void removeUpdate(DocumentEvent e)  { r.run(); }
            @Override public void changedUpdate(DocumentEvent e) { r.run(); }
        };
    }

    /**
     * Creates a {@link DocumentListener} that forwards events to a {@link Consumer}.
     *
     * @param c consumer receiving each {@link DocumentEvent}
     * @return a document listener
     */
    public static DocumentListener simpleDocListener(Consumer<DocumentEvent> c) {
        return new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e)  { c.accept(e); }
            @Override public void removeUpdate(DocumentEvent e)  { c.accept(e); }
            @Override public void changedUpdate(DocumentEvent e) { c.accept(e); }
        };
    }

    /**
     * Styles a {@link JTextField} for dark panels using {@link AppTheme} tokens (centered).
     *
     * @param field the text field to style
     */
    public static void styleTextFieldForDarkCentered(JTextField field) {
        field.putClientProperty("JTextField.showClearButton", true);
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

    /**
     * Styles a generic {@link JTextField} for dark UI (left-aligned).
     *
     * @param field the text field to style
     */
    public static void styleTextField(JTextField field) {
        styleTextField(field, AppTheme.TB_FIELD_BG, AppTheme.TB_TEXT_FG, AppTheme.TB_FIELD_BORDER, false);
    }

    /**
     * Styles a {@link JTextField} with explicit colors and alignment.
     *
     * @param field    the field to style
     * @param bg       background color
     * @param fg       text color
     * @param border   border color
     * @param centered whether to center the text horizontally
     */
    public static void styleTextField(JTextField field, Color bg, Color fg, Color border, boolean centered) {
        field.putClientProperty("JTextField.showClearButton", true);
        field.setForeground(fg);
        field.setBackground(bg);
        field.setCaretColor(fg);
        field.setSelectionColor(darken(bg, 0.25f));
        field.setSelectedTextColor(fg);
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(border),
                BorderFactory.createEmptyBorder(6, 8, 6, 8)
        ));
        field.setHorizontalAlignment(centered ? SwingConstants.CENTER : SwingConstants.LEADING);
    }

    /**
     * Styles a {@link JTextArea} for dark UI in dialogs (e.g., task description).
     *
     * @param area the text area to style
     */
    public static void styleTextArea(JTextArea area) {
        area.setForeground(AppTheme.TB_TEXT_FG);
        area.setBackground(AppTheme.TB_FIELD_BG);
        area.setCaretColor(AppTheme.TB_TEXT_FG);
        area.setSelectionColor(darken(AppTheme.TB_FIELD_BG, 0.25f));
        area.setSelectedTextColor(AppTheme.TB_TEXT_FG);
        area.setLineWrap(true);
        area.setWrapStyleWord(true);
        area.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(AppTheme.TB_FIELD_BORDER),
                BorderFactory.createEmptyBorder(6, 8, 6, 8)
        ));
    }

    /**
     * Styles a ToolBox title label centered.
     *
     * @param label label to style
     */
    public static void styleToolBoxTitleCentered(JLabel label) {
        label.setOpaque(false);
        label.setForeground(AppTheme.TB_TEXT_FG);
        if (AppTheme.TB_TITLE_FONT_LG != null) label.setFont(AppTheme.TB_TITLE_FONT_LG);
        label.setHorizontalAlignment(SwingConstants.CENTER);
    }

    /**
     * Styles a ToolBox regular label centered.
     *
     * @param label label to style
     */
    public static void styleToolBoxLabelCentered(JLabel label) {
        label.setOpaque(false);
        label.setForeground(AppTheme.TB_TEXT_FG);
        if (AppTheme.TB_LABEL_FONT_LG != null) label.setFont(AppTheme.TB_LABEL_FONT_LG);
        label.setHorizontalAlignment(SwingConstants.CENTER);
    }

    /**
     * Wraps a single child in a transparent {@link JPanel} with centered {@link FlowLayout}.
     *
     * @param child component to center
     * @return panel that centers the child
     */
    public static JPanel flowCenter(JComponent child) {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        p.setOpaque(false);
        p.add(child);
        return p;
    }

    /**
     * Creates a primary rounded button with icon (left) and text (right),
     * painted locally to avoid Look-and-Feel artifacts.
     *
     * @param text         button text
     * @param icon         button icon
     * @param width        preferred width
     * @param height       preferred height
     * @param cornerRadius corner radius
     * @param font         font to use (nullable)
     * @param bg           background color
     * @param fg           foreground color
     * @return configured button
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
        b.setHorizontalTextPosition(SwingConstants.RIGHT);
        b.setVerticalTextPosition(SwingConstants.CENTER);
        b.setIconTextGap(8);
        b.setContentAreaFilled(false);
        b.setOpaque(false);
        b.setBackground(bg);
        b.setForeground(fg);
        if (font != null) b.setFont(font);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.setBorder(BorderFactory.createEmptyBorder(6, 12, 6, 12));
        Dimension d = new Dimension(width, height);
        b.setPreferredSize(d);
        b.setMinimumSize(d);
        b.setMaximumSize(d);
        addDarkHoverAndPress(b, bg);
        return b;
    }

    /**
     * Creates a rounded square button (size×size) that paints its own background,
     * with an icon on top and text at the bottom.
     *
     * @param text         button text
     * @param icon         button icon
     * @param bg           background color
     * @param fg           foreground color
     * @param size         square size in pixels
     * @param cornerRadius corner radius
     * @param fontSize     font size
     * @return configured button
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
                g2.setColor(getBackground());
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), cornerRadius, cornerRadius);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        b.setContentAreaFilled(false);
        b.setOpaque(false);
        b.setBackground(bg);
        b.setForeground(fg);
        try {
            if (taskmanagement.ui.styles.AppTheme.CTRL_BUTTON_FONT != null) {
                b.setFont(taskmanagement.ui.styles.AppTheme.CTRL_BUTTON_FONT.deriveFont(fontSize));
            } else {
                b.setFont(b.getFont().deriveFont(Font.PLAIN, fontSize));
            }
        } catch (Throwable ignore) {
            b.setFont(b.getFont().deriveFont(Font.PLAIN, fontSize));
        }
        b.setHorizontalTextPosition(SwingConstants.CENTER);
        b.setVerticalTextPosition(SwingConstants.BOTTOM);
        Dimension d = new Dimension(size, size);
        b.setPreferredSize(d);
        b.setMinimumSize(d);
        b.setMaximumSize(d);
        addDarkHoverAndPress(b, bg);
        return b;
    }

    /**
     * Styles a compact rounded header button (pill-like) with stable hover/press.
     * The background is painted locally to avoid look-and-feel artifacts.
     *
     * @param b  button to style
     * @param bg background color
     * @param fg foreground (text/icon) color
     */
    public static void styleHeaderPillButton(JButton b, Color bg, Color fg) {
        b.setFocusPainted(false);
        b.setRolloverEnabled(true);
        b.setContentAreaFilled(false);
        b.setOpaque(false);
        b.setForeground(fg);
        if (AppTheme.HB_BTN_FONT != null) b.setFont(AppTheme.HB_BTN_FONT);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.setHorizontalTextPosition(SwingConstants.RIGHT);
        b.setIconTextGap(8);
        Dimension d = new Dimension(AppTheme.HB_BTN_MIN_W, AppTheme.HB_BTN_HEIGHT);
        b.setPreferredSize(d);
        b.setMinimumSize(d);
        b.setBorder(BorderFactory.createEmptyBorder(6, 12, 6, 12));
        final Color base = bg;
        final Color hover = shiftForContrast(base, 0.12f);
        final Color press = shiftForContrast(base, 0.22f);
        b.setBackground(base);
        b.getModel().addChangeListener(e -> {
            ButtonModel m = b.getModel();
            if (m.isPressed())      b.setBackground(press);
            else if (m.isRollover()) b.setBackground(hover);
            else                     b.setBackground(base);
        });
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
