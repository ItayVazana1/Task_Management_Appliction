package taskmanagement.app;

import taskmanagement.persistence.derby.DerbyBootstrap;

/**
 * Optional Derby schema initializer.
 * Run this class once if you want to ensure the schema/tables exist
 * before starting the main application UI.
 */
public final class Bootstrap {

    private Bootstrap() { }

    /**
     * CLI entry point for initializing the embedded Derby schema.
     * Delegates to DerbyBootstrap.bootAndEnsureSchema().
     * @param args command-line arguments (unused)
     */
    public static void main(String[] args) {
        DerbyBootstrap.bootAndEnsureSchema();
        System.out.println("Derby schema ready.");
    }
}
