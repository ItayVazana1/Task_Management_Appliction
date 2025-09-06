package taskmanagement.ui.widgets;

import taskmanagement.ui.styles.AppTheme;
import taskmanagement.ui.util.UiUtils;
import taskmanagement.ui.util.RoundedPanel;

import taskmanagement.domain.ITask;
import taskmanagement.application.viewmodel.events.Property;
import taskmanagement.application.viewmodel.sort.SortStrategy;

import taskmanagement.domain.TaskState;
import taskmanagement.domain.filter.ITaskFilter;
import taskmanagement.ui.api.TasksViewAPI;

import javax.swing.*;
import java.awt.*;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * ToolBox
 * <p>Right-side vertical toolbox split into six sections:</p>
 * <ol>
 *   <li>20% — Undo / Redo</li>
 *   <li>20% — Advance / Mark as…</li>
 *   <li>10% — Sort (combo + Apply / Reset)</li>
 *   <li>30% — Filter (checkboxes) + Apply / Reset / Show Filtered</li>
 *   <li>5%  — Counters: Selected / Total</li>
 *   <li>15% — Export</li>
 * </ol>
 *
 * <h3>Wiring</h3>
 * <ul>
 *   <li>Use {@link #wireTo(TasksViewAPI, IdsProvider, Function, Supplier, ExportHandler, Property)}
 *   to fully bind actions + counters in one shot.</li>
 *   <li>Or use incremental setters: {@link #setApi(TasksViewAPI)}, {@link #setIdsProvider(IdsProvider)},
 *   {@link #setSortMapper(Function)}, {@link #bindSortControls(List)}, {@link #setFilterSupplier(Supplier)},
 *   {@link #setExportHandler(ExportHandler)}, {@link #bindSelectionProperty(Property)}.</li>
 * </ul>
 *
 * <p>View-only: no direct access to Model/DAO; calls are delegated to {@link TasksViewAPI}.</p>
 */
public final class ToolBox extends RoundedPanel {

    private static final int INNER_PAD = 12;
    private static final int FALLBACK_WIDTH = 360;

    // === Section 1: Undo/Redo ===
    private final JButton undoBtn = new JButton("Undo");
    private final JButton redoBtn = new JButton("Redo");

    // === Section 2: Advance / Mark as… ===
    private final JButton advanceBtn = new JButton("Advance");
    private final JButton markAsBtn  = new JButton("Mark as…");

    // === Section 3: Sort By + Apply/Reset ===
    private final JComboBox<String> sortCombo = new JComboBox<>();
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

    // ---- Binding state ----
    private TasksViewAPI api;
    private IdsProvider idsProvider;
    private Function<String, SortStrategy> sortMapper;
    private Supplier<ITaskFilter> filterSupplier;
    private ExportHandler exportHandler;
    private Property<int[]> selectionProp; // for counters

    // Keep listeners to avoid GC (when bound via properties)
    private Property.Listener<List<ITask>> tasksListener;
    private Property.Listener<int[]>       selectionListener;
    private Property.Listener<List<ITask>> filteredListener;

    // Internal name->strategy map for bindSortControls
    private final Map<String, SortStrategy> sortMap = new LinkedHashMap<>();

    /** Constructs the toolbox panel with the 6-section layout. */
    public ToolBox() {
        super(AppTheme.PANEL_BG, AppTheme.TB_CORNER_RADIUS);
        setOpaque(false);

        int fixedWidth = Math.max(FALLBACK_WIDTH, AppTheme.TB_EXPORT_W + INNER_PAD * 2);
        Dimension fixed = new Dimension(fixedWidth, 10);
        setPreferredSize(fixed);
        setMinimumSize(fixed);

        setBorder(BorderFactory.createEmptyBorder(INNER_PAD, INNER_PAD, INNER_PAD, INNER_PAD));

        setLayout(new GridBagLayout());
        GridBagConstraints root = new GridBagConstraints();
        root.gridx = 0;
        root.fill = GridBagConstraints.BOTH;
        root.weightx = 1.0;

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

        wirePlaceholders();
    }

    // ---------- Section builders ----------

    private JPanel buildSection1_UndoRedo() {
        JPanel p = makeTransparent();
        p.setLayout(new GridLayout(1, 2, AppTheme.TB_GAP_SM, 0));

        JPanel left  = centerInGridBag(undoBtn);
        JPanel right = centerInGridBag(redoBtn);

        styleMiniRound(undoBtn, AppTheme.TB_UNDO_BG, AppTheme.TB_UNDO_FG);
        styleMiniRound(redoBtn, AppTheme.TB_REDO_BG, AppTheme.TB_REDO_FG);

        p.add(left);
        p.add(right);
        return p;
    }

    private JPanel buildSection2_AdvanceMark() {
        JPanel p = makeTransparent();
        p.setLayout(new GridLayout(1, 2, AppTheme.TB_GAP_SM, 0));

        Icon advIcon = safeIcon(UiUtils.loadRasterIcon(
                "/taskmanagement/ui/resources/advance.png",
                AppTheme.TB_ACTION_ICON, AppTheme.TB_ACTION_ICON));
        Icon markIcon = safeIcon(UiUtils.loadRasterIcon(
                "/taskmanagement/ui/resources/mark.png",
                AppTheme.TB_ACTION_ICON, AppTheme.TB_ACTION_ICON));

        styleIconTextButton(advanceBtn, advIcon, AppTheme.TB_ADVANCE_BG, AppTheme.TB_ADVANCE_FG);
        styleIconTextButton(markAsBtn,  markIcon, AppTheme.TB_MARK_BG,    AppTheme.TB_MARK_FG);

        p.add(centerInGridBag(advanceBtn));
        p.add(centerInGridBag(markAsBtn));
        return p;
    }

    private JPanel buildSection3_Sort() {
        JPanel p = makeTransparent();
        p.setLayout(new GridBagLayout());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0; gbc.weightx = 1.0; gbc.fill = GridBagConstraints.HORIZONTAL;

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

    private JPanel buildSection4_Filter() {
        JPanel p = makeTransparent();
        p.setLayout(new GridBagLayout());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;

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

        JPanel row = makeTransparent();
        row.setLayout(new GridLayout(1, 3, 2, 0));

        styleSmallFilled(filterApplyBtn, AppTheme.TB_FILTER_APPLY_BG, AppTheme.TB_FILTER_APPLY_FG);
        styleSmallFilled(filterResetBtn, AppTheme.TB_FILTER_RESET_BG, AppTheme.TB_FILTER_RESET_FG);
        styleSmallHollow(showFilteredTgl,
                AppTheme.TB_SHOW_BORDER, AppTheme.TB_SHOW_FG,
                AppTheme.TB_SHOW_SELECTED_BG, AppTheme.TB_SHOW_SELECTED_FG);

        Dimension smallBtn = new Dimension(90, 28);
        filterApplyBtn.setPreferredSize(smallBtn);
        filterResetBtn.setPreferredSize(smallBtn);
        showFilteredTgl.setPreferredSize(smallBtn);

        row.add(filterApplyBtn);
        row.add(filterResetBtn);
        row.add(showFilteredTgl);

        gbc.gridy = 1; gbc.weighty = 0.3;
        p.add(row, gbc);

        return p;
    }

    private JPanel buildSection5_Counters() {
        JPanel p = makeTransparent();
        p.setLayout(new GridLayout(1, 2, AppTheme.TB_GAP_SM, 0));

        selectedCountLbl.setForeground(AppTheme.TB_TEXT_FG);
        totalCountLbl.setForeground(AppTheme.TB_TEXT_FG);

        p.add(centerInGridBag(selectedCountLbl));
        p.add(centerInGridBag(totalCountLbl));
        return p;
    }

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

    // ---------- Wiring ----------

    private void wirePlaceholders() {
        // keep placeholders for Undo/Redo + Advance/Mark
        undoBtn.addActionListener(e -> JOptionPane.showMessageDialog(this,"Undo (placeholder)","Undo",JOptionPane.INFORMATION_MESSAGE));
        redoBtn.addActionListener(e -> JOptionPane.showMessageDialog(this,"Redo (placeholder)","Redo",JOptionPane.INFORMATION_MESSAGE));
        advanceBtn.addActionListener(e -> JOptionPane.showMessageDialog(this,"Advance (placeholder)","Advance",JOptionPane.INFORMATION_MESSAGE));
        markAsBtn.addActionListener(e -> JOptionPane.showMessageDialog(this,"Mark as… (placeholder)","Mark",JOptionPane.INFORMATION_MESSAGE));

        // IMPORTANT: no placeholder wiring for Sort Apply/Reset — real wiring happens in setSortMapper()

        // Filter placeholders (real wiring provided via setFilterSupplier)
        filterApplyBtn.addActionListener(e -> JOptionPane.showMessageDialog(this,"Filter Apply (placeholder)","Filter",JOptionPane.INFORMATION_MESSAGE));
        filterResetBtn.addActionListener(e -> JOptionPane.showMessageDialog(this,"Filter Reset (placeholder)","Filter",JOptionPane.INFORMATION_MESSAGE));
        showFilteredTgl.addActionListener(e -> updateTotalsFromApi()); // safe: updates counter only

        if (exportBtn != null) {
            exportBtn.addActionListener(e ->
                    JOptionPane.showMessageDialog(this,"Export (placeholder). Use wireTo(...) to connect.","Export",JOptionPane.INFORMATION_MESSAGE));
        }
    }

    /** Full binding (actions + counters) in one call. */
    public void wireTo(TasksViewAPI api,
                       IdsProvider idsProvider,
                       Function<String, SortStrategy> sortMapper,
                       Supplier<ITaskFilter> filterSupplier,
                       ExportHandler exportHandler,
                       Property<int[]> selectionProperty) {

        setApi(api);
        setIdsProvider(idsProvider);
        setSortMapper(sortMapper);
        setFilterSupplier(filterSupplier);
        setExportHandler(exportHandler);
        bindSelectionProperty(selectionProperty);
        bindTotalsFromApi();
        bindAdvanceAndMarkDialogs();
    }

    /**
     * Minimal safe wiring for existing code that calls setApi(api) only.
     * Binds Undo/Redo; Sort Reset will be bound when sortMapper is provided.
     */
    public void setApi(TasksViewAPI api) {
        this.api = Objects.requireNonNull(api, "api");

        // Clear & bind only safe ops
        for (var l : undoBtn.getActionListeners()) undoBtn.removeActionListener(l);
        for (var l : redoBtn.getActionListeners()) redoBtn.removeActionListener(l);
        for (var l : filterResetBtn.getActionListeners()) filterResetBtn.removeActionListener(l);

        undoBtn.addActionListener(e -> this.api.undo());
        redoBtn.addActionListener(e -> this.api.redo());

        // Filter reset stays here
        filterResetBtn.addActionListener(e -> {
            cbTodo.setSelected(false);
            cbInProgress.setSelected(false);
            cbCompleted.setSelected(false);
            this.api.clearFilter();
            updateTotalsFromApi();
        });
    }

    public void setIdsProvider(IdsProvider idsProvider) {
        this.idsProvider = Objects.requireNonNull(idsProvider, "idsProvider");
        updateSelectionCount(); // initial
        enableActionButtons();
    }

    /** Convenience binder: populate combo from strategies and wire Apply/Reset to the VM API. */
    public void bindSortControls(List<SortStrategy> strategies) {
        Objects.requireNonNull(strategies, "strategies");
        sortMap.clear();

        DefaultComboBoxModel<String> model = new DefaultComboBoxModel<>();
        for (SortStrategy s : strategies) {
            if (s == null) continue;
            String name = Optional.ofNullable(s.displayName()).orElse(s.getClass().getSimpleName());
            sortMap.put(name, s);
        }
        for (String name : sortMap.keySet()) {
            model.addElement(name);
        }
        sortCombo.setModel(model);
        sortCombo.setSelectedIndex(0);

        setSortMapper(key -> sortMap.get(key));
    }

    public void setSortMapper(Function<String, SortStrategy> sortMapper) {
        this.sortMapper = Objects.requireNonNull(sortMapper, "sortMapper");

        // Apply (real wiring)
        for (var l : sortApplyBtn.getActionListeners()) sortApplyBtn.removeActionListener(l);
        sortApplyBtn.addActionListener(e -> {
            String key = Optional.ofNullable((String) sortCombo.getSelectedItem()).orElse("");
            this.api.setSortStrategy(this.sortMapper.apply(key));
        });

        // Reset (returns to first strategy, e.g., "ID")
        for (var l : sortResetBtn.getActionListeners()) sortResetBtn.removeActionListener(l);
        sortResetBtn.addActionListener(e -> {
            sortCombo.setSelectedIndex(0);
            String firstKey = (String) sortCombo.getItemAt(0);
            this.api.setSortStrategy(this.sortMapper.apply(firstKey));
        });
    }

    public void setFilterSupplier(Supplier<ITaskFilter> filterSupplier) {
        this.filterSupplier = Objects.requireNonNull(filterSupplier, "filterSupplier");
        for (var l : filterApplyBtn.getActionListeners()) filterApplyBtn.removeActionListener(l);
        filterApplyBtn.addActionListener(e -> {
            this.api.setFilter(this.filterSupplier.get());
            updateTotalsFromApi();
        });
    }

    public void setExportHandler(ExportHandler exportHandler) {
        this.exportHandler = Objects.requireNonNull(exportHandler, "exportHandler");
        if (exportBtn != null) {
            for (var l : exportBtn.getActionListeners()) exportBtn.removeActionListener(l);
            exportBtn.addActionListener(e ->
                    this.exportHandler.performExport(this.api, showFilteredTgl.isSelected(), safeIds()));
        }
    }

    /** Binds the selection property for counters and button enablement. */
    public void bindSelectionProperty(Property<int[]> selectionProperty) {
        this.selectionProp = Objects.requireNonNull(selectionProperty, "selectionProperty");

        if (selectionListener != null) {
            this.selectionProp.removeListener(selectionListener);
        }
        selectionListener = (oldV, newV) -> {
            updateSelectionCount();
            enableActionButtons();
        };
        this.selectionProp.addListener(selectionListener);

        updateSelectionCount();
        enableActionButtons();
    }

    /** Subscribes to tasks/filtered properties to keep the Total counter fresh. */
    public void bindTotalsFromApi() {
        if (api == null) return;

        if (tasksListener != null)    api.tasksProperty().removeListener(tasksListener);
        if (filteredListener != null) api.filteredTasksProperty().removeListener(filteredListener);

        tasksListener    = (oldList, newList) -> updateTotalsFromApi();
        filteredListener = (oldList, newList) -> updateTotalsFromApi();

        api.tasksProperty().addListener(tasksListener);
        api.filteredTasksProperty().addListener(filteredListener);

        showFilteredTgl.addActionListener(e -> updateTotalsFromApi());
        updateTotalsFromApi();
    }

    /** Replace placeholders for Advance/Mark with real actions + dialogs. */
    public void bindAdvanceAndMarkDialogs() {
        for (var l : advanceBtn.getActionListeners()) advanceBtn.removeActionListener(l);
        for (var l : markAsBtn.getActionListeners()) markAsBtn.removeActionListener(l);

        advanceBtn.addActionListener(e -> onAdvance());
        markAsBtn.addActionListener(e -> onMarkAsDialog());
        enableActionButtons();
    }

    // ---------- Actions ----------

    private void onAdvance() {
        int[] ids = safeIds();
        if (ids.length == 0) {
            JOptionPane.showMessageDialog(this, "No tasks selected.", "Advance", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int rc = JOptionPane.showConfirmDialog(
                this,
                "Advance " + ids.length + " selected task(s) to the next state?",
                "Confirm Advance",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.QUESTION_MESSAGE
        );
        if (rc != JOptionPane.OK_OPTION) return;

        for (int id : ids) api.advanceState(id);
    }

    private void onMarkAsDialog() {
        int[] ids = safeIds();
        if (ids.length == 0) {
            JOptionPane.showMessageDialog(this, "No tasks selected.", "Mark as…", JOptionPane.WARNING_MESSAGE);
            return;
        }

        final JDialog dlg = new JDialog(SwingUtilities.getWindowAncestor(this), "Mark as…", Dialog.ModalityType.APPLICATION_MODAL);
        dlg.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        JPanel content = new JPanel(new GridBagLayout());
        content.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        content.setBackground(getBackground());

        ButtonGroup bg = new ButtonGroup();
        JRadioButton rbTodo = new JRadioButton("To-Do");
        JRadioButton rbInPr = new JRadioButton("In-Progress");
        JRadioButton rbDone = new JRadioButton("Completed");
        rbTodo.setOpaque(false); rbInPr.setOpaque(false); rbDone.setOpaque(false);
        rbTodo.setSelected(true);
        bg.add(rbTodo); bg.add(rbInPr); bg.add(rbDone);

        JButton ok = new JButton("OK");
        JButton cancel = new JButton("Cancel");

        GridBagConstraints g = new GridBagConstraints();
        g.gridx = 0; g.gridy = 0; g.anchor = GridBagConstraints.WEST; g.insets = new Insets(4,4,4,4);
        content.add(new JLabel("Set state for " + ids.length + " selected task(s):"), g);
        g.gridy++; content.add(rbTodo, g);
        g.gridy++; content.add(rbInPr, g);
        g.gridy++; content.add(rbDone, g);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        buttons.setOpaque(false);
        buttons.add(ok); buttons.add(cancel);
        g.gridy++; g.anchor = GridBagConstraints.EAST; g.fill = GridBagConstraints.HORIZONTAL; g.weightx = 1.0;
        content.add(buttons, g);

        ok.addActionListener(ev -> {
            TaskState target = rbTodo.isSelected() ? TaskState.ToDo :
                    rbInPr.isSelected() ? TaskState.InProgress : TaskState.Completed;

            boolean warnedBackward = false;

            for (int id : ids) {
                TaskState current = findCurrentState(id);
                if (current == null) continue;

                int curIdx = stateIndex(current);
                int tgtIdx = stateIndex(target);

                if (tgtIdx == curIdx) {
                    continue;
                } else if (tgtIdx > curIdx) {
                    for (int step = curIdx; step < tgtIdx; step++) {
                        try {
                            api.advanceState(id);
                        } catch (RuntimeException ex) {
                            JOptionPane.showMessageDialog(this,
                                    "Failed to advance task #" + id + ": " + ex.getMessage(),
                                    "Mark as…", JOptionPane.ERROR_MESSAGE);
                            break;
                        }
                    }
                } else {
                    if (!warnedBackward) {
                        JOptionPane.showMessageDialog(this,
                                "Backward transitions are not supported by the current workflow.\n" +
                                        "Requested: " + current + " → " + target,
                                "Mark as…", JOptionPane.WARNING_MESSAGE);
                        warnedBackward = true;
                    }
                }
            }

            dlg.dispose();
        });
        cancel.addActionListener(ev -> dlg.dispose());

        dlg.setContentPane(content);
        dlg.pack();
        dlg.setLocationRelativeTo(this);
        dlg.setVisible(true);
    }

    // ---------- Counters ----------

    private void updateSelectionCount() {
        int sel = 0;
        if (selectionProp != null && selectionProp.getValue() != null) {
            sel = selectionProp.getValue().length;
        } else if (idsProvider != null) {
            sel = safeIds().length;
        }
        selectedCountLbl.setText("Selected: " + sel);
    }

    private void updateTotalsFromApi() {
        if (api == null) return;
        List<?> list = showFilteredTgl.isSelected()
                ? api.filteredTasksProperty().getValue()
                : api.tasksProperty().getValue();
        int total = (list == null) ? 0 : list.size();
        totalCountLbl.setText("Total: " + total);
    }

    private void enableActionButtons() {
        boolean hasSel = false;
        if (selectionProp != null && selectionProp.getValue() != null) {
            hasSel = selectionProp.getValue().length > 0;
        } else if (idsProvider != null) {
            hasSel = safeIds().length > 0;
        }
        advanceBtn.setEnabled(hasSel);
        markAsBtn.setEnabled(hasSel);
    }

    private int[] safeIds() {
        return (idsProvider != null) ? idsProvider.selectedIds() : new int[0];
    }

    // ---------- Styling helpers ----------

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

    private void styleSmallFilled(AbstractButton b, Color bg, Color fg) {
        b.setFocusPainted(false);
        b.setOpaque(true);
        b.setBackground(bg);
        b.setForeground(fg);
        b.setFont(AppTheme.TB_LABEL_FONT_LG);
        b.setBorder(BorderFactory.createCompoundBorder(
                new javax.swing.border.LineBorder(bg.darker(), 1, true),
                BorderFactory.createEmptyBorder(4, 6, 4, 6)
        ));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }

    private void styleSmallHollow(AbstractButton b,
                                  Color border, Color fg,
                                  Color selectedBg, Color selectedFg) {
        b.setFocusPainted(false);
        b.setOpaque(false);
        b.setForeground(fg);
        b.setFont(AppTheme.TB_LABEL_FONT_LG);
        b.setBorder(BorderFactory.createCompoundBorder(
                new javax.swing.border.LineBorder(border, 1, true),
                BorderFactory.createEmptyBorder(4, 6, 4, 6)
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

    // ---------- Public API (for MVVM wiring) ----------

    /** Updates counters text (you may call manually, but normally auto-bound). */
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

    // ---------- Helper contracts ----------

    @FunctionalInterface
    public interface IdsProvider {
        /** @return currently selected task IDs (empty if none). */
        int[] selectedIds();
    }

    @FunctionalInterface
    public interface ExportHandler {
        void performExport(TasksViewAPI api, boolean useFiltered, int[] selectedIds);
    }

    /** Returns the current TaskState of the task with the given id by checking the preferred list. */
    private TaskState findCurrentState(int id) {
        if (api == null) return null;
        java.util.List<ITask> list = getPreferredList();
        if (list != null) {
            for (ITask t : list) if (t.getId() == id) return t.getState();
        }
        list = showFilteredTgl.isSelected()
                ? api.tasksProperty().getValue()
                : api.filteredTasksProperty().getValue();
        if (list != null) {
            for (ITask t : list) if (t.getId() == id) return t.getState();
        }
        return null;
    }

    private java.util.List<ITask> getPreferredList() {
        return showFilteredTgl.isSelected()
                ? api.filteredTasksProperty().getValue()
                : api.tasksProperty().getValue();
    }

    /** Defines the forward order ToDo (0) → InProgress (1) → Completed (2). */
    private static int stateIndex(TaskState s) {
        return switch (s) {
            case ToDo -> 0;
            case InProgress -> 1;
            case Completed -> 2;
        };
    }
}
