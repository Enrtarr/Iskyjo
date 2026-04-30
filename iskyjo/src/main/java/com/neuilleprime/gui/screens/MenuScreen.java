package com.neuilleprime.gui.screens;

import com.neuilleprime.game.actions.BeginGameAction;
import com.neuilleprime.gui.utils.AssetLoader;
import com.neuilleprime.gui.utils.GameLogic;
import com.neuilleprime.gui.utils.ScreenManager;

import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;

/**
 * The main menu screen shown when the application starts.
 * <p>
 * Displays the studio logo and two buttons:
 * <ul>
 *   <li><b>Play</b> — executes a {@link BeginGameAction} on the game controller
 *       and switches to the game screen.</li>
 *   <li><b>Leaderboard</b> — placeholder button (not yet implemented).</li>
 * </ul>
 * </p>
 */
public class MenuScreen {

    /** Screen manager used to navigate between scenes. */
    private final ScreenManager sm;

    /**
     * Constructs a {@code MenuScreen}.
     *
     * @param sm the application screen manager
     */
    public MenuScreen(ScreenManager sm) {
        this.sm = sm;
    }

    /**
     * Builds and returns the menu {@link Scene}.
     * <p>
     * Assumes {@link GameLogic#gameController} has already been initialised
     * before the Play button is pressed.
     * </p>
     *
     * @return the constructed {@link Scene}
     */
    public Scene buildScene() {
        VBox root = new VBox(20);
        root.setAlignment(Pos.CENTER);

        ImageView logo = new ImageView(AssetLoader.NP_LOGO);

        Button playButton = new Button();
        playButton.setGraphic(new ImageView(AssetLoader.BUTTON_PLAY));
        playButton.setStyle("-fx-background-color: transparent;");
        playButton.setOnAction(e -> {
            GameLogic.gameController.execute(new BeginGameAction());
            sm.show("game");
        });

        Button lbBtn = new Button();
        lbBtn.setGraphic(new ImageView(AssetLoader.BUTTON_LEADERBOARD));
        lbBtn.setStyle("-fx-background-color: transparent;");

        root.getChildren().addAll(logo, playButton, lbBtn);

        Scene scene = new Scene(root, 1280, 720);
        scene.getStylesheets().add(getClass().getResource("/style.css").toExternalForm());

        return scene;
    }
}
