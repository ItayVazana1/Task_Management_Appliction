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
 * Three rounded square buttons (Add, Edit, Delete), each size=AppTheme.CTRL_BLOCK_SIZE,
 * centered in the middle of each vertical third of the panel.
 * Each button shows an icon on top (from resources) and text at the bottom.
 *
 * Adjustments:
 * - Fixed sidebar width: preferred == minimum so the center panel cannot steal space.
 * - Subtle inner padding so buttons don't stick to the edge.
 * - Safe icon loading (panel still renders even if an icon is missing).
 *
 * Behavior (temporary, no-VM):
 * - Add/Edit → tries to open TaskEditorDialog reflectively (several package candidates).
 *              If missing → shows a minimal placeholder dialog and closes.
 * - Delete   → confirm dialog, then info message (no-op).
 */
public class ControlPanel extends RoundedPanel {

    /** Horizontal padding inside the panel (px). */
    private static final int INNER_PAD = 8;
    /** Fallback width if ContentArea does not lock width externally. */
    private static final int FALLBACK_WIDTH = 220;

    private final JButton addBtn;
    private final JButton editBtn;
    private final JButton deleteBtn;

    public ControlPanel() {
        super(new Color(0x2C2C2C), 16);
        setOpaque(false);

        // --- Lock a sane sidebar width (preferred == minimum) ---
        int block = AppTheme.CTRL_BLOCK_SIZE; // expected to be a square button size
        int fixedWidth = Math.max(FALLBACK_WIDTH, block + INNER_PAD * 2);
        Dimension fixed = new Dimension(fixedWidth, 10);
        setPreferredSize(fixed);
        setMinimumSize(fixed);

        // Optional inner padding so the rounded background breathes.
        setBorder(BorderFactory.createEmptyBorder(INNER_PAD, INNER_PAD, INNER_PAD, INNER_PAD));

        // --- Layout: 3 rows, each vertically centered within its third ---
        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.weightx = 1.0;
        gbc.insets = new Insets(0, 0, 0, 0);

        // Load icons from resources/taskmanagement.ui_test/icons/ (safe fallback if null)
        Icon addIcon  = safeIcon(UiUtils.loadRasterIcon(
                "/taskmanagement/ui/resources/add.png",
                AppTheme.CTRL_ICON_SIZE, AppTheme.CTRL_ICON_SIZE));

        Icon editIcon = safeIcon(UiUtils.loadRasterIcon(
                "/taskmanagement/ui/resources/edit.png",
                AppTheme.CTRL_ICON_SIZE, AppTheme.CTRL_ICON_SIZE));

        Icon delIcon  = safeIcon(UiUtils.loadRasterIcon(
                "/taskmanagement/ui/resources/delete.png",
                AppTheme.CTRL_ICON_SIZE, AppTheme.CTRL_ICON_SIZE));


        // Row 0: Add
        addBtn = UiUtils.createPaintedRoundedIconButton(
                "Add",
                addIcon,
                AppTheme.CTRL_ADD_BG,
                AppTheme.CTRL_ADD_FG,
                AppTheme.CTRL_BLOCK_SIZE,
                AppTheme.CTRL_CORNER_RAD,
                AppTheme.CTRL_FONT_SIZE
        );
        addBtn.setActionCommand("ADD");
        addBtn.setToolTipText("Add");
        addBtn.setAlignmentX(LEFT_ALIGNMENT);
        gbc.gridy = 0;
        gbc.weighty = 1.0;
        add(addBtn, gbc);

        // Row 1: Edit
        editBtn = UiUtils.createPaintedRoundedIconButton(
                "Edit",
                editIcon,
                AppTheme.CTRL_EDIT_BG,
                AppTheme.CTRL_EDIT_FG,
                AppTheme.CTRL_BLOCK_SIZE,
                AppTheme.CTRL_CORNER_RAD,
                AppTheme.CTRL_FONT_SIZE
        );
        editBtn.setActionCommand("EDIT");
        editBtn.setToolTipText("Edit");
        editBtn.setAlignmentX(LEFT_ALIGNMENT);
        gbc.gridy = 1;
        gbc.weighty = 1.0;
        add(editBtn, gbc);

        // Row 2: Delete
        deleteBtn = UiUtils.createPaintedRoundedIconButton(
                "Delete",
                delIcon,
                AppTheme.CTRL_DELETE_BG,
                AppTheme.CTRL_DELETE_FG,
                AppTheme.CTRL_BLOCK_SIZE,
                AppTheme.CTRL_CORNER_RAD,
                AppTheme.CTRL_FONT_SIZE
        );
        deleteBtn.setActionCommand("DELETE");
        deleteBtn.setToolTipText("Delete");
        deleteBtn.setAlignmentX(LEFT_ALIGNMENT);
        gbc.gridy = 2;
        gbc.weighty = 1.0;
        add(deleteBtn, gbc);

        // Wire actions (no-VM)
        wireActions();
    }

