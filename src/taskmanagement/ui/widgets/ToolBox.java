package taskmanagement.ui.widgets;

import taskmanagement.ui.styles.AppTheme;
import taskmanagement.ui.util.UiUtils;
import taskmanagement.ui.util.RoundedPanel;
import taskmanagement.ui.dialogs.ExportDialog;

import javax.swing.*;
import java.awt.*;

/* =====================  ADD YOUR IMPORT HERE  =====================
 * החלף ל-FQCN האמיתי של הדיאלוג שלך.
 * דוגמה:
 * import taskmanagement.ui.views.ExportDialog;
 * ================================================================= */


/**
 * ToolBox
 * Vertically split into 3 equal sections:
 *  Top third: "Search for a task" (centered) + centered text field.
 *  Middle third: "show me :" (centered) + radio list (left aligned, single selection).
 *  Bottom third: centered "Export" primary button with icon.
 *
 * Uses AppTheme for spacing/colors/fonts and append-only UiUtils helpers.
 * Comments in English only.
 *
 * Adjustments:
 * - Fixed sidebar width (preferred == minimum) so the center panel cannot steal space.
 * - Subtle inner padding via an empty border.
 * - Safe icon loading (fallback transparent icon if resource is missing).
 */
public class ToolBox extends RoundedPanel {

    /** Internal padding (px) to keep content away from the rounded edges. */
    private static final int INNER_PAD = 12;
    /** Fallback width if a global RIGHT_PANEL_WIDTH is not enforced externally. */
    private static final int FALLBACK_WIDTH = 320;

    // Top section (search)
    private final JTextField searchField = new JTextField();

    // Middle section (filters)
    private final ButtonGroup filterGroup = new ButtonGroup();
    private final JRadioButton rbAll        = new JRadioButton("All");
    private final JRadioButton rbTodo       = new JRadioButton("To-Do");
    private final JRadioButton rbInProgress = new JRadioButton("In-Progress");
    private final JRadioButton rbCompleted  = new JRadioButton("Completed");

    // Bottom section (export)
    private JButton exportBtn; // assigned in ctor from bottom section

    public ToolBox() {
        // keep consistent container look (use PANEL_BG we added; equals DARK_GREY)
        super(AppTheme.PANEL_BG, AppTheme.TB_CORNER_RADIUS);
        setOpaque(false);

        // --- Lock a sane sidebar width (preferred == minimum) ---
        int fixedWidth = Math.max(FALLBACK_WIDTH, AppTheme.TB_EXPORT_W + INNER_PAD * 2);
        Dimension fixed = new Dimension(fixedWidth, 10);
        setPreferredSize(fixed);
        setMinimumSize(fixed);

        // Inner padding so content breathes inside the rounded panel.
        setBorder(BorderFactory.createEmptyBorder(INNER_PAD, INNER_PAD, INNER_PAD, INNER_PAD));

        // Root layout: 3 equal rows
        setLayout(new GridBagLayout());
        GridBagConstraints root = new GridBagConstraints();
        root.gridx = 0;
        root.fill = GridBagConstraints.BOTH;
        root.weightx = 1.0;
        root.insets = new Insets(0, 0, 0, 0);

        // Top third
        JPanel top = buildTopSection();
        root.gridy = 0;
        root.weighty = 1.0;
        add(top, root);

        // Middle third
        JPanel middle = buildMiddleSection();
        root.gridy = 1;
        root.weighty = 1.0;
        add(middle, root);

        // Bottom third
        JPanel bottom = buildBottomSection();
        root.gridy = 2;
        root.weighty = 1.0;
        add(bottom, root);

        // get export button reference from bottom
        exportBtn = (JButton) bottom.getClientProperty("btnRef");

        // wire actions (only Export for now)
        wireActions();
    }

    // ---- sections ----

    private JPanel buildTopSection() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0; gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        gbc.insets = new Insets(AppTheme.TB_PAD, AppTheme.TB_PAD, AppTheme.TB_GAP, AppTheme.TB_PAD);

        // Row 0: centered title
        JLabel title = new JLabel("Search for a task");
        UiUtils.styleToolBoxTitleCentered(title);
        gbc.gridy = 0; gbc.weighty = 0.5;
        panel.add(title, gbc);

        // Row 1: centered text field
        UiUtils.styleTextFieldForDarkCentered(searchField);
        gbc.gridy = 1; gbc.weighty = 0.5;
        panel.add(UiUtils.flowCenter(searchField), gbc);

