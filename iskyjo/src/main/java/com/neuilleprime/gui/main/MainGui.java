package com.neuilleprime.gui.main;

import com.neuilleprime.gui.screens.GameScreen;
import com.neuilleprime.gui.screens.MenuScreen;
import com.neuilleprime.gui.utils.GameLogic;
import com.neuilleprime.gui.utils.ScreenManager;

import javafx.application.Application;
import javafx.scene.text.Font;
import javafx.stage.Stage;

public class MainGui extends Application {

    @Override
    public void init() throws Exception {
        // Appelé AVANT start(), sur le thread principal (pas le thread JavaFX)
        // Idéal pour initialiser ton GameController, charger des données, etc.

       GameLogic.gameController = null;

        super.init();
    }

    @Override
    public void start(Stage stage) throws Exception {
        // C'est ici que tu construis et montres ton UI.
        // Appelé sur le JavaFX Application Thread.
        ScreenManager sm = new ScreenManager(stage);

        GameScreen debug = new GameScreen(sm);
        MenuScreen menu = new MenuScreen(sm);
        GameScreen game = new GameScreen(sm);

        sm.register("debug", debug.buildScene());
        sm.register("menu", menu.buildScene());
        sm.register("game", game.buildScene());

        Font.loadFont(getClass().getResourceAsStream("/Assets/Fonts/VCR_OSD_MONO.ttf"), 14);
        Font.loadFont(getClass().getResourceAsStream("/Assets/Fonts/balatro.otf"), 14);


        stage.setTitle("Iskyjo");
        stage.setWidth(1280);
        stage.setHeight(720);
        sm.show("menu");
        stage.show();
    }

    @Override
    public void stop() throws Exception {
        // Appelé quand la fenêtre se ferme.
        // Idéal pour fermer des ressources (ex: ta connexion Discord IPC).
        super.stop();
    }

    public static void main(String[] args) {
        launch(args); // démarre le cycle de vie JavaFX
    }
}