package com.neuilleprime.gui.screens;

import com.neuilleprime.game.Deck;
import com.neuilleprime.game.Player;
import com.neuilleprime.game.events.GameEventListener;
import com.neuilleprime.game.events.RoundEndedEvent;
import com.neuilleprime.gui.components.DeckView;
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

public class ResultScreen {

    private final ScreenManager sm;
    // private final RoundEndedEvent event;

    public ResultScreen(ScreenManager sm, RoundEndedEvent event) {
        this.sm = sm;
        // this.event = event;
    }

    public ResultScreen(ScreenManager sm) {
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
            public void onRoundEnded(RoundEndedEvent event) {
                Platform.runLater(() -> {
                    System.out.println("sus");
                    System.out.println("amogus");

                    centerBar.getChildren().clear();

                    Deck deck = event.playerDecks.get(player);

                    deck.printAll();

                    DeckView deckView = new DeckView(deck);
                    deckView.prefWidthProperty().bind(centerBar.widthProperty().multiply(0.4));
                    deckView.prefHeightProperty().bind(centerBar.heightProperty().multiply(0.6));

                    centerBar.getChildren().add(deckView);

                    topBar.getChildren().clear();

                    Button shopButton = new Button();
                    shopButton.setGraphic(new ImageView(AssetLoader.BUTTON_PLAY));
                    shopButton.setStyle("-fx-background-color: transparent;");
                    shopButton.setOnAction(e -> {
                        sm.show("shop");
                        // GameLogic.gameController.execute(new ReadyUpAction());
                    });

                    topBar.getChildren().add(shopButton);
                });
            }

            //
            //
            // NE PAS OUBLIER DE FAIRE ÇA DANS LE SHOP APRÈS !!!
            // 
            //
            // @Override
            // public void onTurnStarted(TurnStartedEvent event) {
            //     System.out.println("baka");
            //     sm.show("game");
            // }
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
