package taskmanagement.app;

import taskmanagement.application.viewmodel.TasksViewModel;
import taskmanagement.persistence.DAOProvider;
import taskmanagement.persistence.ITasksDAO;
import taskmanagement.ui.views.MainFrame;

import javax.swing.*;

/**
 * Application bootstrap class.
 * <p>
 * Responsibilities:
 * <ul>
 *   <li>Acquire the DAO instance using {@link DAOProvider}.</li>
 *   <li>Construct the {@link TasksViewModel} with the DAO.</li>
 *   <li>Launch the Swing {@link MainFrame} on the EDT.</li>
 * </ul>
 *
 * <h2>MVVM Note</h2>
 * App builds the dependency graph (DAO → ViewModel → View).
 * No model logic is placed here.
 */
public final class App {

    private App() {
        // Utility class: not instantiable
    }

    /**
     * Entry point. Starts the UI on the Event Dispatch Thread.
     *
     * @param args CLI arguments (unused)
     */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                final ITasksDAO dao = DAOProvider.get();
                final TasksViewModel vm = new TasksViewModel(dao);
                final MainFrame frame = new MainFrame(vm);
                frame.setVisible(true);
            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(
                        null,
                        ex.toString(),
                        "Startup Error",
                        JOptionPane.ERROR_MESSAGE
                );
            }
        });
    }
}
