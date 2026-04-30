package com.neuilleprime.gui.screens;

import com.neuilleprime.game.Player;
import com.neuilleprime.game.actions.BuyJokerAction;
import com.neuilleprime.game.actions.ReadyUpAction;
import com.neuilleprime.game.actions.RerollShopAction;
import com.neuilleprime.game.actions.SellJokerAction;
import com.neuilleprime.game.events.GameEventListener;
import com.neuilleprime.game.events.JokerSoldEvent;
import com.neuilleprime.game.events.RoundEndedEvent;
import com.neuilleprime.game.events.ShopRerolledEvent;
import com.neuilleprime.game.events.TurnStartedEvent;
import com.neuilleprime.gui.components.JokerView;
import com.neuilleprime.gui.components.VTextBox;
import com.neuilleprime.gui.utils.AssetLoader;
import com.neuilleprime.gui.utils.SideBarsHelper;
import com.neuilleprime.jokers.Joker;
import com.neuilleprime.gui.utils.GameLogic;
import com.neuilleprime.gui.utils.ScreenManager;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.geometry.Point2D;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.SnapshotParameters;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;

/**
 * Screen displayed between rounds where the player can buy, sell, and reroll jokers.
 * <p>
 * Listens to three events from the game controller:
 * <ul>
 *   <li>{@link ShopRerolledEvent} — refreshes the center joker row with the new
 *       offerings, the reroll button, and the current money balance.</li>
 *   <li>{@link JokerSoldEvent} — refreshes the bottom bar and money display after
 *       a joker has been sold.</li>
 *   <li>{@link RoundEndedEvent} — populates the "Next Round" button, buy/sell drop
 *       zones, and joker inventory. Also resets the {@link #ready} flag.</li>
 *   <li>{@link TurnStartedEvent} — switches immediately to the game screen when
 *       all players are ready and the new round begins.</li>
 * </ul>
 * </p>
 * <p>
 * Player interactions use JavaFX drag-and-drop:
 * <ul>
 *   <li>Dragging a shop joker onto the <b>Buy</b> zone → {@link BuyJokerAction}.</li>
 *   <li>Dragging an owned joker onto the <b>Sell</b> zone → {@link SellJokerAction}.</li>
 * </ul>
 * </p>
 */
public class ShopScreen {

    /** Screen manager used to navigate between scenes. */
    private final ScreenManager sm;

    /**
     * Whether the local player has already pressed "Next Round".
     * Prevents double-firing {@link ReadyUpAction}.
     */
    private boolean ready = false;

    /**
     * The joker currently being dragged, or {@code null} when no drag is active.
     */
    private Joker jokerBeingDragged = null;

    /**
     * Constructs a {@code ShopScreen}.
     *
     * @param sm the application screen manager
     */
    public ShopScreen(ScreenManager sm) {
        this.sm = sm;
    }

