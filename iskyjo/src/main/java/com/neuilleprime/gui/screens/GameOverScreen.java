package com.neuilleprime.gui.screens;

import com.neuilleprime.gui.utils.AssetLoader;
import com.neuilleprime.gui.utils.ScreenManager;

import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;

/**
 * Screen displayed when the player loses the game (score below the round quota).
 * <p>
 * Shows the studio logo and a button that returns to the main menu.
 * </p>
 */
public class GameOverScreen {

    /** Screen manager used to navigate between scenes. */
    private final ScreenManager sm;

    /**
     * Constructs a {@code GameOverScreen}.
     *
     * @param sm the application screen manager
     */
    public GameOverScreen(ScreenManager sm) {
        this.sm = sm;
    }

    /**
     * Builds and returns the game-over {@link Scene}.
     *
     * @return the constructed {@link Scene}
     */
    public Scene buildScene() {
        VBox root = new VBox(20);
        root.setAlignment(Pos.CENTER);

        ImageView logo = new ImageView(AssetLoader.NP_LOGO);

        Button menuButton = new Button();
        menuButton.setGraphic(new ImageView(AssetLoader.BUTTON_PLAY));
        menuButton.setStyle("-fx-background-color: transparent;");
        menuButton.setOnAction(e -> {
            sm.show("menu");
        });

        root.getChildren().addAll(logo, menuButton);

        Scene scene = new Scene(root, 1280, 720);
        scene.getStylesheets().add(getClass().getResource("/style.css").toExternalForm());
        return scene;
    }
}
