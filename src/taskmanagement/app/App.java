package taskmanagement.app;

import taskmanagement.ui.MainFrame;
import taskmanagement.ui.styles.AppTheme;

import javax.swing.SwingUtilities;

/**
 * Application entry point (UI mode).
 * <p>
 * Responsibilities:
 * <ul>
 *   <li>Ensure Swing is launched on the EDT.</li>
 *   <li>Apply global UI defaults (accent and selection colors).</li>
 *   <li>Create and show the {@link MainFrame}.</li>
 * </ul>
 * </p>
 * <p>
 * MVVM note: The ViewModel/DAO wiring is intentionally deferred to the UI layer
 * (see {@code MainFrame}) per the current connection approach for this chat.
 * This class performs no model/persistence work and keeps startup minimal.
 * </p>
 */
public final class App {

    /** Utility class: no instances. */
    private App() { }

    /**
     * Program entry point. Schedules UI initialization on the EDT,
     * applies global theme defaults, sets a default uncaught exception handler,
     * and shows the main application window.
     *
     * @param args command-line arguments (unused)
     */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            // Apply global Swing defaults (e.g., remove blue accent, dark selections).
            AppTheme.applyAccentDefaults();

            // Log unexpected exceptions instead of silent failures.
            Thread.setDefaultUncaughtExceptionHandler((t, e) -> e.printStackTrace(System.err));

            // Create and show the main window.
            MainFrame frame = new MainFrame(); // preserve existing public API (no-arg constructor)
            frame.setVisible(true);
        });
    }
}