    // --------------------------- Actions ---------------------------

    private void wireActions() {
        addBtn.addActionListener(this::onAdd);
        editBtn.addActionListener(this::onEdit);
        deleteBtn.addActionListener(this::onDelete);
    }

    private void onAdd(ActionEvent e) {
        boolean opened = openEditorDialogReflectively("add");
        if (!opened) {
            showPlaceholderDialog("Add Task",
                    "Add mode placeholder (no ViewModel wired yet).");
        }
    }

    private void onEdit(ActionEvent e) {
        boolean opened = openEditorDialogReflectively("edit");
        if (!opened) {
            showPlaceholderDialog("Edit Task",
                    "Edit mode placeholder (no selection / VM wired yet).");
        }
    }

    private void onDelete(ActionEvent e) {
        if (showConfirmDelete()) {
            JOptionPane.showMessageDialog(
                    getOwnerComponent(),
                    "Deleted (no-op for now).",
                    "Delete",
                    JOptionPane.INFORMATION_MESSAGE
            );
        }
    }

    // --------------------------- Reflective dialog open ---------------------------

    /**
     * Tries to open TaskEditorDialog using several FQCN candidates and common ctors:
     * (Frame,String), (Dialog,String), (Frame), (Dialog), ().
     * Makes it modal, non-resizable, packed and centered.
     *
     * @param mode "add" or "edit"
     * @return true if opened successfully; false for fallback.
     */
    private boolean openEditorDialogReflectively(String mode) {
        final String[] candidates = {
                "taskmanagement.ui_test.dialogs.TaskEditorDialog",
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
            } catch (Throwable ignored) {
                // try next candidate
            }
        }
        return false;
    }

    private String titleForMode(String mode) {
        return "add".equalsIgnoreCase(mode) ? "Add Task" : "Edit Task";
    }

    private Constructor<?> getCtor(Class<?> clazz, Class<?>... params) {
        try {
            return clazz.getConstructor(params);
        } catch (NoSuchMethodException ex) {
            return null;
        }
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

    /** Returns a non-null icon (empty 1×1 transparent) if the provided icon is null. */
    private static Icon safeIcon(Icon icon) {
        if (icon != null) return icon;
        // Minimal transparent icon to keep layout stable if resource is missing.
        return new Icon() {
            @Override public void paintIcon(Component c, Graphics g, int x, int y) { /* no-op */ }
            @Override public int getIconWidth() { return 1; }
            @Override public int getIconHeight() { return 1; }
        };
    }

    /** Small placeholder modal if TaskEditorDialog is absent. */
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

    /** Confirm delete dialog (customizable later). */
    private boolean showConfirmDelete() {
        int ans = JOptionPane.showConfirmDialog(
                getOwnerComponent(),
                "Delete selected task(s)?",
                "Confirm Delete",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.WARNING_MESSAGE
        );
        return ans == JOptionPane.OK_OPTION;
    }

    public JButton getAddButton()    { return addBtn; }
    public JButton getEditButton()   { return editBtn; }
    public JButton getDeleteButton() { return deleteBtn; }

    private Component getOwnerComponent() {
        return this;
    }

    private Window getOwnerWindow() {
        return SwingUtilities.getWindowAncestor(this);
    }
}
