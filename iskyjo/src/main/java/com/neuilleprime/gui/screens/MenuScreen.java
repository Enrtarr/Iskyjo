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

public class MenuScreen {

    private final ScreenManager sm;

    public MenuScreen(ScreenManager sm) {
        this.sm = sm;
    }

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
