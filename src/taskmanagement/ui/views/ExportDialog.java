package taskmanagement.ui.views;

import taskmanagement.application.viewmodel.ExportFormat;
import taskmanagement.application.viewmodel.TasksViewModel;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

/**
 * Modal dialog for exporting tasks to CSV/TXT.
 * MVVM: talks only to TasksViewModel (no model/DAO here).
 */
public final class ExportDialog extends JDialog {

    private final TasksViewModel vm;

    private final JTextField pathField = new JTextField(28);
    private final JButton browseBtn = new JButton("Browse…");
    private final JButton exportBtn = new JButton("Export");
    private final JButton cancelBtn = new JButton("Cancel");

    private final JRadioButton csvRadio = new JRadioButton("CSV", true);
    private final JRadioButton txtRadio = new JRadioButton("TXT");

    /**
     * Creates the export dialog.
     * @param owner parent window
     * @param vm view model to invoke export on
     */
    public ExportDialog(Window owner, TasksViewModel vm) {
        super(owner, "Export Tasks", ModalityType.APPLICATION_MODAL);
        this.vm = Objects.requireNonNull(vm, "vm");

        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setContentPane(buildUI());
        pack();
        setLocationRelativeTo(owner);

        // Wire actions
        browseBtn.addActionListener(this::onBrowse);
        exportBtn.addActionListener(this::onExport);
        cancelBtn.addActionListener(e -> dispose());
    }

    private JPanel buildUI() {
        final JPanel root = new JPanel(new BorderLayout(12, 12));
        root.setBorder(new EmptyBorder(12, 12, 12, 12));

        // Path row
        final JPanel pathRow = new JPanel(new BorderLayout(6, 6));
        pathRow.add(new JLabel("Output file:"), BorderLayout.WEST);
        pathRow.add(pathField, BorderLayout.CENTER);
        pathRow.add(browseBtn, BorderLayout.EAST);

        // Format group
        final JPanel fmtPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        final ButtonGroup fmtGroup = new ButtonGroup();
        fmtGroup.add(csvRadio);
        fmtGroup.add(txtRadio);
        fmtPanel.add(new JLabel("Format:"));
        fmtPanel.add(csvRadio);
        fmtPanel.add(txtRadio);

        // Scope group
        final JPanel scopePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));


        // Buttons
        final JPanel btns = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btns.add(cancelBtn);
        btns.add(exportBtn);

        final JPanel center = new JPanel();
        center.setLayout(new BoxLayout(center, BoxLayout.Y_AXIS));
        center.add(pathRow);
        center.add(Box.createVerticalStrut(8));
        center.add(fmtPanel);
        center.add(Box.createVerticalStrut(8));
        center.add(scopePanel);

        root.add(center, BorderLayout.CENTER);
        root.add(btns, BorderLayout.SOUTH);
        return root;
    }

    private void onBrowse(ActionEvent e) {
        final JFileChooser fc = new JFileChooser();
        fc.setDialogTitle("Choose output file");
        final int res = fc.showSaveDialog(this);
        if (res == JFileChooser.APPROVE_OPTION && fc.getSelectedFile() != null) {
            final boolean isCsv = csvRadio.isSelected();
            Path chosen = fc.getSelectedFile().toPath();
            // Ensure extension matches format
            final String name = chosen.getFileName().toString().toLowerCase();
            if (isCsv && !name.endsWith(".csv")) {
                chosen = chosen.resolveSibling(chosen.getFileName() + ".csv");
            } else if (!isCsv && !name.endsWith(".txt")) {
                chosen = chosen.resolveSibling(chosen.getFileName() + ".txt");
            }
            pathField.setText(chosen.toString());
        }
    }

    private void onExport(ActionEvent e) {
        try {
            if (pathField.getText().isBlank()) {
                JOptionPane.showMessageDialog(this, "Please choose an output file.", "Export",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }
            final ExportFormat fmt = csvRadio.isSelected() ? ExportFormat.CSV : ExportFormat.TXT;
            final Path out = Path.of(pathField.getText());

            // Ask before overwrite
            if (Files.exists(out)) {
                final int ans = JOptionPane.showConfirmDialog(this,
                        "File exists. Overwrite?\n" + out,
                        "Confirm Overwrite", JOptionPane.OK_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE);
                if (ans != JOptionPane.OK_OPTION) return;
            }

            // Export using VM (flat tasks export).
            // NOTE: if you want exact rows currently visible, call the overload with filtered IDs.
            vm.exportTasks(out, fmt, false);

            JOptionPane.showMessageDialog(this,
                    "Exported successfully to:\n" + out,
                    "Export", JOptionPane.INFORMATION_MESSAGE);
            dispose();
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this,
                    "Export failed (IO):\n" + ex.getMessage(),
                    "Export Error", JOptionPane.ERROR_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    "Export failed:\n" + ex.getMessage(),
                    "Export Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