    /**
     * Builds and returns the shop {@link Scene}.
     * <p>
     * Registers a {@link GameEventListener} on the local player. Each event
     * type rebuilds the relevant portions of the layout on the JavaFX
     * Application Thread.
     * </p>
     *
     * @return the constructed {@link Scene}
     */
    public Scene buildScene() {
        BorderPane root = new BorderPane();

        Scene scene = new Scene(root);
        scene.getStylesheets().add(getClass().getResource("/style.css").toExternalForm());

        root.getStyleClass().add("root");

        VBox leftBar = new VBox();
        leftBar.setAlignment(Pos.CENTER);
        leftBar.prefWidthProperty().bind(scene.widthProperty().multiply(SideBarsHelper.leftBarWidth));

        VBox rightBar = new VBox();
        rightBar.setAlignment(Pos.CENTER);
        rightBar.prefWidthProperty().bind(scene.widthProperty().multiply(SideBarsHelper.rightBarWidth));

        HBox topBar = new HBox();
        topBar.setAlignment(Pos.CENTER);
        topBar.prefWidthProperty().bind(scene.widthProperty().multiply(SideBarsHelper.topBarWidth));
        topBar.prefHeightProperty().bind(scene.heightProperty().multiply(SideBarsHelper.topBarHeight));

        HBox bottomBar = new HBox();
        bottomBar.setAlignment(Pos.CENTER);
        bottomBar.prefWidthProperty().bind(scene.widthProperty().multiply(SideBarsHelper.bottomBarWidth));
        bottomBar.prefHeightProperty().bind(scene.heightProperty().multiply(SideBarsHelper.bottomBarHeight));

        HBox centerBar = new HBox();
        centerBar.setAlignment(Pos.CENTER);

        Player player = GameLogic.localPlayer;

        root.setLeft(leftBar);
        root.setRight(rightBar);
        root.setTop(topBar);
        root.setBottom(bottomBar);
        root.setCenter(centerBar);

        GameLogic.gameController.addListener(player, new GameEventListener() {

            /**
             * Refreshes the shop's center row with newly rerolled jokers,
             * updates the reroll cost label, and reloads the money and joker bars.
             *
             * @param event the rerolled shop contents
             */
            @Override
            public void onShopRerolledEvent(ShopRerolledEvent event) {
                Platform.runLater(() -> {
                    bottomBar.getChildren().clear();
                    SideBarsHelper.loadBottomBar(bottomBar, player);
                    setupBottomBarInteractions(bottomBar);

                    leftBar.getChildren().clear();
                    SideBarsHelper.loadMoneyView(leftBar, player.getMoney());

                    centerBar.getChildren().clear();

                    VBox rerollButtonBox = new VBox();
                    rerollButtonBox.prefWidthProperty().bind(centerBar.widthProperty().multiply(0.2));
                    rerollButtonBox.prefHeightProperty().bind(centerBar.heightProperty().multiply(0.6));
                    rerollButtonBox.setAlignment(Pos.CENTER);
                    centerBar.getChildren().add(rerollButtonBox);

                    HBox jokerBox = new HBox();
                    jokerBox.prefWidthProperty().bind(centerBar.widthProperty().multiply(0.6));
                    jokerBox.prefHeightProperty().bind(centerBar.heightProperty().multiply(0.2));
                    jokerBox.setAlignment(Pos.CENTER);
                    centerBar.getChildren().add(jokerBox);

                    // add the reroll button and its current price label
                    Button rerollButton = new Button();
                    rerollButton.setGraphic(new ImageView(AssetLoader.BUTTON_REROLL));
                    rerollButton.setStyle("-fx-background-color: transparent;");
                    rerollButton.setOnAction(e -> {
                        if (!ready) {
                            GameLogic.gameController.execute(new RerollShopAction(player));
                        }
                    });
                    rerollButtonBox.getChildren().add(rerollButton);

                    Label rerollCost = new Label();
                    rerollCost.setText("Cost: "+player.getShopRerollPrice()+"₣");
                    rerollCost.setWrapText(true);
                    rerollCost.setAlignment(Pos.CENTER);
                    rerollCost.prefWidthProperty().bind(rerollButtonBox.widthProperty().multiply(0.5));
                    rerollCost.prefHeightProperty().bind(rerollButtonBox.heightProperty().multiply(0.1));
                    rerollCost.prefHeightProperty().addListener((obs, oldVal, newVal) -> {
                        double jokerNameFontSize = newVal.doubleValue() * .2 / rerollCost.getText().length() * 15;
                        double cardPadding = newVal.doubleValue() * .05;
                        double borderRadius = newVal.doubleValue() * .1;
                        double borderWidth = newVal.doubleValue() * .05;
                        rerollCost.setStyle(
                            "-fx-font-size: " + jokerNameFontSize + "px;" +
                            "-fx-font-family: 'VCR OSD Mono';" +
                            "-fx-text-fill: "+"#2c2121"+";" +
                            "-fx-background-color: "+"#cdc0c0"+";" +
                            "-fx-background-radius: "+borderRadius+" "+borderRadius+" "+borderRadius+" "+borderRadius+";" +
                            "-fx-padding: "+cardPadding+" "+cardPadding+" "+cardPadding+" "+cardPadding+";" +
                            "-fx-border-color: "+"#8f8787"+";" +
                            "-fx-border-width: "+borderWidth+";" +
                            "-fx-border-radius: "+borderRadius+" "+borderRadius+" "+borderRadius+" "+borderRadius+";"
                        );
                    });
                    rerollButtonBox.getChildren().add(rerollCost);

                    // populate the shop row with the new jokers
                    for (Joker joker : event.jokers) {
                        JokerView jokerView = new JokerView(joker);
                        jokerView.prefWidthProperty().bind(jokerBox.prefWidthProperty().multiply(1));
                        jokerView.prefHeightProperty().bind(jokerBox.prefHeightProperty().multiply(1));
                        jokerBox.getChildren().add(jokerView);

                        setupJokerInteractions(jokerView, "jokerbuy");
                    }
                });
            }

            /**
             * Refreshes the bottom bar and money display after a joker is sold.
             *
             * @param event details about the player who sold a joker
             */
            @Override
            public void onJokerSold(JokerSoldEvent event) {
                Platform.runLater(() -> {
                    bottomBar.getChildren().clear();
                    SideBarsHelper.loadBottomBar(bottomBar, player);
                    setupBottomBarInteractions(bottomBar);

                    leftBar.getChildren().clear();
                    SideBarsHelper.loadMoneyView(leftBar, player.getMoney());
                });
            }

            /**
             * Populates the shop layout when the round ends:
             * resets the ready flag, adds the "Next Round" button, buy/sell zones,
             * joker inventory bar, and money display.
             *
             * @param event details about the round that ended
             */
            @Override
            public void onRoundEnded(RoundEndedEvent event) {
                Platform.runLater(() -> {
                    bottomBar.getChildren().clear();
                    SideBarsHelper.loadBottomBar(bottomBar, player);
                    setupBottomBarInteractions(bottomBar);

                    leftBar.getChildren().clear();
                    SideBarsHelper.loadMoneyView(leftBar, player.getMoney());

                    ready = false; 
                    topBar.getChildren().clear();
                    Button nextRoundButton = new Button();
                    nextRoundButton.setGraphic(new ImageView(AssetLoader.BUTTON_NEXT_ROUND));
                    nextRoundButton.setStyle("-fx-background-color: transparent;");
                    nextRoundButton.setOnAction(e -> {
                        if (!ready) {
                            GameLogic.gameController.execute(new ReadyUpAction());
                            ready = true;
                        }
                    });
                    topBar.getChildren().add(nextRoundButton);

                    rightBar.getChildren().clear();

                    VTextBox buyZoneView = new VTextBox("Buy");
                    buyZoneView.setNameColor("#00a6ff");
                    buyZoneView.setContentColor("#000000");
                    buyZoneView.setBackgroundColor("#0087a9");
                    buyZoneView.setBorderColor("#00657f");
                    buyZoneView.setNameSize(.1);
                    buyZoneView.setContentSize(.4);
                    buyZoneView.prefWidthProperty().bind(rightBar.prefWidthProperty());
                    buyZoneView.prefHeightProperty().bind(rightBar.prefHeightProperty());
                    setupZoneBuyInteractions(buyZoneView);
                    rightBar.getChildren().add(buyZoneView);

                    VTextBox sellZoneView = new VTextBox("Sell");
                    sellZoneView.setNameColor("#00a6ff");
                    sellZoneView.setContentColor("#000000");
                    sellZoneView.setBackgroundColor("#a92a00");
                    sellZoneView.setBorderColor("#7c2300");
                    sellZoneView.setNameSize(.1);
                    sellZoneView.setContentSize(.4);
                    sellZoneView.prefWidthProperty().bind(rightBar.prefWidthProperty());
                    sellZoneView.prefHeightProperty().bind(rightBar.prefHeightProperty());
                    setupZoneSellInteractions(sellZoneView);
                    rightBar.getChildren().add(sellZoneView);
                });
            }

            /**
             * Switches immediately to the game screen when the new round begins.
             *
             * @param event the turn that just started
             */
            @Override
            public void onTurnStarted(TurnStartedEvent event) {
                // System.out.println("baka");
                sm.show("game");
            }

        });

        return scene;
    }

