package com.neuilleprime.gui.screens;

import com.neuilleprime.gui.utils.AssetLoader;
import com.neuilleprime.gui.utils.ScreenManager;

import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;

public class GameOverScreen {

    private final ScreenManager sm;

    public GameOverScreen(ScreenManager sm) {
        this.sm = sm;
    }

    public Scene buildScene() {
        VBox root = new VBox(20);
        root.setAlignment(Pos.CENTER);

        ImageView logo = new ImageView(AssetLoader.NP_LOGO);

        Button menuButton = new Button();
        menuButton.setGraphic(new ImageView(AssetLoader.BUTTON_PLAY));
        menuButton.setStyle("-fx-background-color: transparent;");
        menuButton.setOnAction(e -> {
            // GameLogic.gameController.execute(new BeginGameAction());
            // GameLogic.gameController = null;
            sm.show("menu");
        });

        root.getChildren().addAll(logo, menuButton);

        Scene scene = new Scene(root, 1280, 720);
        scene.getStylesheets().add(getClass().getResource("/style.css").toExternalForm());
        return scene;
    }
}
