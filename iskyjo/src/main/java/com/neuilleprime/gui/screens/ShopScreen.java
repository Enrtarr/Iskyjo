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

public class ShopScreen {

    private final ScreenManager sm;
    // private final RoundEndedEvent event;
    private boolean ready = false;

    // private String pileBeingDrawned = null;
    private Joker jokerBeingDragged = null;

    public ShopScreen(ScreenManager sm) {
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
        rightBar.prefWidthProperty().bind(scene.widthProperty().multiply(SideBarsHelper.rightBarWidth));
        // rightBar.prefHeightProperty().bind(scene.heightProperty().multiply(1));

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

                    // add the reroll button as well as its price
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

                    // add in the rerolled jokers
                    for (Joker joker : event.jokers) {


                        JokerView jokerView = new JokerView(joker);
                        jokerView.prefWidthProperty().bind(jokerBox.prefWidthProperty().multiply(1));
                        jokerView.prefHeightProperty().bind(jokerBox.prefHeightProperty().multiply(1));
                        jokerBox.getChildren().add(jokerView);

                        setupJokerInteractions(jokerView, "jokerbuy");
                    }


                });
            }

            @Override
            public void onJokerSold(JokerSoldEvent event) {
                Platform.runLater(() -> {
                    bottomBar.getChildren().clear();
                    SideBarsHelper.loadBottomBar(bottomBar, player);
                    setupBottomBarInteractions(bottomBar);

                    leftBar.getChildren().clear();
                    SideBarsHelper.loadMoneyView(leftBar, player.getMoney());

                    // GameLogic.gameController.execute(new RerollShopAction(player));
                });
            }

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

                    // sell zone
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

            @Override
            public void onTurnStarted(TurnStartedEvent event) {
                // System.out.println("baka");
                sm.show("game");
            }

        });

        return scene;
    }

    private void setupJokerInteractions(JokerView jokerView, String mode) {

        TransferMode transferMode = TransferMode.MOVE;
        
        jokerView.setOnDragDetected(e -> {
            Dragboard dragboard = jokerView.startDragAndDrop(transferMode);

            this.jokerBeingDragged = jokerView.getJokerElem();
            // this.pileBeingDrawned = pileName;

            ClipboardContent content = new ClipboardContent();
            content.putString(mode);
            dragboard.setContent(content);

            // the "floating" image under the mouse
            SnapshotParameters params = new SnapshotParameters();
            params.setFill(Color.TRANSPARENT);
            WritableImage snapshot = jokerView.getJokerImageView().snapshot(params, null);
            // dragboard.setDragView(snapshot, e.getX(), e.getY());
            Point2D pointInImage = jokerView.getJokerImageView().sceneToLocal(e.getSceneX(), e.getSceneY());
            dragboard.setDragView(snapshot, pointInImage.getX(), pointInImage.getY());

            e.consume();
        });

        jokerView.setOnDragDone(e -> {
            if (e.getTransferMode() == transferMode) {
                // System.out.println("amogsus");
                // e.get
                
            }
            e.consume();
        });
    }

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