    /**
     * Attaches drag-detection handlers to a {@link JokerView} so it can be
     * dragged onto a buy or sell zone.
     *
     * @param jokerView the joker view to make draggable
     * @param mode      transfer mode string put into the {@link Dragboard}:
     *                  {@code "jokerbuy"} for shop jokers, {@code "jokersell"} for
     *                  owned jokers
     */
    private void setupJokerInteractions(JokerView jokerView, String mode) {

        TransferMode transferMode = TransferMode.MOVE;
        
        jokerView.setOnDragDetected(e -> {
            Dragboard dragboard = jokerView.startDragAndDrop(transferMode);

            this.jokerBeingDragged = jokerView.getJokerElem();

            ClipboardContent content = new ClipboardContent();
            content.putString(mode);
            dragboard.setContent(content);

            SnapshotParameters params = new SnapshotParameters();
            params.setFill(Color.TRANSPARENT);
            WritableImage snapshot = jokerView.getJokerImageView().snapshot(params, null);
            Point2D pointInImage = jokerView.getJokerImageView().sceneToLocal(e.getSceneX(), e.getSceneY());
            dragboard.setDragView(snapshot, pointInImage.getX(), pointInImage.getY());

            e.consume();
        });

        jokerView.setOnDragDone(e -> {
            if (e.getTransferMode() == transferMode) {
            }
            e.consume();
        });
    }

