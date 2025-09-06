package taskmanagement.app;

import taskmanagement.application.viewmodel.TasksViewModel;
import taskmanagement.persistence.DAOProvider;
import taskmanagement.persistence.ITasksDAO;
import taskmanagement.ui.MainFrame;
import taskmanagement.ui.styles.AppTheme;

import javax.swing.SwingUtilities;

/**
 * Application entry point (UI mode).
 * - Initializes DAO and ViewModel.
 * - Applies global UI defaults (accent, selection colors).
 * - Launches Swing UI (MainFrame) on the EDT.
 *
 * MVVM note: App wires dependencies (DAO -> ViewModel). MainFrame is pure View.
 */
public final class App {

    private App() { /* utility class */ }

    public static void main(String[] args) {
        try {
            // Acquire DAO and construct the ViewModel (off-EDT is fine here)
            final ITasksDAO dao = DAOProvider.get();
            final TasksViewModel vm = new TasksViewModel(dao);
            vm.reload(); // initial load from DB

            // Launch UI on the EDT
            SwingUtilities.invokeLater(() -> {
                // Apply global Swing defaults (remove blue accent, set dark selections, etc.)
                AppTheme.applyAccentDefaults();

                // Log unexpected exceptions instead of silent failures
                Thread.setDefaultUncaughtExceptionHandler((t, e) -> e.printStackTrace(System.err));

                // Create and show the main window
                // Prefer constructor that accepts the ViewModel to keep MVVM wiring explicit:
                // MainFrame frame = new MainFrame(vm);
                MainFrame frame = new MainFrame(); // use this if your MainFrame has no-arg ctor
                frame.setVisible(true);
            });

        } catch (Exception ex) {
            ex.printStackTrace(System.err);
            System.exit(1);
        }
    }
}
