package taskmanagement.ui.dialogs;

import taskmanagement.ui.styles.AppTheme;
import taskmanagement.ui.util.RoundedPanel;
import taskmanagement.ui.util.UiUtils;
import taskmanagement.application.viewmodel.ExportFormat;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.io.File;
import java.util.Optional;

/**
 * ExportDialog
 * <p>
 * Modal dialog that lets the user configure task export:
 * <ul>
 *   <li>Choose file destination</li>
 *   <li>Select export format (CSV / TXT)</li>
 *   <li>Optionally export only filtered tasks</li>
 * </ul>
 * Returns the chosen options to the caller.
 * MVVM-safe: caller is responsible for invoking the ViewModel export command.
 */
public final class ExportDialog extends JDialog {

    /**
     * Result DTO for export configuration chosen by the user.
     *
     * @param file         target file (absolute)
     * @param format       export format (CSV/TXT)
     * @param onlyFiltered whether to export only filtered tasks
     */
    public static record ExportResult(File file, ExportFormat format, boolean onlyFiltered) {}

    private final JTextField pathField = new JTextField(25);
    private final JComboBox<ExportFormat> formatCombo = new JComboBox<>(ExportFormat.values());
    private final JCheckBox filteredCheck = new JCheckBox("Export only filtered tasks");
    private JButton exportButton;
    private boolean confirmed;
    private boolean filtered;

    /**
     * Constructs the export dialog as a modal child of the given owner.
     *
     * @param owner parent window; may be null
     */
    public ExportDialog(Window owner) {
        super(owner, "Export Tasks", ModalityType.APPLICATION_MODAL);

        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setContentPane(buildContent());
        pack();
        setResizable(false);
        setLocationRelativeTo(owner);

        // Default button (Enter triggers export)
        if (exportButton != null) {
            getRootPane().setDefaultButton(exportButton);
        }

        // ESC to cancel
        getRootPane().registerKeyboardAction(e -> {
                    confirmed = false;
                    dispose();
                },
                KeyStroke.getKeyStroke("ESCAPE"),
                JComponent.WHEN_IN_FOCUSED_WINDOW
        );
    }

    // ---------------------------------------------------------------------
    // UI
    // ---------------------------------------------------------------------

    private JComponent buildContent() {
        RoundedPanel root = new RoundedPanel(AppTheme.PANEL_BG, AppTheme.WINDOW_CORNER_ARC);
        root.setLayout(new BorderLayout(0, 12));
        root.setBorder(BorderFactory.createEmptyBorder(16, 18, 16, 18));

        JLabel title = new JLabel("Export Tasks");
        title.setFont(AppTheme.CTRL_BUTTON_FONT.deriveFont(Font.BOLD, 16f));
        title.setForeground(AppTheme.MAIN_TEXT);

        JPanel form = new JPanel();
        form.setOpaque(false);
        form.setLayout(new GridBagLayout());
        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(6, 0, 6, 0);
        gc.fill = GridBagConstraints.HORIZONTAL;
        gc.weightx = 1.0;
        gc.gridx = 0;

        // File path
        gc.gridy = 0;
        form.add(makeLabel("File:"), gc);
        gc.gridy = 1;
        try {
            // If your UiUtils has this helper, use it; if not, remove this line safely.
            UiUtils.styleTextFieldForDarkCentered(pathField);
        } catch (Throwable ignored) { /* optional styling only */ }
        form.add(pathField, gc);

        JButton browseBtn = new JButton("Browse…");
        try {
            UiUtils.styleStableHoverButton(browseBtn, AppTheme.TB_EXPORT_BG, AppTheme.MAIN_TEXT);
        } catch (Throwable ignored) { /* optional styling only */ }
        browseBtn.addActionListener(e -> chooseFile());
        gc.gridy = 2;
        form.add(browseBtn, gc);

        // Format
        gc.gridy = 3;
        form.add(makeLabel("Format:"), gc);
        gc.gridy = 4;
        form.add(formatCombo, gc);

        // Filter checkbox
        gc.gridy = 5;
        filteredCheck.setOpaque(false);
        filteredCheck.setForeground(AppTheme.MAIN_TEXT);
        filteredCheck.addActionListener(e -> filtered = filteredCheck.isSelected());
        form.add(filteredCheck, gc);

        // Actions
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        actions.setOpaque(false);

        exportButton = new JButton("Export");
        JButton cancelBtn = new JButton("Cancel");

        try {
            UiUtils.styleStableHoverButton(exportButton, AppTheme.TB_EXPORT_BG, AppTheme.MAIN_TEXT);
            UiUtils.styleStableHoverButton(cancelBtn, AppTheme.DARK_GREY, AppTheme.MAIN_TEXT);
        } catch (Throwable ignored) { /* optional styling only */ }

        exportButton.addActionListener(e -> onExport());
        cancelBtn.addActionListener(e -> {
            confirmed = false;
            dispose();
        });

        actions.add(cancelBtn);
        actions.add(exportButton);

        root.add(title, BorderLayout.NORTH);
        root.add(form, BorderLayout.CENTER);
        root.add(actions, BorderLayout.SOUTH);

        return root;
    }

    private JLabel makeLabel(String text) {
        JLabel l = new JLabel(text);
        l.setForeground(AppTheme.MAIN_TEXT);
        return l;
    }

    private void onExport() {
        String p = pathField.getText().trim();
        if (p.isEmpty()) {
            JOptionPane.showMessageDialog(
                    this,
                    "Please choose a file path.",
                    "Missing File",
                    JOptionPane.WARNING_MESSAGE
            );
            return;
        }
        confirmed = true;
        dispose();
    }

    private void chooseFile() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Choose export file");
        chooser.setFileFilter(new FileNameExtensionFilter("CSV/TXT files", "csv", "txt"));

        // Suggest extension by selected format
        ExportFormat fmt = (ExportFormat) (formatCombo.getSelectedItem() != null
                ? formatCombo.getSelectedItem() : ExportFormat.CSV);
        String ext = (fmt == ExportFormat.TXT) ? "txt" : "csv";

        int result = chooser.showSaveDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File file = chooser.getSelectedFile();
            // Append extension if missing
            if (!file.getName().toLowerCase().endsWith("." + ext)) {
                file = new File(file.getParentFile(), file.getName() + "." + ext);
            }
            pathField.setText(file.getAbsolutePath());
        }
    }

    // ---------------------------------------------------------------------
    // API
    // ---------------------------------------------------------------------

    /**
     * Show the export dialog and return chosen options if confirmed.
     *
     * @param parent parent component for centering
     * @return Optional containing ExportResult if confirmed; otherwise empty
     */
    public static Optional<ExportResult> showDialog(Component parent) {
        Window owner = (parent instanceof Window) ? (Window) parent : SwingUtilities.getWindowAncestor(parent);
        ExportDialog dlg = new ExportDialog(owner);
        dlg.setVisible(true);

        if (!dlg.confirmed) {
            return Optional.empty();
        }

        File file = new File(dlg.pathField.getText().trim());
        ExportFormat format = (ExportFormat) dlg.formatCombo.getSelectedItem();
        boolean onlyFiltered = dlg.filtered;
        return Optional.of(new ExportResult(file, format, onlyFiltered));
    }
}
