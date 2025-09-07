package taskmanagement.app;

import taskmanagement.ui.MainFrame;
import taskmanagement.ui.styles.AppTheme;

import javax.swing.SwingUtilities;

/**
 * Application entry point for launching the Tasks Management Application UI.
 * <p>
 * This class ensures the Swing user interface is started on the Event Dispatch Thread (EDT),
 * applies global theme defaults, and displays the main application window.
 * </p>
 */
public final class App {

    /**
     * Private constructor to prevent instantiation.
     * This is a utility class and should not be instantiated.
     */
    private App() { }

    /**
     * Main program entry point.
     * <p>
     * Responsibilities:
     * <ul>
     *   <li>Schedules UI initialization on the Event Dispatch Thread (EDT).</li>
     *   <li>Applies global Swing theme defaults.</li>
     *   <li>Sets a default uncaught exception handler.</li>
     *   <li>Creates and shows the {@link MainFrame}.</li>
     * </ul>
     * </p>
     *
     * @param args command-line arguments (not used)
     */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            AppTheme.applyAccentDefaults();
            Thread.setDefaultUncaughtExceptionHandler((t, e) -> e.printStackTrace(System.err));
            MainFrame frame = new MainFrame();
            frame.setVisible(true);
        });
    }
}
