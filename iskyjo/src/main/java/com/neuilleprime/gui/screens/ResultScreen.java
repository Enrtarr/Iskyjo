package com.neuilleprime.gui.screens;

import java.util.ArrayList;

import com.neuilleprime.game.Deck;
import com.neuilleprime.game.Player;
import com.neuilleprime.game.events.GameEventListener;
import com.neuilleprime.game.events.RoundEndedEvent;
import com.neuilleprime.gui.components.DeckView;
import com.neuilleprime.gui.components.JokerView;
import com.neuilleprime.gui.components.ScoreView;
import com.neuilleprime.gui.utils.AssetLoader;
import com.neuilleprime.gui.utils.GameLogic;
import com.neuilleprime.gui.utils.ScreenManager;
import com.neuilleprime.gui.utils.SideBarsHelper;

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

        // Scene scene = new Scene(root, 1280, 720);
        Scene scene = new Scene(root);
        scene.getStylesheets().add(getClass().getResource("/style.css").toExternalForm());

        root.getStyleClass().add("root");

        VBox leftBar = new VBox();
        leftBar.setAlignment(Pos.CENTER);
        leftBar.prefWidthProperty().bind(scene.widthProperty().multiply(SideBarsHelper.leftBarWidth));
        // leftBar.prefHeightProperty().bind(scene.heightProperty().multiply(1));

        VBox rightBar = new VBox();
        rightBar.setAlignment(Pos.CENTER);
        rightBar.prefWidthProperty().bind(scene.widthProperty().multiply(.15));
        // rightBar.prefHeightProperty().bind(scene.heightProperty().multiply(1));

        HBox topBar = new HBox();
        topBar.setAlignment(Pos.CENTER);
        topBar.prefWidthProperty().bind(scene.widthProperty().multiply(SideBarsHelper.topBarWidth));
        topBar.prefHeightProperty().bind(scene.heightProperty().multiply(SideBarsHelper.topBarHeight));

        HBox bottomBar = new HBox();
        bottomBar.setAlignment(Pos.CENTER);
        bottomBar.prefWidthProperty().bind(scene.widthProperty().multiply(SideBarsHelper.bottomBarWidth));
        bottomBar.prefHeightProperty().bind(scene.heightProperty().multiply(SideBarsHelper.bottomBarHeight));

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
                    // System.out.println("sus");
                    // System.out.println("amogus");

                    topBar.getChildren().clear();

                    Button shopButton = new Button();
                    shopButton.setGraphic(new ImageView(AssetLoader.BUTTON_SHOP));
                    shopButton.setStyle("-fx-background-color: transparent;");
                    shopButton.setOnAction(e -> {
                        sm.show("shop");
                    });

                    topBar.getChildren().add(shopButton);

                    bottomBar.getChildren().clear();
                    ArrayList<JokerView> bottomBarJokers = SideBarsHelper.loadBottomBar(bottomBar, player);

                    leftBar.getChildren().clear();
                    SideBarsHelper.loadMoneyView(leftBar, event.playerMoneys.get(player));

                    // new Thread(() -> {
                    //     try {

                    //         int playerMoney = event.playerMoneys.get(player);

                    //         Thread.sleep(2500);

                    //         int[] plrInterests = player.getInterests();

                    //         plr.setMoney(plr.getMoney() + this.moneyPerRound);
                    //         // we'll also ward him bonus money based on how well he performed this round
                    //         int moneyToAdd = (((totalValue)/(this.roundScore/this.players.size()))-1)*plr.getBonusMoneyRate();
                    //         // System.out.println("Player went "+(((totalValue)/(this.roundScore/this.players.size()))-1)+"% over the asked amount");
                    //         // System.out.println("Bonus money: "+moneyToAdd);
                    //         if (moneyToAdd >= 0) {
                    //         plr.setMoney(plr.getMoney() + moneyToAdd);
                    //         }
            
                    //         // System.out.println("Current money: "+plrMoney);
                    //         for (int i=0;i<plrInterests[2];i++) {
                    //             // System.out.println("Checking for interests");
                    //             if ((playerMoney[0] - plrInterests[1]) >= 0) {
                    //                 // System.out.println("Interest ok at "+plrMoney);
                    //                 playerMoney[0] -= plrInterests[1];
                    //                 player.setMoney(player.getMoney() + plrInterests[0]);
                    //             }
                    //             else {
                    //                 // System.out.println("Interest not ok at "+plrMoney);
                    //                 break;
                    //             }
                    //         }

                    //         Platform.runLater(() -> {
                    //             leftBar.getChildren().clear();
                    //             SideBarsHelper.loadMoneyView(leftBar, 67);
                    //         });

                    //     } catch (InterruptedException e) {
                    //         e.printStackTrace();
                    //     }
                    // }).start();

                    centerBar.getChildren().clear();

                    Deck deck = event.playerDecks.get(player);

                    DeckView deckView = new DeckView(deck);
                    deckView.prefWidthProperty().bind(centerBar.widthProperty().multiply(0.4));
                    deckView.prefHeightProperty().bind(centerBar.heightProperty().multiply(0.6));

                    rightBar.getChildren().clear();
                    ScoreView scoreView = new ScoreView(deckView, bottomBarJokers, player.getUpgrades());
                    scoreView.prefWidthProperty().bind(leftBar.prefWidthProperty());
                    scoreView.prefHeightProperty().bind(leftBar.prefHeightProperty());
                    
                    rightBar.getChildren().add(scoreView);
                    centerBar.getChildren().add(deckView);

                    // for some reason we have to start it this way, or the 1st anims won't show up
                    Platform.runLater(() -> {
                        scoreView.clear();
                        scoreView.startAnims();
                    });
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
