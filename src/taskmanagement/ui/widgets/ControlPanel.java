package taskmanagement.ui.widgets;

import taskmanagement.ui.styles.AppTheme;
import taskmanagement.ui.util.UiUtils;
import taskmanagement.ui.util.RoundedPanel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.lang.reflect.Constructor;

/**
 * ControlPanel
 * <p>
 * Left rail with five large square buttons arranged in vertical fifths:
 * Refresh, Add, Edit, Delete, Disk Cleanup. Each button shows an icon above
 * and a text label below. Width is locked (preferred == minimum) so the
 * center content cannot steal space. Icons are loaded safely with a fallback
 * 1x1 transparent icon if missing.
 * <p>
 * Current behavior is placeholder-only (no ViewModel wiring yet):
 * <ul>
 *   <li>Refresh: info placeholder (to be wired to vm.reload()).</li>
 *   <li>Add/Edit: tries to open TaskEditorDialog reflectively, else shows
 *       a small placeholder dialog.</li>
 *   <li>Delete: confirm selected deletion (placeholder).</li>
 *   <li>Disk Cleanup: confirm deleting ALL tasks (placeholder).</li>
 * </ul>
 */
public class ControlPanel extends RoundedPanel {

    /** Horizontal padding inside the rail (px). */
    private static final int INNER_PAD = 8;

    /** Fallback width if the parent layout does not constrain the rail. */
    private static final int FALLBACK_WIDTH = 220;

    private final JButton refreshBtn;
    private final JButton addBtn;
    private final JButton editBtn;
    private final JButton deleteBtn;
    private final JButton cleanupBtn;

    /**
     * Creates the left control rail with five vertically-centered blocks.
     * Colors and sizes are taken from {@link AppTheme}.
     */
    public ControlPanel() {
        super(new Color(0x2C2C2C), 16);
        setOpaque(false);

        // Lock rail width (preferred == minimum).
        int block = AppTheme.CTRL_BLOCK_SIZE;
        int fixedWidth = Math.max(FALLBACK_WIDTH, block + INNER_PAD * 2);
        Dimension fixed = new Dimension(fixedWidth, 10);
        setPreferredSize(fixed);
        setMinimumSize(fixed);

        // Inner breathing space.
        setBorder(BorderFactory.createEmptyBorder(INNER_PAD, INNER_PAD, INNER_PAD, INNER_PAD));

        // 5 rows, vertically distributed.
        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.weightx = 1.0;
        gbc.insets = new Insets(0, 0, 0, 0);

        // Load icons (safe fallback if missing)
        Icon refreshIcon = safeIcon(UiUtils.loadRasterIcon(
                "/taskmanagement/ui/resources/refresh.png",  // ← שים את האייקון ב-resources
                AppTheme.CTRL_ICON_SIZE, AppTheme.CTRL_ICON_SIZE));
        Icon addIcon = safeIcon(UiUtils.loadRasterIcon(
                "/taskmanagement/ui/resources/add.png",
                AppTheme.CTRL_ICON_SIZE, AppTheme.CTRL_ICON_SIZE));
        Icon editIcon = safeIcon(UiUtils.loadRasterIcon(
                "/taskmanagement/ui/resources/edit.png",
                AppTheme.CTRL_ICON_SIZE, AppTheme.CTRL_ICON_SIZE));
        Icon delIcon = safeIcon(UiUtils.loadRasterIcon(
                "/taskmanagement/ui/resources/delete.png",
                AppTheme.CTRL_ICON_SIZE, AppTheme.CTRL_ICON_SIZE));
        Icon cleanupIcon = safeIcon(UiUtils.loadRasterIcon(
                "/taskmanagement/ui/resources/cleanup.png",
                AppTheme.CTRL_ICON_SIZE, AppTheme.CTRL_ICON_SIZE));

        // Row 0: Refresh
        refreshBtn = UiUtils.createPaintedRoundedIconButton(
                "Refresh", refreshIcon,
                AppTheme.CTRL_REFRESH_BG, AppTheme.CTRL_REFRESH_FG,
                AppTheme.CTRL_BLOCK_SIZE, AppTheme.CTRL_CORNER_RAD, AppTheme.CTRL_FONT_SIZE
        );
        refreshBtn.setActionCommand("REFRESH");
        refreshBtn.setToolTipText("Refresh tasks");
        refreshBtn.setAlignmentX(LEFT_ALIGNMENT);
        gbc.gridy = 0;
        gbc.weighty = 1.0;
        add(refreshBtn, gbc);

        // Row 1: Add
        addBtn = UiUtils.createPaintedRoundedIconButton(
                "Add", addIcon,
                AppTheme.CTRL_ADD_BG, AppTheme.CTRL_ADD_FG,
                AppTheme.CTRL_BLOCK_SIZE, AppTheme.CTRL_CORNER_RAD, AppTheme.CTRL_FONT_SIZE
        );
        addBtn.setActionCommand("ADD");
        addBtn.setToolTipText("Add");
        addBtn.setAlignmentX(LEFT_ALIGNMENT);
        gbc.gridy = 1;
        gbc.weighty = 1.0;
        add(addBtn, gbc);

        // Row 2: Edit
        editBtn = UiUtils.createPaintedRoundedIconButton(
                "Edit", editIcon,
                AppTheme.CTRL_EDIT_BG, AppTheme.CTRL_EDIT_FG,
                AppTheme.CTRL_BLOCK_SIZE, AppTheme.CTRL_CORNER_RAD, AppTheme.CTRL_FONT_SIZE
        );
        editBtn.setActionCommand("EDIT");
        editBtn.setToolTipText("Edit");
        editBtn.setAlignmentX(LEFT_ALIGNMENT);
        gbc.gridy = 2;
        gbc.weighty = 1.0;
        add(editBtn, gbc);

        // Row 3: Delete
        deleteBtn = UiUtils.createPaintedRoundedIconButton(
                "Delete", delIcon,
                AppTheme.CTRL_DELETE_BG, AppTheme.CTRL_DELETE_FG,
                AppTheme.CTRL_BLOCK_SIZE, AppTheme.CTRL_CORNER_RAD, AppTheme.CTRL_FONT_SIZE
        );
        deleteBtn.setActionCommand("DELETE");
        deleteBtn.setToolTipText("Delete");
        deleteBtn.setAlignmentX(LEFT_ALIGNMENT);
        gbc.gridy = 3;
        gbc.weighty = 1.0;
        add(deleteBtn, gbc);

        // Row 4: Disk Cleanup (Delete All)
        cleanupBtn = UiUtils.createPaintedRoundedIconButton(
                "Cleanup", cleanupIcon,
                AppTheme.CTRL_CLEANUP_BG, AppTheme.CTRL_CLEANUP_FG,
                AppTheme.CTRL_BLOCK_SIZE, AppTheme.CTRL_CORNER_RAD, AppTheme.CTRL_FONT_SIZE
        );
        cleanupBtn.setActionCommand("CLEANUP");
        cleanupBtn.setToolTipText("Delete ALL tasks");
        cleanupBtn.setAlignmentX(LEFT_ALIGNMENT);
        gbc.gridy = 4;
        gbc.weighty = 1.0;
        add(cleanupBtn, gbc);

        wireActions(); // placeholders for now
    }

