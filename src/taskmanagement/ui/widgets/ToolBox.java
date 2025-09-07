package taskmanagement.ui.widgets;

import taskmanagement.ui.styles.AppTheme;
import taskmanagement.ui.util.UiUtils;
import taskmanagement.ui.util.RoundedPanel;

import taskmanagement.domain.ITask;
import taskmanagement.application.viewmodel.events.Property;
import taskmanagement.application.viewmodel.sort.SortStrategy;

import taskmanagement.domain.TaskState;
import taskmanagement.domain.filter.ITaskFilter;
import taskmanagement.domain.filter.Filters;
import taskmanagement.ui.api.TasksViewAPI;

// ===== Export wiring =====
import taskmanagement.ui.dialogs.ExportDialog;
import taskmanagement.application.viewmodel.ExportFormat;

import javax.swing.*;
import java.awt.*;
import java.nio.file.Path;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.IntStream;

/**
 * A right-side toolbox composed of six sections (Undo/Redo, Advance/Mark, Sort, Filter, Counters, Export).
 * <p>
 * This view delegates user actions to {@link TasksViewAPI} (MVVM). It exposes wiring helpers to bind
 * selection, sorting strategies, filters (Combinator), and exporting behavior.
 * </p>
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

    // === Section 4: Filter (title + checkboxes) + Apply / Reset / Show Filtered ===
    private final JTextField titleField = new JTextField();
    private final JCheckBox cbTodo       = new JCheckBox("To-Do");
    private final JCheckBox cbInProgress = new JCheckBox("In-Progress");
    private final JCheckBox cbCompleted  = new JCheckBox("Completed");
    private final JButton       filterApplyBtn  = new JButton("Apply");
    private final JButton       filterResetBtn  = new JButton("Reset");
    private final JToggleButton showFilteredTgl = new JToggleButton("Count filtered as total");

    // === Section 5: Counters (Selected / Total) ===
    private final JLabel selectedCountLbl = new JLabel("Selected: 0", SwingConstants.CENTER);
    private final JLabel totalCountLbl    = new JLabel("Total: 0", SwingConstants.CENTER);

    // === Section 6: Export ===
    private JButton exportBtn;

    // ---- Binding state ----
    private TasksViewAPI api;
    private IdsProvider idsProvider;
    private Function<String, SortStrategy> sortMapper;
    private Supplier<ITaskFilter> filterSupplier;
    private ExportHandler exportHandler;
    private Property<int[]> selectionProp;

    // Keep listeners to avoid GC (when bound via properties)
    private Property.Listener<List<ITask>> tasksListener;
    private Property.Listener<int[]>       selectionListener;
    private Property.Listener<List<ITask>> filteredListener;

    // Internal name->strategy map for bindSortControls
    private final Map<String, SortStrategy> sortMap = new LinkedHashMap<>();

    /**
     * Creates the toolbox panel with all six sections and placeholder wiring.
     */
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

        // Section layout proportions.
        root.gridy = 0; root.weighty = 0.20;
        add(buildSection1_UndoRedo(), root);

        root.gridy = 1; root.weighty = 0.20;
        add(buildSection2_AdvanceMark(), root);

        root.gridy = 2; root.weighty = 0.10;
        add(buildSection3_Sort(), root);

        root.gridy = 3; root.weighty = 0.30;
        add(buildSection4_Filter(), root);

        root.gridy = 4; root.weighty = 0.05;
        add(buildSection5_Counters(), root);

        root.gridy = 5; root.weighty = 0.15;
        add(buildSection6_Export(), root);

        wirePlaceholders();
    }

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
        t.insets = new Insets(AppTheme.TB_PAD, AppTheme.TB_PAD, AppTheme.TB_PAD / 2, AppTheme.TB_PAD);
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
        gbc.gridx = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;

        JPanel search = makeTransparent();
        search.setLayout(new GridBagLayout());
        JLabel titleLbl = new JLabel("Title contains");
        titleLbl.setForeground(AppTheme.TB_TEXT_FG);
        titleField.setPreferredSize(new Dimension(AppTheme.TB_FIELD_WIDTH, AppTheme.TB_FIELD_HEIGHT));

        GridBagConstraints s = new GridBagConstraints();
        s.insets = new Insets(AppTheme.TB_PAD, AppTheme.TB_PAD, AppTheme.TB_PAD / 2, AppTheme.TB_PAD);
        s.gridx = 0; s.gridy = 0; s.anchor = GridBagConstraints.WEST;
        search.add(titleLbl, s);
        s.gridx = 1; s.gridy = 0; s.weightx = 1.0; s.fill = GridBagConstraints.HORIZONTAL;
        search.add(titleField, s);

        gbc.gridy = 0;
        gbc.weighty = 0.15;
        p.add(search, gbc);

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

        gbc.gridy = 1;
        gbc.weighty = 0.55;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        p.add(list, gbc);

        JPanel buttonsArea = makeTransparent();
        buttonsArea.setLayout(new GridBagLayout());

        GridBagConstraints bb = new GridBagConstraints();
        bb.gridx = 0; bb.fill = GridBagConstraints.HORIZONTAL; bb.weightx = 1.0;

        JPanel topRow = makeTransparent();
        topRow.setLayout(new GridLayout(1, 2, AppTheme.TB_GAP_SM, 0));
        styleSmallFilled(filterApplyBtn, AppTheme.TB_FILTER_APPLY_BG, AppTheme.TB_FILTER_APPLY_FG);
        styleSmallFilled(filterResetBtn, AppTheme.TB_FILTER_RESET_BG, AppTheme.TB_FILTER_RESET_FG);

        Dimension smallBtn = new Dimension(90, 28);
        filterApplyBtn.setPreferredSize(smallBtn);
        filterResetBtn.setPreferredSize(smallBtn);

        topRow.add(filterApplyBtn);
        topRow.add(filterResetBtn);

        bb.gridy = 0;
        bb.weighty = 0.5;
        buttonsArea.add(topRow, bb);

        JPanel bottomRow = makeTransparent();
        bottomRow.setLayout(new GridBagLayout());

        styleSmallHollow(
                showFilteredTgl,
                AppTheme.TB_SHOW_BORDER,
                AppTheme.TB_SHOW_FG,
                AppTheme.TB_SHOW_SELECTED_BG,
                AppTheme.TB_SHOW_SELECTED_FG
        );
        showFilteredTgl.setText("Count filtered as total \u25BE");
        showFilteredTgl.setHorizontalAlignment(SwingConstants.CENTER);

        GridBagConstraints bt = new GridBagConstraints();
        bt.gridx = 0; bt.gridy = 0;
        bt.insets = new Insets(AppTheme.TB_PAD / 2, AppTheme.TB_PAD, AppTheme.TB_PAD / 2, AppTheme.TB_PAD);
        bt.fill = GridBagConstraints.HORIZONTAL;
        bt.weightx = 1.0;
        bottomRow.add(showFilteredTgl, bt);

        bb.gridy = 1;
        bb.weighty = 0.5;
        buttonsArea.add(bottomRow, bb);

        gbc.gridy = 2;
        gbc.weighty = 0.30;
        gbc.fill = GridBagConstraints.BOTH;
        p.add(buttonsArea, gbc);

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

    private void wirePlaceholders() {
        // Default placeholder actions until MVVM wiring is provided.
        undoBtn.addActionListener(e -> JOptionPane.showMessageDialog(this,"Undo (placeholder)","Undo",JOptionPane.INFORMATION_MESSAGE));
        redoBtn.addActionListener(e -> JOptionPane.showMessageDialog(this,"Redo (placeholder)","Redo",JOptionPane.INFORMATION_MESSAGE));
        advanceBtn.addActionListener(e -> JOptionPane.showMessageDialog(this,"Advance (placeholder)","Advance",JOptionPane.INFORMATION_MESSAGE));
        markAsBtn.addActionListener(e -> JOptionPane.showMessageDialog(this,"Mark as… (placeholder)","Mark",JOptionPane.INFORMATION_MESSAGE));

        filterApplyBtn.addActionListener(e -> JOptionPane.showMessageDialog(this,"Filter Apply (placeholder)","Filter",JOptionPane.INFORMATION_MESSAGE));
        filterResetBtn.addActionListener(e -> JOptionPane.showMessageDialog(this,"Filter Reset (placeholder)","Filter",JOptionPane.INFORMATION_MESSAGE));
        showFilteredTgl.addActionListener(e -> updateTotalsFromApi());

        if (exportBtn != null) {
            for (var l : exportBtn.getActionListeners()) exportBtn.removeActionListener(l);
            exportBtn.addActionListener(e -> openExportDialogAndRun());
        }
    }

    /**
     * Full binding helper (actions, counters, filters, export).
     *
     * @param api               the UI-facing API
     * @param idsProvider       provider for selected task IDs
     * @param sortMapper        mapping from combo text to a {@link SortStrategy}
     * @param filterSupplier    supplier for composed {@link ITaskFilter}
     * @param exportHandler     handler that performs export
     * @param selectionProperty observable selection property
     */
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
     * Binds the API and wires safe default actions (undo/redo and filter reset).
     *
     * @param api the {@link TasksViewAPI} to use
     * @throws NullPointerException if api is null
     */
    public void setApi(TasksViewAPI api) {
        this.api = Objects.requireNonNull(api, "api");

        for (var l : undoBtn.getActionListeners()) undoBtn.removeActionListener(l);
        for (var l : redoBtn.getActionListeners()) redoBtn.removeActionListener(l);
        for (var l : filterResetBtn.getActionListeners()) filterResetBtn.removeActionListener(l);

        undoBtn.addActionListener(e -> this.api.undo());
        redoBtn.addActionListener(e -> this.api.redo());

        filterResetBtn.addActionListener(e -> {
            clearFilterUI();
            this.api.clearFilter();
            updateTotalsFromApi();
        });

        if (exportHandler == null && exportBtn != null) {
            for (var l : exportBtn.getActionListeners()) exportBtn.removeActionListener(l);
            exportBtn.addActionListener(e -> openExportDialogAndRun());
        }
    }

    /**
     * Sets the provider used to obtain currently selected task IDs.
     *
     * @param idsProvider provider returning selected IDs
     * @throws NullPointerException if idsProvider is null
     */
    public void setIdsProvider(IdsProvider idsProvider) {
        this.idsProvider = Objects.requireNonNull(idsProvider, "idsProvider");
        updateSelectionCount();
        enableActionButtons();
    }

    /**
     * Populates the sort combo and wires Apply/Reset using the first entry as the default.
     *
     * @param strategies strategies to expose
     * @throws NullPointerException if strategies is null
     */
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

    /**
     * Sets a mapper from combo text to {@link SortStrategy} and wires the buttons.
     *
     * @param sortMapper mapping function
     * @throws NullPointerException if sortMapper is null
     */
    public void setSortMapper(Function<String, SortStrategy> sortMapper) {
        this.sortMapper = Objects.requireNonNull(sortMapper, "sortMapper");

        for (var l : sortApplyBtn.getActionListeners()) sortApplyBtn.removeActionListener(l);
        sortApplyBtn.addActionListener(e -> {
            String key = Optional.ofNullable((String) sortCombo.getSelectedItem()).orElse("");
            this.api.setSortStrategy(this.sortMapper.apply(key));
        });

        for (var l : sortResetBtn.getActionListeners()) sortResetBtn.removeActionListener(l);
        sortResetBtn.addActionListener(e -> {
            sortCombo.setSelectedIndex(0);
            String firstKey = (String) sortCombo.getItemAt(0);
            this.api.setSortStrategy(this.sortMapper.apply(firstKey));
        });
    }

    /**
     * Sets the supplier used to produce a composed {@link ITaskFilter} for Apply.
     *
     * @param filterSupplier filter supplier
     * @throws NullPointerException if filterSupplier is null
     */
    public void setFilterSupplier(Supplier<ITaskFilter> filterSupplier) {
        this.filterSupplier = Objects.requireNonNull(filterSupplier, "filterSupplier");
        for (var l : filterApplyBtn.getActionListeners()) filterApplyBtn.removeActionListener(l);
        filterApplyBtn.addActionListener(e -> {
            this.api.setFilter(this.filterSupplier.get());
            updateTotalsFromApi();
        });
    }

    /**
     * Binds the Filter UI directly to the API (Apply/Reset/Toggle).
     *
     * @param api the {@link TasksViewAPI} to bind
     * @throws NullPointerException if api is null
     */
    public void bindFilterControls(TasksViewAPI api) {
        this.api = Objects.requireNonNull(api, "api");

        for (var l : filterApplyBtn.getActionListeners()) filterApplyBtn.removeActionListener(l);
        for (var l : filterResetBtn.getActionListeners()) filterResetBtn.removeActionListener(l);
        for (var l : showFilteredTgl.getActionListeners()) showFilteredTgl.removeActionListener(l);

        filterApplyBtn.addActionListener(e -> {
            this.api.setFilter(buildFilterFromUI());
            updateTotalsFromApi();
        });

        filterResetBtn.addActionListener(e -> {
            clearFilterUI();
            this.api.clearFilter();
            updateTotalsFromApi();
        });

        showFilteredTgl.addActionListener(e -> updateTotalsFromApi());

        updateTotalsFromApi();
    }

    private ITaskFilter buildFilterFromUI() {
        ITaskFilter f = Filters.all();

        String q = titleField.getText();
        if (q != null && !q.isBlank()) {
            f = f.and(Filters.titleContains(q.trim()));
        }

        final EnumSet<TaskState> states = EnumSet.noneOf(TaskState.class);
        if (cbTodo.isSelected())       states.add(TaskState.ToDo);
        if (cbInProgress.isSelected()) states.add(TaskState.InProgress);
        if (cbCompleted.isSelected())  states.add(TaskState.Completed);

        if (!states.isEmpty()) {
            ITaskFilter statesFilter = t -> t != null && t.getState() != null && states.contains(t.getState());
            f = f.and(statesFilter);
        }

        return f;
    }

    private void clearFilterUI() {
        titleField.setText("");
        cbTodo.setSelected(false);
        cbInProgress.setSelected(false);
        cbCompleted.setSelected(false);
    }

    /**
     * Sets a custom export handler. If not set, a dialog-based export is used.
     *
     * @param exportHandler handler to execute export
     * @throws NullPointerException if exportHandler is null
     */
    public void setExportHandler(ExportHandler exportHandler) {
        this.exportHandler = Objects.requireNonNull(exportHandler, "exportHandler");
        if (exportBtn != null) {
            for (var l : exportBtn.getActionListeners()) exportBtn.removeActionListener(l);
            exportBtn.addActionListener(e ->
                    this.exportHandler.performExport(this.api, showFilteredTgl.isSelected(), toIdList(safeIds())));
        }
    }

    /**
     * Binds selection property for counters and enables/disables actions accordingly.
     *
     * @param selectionProperty observable selection property (IDs)
     * @throws NullPointerException if selectionProperty is null
     */
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

    /**
     * Subscribes to tasks and filtered-tasks to keep the Total counter in sync.
     * Safe to call multiple times; replaces previous listeners.
     */
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

    /**
     * Replaces placeholders for Advance/Mark-as with real dialogs and VM calls.
     */
    public void bindAdvanceAndMarkDialogs() {
        for (var l : advanceBtn.getActionListeners()) advanceBtn.removeActionListener(l);
        for (var l : markAsBtn.getActionListeners()) markAsBtn.removeActionListener(l);

        advanceBtn.addActionListener(e -> onAdvance());
        markAsBtn.addActionListener(e -> onMarkAsDialog());
        enableActionButtons();
    }

    private void openExportDialogAndRun() {
        if (api == null) {
            JOptionPane.showMessageDialog(this,
                    "Export is not available: API is not wired.",
                    "Export", JOptionPane.WARNING_MESSAGE);
            return;
        }

        ExportDialog.showDialog(this).ifPresent(res -> {
            Path path = res.file().toPath();
            ExportFormat fmt = res.format();
            boolean onlyFiltered = showFilteredTgl.isSelected();

            try {
                List<Integer> ids = toIdList(safeIds());
                api.exportTasks(path, fmt, onlyFiltered, ids);
                JOptionPane.showMessageDialog(this,
                        "Export completed:\n" + path,
                        "Export", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this,
                        "Export failed:\n" + ex.getMessage(),
                        "Export", JOptionPane.ERROR_MESSAGE);
            }
        });
    }

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

    private static List<Integer> toIdList(int[] ids) {
        return (ids == null || ids.length == 0)
                ? java.util.List.of()
                : IntStream.of(ids).boxed().toList();
    }

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
            @Override public void paintIcon(Component c, Graphics g, int x, int y) { }
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

    /**
     * Updates counters text. Typically invoked automatically via bindings.
     *
     * @param selected number of selected tasks (non-negative)
     * @param total    total number of tasks (non-negative)
     */
    public void updateCounters(int selected, int total) {
        selectedCountLbl.setText("Selected: " + Math.max(0, selected));
        totalCountLbl.setText("Total: " + Math.max(0, total));
    }

    /** @return the Undo button component. */
    public JButton getUndoButton() { return undoBtn; }
    /** @return the Redo button component. */
    public JButton getRedoButton() { return redoBtn; }
    /** @return the Advance button component. */
    public JButton getAdvanceButton() { return advanceBtn; }
    /** @return the Mark-as button component. */
    public JButton getMarkAsButton() { return markAsBtn; }
    /** @return the sort combo box. */
    public JComboBox<String> getSortCombo() { return sortCombo; }
    /** @return the Sort Apply button. */
    public JButton getSortApplyButton() { return sortApplyBtn; }
    /** @return the Sort Reset button. */
    public JButton getSortResetButton() { return sortResetBtn; }
    /** @return the To-Do checkbox. */
    public JCheckBox getCbTodo() { return cbTodo; }
    /** @return the In-Progress checkbox. */
    public JCheckBox getCbInProgress() { return cbInProgress; }
    /** @return the Completed checkbox. */
    public JCheckBox getCbCompleted() { return cbCompleted; }
    /** @return the Filter Apply button. */
    public JButton getFilterApplyButton() { return filterApplyBtn; }
    /** @return the Filter Reset button. */
    public JButton getFilterResetButton() { return filterResetBtn; }
    /** @return the toggle that counts filtered as total. */
    public JToggleButton getShowFilteredToggle() { return showFilteredTgl; }
    /** @return the Selected counter label. */
    public JLabel getSelectedCountLabel() { return selectedCountLbl; }
    /** @return the Total counter label. */
    public JLabel getTotalCountLabel() { return totalCountLbl; }
    /** @return the Export button. */
    public JButton getExportButton() { return exportBtn; }

    /**
     * Supplies currently selected task IDs.
     */
    @FunctionalInterface
    public interface IdsProvider {
        /**
         * @return selected task IDs (empty if none)
         */
        int[] selectedIds();
    }

    /**
     * Handles exporting tasks according to user choices.
     */
    @FunctionalInterface
    public interface ExportHandler {
        /**
         * Performs the export operation.
         *
         * @param api          bound {@link TasksViewAPI}
         * @param useFiltered  whether to export filtered list
         * @param selectedIds  selected task IDs to include (may be empty)
         */
        void performExport(TasksViewAPI api, boolean useFiltered, List<Integer> selectedIds);
    }

    private TaskState findCurrentState(int id) {
        if (api == null) return null;
        java.util.List<ITask> list = getPreferredList();
        if (list != null) {
            for (ITask t : list) if (t.getId() == id) return t.getState();
        }
        list = showFilteredTgl.isSelected()
                ? api.filteredTasksProperty().getValue()
                : api.tasksProperty().getValue();
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

    private static int stateIndex(TaskState s) {
        return switch (s) {
            case ToDo -> 0;
            case InProgress -> 1;
            case Completed -> 2;
        };
    }
}
