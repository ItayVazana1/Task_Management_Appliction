package ui_test.widgets;

import ui_test.util.RoundedPanel;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * TasksPanel
 * ------------------------------
 * Sticky header + scrollable list. Each row is split into 3 mini-cards:
 *  [Left]   Checkbox
 *  [Middle] ID | Title | Status
 *  [Right]  Show more
 *
 * Header width now matches the viewport width (excludes vertical scrollbar)
 * by placing a right spacer whose width equals the scrollbar width.
 *
 * Comments in English only.
 */
public final class TasksPanel extends JPanel {

    // ---------- preview clipping ----------
    private static final int TITLE_PREVIEW_MAX = 24;

    // ---------- typography ----------
    private static final float FONT_SIZE_BASE   = 11f;
    private static final float HEADER_FONT_SIZE = 10f; // smaller header labels (bold)
    private static final float FONT_SIZE_PILL   = 11f;
    private static final float FONT_SIZE_ID     = 11f;

    // ---------- visuals ----------
    private static final Color CARD_BG        = new Color(58, 58, 58);   // outer row background
    private static final Color MINI_BG        = new Color(66, 66, 66);   // mini-card background
    private static final int   OUTER_RADIUS   = 10;
    private static final int   MINI_RADIUS    = 8;
    private static final Insets OUTER_INSETS  = new Insets(6, 8, 6, 8);
    private static final int   ROW_V_GAP      = 8;

    private static final Color HEADER_BG      = new Color(24, 24, 24);   // darker header
    private static final Insets HEADER_INSETS = new Insets(6, 8, 6, 8);  // same inner padding as rows

    private static final Color TODO_BG   = new Color(0xE74C3C);
    private static final Color TODO_FG   = Color.WHITE;
    private static final Color INPROG_BG = Color.WHITE;
    private static final Color INPROG_FG = new Color(0x1E1E1E);
    private static final Color DONE_BG   = new Color(0x154F2A);
    private static final Color DONE_FG   = Color.WHITE;

    private static final Color PIPE_FG   = new Color(140, 140, 140);

    // ---------- columns (no Description) ----------
    // Header 9 columns (with pipes):
    // idx: 0   1    2   3     4    5      6    7     8
    //     [CHK, |,  ID, |,  TITLE, |,   STATUS, |,  BTN]
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

    // Middle sub-grid weights (header cols 1..7)
    private static final double[] MID_WEIGHTS = new double[] {
            0.0,     // pipe
            W_ID,    // ID
            0.0,     // pipe
            W_TITLE, // Title
            0.0,     // pipe
            W_STAT,  // Status
            0.0      // pipe
    };

    // Fixed pipe width
    private static final int PIPE_COL_W = 12;

    // Minimum px per content col
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

    /** Spacer on the right of the header to compensate for vertical scrollbar width. */
    private final JPanel headerRightSpacer;

    /** Last computed header 9-column widths. */
    private int[] headerColsPx = null;