    // --------------------------- Actions (placeholders) ---------------------------

    private void wireActions() {
        refreshBtn.addActionListener(this::onRefresh);
        addBtn.addActionListener(this::onAdd);
        editBtn.addActionListener(this::onEdit);
        deleteBtn.addActionListener(this::onDelete);
        cleanupBtn.addActionListener(this::onCleanupAll);
    }

    private void onRefresh(ActionEvent e) {
        JOptionPane.showMessageDialog(
                getOwnerComponent(),
                "Refresh (placeholder — will call vm.reload()).",
                "Refresh",
                JOptionPane.INFORMATION_MESSAGE
        );
    }

    private void onAdd(ActionEvent e) {
        if (!openEditorDialogReflectively("add")) {
            showPlaceholderDialog("Add Task", "Add mode placeholder (no VM wiring yet).");
        }
    }

    private void onEdit(ActionEvent e) {
        if (!openEditorDialogReflectively("edit")) {
            showPlaceholderDialog("Edit Task", "Edit mode placeholder (no VM wiring yet).");
        }
    }

    private void onDelete(ActionEvent e) {
        if (showConfirmDeleteSelected()) {
            JOptionPane.showMessageDialog(
                    getOwnerComponent(),
                    "Deleted selected (placeholder — will call vm.deleteSelected()).",
                    "Delete",
                    JOptionPane.INFORMATION_MESSAGE
            );
        }
    }

    private void onCleanupAll(ActionEvent e) {
        if (showConfirmDeleteAll()) {
            JOptionPane.showMessageDialog(
                    getOwnerComponent(),
                    "All tasks deleted (placeholder — will call vm.deleteAll()).",
                    "Disk Cleanup",
                    JOptionPane.WARNING_MESSAGE
            );
        }
    }

    // --------------------------- Reflective dialog opening ---------------------------

