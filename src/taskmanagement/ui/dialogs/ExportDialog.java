package taskmanagement.ui.dialogs;

import taskmanagement.ui.styles.AppTheme;
import taskmanagement.ui.util.RoundedPanel;
import taskmanagement.ui.util.UiUtils;
import taskmanagement.application.viewmodel.ExportFormat;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.util.Optional;

/**
 * ExportDialog
 * ------------
 * Modal dialog that lets the user configure task export:
 * - Choose file destination
 * - Select export format (CSV / TXT)
 * - Optionally export only filtered tasks
 *
 * Returns the chosen options to the caller.
 * MVVM-safe: caller is responsible for invoking the ViewModel export command.
 */
public final class ExportDialog extends JDialog {

    /** Container for user’s export configuration. */
    public static record ExportResult(File file, ExportFormat format, boolean onlyFiltered) {}

    private final JTextField pathField;
    private final JComboBox<ExportFormat> formatCombo;
    private final JCheckBox filteredCheck;
    private JButton exportButton;
    private boolean confirmed;

    /**
     * Constructs the export dialog as a modal child of the given owner.
     *
     * @param owner parent window; may be null
     */
    public ExportDialog(Window owner) {
        super(owner, "Export Tasks", ModalityType.APPLICATION_MODAL);
        this.pathField = new JTextField(25);
        this.formatCombo = new JComboBox<>(ExportFormat.values());
        this.filtered = false;
        this.filteredCheck = new JCheckBox("Export only filtered tasks");

        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setContentPane(buildContent());
        pack();
        setResizable(false);
        setLocationRelativeTo(owner);
        getRootPane().setDefaultButton(exportButton);
    }

    private boolean filtered;

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

        // File path
        gc.gridx = 0; gc.gridy = 0;
        form.add(new JLabel("File:"), gc);
        gc.gridy = 1;
        UiUtils.styleTextFieldForDarkCentered(pathField);
        form.add(pathField, gc);

        JButton browseBtn = new JButton("Browse…");
        UiUtils.styleStableHoverButton(browseBtn, AppTheme.TB_EXPORT_BG, AppTheme.MAIN_TEXT);
        browseBtn.addActionListener(e -> chooseFile());
        gc.gridy = 2;
        form.add(browseBtn, gc);

        // Format
        gc.gridy = 3;
        form.add(new JLabel("Format:"), gc);
        gc.gridy = 4;
        form.add(formatCombo, gc);

        // Filter checkbox
        gc.gridy = 5;
        filteredCheck.setOpaque(false);
        filteredCheck.setForeground(AppTheme.MAIN_TEXT);
        filteredCheck.addActionListener(e -> filtered = filteredCheck.isSelected());
        form.add(filteredCheck, gc);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        actions.setOpaque(false);

        exportButton = new JButton("Export");
        UiUtils.styleStableHoverButton(exportButton, AppTheme.TB_EXPORT_BG, AppTheme.MAIN_TEXT);
        JButton cancelBtn = new JButton("Cancel");
        UiUtils.styleStableHoverButton(cancelBtn, AppTheme.DARK_GREY, AppTheme.MAIN_TEXT);

        exportButton.addActionListener(e -> {
            if (pathField.getText().trim().isEmpty()) {
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
        });

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

    private void chooseFile() {
        JFileChooser chooser = new JFileChooser();
        int result = chooser.showSaveDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File file = chooser.getSelectedFile();
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