        return panel;
    }

    private JPanel buildMiddleSection() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0; gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        gbc.insets = new Insets(4, AppTheme.TB_PAD, 4, AppTheme.TB_PAD);

        // Row 0: centered "show me :"
        JLabel label = new JLabel("Show me:");
        UiUtils.styleToolBoxLabelCentered(label);
        gbc.gridy = 0; gbc.weighty = 0.5;
        panel.add(label, gbc);

        // Radio buttons (left aligned)
        styleRadio(rbAll);
        styleRadio(rbTodo);
        styleRadio(rbInProgress);
        styleRadio(rbCompleted);

        filterGroup.add(rbAll);
        filterGroup.add(rbTodo);
        filterGroup.add(rbInProgress);
        filterGroup.add(rbCompleted);
        rbAll.setSelected(true);

        gbc.weighty = 0.2;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.gridy = 1; panel.add(rbAll, gbc);
        gbc.gridy = 2; panel.add(rbTodo, gbc);
        gbc.gridy = 3; panel.add(rbInProgress, gbc);
        gbc.gridy = 4; panel.add(rbCompleted, gbc);

        return panel;
    }

    private JPanel buildBottomSection() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setOpaque(false);

        Icon exportIcon = safeIcon(UiUtils.loadRasterIcon(
                "/taskmanagement/ui/resources/download.png",
                AppTheme.TB_EXPORT_ICON,
                AppTheme.TB_EXPORT_ICON
        ));

        JButton btn = UiUtils.createPrimaryIconButton(
                "Export",
                exportIcon,
                AppTheme.TB_EXPORT_W,
                AppTheme.TB_EXPORT_H,
                AppTheme.TB_EXPORT_RADIUS,
                AppTheme.TB_EXPORT_FONT,
                AppTheme.TB_EXPORT_BG,
                AppTheme.TB_EXPORT_FG
        );

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0; gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.insets = new Insets(AppTheme.TB_PAD, AppTheme.TB_PAD, AppTheme.TB_PAD, AppTheme.TB_PAD);
        panel.add(btn, gbc);

        // expose to ctor
        panel.putClientProperty("btnRef", btn);
        return panel;
    }

    // ---- actions ----

    private void wireActions() {
        if (exportBtn != null) {
            exportBtn.addActionListener(e -> onExport());
        }
    }

    /** Opens ExportDialog directly (no reflection). Adjust constructor if needed. */
    private void onExport() {
        try {
            // ברירת מחדל: בנאי ללא פרמטרים. עדכן אם אצלך נדרש owner/VM.
            ExportDialog dlg = new ExportDialog(null); // <-- שנה בהתאם לצורך
            dlg.setModal(true);
            dlg.setResizable(false);
            dlg.pack();
            dlg.setLocationRelativeTo(this);
            dlg.setVisible(true);
            dlg.dispose();
        } catch (Throwable ex) {
            JOptionPane.showMessageDialog(this,
                    "Failed to open ExportDialog. Please adjust the import/constructor.",
                    "Export", JOptionPane.ERROR_MESSAGE);
        }
    }

    // ---- helpers ----

    private void styleRadio(JRadioButton rb) {
        rb.setOpaque(false);
        rb.setForeground(AppTheme.TB_TEXT_FG);
        rb.setFocusPainted(false);
        rb.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
        rb.setFont(AppTheme.TB_RADIO_FONT);
    }

    /** Returns a non-null icon (empty 1×1 transparent) if the provided icon is null. */
    private static Icon safeIcon(Icon icon) {
        if (icon != null) return icon;
        return new Icon() {
            @Override public void paintIcon(Component c, Graphics g, int x, int y) { /* no-op */ }
            @Override public int getIconWidth() { return 1; }
            @Override public int getIconHeight() { return 1; }
        };
    }

    // ---- public API (unchanged) ----

    public String getSearchText() { return searchField.getText(); }

    /** Returns "all", "todo", "inprogress", or "completed". */
    public String getSelectedFilterKey() {
        if (rbTodo.isSelected())       return "todo";
        if (rbInProgress.isSelected()) return "inprogress";
        if (rbCompleted.isSelected())  return "completed";
        return "all";
    }

    public JButton getExportButton() { return exportBtn; }
    public JTextField getSearchField() { return searchField; }

    public JRadioButton getRbAll()        { return rbAll; }
    public JRadioButton getRbTodo()       { return rbTodo; }
    public JRadioButton getRbInProgress() { return rbInProgress; }
    public JRadioButton getRbCompleted()  { return rbCompleted; }
}
