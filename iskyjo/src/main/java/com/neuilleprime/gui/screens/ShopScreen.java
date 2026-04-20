package com.neuilleprime.gui.screens;

import com.neuilleprime.game.Player;
import com.neuilleprime.game.actions.ReadyUpAction;
import com.neuilleprime.game.events.GameEventListener;
import com.neuilleprime.game.events.RoundEndedEvent;
import com.neuilleprime.game.events.ShopRerolledEvent;
import com.neuilleprime.game.events.TurnStartedEvent;
import com.neuilleprime.gui.components.VTextBox;
import com.neuilleprime.gui.utils.AssetLoader;
import com.neuilleprime.gui.utils.GameLogic;
import com.neuilleprime.gui.utils.ScreenManager;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

public class ShopScreen {

    private final ScreenManager sm;
    // private final RoundEndedEvent event;
    private boolean ready = false;

    public ShopScreen(ScreenManager sm) {
        this.sm = sm;
    }

    public Scene buildScene() {
        BorderPane root = new BorderPane();

        Scene scene = new Scene(root, 1280, 720);
        scene.getStylesheets().add(getClass().getResource("/style.css").toExternalForm());

        root.getStyleClass().add("root");

        VBox leftBar = new VBox();
        leftBar.setAlignment(Pos.CENTER);
        leftBar.prefWidthProperty().bind(scene.widthProperty().multiply(.1));
        // leftBar.prefHeightProperty().bind(scene.heightProperty().multiply(1));

        VBox rightBar = new VBox();
        rightBar.setAlignment(Pos.CENTER);
        rightBar.prefWidthProperty().bind(scene.widthProperty().multiply(.1));
        // rightBar.prefHeightProperty().bind(scene.heightProperty().multiply(1));

        HBox topBar = new HBox();
        topBar.setAlignment(Pos.CENTER);
        // topBar.prefWidthProperty().bind(scene.widthProperty().multiply(1));
        topBar.prefHeightProperty().bind(scene.heightProperty().multiply(.1));

        HBox bottomBar = new HBox();
        bottomBar.setAlignment(Pos.CENTER);
        // bottomBar.prefWidthProperty().bind(scene.widthProperty().multiply(1));
        // bottomBar.prefHeightProperty().bind(scene.heightProperty().multiply(1));

        StackPane centerBar = new StackPane();
        centerBar.setAlignment(Pos.CENTER);
        // centerBar.prefWidthProperty().bind(scene.widthProperty().multiply(1));
        // centerBar.prefHeightProperty().bind(scene.heightProperty().multiply(1));

        

        // Player player = GameLogic.gameController.getCurrentPlayer();
        Player player = GameLogic.localPlayer;

        root.setLeft(leftBar);
        root.setRight(rightBar);
        root.setTop(topBar);
        root.setBottom(bottomBar);
        root.setCenter(centerBar);

        // delay(2500, () -> {
        //     deck.removeColumns();
        //     deck.removeColumn(0);
        // });

        GameLogic.gameController.addListener(player, new GameEventListener() {
            @Override
            public void shopRerolledEvent(ShopRerolledEvent event) {
                Platform.runLater(() -> {
                    leftBar.getChildren().clear();

                    VTextBox moneyView = new VTextBox("Money");
                    moneyView.setText(player.getMoney()+"₣");
                    moneyView.prefWidthProperty().bind(rightBar.prefWidthProperty());
                    moneyView.prefHeightProperty().bind(rightBar.prefHeightProperty());

                    leftBar.getChildren().add(moneyView);
                });
            }
            
            @Override
            public void onTurnStarted(TurnStartedEvent event) {
                System.out.println("baka");
                sm.show("game");
            }

            @Override
            public void onRoundEnded(RoundEndedEvent event) {
                Platform.runLater(() -> {

                    leftBar.getChildren().clear();

                    VTextBox moneyView = new VTextBox("Money");
                    moneyView.setText(player.getMoney()+"₣");
                    moneyView.setNameColor("#00a6ff");
                    moneyView.setContentColor("#000000");
                    moneyView.setBackgroundColor("#2da900");
                    moneyView.setBorderColor("#217c00");
                    moneyView.setContentSize(.1);
                    moneyView.prefWidthProperty().bind(rightBar.prefWidthProperty());
                    moneyView.prefHeightProperty().bind(rightBar.prefHeightProperty());

                    leftBar.getChildren().add(moneyView);

                    ready = false; 
                    topBar.getChildren().clear();
                    Button nextRoundButton = new Button();
                    nextRoundButton.setGraphic(new ImageView(AssetLoader.BUTTON_PLAY));
                    nextRoundButton.setStyle("-fx-background-color: transparent;");
                    nextRoundButton.setOnAction(e -> {
                        if (!ready) {
                            GameLogic.gameController.execute(new ReadyUpAction());
                            ready = true;
                        }
                    });
                    topBar.getChildren().add(nextRoundButton);
                });
            }

        });

        return scene;
    }

    // courtesy to @DaveB on stackoverflow
    public static void delay(long millis, Runnable continuation) {
      Task<Void> sleeper = new Task<Void>() {
          @Override
          protected Void call() throws Exception {
              try { Thread.sleep(millis); }
              catch (InterruptedException e) { }
              return null;
          }
      };
      sleeper.setOnSucceeded(event -> continuation.run());
      new Thread(sleeper).start();
    }
}
