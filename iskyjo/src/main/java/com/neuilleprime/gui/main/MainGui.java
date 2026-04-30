package com.neuilleprime.gui.main;

import com.neuilleprime.gui.screens.DebugScreen;
import com.neuilleprime.gui.screens.GameOverScreen;
import com.neuilleprime.gui.screens.GameScreen;
import com.neuilleprime.gui.screens.MenuScreen;
import com.neuilleprime.gui.screens.ResultScreen;
import com.neuilleprime.gui.screens.ShopScreen;
import com.neuilleprime.gui.utils.GameLogic;
import com.neuilleprime.gui.utils.ScreenManager;

import javafx.application.Application;
import javafx.scene.text.Font;
import javafx.stage.Stage;

/**
 * JavaFX entry point for the Iskyjo graphical user interface.
 * <p>
 * Follows the standard JavaFX {@link Application} lifecycle:
 * <ol>
 *   <li>{@link #init()} — runs on the main thread before the UI starts;
 *       used to reset shared game state.</li>
 *   <li>{@link #start(Stage)} — runs on the JavaFX Application Thread;
 *       builds all screens, registers them with a {@link ScreenManager},
 *       loads custom fonts, maximizes the window, and shows the menu.</li>
 *   <li>{@link #stop()} — runs when the window closes; used to release
 *       resources (e.g. a Discord IPC connection).</li>
 * </ol>
 * </p>
 */
public class MainGui extends Application {

    /**
     * Called before {@link #start(Stage)}, on the main thread (not the JavaFX
     * Application Thread). Resets the shared game controller to {@code null}
     * so no stale state leaks between runs.
     *
     * @throws Exception if the super {@code init()} throws
     */
    @Override
    public void init() throws Exception {
        // Appelé AVANT start(), sur le thread principal (pas le thread JavaFX)
        // Idéal pour initialiser ton GameController, charger des données, etc.

       GameLogic.gameController = null;

        super.init();
    }

    /**
     * Builds the UI, registers all screens with the {@link ScreenManager},
     * loads custom fonts, and shows the application window on the menu screen.
     *
     * @param stage the primary stage provided by the JavaFX runtime
     * @throws Exception if any screen fails to build or a font cannot be loaded
     */
    @Override
    public void start(Stage stage) throws Exception {
        // C'est ici que tu construis et montres ton UI.
        // Appelé sur le JavaFX Application Thread.
        ScreenManager sm = new ScreenManager(stage);

        DebugScreen debug = new DebugScreen(sm);
        MenuScreen menu = new MenuScreen(sm);
        GameScreen game = new GameScreen(sm);
        GameOverScreen gameover = new GameOverScreen(sm);
        ResultScreen result = new ResultScreen(sm);
        ShopScreen shop = new ShopScreen(sm);

        sm.register("debug", debug.buildScene());
        sm.register("menu", menu.buildScene());
        sm.register("game", game.buildScene());
        sm.register("gameover", gameover.buildScene());
        sm.register("result", result.buildScene());
        sm.register("shop", shop.buildScene());

        Font.loadFont(getClass().getResourceAsStream("/Assets/Fonts/VCR_OSD_MONO.ttf"), 14);
        Font.loadFont(getClass().getResourceAsStream("/Assets/Fonts/balatro.otf"), 14);

        stage.setTitle("Iskyjo");
        stage.setMaximized(true);
        sm.show("menu");
        stage.show();
    }

    /**
     * Called when the application window closes. Override to release any
     * long-lived resources (e.g. a Discord IPC connection).
     *
     * @throws Exception if the super {@code stop()} throws
     */
    @Override
    public void stop() throws Exception {
        // Appelé quand la fenêtre se ferme.
        // Idéal pour fermer des ressources (ex: ta connexion Discord IPC).
        super.stop();
    }

    /**
     * Application entry point — delegates to {@link Application#launch(String...)}.
     *
     * @param args command-line arguments (not used)
     */
    public static void main(String[] args) {
        launch(args); // démarre le cycle de vie JavaFX
    }
}
