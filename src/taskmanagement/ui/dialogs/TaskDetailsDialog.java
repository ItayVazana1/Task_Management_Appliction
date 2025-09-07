package taskmanagement.ui.dialogs;

import taskmanagement.domain.ITask;
import taskmanagement.ui.styles.AppTheme;
import taskmanagement.ui.util.RoundedPanel;
import taskmanagement.ui.util.UiUtils;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;

/**
 * Modal dialog that presents task details in a read-only, presentation-only view.
 * <p>
 * MVVM-safe: performs no DAO/model access and no state mutations.
 */
public final class TaskDetailsDialog extends JDialog {

    private JButton closeButton;

    /**
     * Shows a modal Task Details dialog for the given task.
     *
     * @param parent a component inside the owner window; may be {@code null}
     * @param task   the task to display; must not be {@code null}
     * @throws NullPointerException if {@code task} is {@code null}
     */
    public static void showDialog(Component parent, ITask task) {
        Window owner = parent instanceof Window ? (Window) parent : SwingUtilities.getWindowAncestor(parent);
        TaskDetailsDialog dlg = new TaskDetailsDialog(owner, task);
        dlg.setVisible(true);
    }

    /**
     * Creates the dialog. Prefer using {@link #showDialog(Component, ITask)}.
     *
     * @param owner window owner; may be {@code null}
     * @param task  task to display; must not be {@code null}
     * @throws NullPointerException if {@code task} is {@code null}
     */
    public TaskDetailsDialog(Window owner, ITask task) {
        super(owner, "Task Details", ModalityType.APPLICATION_MODAL);
        if (task == null) throw new NullPointerException("task");

        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setContentPane(buildContent(task));
        pack();
        setResizable(false);
        setLocationRelativeTo(owner);
        getRootPane().setDefaultButton(closeButton);

        var im = getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        var am = getRootPane().getActionMap();
        im.put(KeyStroke.getKeyStroke("ESCAPE"), "close");
        am.put("close", new AbstractAction() {
            @Override public void actionPerformed(ActionEvent e) { dispose(); }
        });
    }

    private JComponent buildContent(ITask t) {
        final RoundedPanel root = new RoundedPanel(AppTheme.PANEL_BG, AppTheme.TB_CORNER_RADIUS);
        root.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));
        root.setLayout(new BorderLayout(0, 12));

        Icon taskIcon = UiUtils.loadRasterIcon("/taskmanagement/ui/resources/task.png", 40, 40);

        JLabel title = new JLabel(ellipsize(t.getTitle(), 60));
        title.setFont(new Font("Segoe UI", Font.BOLD, 18));
        title.setForeground(AppTheme.MAIN_TEXT);

        JLabel subtitle = new JLabel("Task #" + t.getId());
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        subtitle.setForeground(new Color(0x888888));

        JPanel titles = new JPanel();
        titles.setOpaque(false);
        titles.setLayout(new BoxLayout(titles, BoxLayout.Y_AXIS));
        titles.add(title);
        titles.add(Box.createVerticalStrut(4));
        titles.add(subtitle);

        JPanel header = new JPanel(new BorderLayout(10, 0));
        header.setOpaque(false);
        if (taskIcon != null) header.add(new JLabel(taskIcon), BorderLayout.WEST);
        header.add(titles, BorderLayout.CENTER);

        JPanel body = new JPanel();
        body.setOpaque(false);
        body.setLayout(new GridBagLayout());
        GridBagConstraints g = new GridBagConstraints();
        g.gridx = 0; g.gridy = 0;
        g.insets = new Insets(6, 4, 6, 8);
        g.anchor = GridBagConstraints.NORTHWEST;

        g.gridy++; body.add(dim("Status:"), g);
        g.gridx = 1; body.add(pill(t.getState().name()), g);

        g.gridx = 0; g.gridy++; body.add(dim("Description:"), g);
        g.gridx = 1; g.fill = GridBagConstraints.HORIZONTAL; g.weightx = 1.0;
        JTextArea ta = new JTextArea(t.getDescription() == null ? "" : t.getDescription());
        ta.setEditable(false);
        ta.setLineWrap(true);
        ta.setWrapStyleWord(true);
        ta.setBackground(new Color(60, 60, 60));
        ta.setForeground(new Color(235, 235, 235));
        ta.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        ta.setBorder(new EmptyBorder(8, 8, 8, 8));
        ta.setFocusable(false);
        ta.setHighlighter(null);
        ta.setDragEnabled(false);
        ta.setCursor(Cursor.getDefaultCursor());

        JScrollPane sp = new JScrollPane(ta,
                ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        sp.setPreferredSize(new Dimension(420, 160));
        body.add(sp, g);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        actions.setOpaque(false);

        closeButton = new JButton("Close");
        closeButton.setMnemonic('C');
        closeButton.getAccessibleContext().setAccessibleName("Close");
        UiUtils.styleStableHoverButton(closeButton, new Color(0x2F7BFF), Color.WHITE);
        closeButton.addActionListener(e -> dispose());
        actions.add(closeButton);

        root.add(header, BorderLayout.NORTH);
        root.add(body, BorderLayout.CENTER);
        root.add(actions, BorderLayout.SOUTH);
        return root;
    }

    private static JLabel dim(String s) {
        JLabel l = new JLabel(s);
        l.setForeground(AppTheme.MAIN_TEXT);
        l.setFont(new Font("Segoe UI", Font.BOLD, 12));
        return l;
    }

    private static JLabel val(String s) {
        JLabel l = new JLabel(s == null ? "" : s);
        l.setForeground(new Color(235, 235, 235));
        l.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        return l;
    }

    private static JLabel pill(String status) {
        String s = status == null ? "" : status;
        JLabel l = new JLabel(s, SwingConstants.CENTER);
        l.setOpaque(true);
        l.setBorder(BorderFactory.createEmptyBorder(2, 8, 2, 8));
        Color bg, fg;
        switch (s.toLowerCase()) {
            case "to-do", "todo" -> { bg = new Color(0xE74C3C); fg = Color.WHITE; }
            case "in-progress", "in progress", "inprog" -> { bg = Color.WHITE; fg = new Color(0x1E1E1E); }
            case "completed", "done" -> { bg = new Color(0x154F2A); fg = Color.WHITE; }
            default -> { bg = new Color(90, 90, 90); fg = new Color(240, 240, 240); }
        }
        l.setBackground(bg);
        l.setForeground(fg);
        l.setFont(new Font("Segoe UI", Font.BOLD, 11));
        return l;
    }

    private static String ellipsize(String s, int max) {
        if (s == null) return "";
        if (s.length() <= max) return s;
        return s.substring(0, Math.max(0, max - 1)) + "…";
    }
}
