package taskmanagement.ui.widgets;

import taskmanagement.ui.styles.AppTheme;
import taskmanagement.ui.util.UiUtils;
import taskmanagement.ui.util.RoundedPanel;

import taskmanagement.ui.dialogs.ExportDialog; // kept for backwards compat if you already import it

import taskmanagement.ui.widgets.ToolBox.ExportHandler;
import taskmanagement.ui.widgets.ToolBox.IdsProvider;

import taskmanagement.application.viewmodel.TasksViewModel;
import taskmanagement.application.viewmodel.ExportFormat;
import taskmanagement.application.viewmodel.sort.SortStrategy;

import taskmanagement.domain.TaskState;
import taskmanagement.domain.filter.ITaskFilter;
import taskmanagement.ui.api.TasksViewAPI;

import javax.swing.*;
import java.awt.*;
import java.nio.file.Path;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * ToolBox
 * <p>Right-side vertical toolbox split into six sections (height ratios sum to 100%).</p>
 * <ol>
 *   <li>20% — Undo / Redo (two round mini buttons, centered per half).</li>
 *   <li>20% — Advance / Mark as… (two icon+text buttons, centered per half).</li>
 *   <li>10% — Sort by (top 5%: label+combo; bottom 5%: Apply / Reset).</li>
 *   <li>30% — Filter (three checkboxes) + row of Apply / Reset / Show Filtered.</li>
 *   <li>5%  — Counters: Selected / Total (two equal halves).</li>
 *   <li>15% — Export (primary button centered).</li>
 * </ol>
 * <p>
 * Default behavior wires placeholders. Call {@link #wireTo(TasksViewAPI, IdsProvider, Function, Supplier, ExportHandler)}
 * to bind actual VM operations without leaking model/DAO into the UI.
 * </p>
 */
public final class ToolBox extends RoundedPanel {

    /** Internal padding (px) to keep content away from rounded edges. */
    private static final int INNER_PAD = 12;
    /** Fallback width to avoid clipping the three-button filter row. */
    private static final int FALLBACK_WIDTH = 360;

    // === Section 1: Undo/Redo ===
    private final JButton undoBtn = new JButton("Undo");
    private final JButton redoBtn = new JButton("Redo");

    // === Section 2: Advance / Mark as… ===
    private final JButton advanceBtn = new JButton("Advance");
    private final JButton markAsBtn  = new JButton("Mark as…");

    // === Section 3: Sort By + Apply/Reset ===
    private final JComboBox<String> sortCombo =
            new JComboBox<>(new String[] {"", "Title", "State", "ID"});
    private final JButton sortApplyBtn = new JButton("Apply");
    private final JButton sortResetBtn = new JButton("Reset");

    // === Section 4: Filter (checkboxes) + Apply / Reset / Show Filtered ===
    private final JCheckBox cbTodo       = new JCheckBox("To-Do");
    private final JCheckBox cbInProgress = new JCheckBox("In-Progress");
    private final JCheckBox cbCompleted  = new JCheckBox("Completed");

    private final JButton       filterApplyBtn  = new JButton("Apply");
    private final JButton       filterResetBtn  = new JButton("Reset");
    private final JToggleButton showFilteredTgl = new JToggleButton("Show Filtered");

    // === Section 5: Counters (Selected / Total) ===
    private final JLabel selectedCountLbl = new JLabel("Selected: 0", SwingConstants.CENTER);
    private final JLabel totalCountLbl    = new JLabel("Total: 0", SwingConstants.CENTER);

    // === Section 6: Export ===
    private JButton exportBtn; // created in buildSection6()

    // ---- Binding state (optional) ----
    private TasksViewAPI api; // set by wireTo(...)
    private IdsProvider idsProvider; // set by wireTo(...)
    private Function<String, SortStrategy> sortMapper; // set by wireTo(...)
    private Supplier<ITaskFilter> filterSupplier; // set by wireTo(...)
    private ExportHandler exportHandler; // set by wireTo(...)

    /** Constructs the toolbox panel with the 6-section layout. */
    public ToolBox() {
        super(AppTheme.PANEL_BG, AppTheme.TB_CORNER_RADIUS);
        setOpaque(false);

        // Width lock (preferred == minimum).
        int fixedWidth = Math.max(FALLBACK_WIDTH, AppTheme.TB_EXPORT_W + INNER_PAD * 2);
        Dimension fixed = new Dimension(fixedWidth, 10);
        setPreferredSize(fixed);
        setMinimumSize(fixed);

        // Inner padding.
        setBorder(BorderFactory.createEmptyBorder(INNER_PAD, INNER_PAD, INNER_PAD, INNER_PAD));

        // Root layout: 6 rows with explicit weight ratios (sum = 1.0).
        setLayout(new GridBagLayout());
        GridBagConstraints root = new GridBagConstraints();
        root.gridx = 0;
        root.fill = GridBagConstraints.BOTH;
        root.weightx = 1.0;
        root.insets = new Insets(0, 0, 0, 0);

        // 1) Undo/Redo — 20%
        root.gridy = 0; root.weighty = 0.20;
        add(buildSection1_UndoRedo(), root);

        // 2) Advance / Mark as… — 20%
        root.gridy = 1; root.weighty = 0.20;
        add(buildSection2_AdvanceMark(), root);

        // 3) Sort — 10%
        root.gridy = 2; root.weighty = 0.10;
        add(buildSection3_Sort(), root);

        // 4) Filter — 30%
        root.gridy = 3; root.weighty = 0.30;
        add(buildSection4_Filter(), root);

        // 5) Counters — 5%
        root.gridy = 4; root.weighty = 0.05;
        add(buildSection5_Counters(), root);

        // 6) Export — 15%
        root.gridy = 5; root.weighty = 0.15;
        add(buildSection6_Export(), root);

        // Default placeholders (no API bound yet).
        wirePlaceholders();
    }

    // ---------------------------------------------------------------------
    // Section builders
    // ---------------------------------------------------------------------

    /** Builds section 1 (Undo/Redo) with two centered mini buttons. */
    private JPanel buildSection1_UndoRedo() {
        JPanel p = makeTransparent();
        p.setLayout(new GridLayout(1, 2, AppTheme.TB_GAP_SM, 0));

        JPanel left  = makeTransparent();
        JPanel right = makeTransparent();
        left.setLayout(new GridBagLayout());
        right.setLayout(new GridBagLayout());

        styleMiniRound(undoBtn, AppTheme.TB_UNDO_BG, AppTheme.TB_UNDO_FG);
        styleMiniRound(redoBtn, AppTheme.TB_REDO_BG, AppTheme.TB_REDO_FG);
        undoBtn.setToolTipText("Undo last action");
        redoBtn.setToolTipText("Redo last undone action");

        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.CENTER;
        left.add(undoBtn, c);
        right.add(redoBtn, c);

        p.add(left);
        p.add(right);
        return p;
    }

    /** Builds section 2 (Advance / Mark as…) with two centered icon+text buttons. */
    private JPanel buildSection2_AdvanceMark() {
        JPanel p = makeTransparent();
        p.setLayout(new GridLayout(1, 2, AppTheme.TB_GAP_SM, 0));

        JPanel left  = makeTransparent();
        JPanel right = makeTransparent();
        left.setLayout(new GridBagLayout());
        right.setLayout(new GridBagLayout());

        Icon advIcon = safeIcon(UiUtils.loadRasterIcon(
                "/taskmanagement/ui/resources/advance.png",
                AppTheme.TB_ACTION_ICON, AppTheme.TB_ACTION_ICON));
        Icon markIcon = safeIcon(UiUtils.loadRasterIcon(
                "/taskmanagement/ui/resources/mark.png",
                AppTheme.TB_ACTION_ICON, AppTheme.TB_ACTION_ICON));

        styleIconTextButton(advanceBtn, advIcon, AppTheme.TB_ADVANCE_BG, AppTheme.TB_ADVANCE_FG);
        styleIconTextButton(markAsBtn,  markIcon, AppTheme.TB_MARK_BG,    AppTheme.TB_MARK_FG);

        advanceBtn.setToolTipText("Advance selected task(s) to next state");
        markAsBtn.setToolTipText("Mark selected task(s) to a specific state");

        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.CENTER;
        left.add(advanceBtn, c);
        right.add(markAsBtn, c);

        p.add(left);
        p.add(right);
        return p;
    }

    /** Builds section 3 (Sort) with label+combo and Apply/Reset row. */
    private JPanel buildSection3_Sort() {
        JPanel p = makeTransparent();
        p.setLayout(new GridBagLayout());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0; gbc.weightx = 1.0; gbc.fill = GridBagConstraints.HORIZONTAL;

        // Top half: "Sort by" + combo
        JPanel top = makeTransparent();
        top.setLayout(new GridBagLayout());
        JLabel sortLbl = new JLabel("Sort by");
        sortLbl.setForeground(AppTheme.TB_TEXT_FG);
        sortCombo.setPreferredSize(new Dimension(AppTheme.TB_FIELD_WIDTH, AppTheme.TB_FIELD_HEIGHT));

        GridBagConstraints t = new GridBagConstraints();
        t.insets = new Insets(AppTheme.TB_PAD, AppTheme.TB_PAD, AppTheme.TB_PAD, AppTheme.TB_PAD);
        t.gridx = 0; t.gridy = 0; t.anchor = GridBagConstraints.WEST;
        top.add(sortLbl, t);
        t.gridx = 1; t.gridy = 0; t.weightx = 1.0; t.fill = GridBagConstraints.HORIZONTAL;
        top.add(sortCombo, t);

        gbc.gridy = 0; gbc.weighty = 0.5;
        p.add(top, gbc);

        // Bottom half: Apply / Reset
        JPanel bottom = makeTransparent();
        bottom.setLayout(new GridLayout(1, 2, AppTheme.TB_GAP_SM, 0));
        styleSmallFilled(sortApplyBtn, AppTheme.TB_SORT_APPLY_BG, AppTheme.TB_SORT_APPLY_FG);
        styleSmallFilled(sortResetBtn, AppTheme.TB_SORT_RESET_BG, AppTheme.TB_SORT_RESET_FG);

        bottom.add(sortApplyBtn);
        bottom.add(sortResetBtn);

        gbc.gridy = 1; gbc.weighty = 0.5;
        p.add(bottom, gbc);

        return p;
    }

    /** Builds section 4 (Filter) with three checkboxes and Apply/Reset/Show Filtered row. */
    private JPanel buildSection4_Filter() {
        JPanel p = makeTransparent();
        p.setLayout(new GridBagLayout());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;

        // Checkbox list
        JPanel list = makeTransparent();
        list.setLayout(new GridBagLayout());

        styleCheck(cbTodo);
        styleCheck(cbInProgress);
        styleCheck(cbCompleted);

        GridBagConstraints r = new GridBagConstraints();
        r.gridx = 0; r.weightx = 1.0; r.anchor = GridBagConstraints.WEST;
        r.insets = new Insets(2, AppTheme.TB_PAD, 2, AppTheme.TB_PAD);
        r.gridy = 0; list.add(cbTodo, r);
        r.gridy = 1; list.add(cbInProgress, r);
        r.gridy = 2; list.add(cbCompleted, r);

        gbc.gridy = 0; gbc.weighty = 0.7;
        p.add(list, gbc);

        // Buttons row (equal spacing for 3 buttons)
        JPanel row = makeTransparent();
        row.setLayout(new GridLayout(1, 3, 2, 0)); // 2px horizontal gap

        styleSmallFilled(filterApplyBtn, AppTheme.TB_FILTER_APPLY_BG, AppTheme.TB_FILTER_APPLY_FG);
        styleSmallFilled(filterResetBtn, AppTheme.TB_FILTER_RESET_BG, AppTheme.TB_FILTER_RESET_FG);
        styleSmallHollow(showFilteredTgl,
                AppTheme.TB_SHOW_BORDER, AppTheme.TB_SHOW_FG,
                AppTheme.TB_SHOW_SELECTED_BG, AppTheme.TB_SHOW_SELECTED_FG);

        Dimension smallBtn = new Dimension(90, 28);
        filterApplyBtn.setPreferredSize(smallBtn);
        filterResetBtn.setPreferredSize(smallBtn);
        showFilteredTgl.setPreferredSize(smallBtn);

        filterApplyBtn.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        filterResetBtn.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        showFilteredTgl.setFont(new Font("Segoe UI", Font.PLAIN, 12));

        row.add(filterApplyBtn);
        row.add(filterResetBtn);
        row.add(showFilteredTgl);

        gbc.gridy = 1; gbc.weighty = 0.3;
        p.add(row, gbc);

        return p;
    }

    /** Builds section 5 (Counters) with two equal halves: Selected / Total. */
    private JPanel buildSection5_Counters() {
        JPanel p = makeTransparent();
        p.setLayout(new GridLayout(1, 2, AppTheme.TB_GAP_SM, 0));

        selectedCountLbl.setForeground(AppTheme.TB_TEXT_FG);
        totalCountLbl.setForeground(AppTheme.TB_TEXT_FG);

        p.add(centerInGridBag(selectedCountLbl));
        p.add(centerInGridBag(totalCountLbl));
        return p;
    }

    /** Builds section 6 (Export) with a centered primary export button. */
    private JPanel buildSection6_Export() {
        JPanel p = makeTransparent();
        p.setLayout(new GridBagLayout());

        Icon exportIcon = safeIcon(UiUtils.loadRasterIcon(
                "/taskmanagement/ui/resources/download.png",
                AppTheme.TB_EXPORT_ICON, AppTheme.TB_EXPORT_ICON));

        exportBtn = UiUtils.createPrimaryIconButton(
                "Export",
                exportIcon,
                AppTheme.TB_EXPORT_W,
                AppTheme.TB_EXPORT_H,
                AppTheme.TB_EXPORT_RADIUS,
                AppTheme.TB_EXPORT_FONT,
                AppTheme.TB_EXPORT_BG,
                AppTheme.TB_EXPORT_FG
        );

        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0; c.gridy = 0; c.anchor = GridBagConstraints.CENTER;
        c.insets = new Insets(AppTheme.TB_PAD, AppTheme.TB_PAD, AppTheme.TB_PAD, AppTheme.TB_PAD);
        p.add(exportBtn, c);

        return p;
    }

    // ---------------------------------------------------------------------
    // Wiring (placeholders by default, real VM via wireTo(...))
    // ---------------------------------------------------------------------

    /** Default placeholder wiring (kept for backward compatibility). */
    private void wirePlaceholders() {
        undoBtn.addActionListener(e ->
                JOptionPane.showMessageDialog(this, "Undo (placeholder)", "Undo", JOptionPane.INFORMATION_MESSAGE));
        redoBtn.addActionListener(e ->
                JOptionPane.showMessageDialog(this, "Redo (placeholder)", "Redo", JOptionPane.INFORMATION_MESSAGE));

        advanceBtn.addActionListener(e ->
                JOptionPane.showMessageDialog(this, "Advance (placeholder)", "Advance", JOptionPane.INFORMATION_MESSAGE));
        markAsBtn.addActionListener(e ->
                JOptionPane.showMessageDialog(this, "Mark as… (placeholder)", "Mark as…", JOptionPane.INFORMATION_MESSAGE));

        sortApplyBtn.addActionListener(e ->
                JOptionPane.showMessageDialog(this, "Sort Apply (placeholder)", "Sort", JOptionPane.INFORMATION_MESSAGE));
        sortResetBtn.addActionListener(e ->
                JOptionPane.showMessageDialog(this, "Sort Reset (placeholder)", "Sort", JOptionPane.INFORMATION_MESSAGE));

        filterApplyBtn.addActionListener(e ->
                JOptionPane.showMessageDialog(this, "Filter Apply (placeholder)", "Filter", JOptionPane.INFORMATION_MESSAGE));
        filterResetBtn.addActionListener(e ->
                JOptionPane.showMessageDialog(this, "Filter Reset (placeholder)", "Filter", JOptionPane.INFORMATION_MESSAGE));
        showFilteredTgl.addActionListener(e -> {
            boolean on = showFilteredTgl.isSelected();
            showFilteredTgl.setOpaque(on);
            showFilteredTgl.setBackground(on ? AppTheme.TB_SHOW_SELECTED_BG : new Color(0, 0, 0, 0));
            showFilteredTgl.setForeground(on ? AppTheme.TB_SHOW_SELECTED_FG : AppTheme.TB_SHOW_FG);
            JOptionPane.showMessageDialog(this,
                    (on ? "Show Filtered: ON" : "Show Filtered: OFF"),
                    "Filter", JOptionPane.INFORMATION_MESSAGE);
        });

        if (exportBtn != null) {
            exportBtn.addActionListener(e -> onExportPlaceholder());
        }
    }

    /**
     * Binds this ToolBox to the provided API and UI-side providers without guessing any implementation detail.
     * After calling this method, all buttons call the real VM operations instead of placeholders.
     *
     * @param api             UI-exposed API adapter for the ViewModel
     * @param idsProvider     supplier for currently selected task IDs and counters
     * @param sortMapper      maps combo selection ("", "Title", "State", "ID") to a SortStrategy (null clears)
     * @param filterSupplier  builds the current combinator filter (null clears)
     * @param exportHandler   handles the export interaction (dialog + api calls)
     */
    public void wireTo(TasksViewAPI api,
                       IdsProvider idsProvider,
                       Function<String, SortStrategy> sortMapper,
                       Supplier<ITaskFilter> filterSupplier,
                       ExportHandler exportHandler) {

        this.api = Objects.requireNonNull(api, "api");
        this.idsProvider = Objects.requireNonNull(idsProvider, "idsProvider");
        this.sortMapper = Objects.requireNonNull(sortMapper, "sortMapper");
        this.filterSupplier = Objects.requireNonNull(filterSupplier, "filterSupplier");
        this.exportHandler = Objects.requireNonNull(exportHandler, "exportHandler");

        // Clear previous listeners (keep it simple by replacing ActionListeners)
        for (var l : undoBtn.getActionListeners()) undoBtn.removeActionListener(l);
        for (var l : redoBtn.getActionListeners()) redoBtn.removeActionListener(l);
        for (var l : advanceBtn.getActionListeners()) advanceBtn.removeActionListener(l);
        for (var l : markAsBtn.getActionListeners()) markAsBtn.removeActionListener(l);
        for (var l : sortApplyBtn.getActionListeners()) sortApplyBtn.removeActionListener(l);
        for (var l : sortResetBtn.getActionListeners()) sortResetBtn.removeActionListener(l);
        for (var l : filterApplyBtn.getActionListeners()) filterApplyBtn.removeActionListener(l);
        for (var l : filterResetBtn.getActionListeners()) filterResetBtn.removeActionListener(l);
        for (var l : showFilteredTgl.getActionListeners()) showFilteredTgl.removeActionListener(l);
        if (exportBtn != null) for (var l : exportBtn.getActionListeners()) exportBtn.removeActionListener(l);

        // ---- Core ops ----
        undoBtn.addActionListener(e -> this.api.undo());
        redoBtn.addActionListener(e -> this.api.redo());

        advanceBtn.addActionListener(e -> {
            int[] ids = safeIds();
            for (int id : ids) this.api.advanceState(id);
        });

        markAsBtn.addActionListener(e -> showMarkPopup());

        // ---- Sorting ----
        sortApplyBtn.addActionListener(e -> {
            String key = Optional.ofNullable((String) sortCombo.getSelectedItem()).orElse("");
            this.api.setSortStrategy(this.sortMapper.apply(key));
        });
        sortResetBtn.addActionListener(e -> {
            sortCombo.setSelectedIndex(0);
            this.api.setSortStrategy(null);
        });

        // ---- Filtering ----
        filterApplyBtn.addActionListener(e -> this.api.setFilter(this.filterSupplier.get()));
        filterResetBtn.addActionListener(e -> {
            cbTodo.setSelected(false);
            cbInProgress.setSelected(false);
            cbCompleted.setSelected(false);
            this.api.clearFilter();
        });
        showFilteredTgl.addActionListener(e -> {
            boolean on = showFilteredTgl.isSelected();
            showFilteredTgl.setOpaque(on);
            showFilteredTgl.setBackground(on ? AppTheme.TB_SHOW_SELECTED_BG : new Color(0, 0, 0, 0));
            showFilteredTgl.setForeground(on ? AppTheme.TB_SHOW_SELECTED_FG : AppTheme.TB_SHOW_FG);
            // Consumers of this toggle should read its state via getter when executing actions.
            // No API call is made here by design.
        });

        // ---- Export ----
        if (exportBtn != null) {
            exportBtn.addActionListener(e -> this.exportHandler.performExport(
                    this.api,
                    showFilteredTgl.isSelected(),
                    safeIds()
            ));
        }
    }

    /** Shows a simple popup to select target TaskState, then calls api.markState for each selected id. */
    private void showMarkPopup() {
        if (api == null) return; // not bound
        JPopupMenu menu = new JPopupMenu();
        for (TaskState s : TaskState.values()) {
            JMenuItem mi = new JMenuItem(s.name());
            mi.addActionListener(e2 -> {
                int[] ids = safeIds();
                for (int id : ids) api.markState(id, s);
            });
            menu.add(mi);
        }
        menu.show(markAsBtn, markAsBtn.getWidth() / 2, markAsBtn.getHeight());
    }

    /** Placeholder export when not bound to API (kept for backwards compatibility). */
    private void onExportPlaceholder() {
        JOptionPane.showMessageDialog(this,
                "Export (placeholder). Use wireTo(...) to connect real export flow.",
                "Export", JOptionPane.INFORMATION_MESSAGE);
    }

    private int[] safeIds() {
        return (idsProvider != null) ? idsProvider.selectedIds() : new int[0];
    }

    // ---------------------------------------------------------------------
    // Styling helpers
    // ---------------------------------------------------------------------

    private static JPanel makeTransparent() {
        JPanel p = new JPanel();
        p.setOpaque(false);
        return p;
    }

    private static JPanel centerInGridBag(JComponent c) {
        JPanel wrap = makeTransparent();
        wrap.setLayout(new GridBagLayout());
        wrap.add(c, new GridBagConstraints());
        return wrap;
    }

    private void styleMiniRound(JButton b, Color bg, Color fg) {
        b.setFocusPainted(false);
        b.setOpaque(true);
        b.setBackground(bg);
        b.setForeground(fg);
        b.setFont(AppTheme.TB_LABEL_FONT_LG);
        b.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(AppTheme.TB_FIELD_BORDER, 1, true),
                BorderFactory.createEmptyBorder(8, 14, 8, 14)
        ));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }

    private void styleIconTextButton(JButton b, Icon icon, Color bg, Color fg) {
        b.setIcon(icon);
        b.setFocusPainted(false);
        b.setOpaque(true);
        b.setBackground(bg);
        b.setForeground(fg);
        b.setFont(AppTheme.TB_LABEL_FONT_LG);
        b.setHorizontalTextPosition(SwingConstants.CENTER);
        b.setVerticalTextPosition(SwingConstants.BOTTOM);
        b.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(AppTheme.TB_FIELD_BORDER, 1, true),
                BorderFactory.createEmptyBorder(8, 16, 8, 16)
        ));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }

    /** Smaller inner padding + rounded border for compact small buttons. */
    private void styleSmallFilled(AbstractButton b, Color bg, Color fg) {
        b.setFocusPainted(false);
        b.setOpaque(true);
        b.setBackground(bg);
        b.setForeground(fg);
        b.setFont(AppTheme.TB_LABEL_FONT_LG);
        b.setBorder(BorderFactory.createCompoundBorder(
                new javax.swing.border.LineBorder(bg.darker(), 1, true), // rounded
                BorderFactory.createEmptyBorder(4, 6, 4, 6)               // tighter padding
        ));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }

    /** Smaller inner padding + rounded border for hollow buttons / toggle. */
    private void styleSmallHollow(AbstractButton b,
                                  Color border, Color fg,
                                  Color selectedBg, Color selectedFg) {
        b.setFocusPainted(false);
        b.setOpaque(false);
        b.setForeground(fg);
        b.setFont(AppTheme.TB_LABEL_FONT_LG);
        b.setBorder(BorderFactory.createCompoundBorder(
                new javax.swing.border.LineBorder(border, 1, true),      // rounded
                BorderFactory.createEmptyBorder(4, 6, 4, 6)               // tighter padding
        ));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.setBackground(new Color(0, 0, 0, 0));
        if (b instanceof JToggleButton tgl) {
            tgl.addItemListener(e -> {
                boolean on = tgl.isSelected();
                tgl.setOpaque(on);
                tgl.setBackground(on ? selectedBg : new Color(0, 0, 0, 0));
                tgl.setForeground(on ? selectedFg : fg);
            });
        }
    }

    /** Ensures a non-null icon by returning a 1×1 transparent fallback. */
    private static Icon safeIcon(Icon icon) {
        if (icon != null) return icon;
        return new Icon() {
            @Override public void paintIcon(Component c, Graphics g, int x, int y) { /* no-op */ }
            @Override public int getIconWidth() { return 1; }
            @Override public int getIconHeight() { return 1; }
        };
    }

    private void styleCheck(JCheckBox cb) {
        cb.setOpaque(false);
        cb.setForeground(AppTheme.TB_TEXT_FG);
        cb.setFocusPainted(false);
        cb.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
        cb.setFont(AppTheme.TB_RADIO_FONT);
    }

    // ---------------------------------------------------------------------
    // Public API (for MVVM wiring)
    // ---------------------------------------------------------------------

    /** Updates the counters text (call from outside when selection/rows change). */
    public void updateCounters(int selected, int total) {
        selectedCountLbl.setText("Selected: " + Math.max(0, selected));
        totalCountLbl.setText("Total: " + Math.max(0, total));
    }

    public JButton getUndoButton() { return undoBtn; }
    public JButton getRedoButton() { return redoBtn; }
    public JButton getAdvanceButton() { return advanceBtn; }
    public JButton getMarkAsButton() { return markAsBtn; }
    public JComboBox<String> getSortCombo() { return sortCombo; }
    public JButton getSortApplyButton() { return sortApplyBtn; }
    public JButton getSortResetButton() { return sortResetBtn; }
    public JCheckBox getCbTodo() { return cbTodo; }
    public JCheckBox getCbInProgress() { return cbInProgress; }
    public JCheckBox getCbCompleted() { return cbCompleted; }
    public JButton getFilterApplyButton() { return filterApplyBtn; }
    public JButton getFilterResetButton() { return filterResetBtn; }
    public JToggleButton getShowFilteredToggle() { return showFilteredTgl; }
    public JLabel getSelectedCountLabel() { return selectedCountLbl; }
    public JLabel getTotalCountLabel() { return totalCountLbl; }
    public JButton getExportButton() { return exportBtn; }


    // ---------------------------------------------------------------------
    // Backward-compatible wiring
    // ---------------------------------------------------------------------

