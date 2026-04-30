package com.neuilleprime;

/**
 * Console entry point for Iskyjo.
 * <p>
 * Running this class directly prints usage information.
 * To launch the graphical version of the game, use the JavaFX entry point
 * {@link com.neuilleprime.gui.main.MainGui} instead.
 * </p>
 */
public class Main {

    /**
     * Prints usage information to stdout.
     *
     * @param args command-line arguments (not used)
     */
    public static void main(String[] args) {
        System.out.println("  Iskyjo - by Enrtarr & maaple");
        System.out.println();
        System.out.println("To run the game, use the JavaFX GUI entry point:");
        System.out.println();
        System.out.println("  mvn javafx:run");
        System.out.println();
        System.out.println("Requirements:");
        System.out.println("  - Java 21 or later");
        System.out.println("  - Maven");
        System.out.println("  - JavaFX 21");
        System.out.println();
        System.out.println("Run from the iskyjo/ directory (where pom.xml is located)");
    }
}
