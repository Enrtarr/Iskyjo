package com.neuilleprime.gui.screens;

import com.neuilleprime.game.Card;
import com.neuilleprime.game.Player;
import com.neuilleprime.game.actions.DiscardCardAction;
import com.neuilleprime.game.actions.DrawCardAction;
import com.neuilleprime.game.actions.FlipCardAction;
import com.neuilleprime.game.actions.ReplaceCardAction;
import com.neuilleprime.game.events.GameEventListener;
import com.neuilleprime.game.events.RoundEndedEvent;
import com.neuilleprime.game.events.TurnStartedEvent;
import com.neuilleprime.gui.components.CardView;
import com.neuilleprime.gui.components.DeckView;
import com.neuilleprime.gui.components.PileTopView;
import com.neuilleprime.gui.components.TopTextView;
import com.neuilleprime.gui.components.VZoneView;
import com.neuilleprime.gui.utils.GameLogic;
import com.neuilleprime.gui.utils.ScreenManager;

import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.SnapshotParameters;
import javafx.scene.control.Label;
import javafx.scene.image.WritableImage;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;

public class GameScreen {

    private final ScreenManager sm;

    private String pileBeingDrawned = null;
    private Card cardBeingDragged = null;
    private int[] deckCardClicked = null;
    private boolean waitingForCardSelection = false;

    public GameScreen(ScreenManager sm) {
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

        root.setLeft(leftBar);
        root.setRight(rightBar);
        root.setTop(topBar);
        root.setBottom(bottomBar);
        root.setCenter(centerBar);

        // if no game is found, we create a new one and start it
        if (GameLogic.gameController == null) {
            GameLogic.gameController = GameLogic.setupGame(1, 150); 
            
        }

        // Player player = GameLogic.gameController.getCurrentPlayer();
        Player player = GameLogic.localPlayer;

        GameLogic.gameController.addListener(player, new GameEventListener() {

            @Override
            public void onTurnStarted(TurnStartedEvent event) {
                Platform.runLater(() -> {
                    System.out.println("amogonus");
                    boolean isLocalPlrTurn = event.currentPlayer == GameLogic.localPlayer;

                    topBar.getChildren().clear();
                    leftBar.getChildren().clear();
                    rightBar.getChildren().clear();
                    centerBar.getChildren().clear();

                    // for testing purposes, should be removed in the future
                    System.out.println("Current deck:");
                    event.currentPlayer.getDeck().printAll();

                    // top bar, displaying info
                    Label playerTurnLabel = new TopTextView(event.currentPlayer.getName()+"'s turn", .3);
                    Label roundLabel = new TopTextView("Round "+event.round, .3);
                    Label roundScoreLabel = new TopTextView("Score to beat: "+event.roundScore, .3);

                    playerTurnLabel.prefWidthProperty().bind(topBar.prefWidthProperty());
                    playerTurnLabel.prefHeightProperty().bind(topBar.prefHeightProperty());
                    roundLabel.prefWidthProperty().bind(topBar.prefWidthProperty());
                    roundLabel.prefHeightProperty().bind(topBar.prefHeightProperty());
                    roundScoreLabel.prefWidthProperty().bind(topBar.prefWidthProperty());
                    roundScoreLabel.prefHeightProperty().bind(topBar.prefHeightProperty());

                    topBar.spacingProperty().bind(topBar.widthProperty().multiply(.05));
                    
                    topBar.getChildren().addAll(playerTurnLabel, roundLabel, roundScoreLabel);

                    // both piles, with regards to single/multi-player
                    boolean isDrawPileEmpty = event.drawPileTop == null;
                    boolean isDiscardPileEmpty = event.discardPileTop == null;

                    if (!isDrawPileEmpty) {
                        event.drawPileTop.show();
                        PileTopView drawPileView = new PileTopView(event.drawPileTop, "Draw pile");
                        drawPileView.prefWidthProperty().bind(leftBar.prefWidthProperty());
                        drawPileView.prefHeightProperty().bind(leftBar.prefHeightProperty());

                        if (isLocalPlrTurn) {setupPileInteractions(drawPileView, "draw");}
                        
                        leftBar.getChildren().add(drawPileView);
                    }
                    else {
                        PileTopView drawPileView = new PileTopView(new Card(), "Draw pile");
                        drawPileView.prefWidthProperty().bind(leftBar.prefWidthProperty());
                        drawPileView.prefHeightProperty().bind(leftBar.prefHeightProperty());
                        leftBar.getChildren().add(drawPileView);
                    }

                    if (!isDiscardPileEmpty) {
                        event.discardPileTop.show();
                        PileTopView discardPileView = new PileTopView(event.discardPileTop, "Discard pile");
                        discardPileView.prefWidthProperty().bind(leftBar.prefWidthProperty());
                        discardPileView.prefHeightProperty().bind(leftBar.prefHeightProperty());

                        if (GameLogic.gameController.getPlayerCount() > 1 && isLocalPlrTurn) {
                            setupPileInteractions(discardPileView, "discard");
                        }

                        leftBar.getChildren().add(discardPileView);
                    }
                    else {
                        PileTopView discardPileView = new PileTopView(new Card(), "Discard pile");
                        discardPileView.prefWidthProperty().bind(leftBar.prefWidthProperty());
                        discardPileView.prefHeightProperty().bind(leftBar.prefHeightProperty());
                        leftBar.getChildren().add(discardPileView);
                    }

                    // discard zone
                    VZoneView discardZoneView = new VZoneView("Discard");
                    discardZoneView.prefWidthProperty().bind(rightBar.prefWidthProperty());
                    discardZoneView.prefHeightProperty().bind(rightBar.prefHeightProperty());

                    if (isLocalPlrTurn) {setupZoneInteractions(discardZoneView);}

                    rightBar.getChildren().add(discardZoneView);

                    // player deck, centered in the middle of the window
                    DeckView deckView = new DeckView(event.currentPlayer.getDeck());
                    deckView.prefWidthProperty().bind(centerBar.widthProperty().multiply(0.4));
                    deckView.prefHeightProperty().bind(centerBar.heightProperty().multiply(0.6));

                    // making sure the deck can still be d&d/clicked, even when resized
                    deckView.setOnRebuild(() -> setupDeckInteractions(deckView));
                    setupDeckInteractions(deckView);

                    centerBar.getChildren().add(deckView);

                    // update all the elements we just added
                    root.setLeft(leftBar);
                    root.setRight(rightBar);
                    root.setTop(topBar);
                    root.setBottom(bottomBar);
                    root.setCenter(centerBar);
                });
            }

            @Override
            public void onRoundEnded(RoundEndedEvent event) {
                Platform.runLater(() -> {
                    if (event.gameState == 0) {
                        System.out.println("sad2");
                        sm.show("gameover");
                    } else {
                        if (!event.setup) {
                            // (we only show the result if this wasn't the setup (= the 1st round))
                            sm.show("result");
                        }
                    }
                });
            }
        });

        return scene;
    }

