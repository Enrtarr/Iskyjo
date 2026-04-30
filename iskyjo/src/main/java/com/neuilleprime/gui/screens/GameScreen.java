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
import com.neuilleprime.gui.components.VTextBox;
import com.neuilleprime.gui.utils.SideBarsHelper;
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

/**
 * The main in-game screen where the player takes their turns.
 * <p>
 * Listens to {@link TurnStartedEvent} to rebuild the full UI for each turn:
 * pile views, the player's deck, top-bar info labels, and joker/consumable bars.
 * Listens to {@link RoundEndedEvent} to route to either the game-over screen or
 * the result screen.
 * </p>
 * <p>
 * Player interactions are handled via JavaFX drag-and-drop and mouse-click:
 * <ul>
 *   <li>Dragging a pile card onto a deck card → replace that card.</li>
 *   <li>Dragging a pile card onto the discard zone → flip a hidden card.</li>
 *   <li>Clicking a hidden deck card → selects it for the flip interaction.</li>
 * </ul>
 * </p>
 */
public class GameScreen {

    /** Screen manager used to navigate between scenes. */
    private final ScreenManager sm;

    /**
     * Name of the pile the player is currently drawing from ({@code "draw"} or
     * {@code "discard"}), or {@code null} when no drag is active.
     */
    private String pileBeingDrawned = null;

    /**
     * The card being dragged from a pile, or {@code null} when no drag is active.
     */
    private Card cardBeingDragged = null;

    /**
     * Coordinates {@code [row, col]} of the deck card the player last clicked,
     * or {@code null} if none has been clicked yet.
     */
    private int[] deckCardClicked = null;

    /**
     * {@code true} while the game is waiting for the player to click a hidden
     * card after dropping a pile card onto the discard zone.
     */
    private boolean waitingForCardSelection = false;

    /**
     * Constructs a {@code GameScreen}.
     *
     * @param sm the application screen manager
     */
    public GameScreen(ScreenManager sm) {
        this.sm = sm;
    }

    /**
     * Builds and returns the game {@link Scene}.
     * <p>
     * Registers a {@link GameEventListener} on the local player that rebuilds
     * the entire layout on each turn and routes to the appropriate screen when
     * a round ends.
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

        StackPane centerBar = new StackPane();
        centerBar.setAlignment(Pos.CENTER);

        root.setLeft(leftBar);
        root.setRight(rightBar);
        root.setTop(topBar);
        root.setBottom(bottomBar);
        root.setCenter(centerBar);

        if (GameLogic.gameController == null) {
            GameLogic.gameController = GameLogic.setupGame(1, 150); 
        }

        Player player = GameLogic.localPlayer;

        GameLogic.gameController.addListener(player, new GameEventListener() {

            @Override
            public void onTurnStarted(TurnStartedEvent event) {
                Platform.runLater(() -> {
                    boolean isLocalPlrTurn = event.currentPlayer == GameLogic.localPlayer;

                    topBar.getChildren().clear();
                    bottomBar.getChildren().clear();
                    leftBar.getChildren().clear();
                    rightBar.getChildren().clear();
                    centerBar.getChildren().clear();

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

                    SideBarsHelper.loadBottomBar(bottomBar, event.currentPlayer);

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

                    VTextBox discardZoneView = new VTextBox("Discard");
                    discardZoneView.setNameColor("#00a6ff");
                    discardZoneView.setContentColor("#000000");
                    discardZoneView.setBackgroundColor("#a92a00");
                    discardZoneView.setBorderColor("#7c2300");
                    discardZoneView.setNameSize(.1);
                    discardZoneView.setContentSize(.4);
                    discardZoneView.prefWidthProperty().bind(rightBar.prefWidthProperty());
                    discardZoneView.prefHeightProperty().bind(rightBar.prefHeightProperty());

                    if (isLocalPlrTurn) {setupZoneInteractions(discardZoneView);}

                    rightBar.getChildren().add(discardZoneView);

                    DeckView deckView = new DeckView(event.currentPlayer.getDeck());
                    deckView.prefWidthProperty().bind(centerBar.widthProperty().multiply(0.4));
                    deckView.prefHeightProperty().bind(centerBar.heightProperty().multiply(0.6));

                    deckView.setOnRebuild(() -> setupDeckInteractions(deckView));
                    setupDeckInteractions(deckView);

                    centerBar.getChildren().add(deckView);

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
                            sm.show("result");
                        }
                    }
                });
            }
        });

        return scene;
    }

    /**
     * Attaches drag-detection handlers to the top card of a pile view so the
     * player can drag it onto a deck card or the discard zone.
     *
     * @param pileTopView the pile view to make draggable
     * @param pileName    {@code "draw"} or {@code "discard"} — identifies the source pile
     */
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

            SnapshotParameters params = new SnapshotParameters();
            params.setFill(Color.TRANSPARENT);
            WritableImage snapshot = pileTopView.getTopCardView().snapshot(params, null);
            dragboard.setDragView(snapshot, e.getX(), e.getY());

            e.consume();
        });

        topCardView.setOnDragDone(e -> {
            if (e.getTransferMode() == transferMode) {
            }
            e.consume();
        });
    }

    /**
     * Attaches drag-over, drag-enter, drag-exit, drag-drop, and click handlers
     * to every {@link CardView} inside the given {@link DeckView}.
     * <p>
     * Dropping a card from a pile replaces the target deck card.
     * Clicking a hidden card selects it; if the player previously dropped a
     * card on the discard zone, the click also completes the flip interaction.
     * </p>
     *
     * @param deckView the deck view whose cards should become interactive
     */
    private void setupDeckInteractions(DeckView deckView) {
        for (Node node : deckView.getChildren()) {
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

                        GameLogic.gameController.execute(new DrawCardAction(this.pileBeingDrawned));

                        int[] coords = new int[] {GridPane.getRowIndex(node), GridPane.getColumnIndex(node)};
                        GameLogic.gameController.execute(new ReplaceCardAction(coords, this.cardBeingDragged));

                        this.cardBeingDragged = null;
                        succes = true;
                    }

                    e.setDropCompleted(succes);
                    e.consume();
                });

                cardView.setOnMouseClicked(e -> {
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

    /**
     * Attaches drag-over, drag-enter, drag-exit, and drag-drop handlers to the
     * discard zone.
     * <p>
     * When a pile card is dropped here, the game enters the "waiting for card
     * selection" state and updates the zone label with an instruction prompt.
     * The actual flip and discard are executed once the player clicks a hidden
     * deck card.
     * </p>
     *
     * @param zoneView the {@link VTextBox} acting as the discard drop zone
     */
    private void setupZoneInteractions(VTextBox zoneView) {
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

                this.waitingForCardSelection = true;
                zoneView.setText("Please click on a card to reveal it");

                succes = true;
            }

            e.setDropCompleted(succes);
            e.consume();
        });
    }
}