    /**
     * Attempts to open TaskEditorDialog with common constructors:
     * (Frame,String), (Dialog,String), (Frame), (Dialog), or default ().
     * Dialog is modal, non-resizable, packed and centered.
     *
     * @param mode "add" or "edit"
     * @return true if dialog was instantiated and shown.
     */
    private boolean openEditorDialogReflectively(String mode) {
        final String[] candidates = {
                "taskmanagement.ui_test.dialogs.TaskEditorDialog",
                "taskmanagement.ui.views.dialogs.TaskEditorDialog",
                "taskmanagement.ui.dialogs.TaskEditorDialog"
        };
        Window owner = getOwnerWindow();

        for (String fqcn : candidates) {
            try {
                Class<?> clazz = Class.forName(fqcn);
                // (Frame,String)
                if (owner instanceof Frame f) {
                    Constructor<?> c = getCtor(clazz, Frame.class, String.class);
                    if (c != null && JDialog.class.isAssignableFrom(clazz)) {
                        JDialog dlg = (JDialog) c.newInstance(f, mode);
                        configureAndShowDialog(dlg, titleForMode(mode));
                        return true;
                    }
                }
                // (Dialog,String)
                if (owner instanceof Dialog d) {
                    Constructor<?> c = getCtor(clazz, Dialog.class, String.class);
                    if (c != null && JDialog.class.isAssignableFrom(clazz)) {
                        JDialog dlg = (JDialog) c.newInstance(d, mode);
                        configureAndShowDialog(dlg, titleForMode(mode));
                        return true;
                    }
                }
                // (Frame)
                if (owner instanceof Frame f2) {
                    Constructor<?> c = getCtor(clazz, Frame.class);
                    if (c != null && JDialog.class.isAssignableFrom(clazz)) {
                        JDialog dlg = (JDialog) c.newInstance(f2);
                        configureAndShowDialog(dlg, titleForMode(mode));
                        return true;
                    }
                }
                // (Dialog)
                if (owner instanceof Dialog d2) {
                    Constructor<?> c = getCtor(clazz, Dialog.class);
                    if (c != null && JDialog.class.isAssignableFrom(clazz)) {
                        JDialog dlg = (JDialog) c.newInstance(d2);
                        configureAndShowDialog(dlg, titleForMode(mode));
                        return true;
                    }
                }
                // ()
                Constructor<?> c = getCtor(clazz);
                if (c != null && JDialog.class.isAssignableFrom(clazz)) {
                    JDialog dlg = (JDialog) c.newInstance();
                    configureAndShowDialog(dlg, titleForMode(mode));
                    return true;
                }
            } catch (Throwable ignored) { /* try next candidate */ }
        }
        return false;
    }

    private String titleForMode(String mode) {
        return "add".equalsIgnoreCase(mode) ? "Add Task" : "Edit Task";
    }

    private Constructor<?> getCtor(Class<?> clazz, Class<?>... params) {
        try { return clazz.getConstructor(params); }
        catch (NoSuchMethodException ex) { return null; }
    }

    private void configureAndShowDialog(JDialog dialog, String title) {
        try { dialog.setTitle(title); } catch (Throwable ignored) {}
        dialog.setModal(true);
        dialog.setResizable(false);
        dialog.pack();
        dialog.setLocationRelativeTo(getOwnerComponent());
        dialog.setVisible(true);
        dialog.dispose();
    }

    // --------------------------- Helpers ---------------------------

    /** Ensures a non-null icon by returning a 1×1 transparent fallback. */
    private static Icon safeIcon(Icon icon) {
        if (icon != null) return icon;
        return new Icon() {
            @Override public void paintIcon(Component c, Graphics g, int x, int y) { /* no-op */ }
            @Override public int getIconWidth() { return 1; }
            @Override public int getIconHeight() { return 1; }
        };
    }

    /** Tiny placeholder modal used when the real editor dialog is absent. */
    private void showPlaceholderDialog(String title, String message) {
        JDialog dlg = new JDialog(getOwnerWindow(), title, Dialog.ModalityType.APPLICATION_MODAL);
        dlg.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        dlg.setResizable(false);

        JPanel p = new JPanel(new BorderLayout(12, 12));
        p.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));
        p.add(new JLabel("<html><body style='width:260px'>" + message + "</body></html>"),
                BorderLayout.CENTER);

        JButton ok = new JButton("Close");
        ok.addActionListener(ev -> dlg.dispose());
        JPanel south = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        south.add(ok);
        p.add(south, BorderLayout.SOUTH);

        dlg.setContentPane(p);
        dlg.pack();
        dlg.setLocationRelativeTo(getOwnerComponent());
        dlg.setVisible(true);
        dlg.dispose();
    }

    /** Confirmation for deleting selected tasks. */
    private boolean showConfirmDeleteSelected() {
        int ans = JOptionPane.showConfirmDialog(
                getOwnerComponent(),
                "Delete selected task(s)?",
                "Confirm Delete",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.WARNING_MESSAGE
        );
        return ans == JOptionPane.OK_OPTION;
    }

    /** Confirmation for deleting ALL tasks. */
    private boolean showConfirmDeleteAll() {
        int ans = JOptionPane.showConfirmDialog(
                getOwnerComponent(),
                "Delete ALL tasks? This cannot be undone.",
                "Disk Cleanup",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
        );
        return ans == JOptionPane.YES_OPTION;
    }

    // --------------------------- Accessors for wiring ---------------------------

    public JButton getRefreshButton() { return refreshBtn; }
    public JButton getAddButton()     { return addBtn; }
    public JButton getEditButton()    { return editBtn; }
    public JButton getDeleteButton()  { return deleteBtn; }
    public JButton getCleanupButton() { return cleanupBtn; }

    private Component getOwnerComponent() { return this; }
    private Window getOwnerWindow() { return SwingUtilities.getWindowAncestor(this); }
}