    private void setupPileInteractions(PileTopView pileTopView, String pileName) {

        CardView topCardView = pileTopView.getTopCardView();
        TransferMode transferMode = TransferMode.MOVE;
        
        topCardView.setOnDragDetected(e -> {
            Dragboard dragboard = topCardView.startDragAndDrop(transferMode);

            this.cardBeingDragged = topCardView.getCardElem();
            this.pileBeingDrawned = pileName;

            ClipboardContent content = new ClipboardContent();
            content.putString("card");
            dragboard.setContent(content);

            // the "floating" image under the mouse
            SnapshotParameters params = new SnapshotParameters();
            params.setFill(Color.TRANSPARENT);
            WritableImage snapshot = pileTopView.getTopCardView().snapshot(params, null);
            dragboard.setDragView(snapshot, e.getX(), e.getY());

            e.consume();
        });

        topCardView.setOnDragDone(e -> {
            if (e.getTransferMode() == transferMode) {
                System.out.println("pipi");
                // e.get
                
            }
            e.consume();
        });
    }

    private void setupDeckInteractions(DeckView deckView) {
        for (Node node : deckView.getChildren()) {
            // drag and drop
            if (node instanceof CardView cardView) {

                cardView.setOnDragOver(e -> {
                    if (e.getGestureSource() != cardView 
                            && e.getDragboard().hasString() 
                            && e.getDragboard().getString().equals("card")) {
                        e.acceptTransferModes(TransferMode.MOVE);
                    }
                    e.consume();
                });

                cardView.setOnDragEntered(e -> {
                    if (e.getDragboard().hasString()) {
                        cardView.setOpacity(0.6);
                    }
                    e.consume();
                });

                cardView.setOnDragExited(e -> {
                    cardView.setOpacity(1.0);
                    e.consume();
                });

                cardView.setOnDragDropped(e -> {
                    Dragboard dragboard = e.getDragboard();
                    boolean succes = false;

                    if (dragboard.hasString() && dragboard.getString().equals("card") && this.cardBeingDragged != null) {
                        // System.out.println("prout");

                        GameLogic.gameController.execute(new DrawCardAction(this.pileBeingDrawned));

                        int[] coords = new int[] {GridPane.getRowIndex(node), GridPane.getColumnIndex(node)};
                        GameLogic.gameController.execute(new ReplaceCardAction(coords, this.cardBeingDragged));

                        this.cardBeingDragged = null;
                        succes = true;
                    }

                    e.setDropCompleted(succes);
                    e.consume();
                });

                // clickability
                cardView.setOnMouseClicked(e -> {
                    // we only allow to select a card if it is not revealed yet
                    if (cardView.getCardElem().isHidden()) {
                        this.deckCardClicked = new int[] {GridPane.getRowIndex(node), GridPane.getColumnIndex(node)};
                    }

                    if (this.waitingForCardSelection 
                            && this.cardBeingDragged != null
                            && this.deckCardClicked != null) {
                        GameLogic.gameController.execute(new DrawCardAction(this.pileBeingDrawned));
                        GameLogic.gameController.execute(new FlipCardAction(this.deckCardClicked));
                        GameLogic.gameController.execute(new DiscardCardAction(this.cardBeingDragged));

                        this.cardBeingDragged = null;
                        this.deckCardClicked = null;
                        this.waitingForCardSelection = false;
                    }

                });
            }
        }
    }

    private void setupZoneInteractions(VZoneView zoneView) {
        zoneView.setOnDragOver(e -> {
            if (e.getGestureSource() != zoneView 
                    && e.getDragboard().hasString() 
                    && e.getDragboard().getString().equals("card")) {
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
                    && dragboard.getString().equals("card") 
                    && this.cardBeingDragged != null) {

                // now we wait for the player to click on a hidden card
                this.waitingForCardSelection = true;

                // we also tell them by adding an indicator
                zoneView.setWaiting();

                succes = true;
            }

            e.setDropCompleted(succes);
            e.consume();
        });
    }
}
