// src/taskmanagement/app/App.java
package taskmanagement.app;

import taskmanagement.application.viewmodel.TasksViewModel;
import taskmanagement.persistence.DAOProvider;
import taskmanagement.persistence.ITasksDAO;
import taskmanagement.ui.MainFrame;

import javax.swing.SwingUtilities;

/**
 * Application entry point (UI mode).
 * - Initializes DAO and ViewModel.
 * - Launches Swing UI (MainFrame) on the EDT.
 *
 * MVVM note: App wires dependencies (DAO -> ViewModel). MainFrame is pure View.
 */
public final class App {

    private App() { /* utility class */ }

    public static void main(String[] args) {
        try {
            // Acquire DAO and construct the ViewModel
            final ITasksDAO dao = DAOProvider.get();
            final TasksViewModel vm = new TasksViewModel(dao);
            vm.reload(); // initial load from DB

            // Launch UI on the EDT
            SwingUtilities.invokeLater(() -> {
                // log unexpected exceptions instead of silent failures
                Thread.setDefaultUncaughtExceptionHandler((t, e) -> e.printStackTrace(System.err));
                // Create and show the main window
                MainFrame frame = new MainFrame(); // אם יש לך בנאי שמקבל VM, אפשר: new MainFrame(vm)
                frame.setVisible(true);
            });

        } catch (Exception ex) {
            ex.printStackTrace(System.err);
            System.exit(1);
        }
    }
}