    /**
     * Attaches drag-over, drag-enter, drag-exit, and drag-drop handlers to the
     * sell zone.
     * <p>
     * Only accepts drags with the {@code "jokersell"} transfer string. On drop,
     * executes a {@link SellJokerAction} for the currently dragged joker.
     * </p>
     *
     * @param zoneView the {@link VTextBox} acting as the sell drop zone
     */
    private void setupZoneSellInteractions(VTextBox zoneView) {
        zoneView.setOnDragOver(e -> {
            if (e.getGestureSource() != zoneView 
                    && e.getDragboard().hasString() 
                    && e.getDragboard().getString().equals("jokersell")) {
                e.acceptTransferModes(TransferMode.MOVE);
            }
            e.consume();
        });

        zoneView.setOnDragEntered(e -> {
            if (e.getDragboard().hasString()) {
                zoneView.setOpacity(0.6);
            }
            e.consume();
        });

        zoneView.setOnDragExited(e -> {
            zoneView.setOpacity(1.0);
            e.consume();
        });

        zoneView.setOnDragDropped(e -> {
            Dragboard dragboard = e.getDragboard();
            boolean succes = false;

            if (dragboard.hasString() 
                    && dragboard.getString().equals("jokersell") 
                    && this.jokerBeingDragged != null) {

                GameLogic.gameController.execute(new SellJokerAction(GameLogic.localPlayer, this.jokerBeingDragged));

                succes = true;
            }

            e.setDropCompleted(succes);
            e.consume();
        });
    }

    /**
     * Attaches drag-over, drag-enter, drag-exit, and drag-drop handlers to the
     * buy zone.
     * <p>
     * Only accepts drags with the {@code "jokerbuy"} transfer string. On drop,
     * executes a {@link BuyJokerAction} for the currently dragged joker.
     * </p>
     *
     * @param zoneView the {@link VTextBox} acting as the buy drop zone
     */
    private void setupZoneBuyInteractions(VTextBox zoneView) {
        zoneView.setOnDragOver(e -> {
            if (e.getGestureSource() != zoneView 
                    && e.getDragboard().hasString() 
                    && e.getDragboard().getString().equals("jokerbuy")) {
                e.acceptTransferModes(TransferMode.MOVE);
            }
            e.consume();
        });

        zoneView.setOnDragEntered(e -> {
            if (e.getDragboard().hasString()) {
                zoneView.setOpacity(0.6);
            }
            e.consume();
        });

        zoneView.setOnDragExited(e -> {
            zoneView.setOpacity(1.0);
            e.consume();
        });

        zoneView.setOnDragDropped(e -> {
            Dragboard dragboard = e.getDragboard();
            boolean succes = false;

            if (dragboard.hasString() 
                    && dragboard.getString().equals("jokerbuy") 
                    && this.jokerBeingDragged != null) {

                GameLogic.gameController.execute(new BuyJokerAction(GameLogic.localPlayer, this.jokerBeingDragged));

                succes = true;
            }

            e.setDropCompleted(succes);
            e.consume();
        });
    }

    /**
     * Walks the bottom bar's child hierarchy and attaches {@code "jokersell"}
     * drag-detection to every {@link JokerView} found.
     * <p>
     * The bottom bar contains {@link HBox}es (one for jokers, one for consumables),
     * each of which contains {@link JokerView}s.
     * </p>
     *
     * @param bottomBar the bottom {@link HBox} populated by
     *                  {@link SideBarsHelper#loadBottomBar}
     */
    private void setupBottomBarInteractions(HBox bottomBar) {
        for (Node child : bottomBar.getChildren()) {
            if (child instanceof HBox hbox) {
                for (Node inner : hbox.getChildren()) {
                    if (inner instanceof JokerView jokerView) {
                        setupJokerInteractions(jokerView, "jokersell");
                    }
                }
            }
        }
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