// ---- Add these setters (optional wiring in small steps) ----
    /** Backward-compatible hook for existing code that calls setApi(api). */
    public void setApi(TasksViewAPI api) {
        this.api = Objects.requireNonNull(api, "api");
        // Minimal safe wiring that doesn't assume anything about selection/filter/export.
        // Buttons that don't require extra providers are bound now; others remain as-is
        // until you call the extra setters or wireTo(...).

        // Clear previous listeners for these buttons only
        for (var l : undoBtn.getActionListeners()) undoBtn.removeActionListener(l);
        for (var l : redoBtn.getActionListeners()) redoBtn.removeActionListener(l);
        for (var l : sortResetBtn.getActionListeners()) sortResetBtn.removeActionListener(l);
        for (var l : filterResetBtn.getActionListeners()) filterResetBtn.removeActionListener(l);

        // Bind core ops that don't need extra context
        undoBtn.addActionListener(e -> this.api.undo());
        redoBtn.addActionListener(e -> this.api.redo());

        // Sort reset = clear strategy (safe, no mapper needed)
        sortResetBtn.addActionListener(e -> {
            sortCombo.setSelectedIndex(0);
            this.api.setSortStrategy(null);
        });

        // Filter reset = clear filter (safe, no supplier needed)
        filterResetBtn.addActionListener(e -> {
            cbTodo.setSelected(false);
            cbInProgress.setSelected(false);
            cbCompleted.setSelected(false);
            this.api.clearFilter();
        });

        // Note: advance/mark/sort-apply/filter-apply/export stay with their current listeners
        // until you provide the missing context via the setters below or wireTo(...).
    }

    /** Provide selected IDs when you’re ready (e.g., from table). */
    public void setIdsProvider(IdsProvider idsProvider) {
        this.idsProvider = Objects.requireNonNull(idsProvider, "idsProvider");
    }

    /** Provide a mapper from combo value ("", "Title", "State", "ID") to SortStrategy. */
    public void setSortMapper(Function<String, SortStrategy> sortMapper) {
        this.sortMapper = Objects.requireNonNull(sortMapper, "sortMapper");
        // Rebind sortApply to use mapper
        for (var l : sortApplyBtn.getActionListeners()) sortApplyBtn.removeActionListener(l);
        sortApplyBtn.addActionListener(e -> {
            String key = (String) sortCombo.getSelectedItem();
            this.api.setSortStrategy(this.sortMapper.apply(key == null ? "" : key));
        });
    }

    /** Provide a supplier that builds the current combinator filter. */
    public void setFilterSupplier(Supplier<ITaskFilter> filterSupplier) {
        this.filterSupplier = Objects.requireNonNull(filterSupplier, "filterSupplier");
        // Rebind filterApply to use supplier
        for (var l : filterApplyBtn.getActionListeners()) filterApplyBtn.removeActionListener(l);
        filterApplyBtn.addActionListener(e -> this.api.setFilter(this.filterSupplier.get()));
    }

    /** Provide an export handler that opens your dialog and calls the API. */
    public void setExportHandler(ExportHandler exportHandler) {
        this.exportHandler = Objects.requireNonNull(exportHandler, "exportHandler");
        if (exportBtn != null) {
            for (var l : exportBtn.getActionListeners()) exportBtn.removeActionListener(l);
            exportBtn.addActionListener(e ->
                    this.exportHandler.performExport(this.api, showFilteredTgl.isSelected(), safeIds()));
        }
    }

    // ---------------------------------------------------------------------
    // Small UI-side helper contracts
    // ---------------------------------------------------------------------

    /**
     * Provides selected IDs and counters from the center content.
     * Implement this at the UI layer (e.g., by delegating to your table component).
     */
    @FunctionalInterface
    public interface IdsProvider {
        /** @return currently selected task IDs (empty if none). */
        int[] selectedIds();
        // If you need more later, prefer adding separate setters on ToolBox (e.g., updateCounters).
    }

    /**
     * Handles the export interaction (dialog + API calls). Kept outside the ToolBox to avoid guessing dialog signatures.
     */
    @FunctionalInterface
    public interface ExportHandler {
        /**
         * Performs the export using the given API.
         * @param api UI API
         * @param useFiltered whether the toggle "Show Filtered" is ON
         * @param selectedIds currently selected IDs (may be empty)
         */
        void performExport(TasksViewAPI api, boolean useFiltered, int[] selectedIds);
    }
}
