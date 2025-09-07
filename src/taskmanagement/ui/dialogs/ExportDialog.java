package taskmanagement.ui.dialogs;

import taskmanagement.ui.styles.AppTheme;
import taskmanagement.ui.util.RoundedPanel;
import taskmanagement.ui.util.UiUtils;
import taskmanagement.application.viewmodel.ExportFormat;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.io.File;
import java.util.Locale;
import java.util.Optional;

/**
 * Modal dialog for configuring task export (file destination and format).
 * <p>
 * Presentation-only (MVVM-safe). The caller is responsible for invoking the
 * ViewModel export command based on the returned {@link #showDialog(Component)} result.
 */
public final class ExportDialog extends JDialog {

    /**
     * Result of the export configuration chosen by the user.
     *
     * @param file         the target file
     * @param format       the selected export format
     * @param onlyFiltered legacy flag retained for API compatibility (always {@code false})
     */
    public static record ExportResult(File file, ExportFormat format, boolean onlyFiltered) {}

    private final JTextField pathField = new JTextField(28);
    private final JComboBox<ExportFormat> formatCombo = new JComboBox<>(ExportFormat.values());
    private JButton exportButton;
    private boolean confirmed;

    /**
     * Constructs the export dialog as a modal child of the given owner.
     *
     * @param owner parent window; may be {@code null}
     */
    public ExportDialog(Window owner) {
        super(owner, "Export Tasks", ModalityType.APPLICATION_MODAL);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setContentPane(buildContent());
        pack();
        setResizable(false);
        setLocationRelativeTo(owner);
        if (exportButton != null) {
            getRootPane().setDefaultButton(exportButton);
        }
        getRootPane().registerKeyboardAction(e -> {
                    confirmed = false;
                    dispose();
                },
                KeyStroke.getKeyStroke("ESCAPE"),
                JComponent.WHEN_IN_FOCUSED_WINDOW
        );
    }

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

        gc.gridy = 0;
        form.add(makeLabel("File:"), gc);
        gc.gridy = 1;
        try {
            UiUtils.styleTextFieldForDarkCentered(pathField);
        } catch (Throwable ignored) { }
        form.add(pathField, gc);

        JButton browseBtn = new JButton("Browse…");
        try {
            UiUtils.styleStableHoverButton(browseBtn, AppTheme.TB_SHOW_SELECTED_BG, AppTheme.MAIN_TEXT);
        } catch (Throwable ignored) { }
        browseBtn.addActionListener(e -> chooseFile());
        gc.gridy = 2;
        form.add(browseBtn, gc);

        gc.gridy = 3;
        form.add(makeLabel("Format:"), gc);
        gc.gridy = 4;

        formatCombo.setRenderer(new DefaultListCellRenderer() {
            @Override public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                                                                    boolean isSelected, boolean cellHasFocus) {
                String text = (value instanceof ExportFormat ef)
                        ? switch (ef) { case CSV -> "CSV (Comma-Separated)"; case TXT -> "TXT (Plain Text)"; }
                        : String.valueOf(value);
                return super.getListCellRendererComponent(list, text, index, isSelected, cellHasFocus);
            }
        });
        formatCombo.addActionListener(e -> harmonizePathWithSelectedFormat());
        form.add(formatCombo, gc);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        actions.setOpaque(false);

        exportButton = new JButton("Export");
        JButton cancelBtn = new JButton("Cancel");

        try {
            UiUtils.styleStableHoverButton(exportButton, AppTheme.TB_FILTER_APPLY_BG, AppTheme.MAIN_TEXT);
            UiUtils.styleStableHoverButton(cancelBtn, AppTheme.TB_SORT_RESET_BG, AppTheme.MAIN_TEXT);
        } catch (Throwable ignored) { }

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
        ExportFormat fmt = currentFormat();
        File f = new File(p);
        f = ensureExtension(f, extFor(fmt));
        pathField.setText(f.getAbsolutePath());
        confirmed = true;
        dispose();
    }

    private void chooseFile() {
        ExportFormat fmt = currentFormat();
        String ext = extFor(fmt);

        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Choose export file");
        chooser.setFileFilter(new FileNameExtensionFilter(ext.toUpperCase(Locale.ROOT) + " files", ext));

        if (pathField.getText().isBlank()) {
            chooser.setSelectedFile(new File("tasks_export." + ext));
        }

        int result = chooser.showSaveDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File file = chooser.getSelectedFile();
            file = ensureExtension(file, ext);
            pathField.setText(file.getAbsolutePath());
        }
    }

    private void harmonizePathWithSelectedFormat() {
        String p = pathField.getText().trim();
        if (p.isEmpty()) return;
        String ext = extFor(currentFormat());
        File f = ensureExtension(new File(p), ext);
        pathField.setText(f.getAbsolutePath());
    }

    private ExportFormat currentFormat() {
        Object sel = formatCombo.getSelectedItem();
        return (sel instanceof ExportFormat ef) ? ef : ExportFormat.CSV;
    }

    private static String extFor(ExportFormat fmt) {
        return (fmt == ExportFormat.TXT) ? "txt" : "csv";
    }

    private static File ensureExtension(File file, String ext) {
        String name = file.getName();
        int dot = name.lastIndexOf('.');
        if (dot > 0) {
            String current = name.substring(dot + 1).toLowerCase(Locale.ROOT);
            if (!current.equals(ext)) {
                name = name.substring(0, dot) + "." + ext;
            }
        } else {
            name = name + "." + ext;
        }
        return new File(file.getParentFile() == null ? new File(".") : file.getParentFile(), name);
    }

    /**
     * Shows the export dialog and returns chosen options if confirmed.
     *
     * @param parent parent component for centering
     * @return an {@link Optional} containing {@link ExportResult} if confirmed; otherwise empty
     */
    public static Optional<ExportResult> showDialog(Component parent) {
        Window owner = (parent instanceof Window) ? (Window) parent : SwingUtilities.getWindowAncestor(parent);
        ExportDialog dlg = new ExportDialog(owner);
        dlg.setVisible(true);
        if (!dlg.confirmed) {
            return Optional.empty();
        }
        File file = new File(dlg.pathField.getText().trim());
        ExportFormat format = dlg.currentFormat();
        boolean onlyFiltered = false;
        return Optional.of(new ExportResult(file, format, onlyFiltered));
    }
}
