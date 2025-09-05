package ui_test.widgets;

import ui_test.styles.AppTheme;
import ui_test.util.UiUtils;
import ui_test.util.RoundedPanel;

import javax.swing.*;
import java.awt.*;

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
        // If you also lock width in ContentArea, this simply complements it.
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

        // Load icons from resources/ui_test/icons/ (safe fallback if null)
        Icon addIcon  = safeIcon(UiUtils.loadRasterIcon("/ui_test/resources/add.png",
                AppTheme.CTRL_ICON_SIZE, AppTheme.CTRL_ICON_SIZE));
        Icon editIcon = safeIcon(UiUtils.loadRasterIcon("/ui_test/resources/edit.png",
                AppTheme.CTRL_ICON_SIZE, AppTheme.CTRL_ICON_SIZE));
        Icon delIcon  = safeIcon(UiUtils.loadRasterIcon("/ui_test/resources/delete.png",
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
    }

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

    public JButton getAddButton()    { return addBtn; }
    public JButton getEditButton()   { return editBtn; }
    public JButton getDeleteButton() { return deleteBtn; }
}
