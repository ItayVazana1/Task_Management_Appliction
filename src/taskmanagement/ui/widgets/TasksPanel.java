package taskmanagement.ui.widgets;

import taskmanagement.application.viewmodel.events.Property; // strong-typed listener
import taskmanagement.domain.ITask;
import taskmanagement.ui.api.TasksViewAPI;
import taskmanagement.ui.util.RoundedPanel;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * TasksPanel
 * ----------
 * Sticky header + scrollable list of tasks bound to {@link TasksViewAPI}.
 *
 * Rows are built from either filtered or full list (auto-picked).
 * We subscribe to BOTH properties to avoid missed updates when ViewModel
 * only notifies one side (filtered/all) or reuses list instances.
 *
 * <p>Selection:
 * Each row has a checkbox that toggles the task selection. The current
 * selection is exposed via {@link #selectedIdsProperty()} for observers
 * (e.g., ToolBox counters and actions) and via the compatibility getter
 * {@link #getSelectedIds()} used by existing wiring.</p>
 */
public final class TasksPanel extends JPanel {

    // ---------- preview clipping ----------
    private static final int TITLE_PREVIEW_MAX = 24;

    // ---------- typography ----------
    private static final float FONT_SIZE_BASE   = 11f;
    private static final float HEADER_FONT_SIZE = 10f;
    private static final float FONT_SIZE_PILL   = 11f;
    private static final float FONT_SIZE_ID     = 11f;

    // ---------- visuals ----------
    private static final Color CARD_BG        = new Color(58, 58, 58);
    private static final Color MINI_BG        = new Color(66, 66, 66);
    private static final int   OUTER_RADIUS   = 10;
    private static final int   MINI_RADIUS    = 8;
    private static final Insets OUTER_INSETS  = new Insets(6, 8, 6, 8);
    private static final int   ROW_V_GAP      = 8;

    private static final Color HEADER_BG      = new Color(24, 24, 24);
    private static final Insets HEADER_INSETS = new Insets(6, 8, 6, 8);

    private static final Color TODO_BG   = new Color(0xE74C3C);
    private static final Color TODO_FG   = Color.WHITE;
    private static final Color INPROG_BG = Color.WHITE;
    private static final Color INPROG_FG = new Color(0x1E1E1E);
    private static final Color DONE_BG   = new Color(0x154F2A);
    private static final Color DONE_FG   = Color.WHITE;

    private static final Color PIPE_FG   = new Color(140, 140, 140);

    // ---------- layout weights ----------
    private static final double W_CHECK = 0.30;
    private static final double W_ID    = 0.50;
    private static final double W_TITLE = 1.40;
    private static final double W_STAT  = 1.00;
    private static final double W_BTN   = 0.90;

    private static final double[] COL_WEIGHTS = new double[] {
            W_CHECK, 0.0,
            W_ID,    0.0,
            W_TITLE, 0.0,
            W_STAT,  0.0,
            W_BTN
    };

    private static final double[] MID_WEIGHTS = new double[] {
            0.0, W_ID, 0.0, W_TITLE, 0.0, W_STAT, 0.0
    };

    private static final int PIPE_COL_W = 12;
    private static final int MIN_CHECK_W = 30;
    private static final int MIN_ID_W    = 36;
    private static final int MIN_TITLE_W = 90;
    private static final int MIN_STAT_W  = 100;
    private static final int MIN_BTN_W   = 92;

    // ---------- members ----------
    private final JPanel headerWrapper;
    private final RoundedPanel headerPanel;
    private final JPanel listPanel;
    private final JScrollPane scrollPane;
    private final JPanel headerRightSpacer;

    /** Last computed header 9-column widths. */
    private int[] headerColsPx = null;

    private TasksViewAPI api;

    /** Strong refs to listeners (avoid GC & missed updates). */
    private Property.Listener<List<ITask>> tasksListener;
    private Property.Listener<List<ITask>> filteredListener;

    /** Observable property containing the currently selected task IDs (never null). */
    private final Property<int[]> selectedIdsProp = new Property<>(new int[0]);

    public TasksPanel() {
        super(new BorderLayout());
        setOpaque(false);
        ensureSafeFont(this);

        // Sticky header
        headerPanel = buildHeader();
        headerWrapper = new JPanel(new BorderLayout());
        headerWrapper.setOpaque(false);
        headerWrapper.setBorder(new EmptyBorder(0, 8, ROW_V_GAP, 8));
        headerWrapper.add(headerPanel, BorderLayout.CENTER);

        headerRightSpacer = new JPanel();
        headerRightSpacer.setOpaque(false);
        headerRightSpacer.setPreferredSize(new Dimension(0, 1));
        headerWrapper.add(headerRightSpacer, BorderLayout.EAST);
        add(headerWrapper, BorderLayout.NORTH);

        // Rows container
        listPanel = new JPanel();
        listPanel.setLayout(new BoxLayout(listPanel, BoxLayout.Y_AXIS));
        listPanel.setOpaque(false);
        listPanel.setBorder(new EmptyBorder(0, 8, 0, 8));
        ensureSafeFont(listPanel);

        // Scroll
        scrollPane = new JScrollPane(new ScrollableWidthPanel(listPanel),
                ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setBorder(null);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.setOpaque(false);
        ensureSafeFont(scrollPane);
        add(scrollPane, BorderLayout.CENTER);

        headerWrapper.addComponentListener(new ComponentAdapter() {
            @Override public void componentResized(ComponentEvent e) {
                SwingUtilities.invokeLater(TasksPanel.this::recomputeAndApplyFromHeader);
            }
        });
    }

    // ---------------------------------------------------------------------
    // API wiring
    // ---------------------------------------------------------------------

    /**
     * Injects the {@link TasksViewAPI}, renders a snapshot on the EDT, and subscribes
     * to both all-tasks and filtered-tasks properties so edits are not missed.
     *
     * @param api the API to bind to (must not be null)
     * @throws NullPointerException if api is null
     */
    public void setApi(TasksViewAPI api) {
        this.api = Objects.requireNonNull(api, "api");

        // Paint immediately with current snapshot on EDT (preferring filtered if present)
        if (SwingUtilities.isEventDispatchThread()) {
            renderFromApi();
        } else {
            SwingUtilities.invokeLater(this::renderFromApi);
        }

        // Subscribe to ALL-TASKS
        tasksListener = (oldList, newList) -> {
            if (SwingUtilities.isEventDispatchThread()) renderFromApi();
            else SwingUtilities.invokeLater(this::renderFromApi);
        };
        api.tasksProperty().addListener(tasksListener);

        // Subscribe to FILTERED-TASKS (some VMs only notify this on edit)
        filteredListener = (oldList, newList) -> {
            if (SwingUtilities.isEventDispatchThread()) renderFromApi();
            else SwingUtilities.invokeLater(this::renderFromApi);
        };
        api.filteredTasksProperty().addListener(filteredListener);
    }

    /**
     * Forces a repaint from the latest API snapshot (useful after explicit reload()).
     */
    public void refreshNow() {
        if (SwingUtilities.isEventDispatchThread()) {
            renderFromApi();
        } else {
            SwingUtilities.invokeLater(this::renderFromApi);
        }
    }

    /**
     * @return a live observable property of the currently selected task IDs (never null).
     */
    public Property<int[]> selectedIdsProperty() {
        return selectedIdsProp;
    }

    /**
     * @return the currently selected task IDs as a defensive copy.
     */
    public int[] selectedIds() {
        int[] v = selectedIdsProp.getValue();
        return (v == null) ? new int[0] : v.clone();
    }

    /**
     * Compatibility method used by existing wiring (ContentArea).
     * @return list of currently selected task IDs.
     */
    public List<Integer> getSelectedIds() {
        List<Integer> ids = new ArrayList<>();
        for (Component c : listPanel.getComponents()) {
            if (c instanceof RoundedPanel row) {
                JCheckBox chk = (JCheckBox) findByName(row, "row-check");
                if (chk != null && chk.isSelected()) {
                    Integer id = (Integer) chk.getClientProperty("taskId");
                    if (id != null) ids.add(id);
                }
            }
        }
        return ids;
    }

    // ---------------------------------------------------------------------
    // Rendering
    // ---------------------------------------------------------------------

    /** Prefer filtered list when non-null, otherwise full list. */
    private void renderFromApi() {
        if (api == null) return;
        List<ITask> filtered = api.filteredTasksProperty().getValue();
        List<ITask> all      = api.tasksProperty().getValue();
        List<ITask> toShow   = (filtered != null) ? filtered : all;
        renderTasks(toShow);
    }

    private void renderTasks(List<ITask> tasks) {
        // Keep old selection to restore if possible (by ID).
        int[] oldSel = selectedIds();

        listPanel.removeAll();
        if (tasks != null) {
            for (ITask t : tasks) {
                listPanel.add(buildRow(t));
                listPanel.add(Box.createVerticalStrut(ROW_V_GAP));
            }
        }
        listPanel.revalidate();
        listPanel.repaint();

        // Restore selection where possible.
        if (oldSel.length > 0) {
            for (Component c : listPanel.getComponents()) {
                if (c instanceof RoundedPanel row) {
                    JCheckBox chk = (JCheckBox) findByName(row, "row-check");
                    if (chk != null) {
                        Integer id = (Integer) chk.getClientProperty("taskId");
                        if (id != null && contains(oldSel, id)) chk.setSelected(true);
                    }
                }
            }
        }
        // Recompute property after redraw.
        fireSelectionChanged();

        SwingUtilities.invokeLater(this::recomputeAndApplyFromHeader);
    }

    private RoundedPanel buildHeader() {
        RoundedPanel header = new RoundedPanel(HEADER_BG, 10);
        header.setOpaque(false);
        header.setLayout(new GridBagLayout());
        header.setBorder(new EmptyBorder(HEADER_INSETS));
        ensureSafeFont(header);

        ((GridBagLayout) header.getLayout()).columnWeights = COL_WEIGHTS.clone();

        GridBagConstraints gc = new GridBagConstraints();
        gc.gridy = 0;
        gc.insets = new Insets(0, 0, 0, 6);
        gc.anchor = GridBagConstraints.CENTER;
        gc.fill   = GridBagConstraints.HORIZONTAL;
        gc.weighty = 0;

        int x = 0;
        headerAdd(header, gc, x++, headerLabel("✅"),      W_CHECK);
        headerAdd(header, gc, x++, pipeLabel(),           0);
        headerAdd(header, gc, x++, headerLabel("ID"),     W_ID);
        headerAdd(header, gc, x++, pipeLabel(),           0);
        headerAdd(header, gc, x++, headerLabel("Title"),  W_TITLE);
        headerAdd(header, gc, x++, pipeLabel(),           0);
        headerAdd(header, gc, x++, headerLabel("Status"), W_STAT);
        headerAdd(header, gc, x++, pipeLabel(),           0);
        headerAdd(header, gc, x++, headerLabel("[...]"),  W_BTN);

        header.setAlignmentX(LEFT_ALIGNMENT);
        header.setMaximumSize(new Dimension(Integer.MAX_VALUE, header.getPreferredSize().height));
        return header;
    }

    private static void headerAdd(JPanel header, GridBagConstraints gc, int gridx, JComponent comp, double weightx) {
        GridBagConstraints c = (GridBagConstraints) gc.clone();
        c.gridx = gridx; c.weightx = weightx;
        header.add(comp, c);
    }

    private static JLabel headerLabel(String text) {
        JLabel l = new JLabel(text, SwingConstants.CENTER);
        ensureSafeFont(l);
        l.setForeground(Color.WHITE);
        l.setFont(safeDerive(l.getFont(), Font.BOLD).deriveFont(HEADER_FONT_SIZE));
        return l;
    }

    private static JLabel pipeLabel() {
        JLabel p = new JLabel("|", SwingConstants.CENTER);
        ensureSafeFont(p);
        p.setForeground(PIPE_FG);
        p.setFont(p.getFont().deriveFont(FONT_SIZE_BASE));
        return p;
    }

    // ---------------------------------------------------------------------
    // Row construction
    // ---------------------------------------------------------------------

    private RoundedPanel buildRow(ITask task) {
        RoundedPanel row = new RoundedPanel(CARD_BG, OUTER_RADIUS);
        row.setOpaque(false);
        row.setBorder(new EmptyBorder(OUTER_INSETS));
        row.setLayout(new GridBagLayout());
        ensureSafeFont(row);

        GridBagConstraints gc = new GridBagConstraints();
        gc.gridy = 0;
        gc.insets = new Insets(0, 0, 0, 6);
        gc.anchor = GridBagConstraints.CENTER;
        gc.fill   = GridBagConstraints.BOTH;

        // Left mini-card: checkbox
        RoundedPanel left = new RoundedPanel(MINI_BG, MINI_RADIUS);
        left.setOpaque(false);
        left.setLayout(new GridBagLayout());
        left.setBorder(new EmptyBorder(4, 6, 4, 6));
        JCheckBox chk = new JCheckBox();
        chk.setName("row-check");
        chk.putClientProperty("taskId", task.getId());
        chk.setOpaque(false);
        chk.setFocusable(false);
        Dimension cbSize = new Dimension(18, 18);
        chk.setPreferredSize(cbSize);
        chk.setMinimumSize(cbSize);
        // Update selection property on toggle
        chk.addActionListener(e -> fireSelectionChanged());
        left.add(chk, new GridBagConstraints());
        gc.gridx = 0; gc.weightx = 0;
        row.add(left, gc);

        // Middle mini-card: ID | Title | Status
        RoundedPanel mid = new RoundedPanel(MINI_BG, MINI_RADIUS);
        mid.setOpaque(false);
        mid.setLayout(new GridBagLayout());
        mid.setBorder(new EmptyBorder(4, 6, 4, 6));
        mid.putClientProperty("role", "mid-panel");

        GridBagConstraints g = new GridBagConstraints();
        g.gridy = 0;
        g.insets = new Insets(0, 0, 0, 6);
        g.anchor = GridBagConstraints.CENTER;
        g.fill   = GridBagConstraints.HORIZONTAL;

        int xx = 0;
        addMid(mid, g, xx++, pipeLabel(), 0);

        JLabel id = centeredLabel(String.valueOf(task.getId()));
        id.setFont(safeDerive(id.getFont(), Font.BOLD).deriveFont(FONT_SIZE_ID));
        addMid(mid, g, xx++, id, W_ID);

        addMid(mid, g, xx++, pipeLabel(), 0);

        JLabel title = centeredLabel(clipForPreview(task.getTitle(), TITLE_PREVIEW_MAX));
        title.setFont(title.getFont().deriveFont(FONT_SIZE_BASE));
        addMid(mid, g, xx++, title, W_TITLE);

        addMid(mid, g, xx++, pipeLabel(), 0);

        JLabel status = pill(task.getState().name());
        status.setFont(safeDerive(status.getFont(), Font.BOLD).deriveFont(FONT_SIZE_PILL));
        addMid(mid, g, xx++, centerWrap(status), W_STAT);

        addMid(mid, g, xx++, pipeLabel(), 0);

        gc.gridx = 1; gc.weightx = 1;
        row.add(mid, gc);

        // Right mini-card: button
        RoundedPanel right = new RoundedPanel(MINI_BG, MINI_RADIUS);
        right.setOpaque(false);
        right.setLayout(new GridBagLayout());
        right.setBorder(new EmptyBorder(4, 6, 4, 6));
        JButton more = new JButton("Show more");
        ensureSafeFont(more);
        more.setFont(more.getFont().deriveFont(FONT_SIZE_BASE));
        more.setBorder(BorderFactory.createEmptyBorder(2, 6, 2, 6));
        more.addActionListener(e -> showTaskDialog(row, task));
        right.add(more, new GridBagConstraints());
        gc.gridx = 2; gc.weightx = 0; gc.insets = new Insets(0, 0, 0, 0);
        row.add(right, gc);

        row.setAlignmentX(LEFT_ALIGNMENT);
        Dimension pref = row.getPreferredSize();
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, pref.height));

        if (headerColsPx != null) syncRowLayouts(row);
        return row;
    }

    private static void addMid(JPanel panel, GridBagConstraints base, int gridx, JComponent comp, double weightx) {
        GridBagConstraints c = (GridBagConstraints) base.clone();
        c.gridx = gridx; c.weightx = weightx;
        panel.add(comp, c);
    }

    // ---------------------------------------------------------------------
    // Header sync
    // ---------------------------------------------------------------------

    private void recomputeAndApplyFromHeader() {
        JScrollBar vsb = scrollPane.getVerticalScrollBar();
        int sbw = (vsb != null && vsb.isShowing()) ? Math.max(0, vsb.getWidth()) : 0;
        headerRightSpacer.setPreferredSize(new Dimension(sbw, 1));
        headerRightSpacer.setMinimumSize(new Dimension(sbw, 1));
        headerRightSpacer.setMaximumSize(new Dimension(sbw, Integer.MAX_VALUE));
        headerWrapper.revalidate();

        int available = headerPanel.getWidth();
        if (available <= 0) { SwingUtilities.invokeLater(this::recomputeAndApplyFromHeader); return; }

        int inner = available - (HEADER_INSETS.left + HEADER_INSETS.right);
        if (inner <= 0) return;

        final int PIPE_COUNT = 4;
        int pipesWidth = PIPE_COUNT * PIPE_COL_W;

        int[] mins = { MIN_CHECK_W, MIN_ID_W, MIN_TITLE_W, MIN_STAT_W, MIN_BTN_W };
        double[] ws = { W_CHECK, W_ID, W_TITLE, W_STAT, W_BTN };

        int contentWidth = inner - pipesWidth;
        int minSum = Arrays.stream(mins).sum();

        int[] cols = mins.clone();
        int extra = Math.max(0, contentWidth - minSum);
        double wSum = Arrays.stream(ws).sum();
        for (int i = 0; i < cols.length; i++) {
            cols[i] += (int) Math.floor(extra * (ws[i] / wSum));
        }
        int used = Arrays.stream(cols).sum();
        int delta = contentWidth - used;
        if (delta != 0) cols[2] += delta;

        headerColsPx = new int[] {
                cols[0], PIPE_COL_W,
                cols[1], PIPE_COL_W,
                cols[2], PIPE_COL_W,
                cols[3], PIPE_COL_W,
                cols[4]
        };

        GridBagLayout h = (GridBagLayout) headerPanel.getLayout();
        h.columnWeights = COL_WEIGHTS.clone();
        h.columnWidths  = headerColsPx.clone();
        headerPanel.revalidate();

        for (Component c : listPanel.getComponents()) {
            if (c instanceof RoundedPanel row) syncRowLayouts(row);
        }
        listPanel.revalidate();
        listPanel.repaint();
    }

    private void syncRowLayouts(RoundedPanel row) {
        int leftW  = headerColsPx[0];
        int midW   = 0; for (int i = 1; i <= 7; i++) midW += headerColsPx[i];
        int rightW = headerColsPx[8];

        GridBagLayout outer = (GridBagLayout) row.getLayout();
        outer.columnWeights = new double[] { 0, 1, 0 };
        outer.columnWidths  = new int[] { leftW, midW, rightW };

        for (Component cc : row.getComponents()) {
            if (cc instanceof RoundedPanel p && "mid-panel".equals(p.getClientProperty("role"))) {
                if (p.getLayout() instanceof GridBagLayout mid) {
                    int[] midCols = Arrays.copyOfRange(headerColsPx, 1, 8);
                    mid.columnWeights = MID_WEIGHTS.clone();
                    mid.columnWidths  = midCols;
                    p.revalidate();
                }
            }
        }
        row.revalidate();
    }

    // ---------------------------------------------------------------------
    // Dialog
    // ---------------------------------------------------------------------

    private void showTaskDialog(Component parent, ITask t) {
        JDialog dlg = new JDialog(SwingUtilities.getWindowAncestor(parent),
                "Task Details", Dialog.ModalityType.APPLICATION_MODAL);

        JPanel content = new JPanel(new GridBagLayout());
        content.setBackground(new Color(46, 46, 46));
        content.setBorder(new EmptyBorder(16, 16, 16, 16));
        ensureSafeFont(content);

        GridBagConstraints g = new GridBagConstraints();
        g.gridx = 0; g.gridy = 0; g.insets = new Insets(4, 4, 4, 4);
        g.anchor = GridBagConstraints.WEST;

        content.add(dim("Task ID:"), g);
        g.gridx = 1; content.add(val(String.valueOf(t.getId())), g);

        g.gridx = 0; g.gridy++; content.add(dim("Title:"), g);
        g.gridx = 1; content.add(val(t.getTitle()), g);

        g.gridx = 0; g.gridy++; content.add(dim("Description:"), g);
        g.gridx = 1; g.fill = GridBagConstraints.HORIZONTAL; g.weightx = 1.0;
        JTextArea ta = new JTextArea(t.getDescription());
        ensureSafeFont(ta);
        ta.setEditable(false);
        ta.setLineWrap(true);
        ta.setWrapStyleWord(true);
        ta.setBackground(new Color(60, 60, 60));
        ta.setForeground(new Color(235, 235, 235));
        ta.setBorder(new EmptyBorder(8, 8, 8, 8));
        JScrollPane sp = new JScrollPane(ta,
                ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        sp.setPreferredSize(new Dimension(380, 140));
        ensureSafeFont(sp);
        content.add(sp, g);

        g.gridx = 0; g.gridy++; g.fill = GridBagConstraints.NONE; g.weightx = 0;
        content.add(dim("Status:"), g);
        g.gridx = 1; content.add(pill(t.getState().name()), g);

        g.gridx = 0; g.gridy++; g.gridwidth = 2; g.anchor = GridBagConstraints.CENTER;
        g.insets = new Insets(16, 4, 4, 4);
        JButton ok = new JButton("Close");
        ensureSafeFont(ok);
        ok.addActionListener(ev -> dlg.dispose());
        content.add(ok, g);

        dlg.setContentPane(content);
        dlg.pack();
        dlg.setLocationRelativeTo(parent);
        dlg.setVisible(true);
    }

    // ---------------------------------------------------------------------
    // Selection helpers
    // ---------------------------------------------------------------------

    /** Recomputes the current selection from the visible rows and updates the property. */
    private void fireSelectionChanged() {
        List<Integer> ids = getSelectedIds();
        int[] now = ids.stream().mapToInt(Integer::intValue).toArray();
        int[] prev = selectedIdsProp.getValue();
        if (!Arrays.equals(prev, now)) {
            selectedIdsProp.setValue(now);
        }
    }

    private static boolean contains(int[] arr, int id) {
        for (int v : arr) if (v == id) return true;
        return false;
    }

    // ---------------------------------------------------------------------
    // General helpers
    // ---------------------------------------------------------------------

    private static String clipForPreview(String s, int n) {
        if (s == null) return "";
        if (n <= 3)    return (s.length() <= 3) ? s : "...";
        if (s.length() <= n) return s;
        return s.substring(0, n - 3) + "...";
    }

    private static JComponent centerWrap(JComponent c) {
        JPanel p = new JPanel(new GridBagLayout());
        p.setOpaque(false);
        p.add(c, new GridBagConstraints());
        return p;
    }

    private static JLabel centeredLabel(String s) {
        JLabel l = new JLabel(s, SwingConstants.CENTER);
        ensureSafeFont(l);
        l.setForeground(new Color(235, 235, 235));
        l.setFont(l.getFont().deriveFont(FONT_SIZE_BASE));
        return l;
    }

    private static JLabel dim(String s) {
        JLabel l = new JLabel(s);
        ensureSafeFont(l);
        l.setForeground(new Color(200, 200, 200));
        l.setFont(safeDerive(l.getFont(), Font.BOLD).deriveFont(FONT_SIZE_BASE));
        return l;
    }

    private static JLabel val(String s) {
        JLabel l = new JLabel(s);
        ensureSafeFont(l);
        l.setForeground(new Color(235, 235, 235));
        l.setFont(l.getFont().deriveFont(FONT_SIZE_BASE));
        return l;
    }

    private static JLabel pill(String status) {
        String s = status == null ? "" : status;
        JLabel l = new JLabel(s, SwingConstants.CENTER);
        ensureSafeFont(l);
        l.setOpaque(true);
        l.setBorder(BorderFactory.createEmptyBorder(2, 6, 2, 6));

        Color bg, fg;
        switch (s.toLowerCase()) {
            case "to-do", "todo" -> { bg = TODO_BG;   fg = TODO_FG; }
            case "in-progress", "in progress", "inprog" -> { bg = INPROG_BG; fg = INPROG_FG; }
            case "completed", "done" -> { bg = DONE_BG;   fg = DONE_FG; }
            default -> { bg = new Color(90, 90, 90); fg = new Color(240, 240, 240); }
        }
        l.setBackground(bg);
        l.setForeground(fg);
        l.setFont(safeDerive(l.getFont(), Font.BOLD).deriveFont(FONT_SIZE_PILL));
        return l;
    }

    private static JComponent findByName(Container parent, String name) {
        for (Component c : parent.getComponents()) {
            if (name.equals(c.getName())) return (JComponent) c;
            if (c instanceof Container nested) {
                JComponent f = findByName(nested, name);
                if (f != null) return f;
            }
        }
        return null;
    }

    private static final class ScrollableWidthPanel extends JPanel implements Scrollable {
        private final JComponent inner;
        ScrollableWidthPanel(JComponent inner) {
            super(new BorderLayout());
            this.inner = inner;
            add(inner, BorderLayout.NORTH);
            setOpaque(false);
            ensureSafeFont(this);
            ensureSafeFont(inner);
        }
        @Override public Dimension getPreferredScrollableViewportSize() { return inner.getPreferredSize(); }
        @Override public int getScrollableUnitIncrement(Rectangle r, int o, int d) { return 24; }
        @Override public int getScrollableBlockIncrement(Rectangle r, int o, int d) { return Math.max(r.height - 24, 24); }
        @Override public boolean getScrollableTracksViewportWidth() { return true; }
        @Override public boolean getScrollableTracksViewportHeight() { return false; }
    }

    private static void ensureSafeFont(JComponent c) {
        if (c.getFont() == null) {
            Font f = UIManager.getFont("Label.font");
            if (f == null) f = new JLabel().getFont();
            c.setFont(f);
        }
    }

    private static Font safeDerive(Font base, int style) {
        if (base == null) {
            Font f = UIManager.getFont("Label.font");
            if (f == null) f = new JLabel().getFont();
            return f.deriveFont(style);
        }
        return base.deriveFont(style);
    }
}
