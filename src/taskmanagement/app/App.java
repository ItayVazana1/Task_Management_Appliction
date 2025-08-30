package taskmanagement.app;

import com.formdev.flatlaf.FlatLightLaf;
import taskmanagement.application.viewmodel.TasksViewModel;
import taskmanagement.persistence.DAOProvider;
import taskmanagement.persistence.ITasksDAO;
import taskmanagement.ui.MainFrame;

import javax.swing.*;

/**
 * Application entry point. Initializes Look & Feel, wires ViewModel to the UI,
 * and ensures everything runs on the Event Dispatch Thread (EDT).
 */
public final class App {

    private App() {
        // Utility class – prevent instantiation
    }

    /**
     * Main entry point. Always schedules UI creation on the EDT.
     * @param args command-line arguments (unused)
     */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                // Setup FlatLaf Look & Feel
                FlatLightLaf.setup();
                UIManager.put("Component.hideMnemonics", Boolean.TRUE); // small UX tweak

                // Wire DAO -> ViewModel -> UI
                ITasksDAO dao = DAOProvider.get();
                TasksViewModel viewModel = new TasksViewModel(dao);

                MainFrame frame = new MainFrame(viewModel);
                frame.setVisible(true);
            } catch (Throwable t) {
                t.printStackTrace();
                JOptionPane.showMessageDialog(
                        null,
                        t.getMessage(),
                        "Startup Error",
                        JOptionPane.ERROR_MESSAGE
                );
            }
        });
    }
}
