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

/**
 * Screen displayed after a round ends successfully, showing the animated
 * scoring breakdown and a button to proceed to the shop.
 * <p>
 * Registers a {@link GameEventListener} on the local player that populates the
 * layout when a {@link RoundEndedEvent} is received:
 * <ul>
 *   <li>Top bar — "Shop" button to navigate to the shop screen.</li>
 *   <li>Bottom bar — player's jokers and consumables.</li>
 *   <li>Left bar — player's current money balance.</li>
 *   <li>Center — snapshot of the player's final deck for the round.</li>
 *   <li>Right bar — {@link ScoreView} that plays the scoring animation.</li>
 * </ul>
 * </p>
 */
public class ResultScreen {

    /** Screen manager used to navigate between scenes. */
    private final ScreenManager sm;

    /**
     * Constructs a {@code ResultScreen} with a pre-existing round-ended event.
     * The event parameter is kept for future use but is not yet consumed.
     *
     * @param sm    the application screen manager
     * @param event the round-ended event (currently unused)
     */
    public ResultScreen(ScreenManager sm, RoundEndedEvent event) {
        this.sm = sm;
        // this.event = event;
    }

    /**
     * Constructs a {@code ResultScreen} without a pre-existing event.
     * The screen will be populated reactively when the next
     * {@link RoundEndedEvent} fires.
     *
     * @param sm the application screen manager
     */
    public ResultScreen(ScreenManager sm) {
        this.sm = sm;
    }

    /**
     * Builds and returns the result {@link Scene}.
     * <p>
     * Registers a {@link GameEventListener} on the local player. On
     * {@link RoundEndedEvent}, the layout is rebuilt on the JavaFX Application
     * Thread and the {@link ScoreView} animation is started.
     * </p>
     *
     * @return the constructed {@link Scene}
     */
    public Scene buildScene() {
        BorderPane root = new BorderPane();

        // Scene scene = new Scene(root, 1280, 720);
        Scene scene = new Scene(root);
        scene.getStylesheets().add(getClass().getResource("/style.css").toExternalForm());

        root.getStyleClass().add("root");

        VBox leftBar = new VBox();
        leftBar.setAlignment(Pos.CENTER);
        leftBar.prefWidthProperty().bind(scene.widthProperty().multiply(SideBarsHelper.leftBarWidth));

        VBox rightBar = new VBox();
        rightBar.setAlignment(Pos.CENTER);
        rightBar.prefWidthProperty().bind(scene.widthProperty().multiply(.15));

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

        Player player = GameLogic.localPlayer;

        root.setLeft(leftBar);
        root.setRight(rightBar);
        root.setTop(topBar);
        root.setBottom(bottomBar);
        root.setCenter(centerBar);

        GameLogic.gameController.addListener(player, new GameEventListener() {

            /**
             * Rebuilds the result screen layout when a round ends.
             * Populates the top bar with the shop button, bottom bar with jokers,
             * left bar with money, and center/right with the deck snapshot and
             * score animation.
             *
             * @param event details about the round that just ended
             */
            @Override
            public void onRoundEnded(RoundEndedEvent event) {
                Platform.runLater(() -> {
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

    /**
     * Runs a {@link Runnable} after a delay on the JavaFX Application Thread.
     * <p>
     * Courtesy of {@code @DaveB} on Stack Overflow.
     * </p>
     *
     * @param millis       delay in milliseconds before {@code continuation} runs
     * @param continuation the action to run after the delay
     */
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