    public TasksPanel() {
        super(new BorderLayout());
        setOpaque(false);
        ensureSafeFont(this);

        // Sticky header wrapper — add right spacer to match viewport width
        headerPanel = buildHeader();
        headerWrapper = new JPanel(new BorderLayout());
        headerWrapper.setOpaque(false);
        headerWrapper.setBorder(new EmptyBorder(0, 8, ROW_V_GAP, 8)); // left/right + bottom gap
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

        // Vertical-only scroll
        scrollPane = new JScrollPane(new ScrollableWidthPanel(listPanel),
                ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setBorder(null);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.setOpaque(false);
        ensureSafeFont(scrollPane);

        add(scrollPane, BorderLayout.CENTER);

        // Recompute widths on header wrapper resize
        headerWrapper.addComponentListener(new ComponentAdapter() {
            @Override public void componentResized(ComponentEvent e) {
                SwingUtilities.invokeLater(TasksPanel.this::recomputeAndApplyFromHeader);
            }
        });

        addDemoTasks();
        SwingUtilities.invokeLater(this::recomputeAndApplyFromHeader);
    }

    // ---------- header ----------
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
        headerAdd(header, gc, x++, pipeLabel(),          0);
        headerAdd(header, gc, x++, headerLabel("ID"),    W_ID);
        headerAdd(header, gc, x++, pipeLabel(),          0);
        headerAdd(header, gc, x++, headerLabel("Title"), W_TITLE);
        headerAdd(header, gc, x++, pipeLabel(),          0);
        headerAdd(header, gc, x++, headerLabel("Status"),W_STAT);
        headerAdd(header, gc, x++, pipeLabel(),          0);
        headerAdd(header, gc, x++, headerLabel("[...]"),      W_BTN);

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

    // ---------- compute & apply widths ----------
    private void recomputeAndApplyFromHeader() {
        // 1) Adjust right spacer to the visible vertical scrollbar width (if any)
        JScrollBar vsb = scrollPane.getVerticalScrollBar();
        int sbw = (vsb != null && vsb.isShowing()) ? Math.max(0, vsb.getWidth()) : 0;
        headerRightSpacer.setPreferredSize(new Dimension(sbw, 1));
        headerRightSpacer.setMinimumSize(new Dimension(sbw, 1));
        headerRightSpacer.setMaximumSize(new Dimension(sbw, Integer.MAX_VALUE));
        headerWrapper.revalidate();

        // 2) Now the headerPanel.getWidth() equals the viewport width (+ header inset)
        int available = headerPanel.getWidth();
        if (available <= 0) { SwingUtilities.invokeLater(this::recomputeAndApplyFromHeader); return; }

        int inner = available - (HEADER_INSETS.left + HEADER_INSETS.right);
        if (inner <= 0) return;

        // 4 pipes total
        final int PIPE_COUNT = 4;
        int pipesWidth = PIPE_COUNT * PIPE_COL_W;

        int[] mins = { MIN_CHECK_W, MIN_ID_W, MIN_TITLE_W, MIN_STAT_W, MIN_BTN_W };
        double[] ws = { W_CHECK, W_ID, W_TITLE, W_STAT, W_BTN };

        int contentWidth = inner - pipesWidth;
        int minSum = 0; for (int m : mins) minSum += m;

        int[] cols = mins.clone();
        int extra = Math.max(0, contentWidth - minSum);
        double wSum = 0; for (double w : ws) wSum += w;
        for (int i = 0; i < cols.length; i++) {
            cols[i] += (int) Math.floor(extra * (ws[i] / wSum));
        }
        // rounding fix
        int used = 0; for (int c : cols) used += c;
        int delta = contentWidth - used;
        if (delta != 0) cols[2] += delta; // adjust Title

        // 9-length widths (with pipes)
        headerColsPx = new int[] {
                cols[0], PIPE_COL_W,
                cols[1], PIPE_COL_W,
                cols[2], PIPE_COL_W,
                cols[3], PIPE_COL_W,
                cols[4]
        };

        // Apply to header
        GridBagLayout h = (GridBagLayout) headerPanel.getLayout();
        h.columnWeights = COL_WEIGHTS.clone();
        h.columnWidths  = headerColsPx.clone();
        headerPanel.revalidate();

        // Apply to each row
        for (Component c : listPanel.getComponents()) {
            if (c instanceof RoundedPanel row) syncRowLayouts(row);
        }
        listPanel.revalidate();
        listPanel.repaint();
    }

    /** Apply widths to the row: outer 3 mini-cards and inner middle sub-grid. */
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

    // ---------- demo data ----------
    private void addDemoTasks() {
        listPanel.removeAll();

        List<Task> items = new ArrayList<>(25);
        for (int i = 1; i <= 25; i++) {
            String title  = "Demo Task " + i;
            String desc   = "This is a description for demo task " + i + " with some sample text.";
            String status = switch (i % 3) { case 1 -> "To-Do"; case 2 -> "In-Progress"; default -> "Completed"; };
            items.add(new Task(i, title, desc, status));
        }

        for (Task t : items) {
            listPanel.add(buildRow(t));
            listPanel.add(Box.createVerticalStrut(ROW_V_GAP));
        }
        listPanel.revalidate();
        listPanel.repaint();
    }

    // ---------- public API ----------
    public void addTaskCard(int id, String title, String description, String status) {
        listPanel.add(buildRow(new Task(id, title, description, status)));
        listPanel.add(Box.createVerticalStrut(ROW_V_GAP));
        listPanel.revalidate();
        listPanel.repaint();
        SwingUtilities.invokeLater(this::recomputeAndApplyFromHeader);
    }

    // ---------- row construction (3 mini-cards) ----------
    private RoundedPanel buildRow(Task task) {
        RoundedPanel row = new RoundedPanel(CARD_BG, OUTER_RADIUS);
        row.setOpaque(false);
        row.setBorder(new EmptyBorder(OUTER_INSETS));
        row.setLayout(new GridBagLayout());
        ensureSafeFont(row);

        GridBagConstraints gc = new GridBagConstraints();
        gc.gridy = 0;
        gc.insets = new Insets(0, 0, 0, 6); // gap between mini-cards
        gc.anchor = GridBagConstraints.CENTER;
        gc.fill   = GridBagConstraints.BOTH;
        gc.weighty = 0;

        // Left mini-card: checkbox
        RoundedPanel left = new RoundedPanel(MINI_BG, MINI_RADIUS);
        left.setOpaque(false);
        left.setLayout(new GridBagLayout());
        left.setBorder(new EmptyBorder(4, 6, 4, 6));
        JCheckBox chk = new JCheckBox();
        chk.setOpaque(false);
        chk.setFocusable(false);
        Dimension cbSize = new Dimension(18, 18);
        chk.setPreferredSize(cbSize);
        chk.setMinimumSize(cbSize);
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
        g.weighty = 0;

        int xx = 0;
        addMid(mid, g, xx++, pipeLabel(), 0);

        JLabel id = centeredLabel(String.valueOf(task.id()));
        id.setFont(safeDerive(id.getFont(), Font.BOLD).deriveFont(FONT_SIZE_ID));
        addMid(mid, g, xx++, id, W_ID);

        addMid(mid, g, xx++, pipeLabel(), 0);

        JLabel title = centeredLabel(clipForPreview(task.title(), TITLE_PREVIEW_MAX));
        title.setFont(title.getFont().deriveFont(FONT_SIZE_BASE));
        addMid(mid, g, xx++, title, W_TITLE);

        addMid(mid, g, xx++, pipeLabel(), 0);

        JLabel status = pill(task.status());
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

        // Stretch to full width
        row.setAlignmentX(LEFT_ALIGNMENT);
        Dimension pref = row.getPreferredSize();
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, pref.height));

        // Sync if header widths are ready
        if (headerColsPx != null) syncRowLayouts(row);
        return row;
    }

    private static void addMid(JPanel panel, GridBagConstraints base, int gridx, JComponent comp, double weightx) {
        GridBagConstraints c = (GridBagConstraints) base.clone();
        c.gridx = gridx; c.weightx = weightx;
        panel.add(comp, c);
    }

    // ---------- dialog ----------
    private void showTaskDialog(Component parent, Task t) {
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
        g.gridx = 1; content.add(val(String.valueOf(t.id())), g);

        g.gridx = 0; g.gridy++; content.add(dim("Title:"), g);
        g.gridx = 1; content.add(val(t.title()), g);

        g.gridx = 0; g.gridy++; content.add(dim("Description:"), g);
        g.gridx = 1; g.fill = GridBagConstraints.HORIZONTAL; g.weightx = 1.0;
        JTextArea ta = new JTextArea(t.description());
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
        g.gridx = 1; content.add(pill(t.status()), g);

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

    // ---------- helpers ----------
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

    private record Task(int id, String title, String description, String status) { }

    // ---------- scrollable container ----------
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

    // ---------- font safety ----------
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
