package com.neuilleprime.gui.components;

import java.util.ArrayList;
import java.util.List;

import com.neuilleprime.game.Deck;
import javafx.geometry.Pos;
import javafx.scene.layout.GridPane;

/**
 * JavaFX component that renders a {@link Deck} as a grid of {@link CardView}s.
 * <p>
 * The grid is rebuilt automatically whenever the deck's dimensions (length or
 * height) change, or whenever the component's preferred size changes.
 * An optional {@link #setOnRebuild(Runnable)} callback fires after each rebuild
 * so callers can re-attach interaction handlers to the new card views.
 * </p>
 */
public class DeckView extends GridPane {

    /** The underlying game model deck this view is bound to. */
    private Deck deckElem;

    /** 2-D array of card views, indexed by {@code [row][col]}. */
    private CardView[][] cardViews;

    /** Optional callback invoked after every grid rebuild. */
    private Runnable onRebuild = null;

    /**
     * Registers a callback to be invoked each time the grid is rebuilt.
     * Useful for re-attaching drag-and-drop or click handlers to the new views.
     *
     * @param callback the runnable to call after every rebuild
     */
    public void setOnRebuild(Runnable callback) {
        this.onRebuild = callback;
    }

    /**
     * Constructs a {@code DeckView} bound to the given {@link Deck}.
     * Builds the initial grid and registers property listeners on the deck
     * dimensions and the component's preferred size.
     *
     * @param deck the game model deck to display
     */
    public DeckView(Deck deck) {
        this.deckElem = deck;
        cardViews = new CardView[deck.getHeight()][deck.getLength()];

        updateDeckGrid(0, this.prefHeightProperty().getValue());

        // bind the deck to auto-update its visuals based on the original one
        this.deckElem.lengthProperty().addListener((obs, oldVal, newVal) -> {
            updateDeckGrid(oldVal, newVal);
        });
        this.deckElem.heightProperty().addListener((obs, oldVal, newVal) -> {
            updateDeckGrid(oldVal, newVal);
        });

        this.prefHeightProperty().addListener((obs, oldVal, newVal) -> {
            updateDeckGrid(oldVal, newVal);
        });
        this.prefWidthProperty().addListener((obs, oldVal, newVal) -> {
            updateDeckGrid(oldVal, newVal);
        });

        // styling
        this.setAlignment(Pos.CENTER);
    }

    /**
     * Rebuilds the card grid from scratch based on the current deck dimensions
     * and preferred size. Clears all existing children, recreates {@link CardView}
     * instances, and fires {@link #onRebuild} if set.
     *
     * @param oldVal previous dimension value (unused, kept for listener compatibility)
     * @param newVal new dimension value used to compute padding and border styling
     */
    private void updateDeckGrid(Number oldVal, Number newVal) {
        // styling
        double cardPadding = newVal.doubleValue() * .05;
        double borderRadius = newVal.doubleValue() * .05;
        double borderWidth = newVal.doubleValue() * .02;
        this.setStyle(
            "-fx-background-color: #005aa9;" +
            "-fx-background-radius: "+borderRadius+" "+borderRadius+" "+borderRadius+" "+borderRadius+";" +
            "-fx-padding: "+cardPadding+" "+cardPadding+" "+cardPadding+" "+cardPadding+";" +
            "-fx-border-color: #00427c;" +
            "-fx-border-width: "+borderWidth+";" +
            "-fx-border-radius: "+borderRadius+" "+borderRadius+" "+borderRadius+" "+borderRadius+";"
        );

        // filling with actual content
        this.getChildren().clear();

        int rows = deckElem.getHeight();
        int cols = deckElem.getLength();

        for (int h = 0; h < rows; h++) {
            for (int l = 0; l < cols; l++) {
                CardView card = new CardView(deckElem.getCardAtCoords(h, l));
                card.prefHeightProperty().bind(this.prefHeightProperty().divide(rows));
                card.prefWidthProperty().bind(this.prefWidthProperty().divide(cols));

                // store the card in the 2D array
                cardViews[h][l] = card;

                this.add(card, l, h);
            }
        }

        if (onRebuild != null) onRebuild.run(); 
    }

    /**
     * Returns the {@link CardView} at the given grid coordinates, or {@code null}
     * if the coordinates are out of bounds.
     *
     * @param row row index (0-based)
     * @param col column index (0-based)
     * @return the card view at that position, or {@code null}
     */
    public CardView getCardViewAtCoords(int row, int col) {
        if (row < 0 || col < 0 || row >= cardViews.length || col >= cardViews[0].length) {
            return null;
        }
        return cardViews[row][col];
    }

    /**
     * Returns a flat list of all non-null {@link CardView}s in the grid,
     * in row-major order.
     *
     * @return list of all card views currently displayed
     */
    public List<CardView> getAllCardViews() {
        ArrayList<CardView> allCardViews = new ArrayList<>();

        for (int i = 0; i < cardViews.length; i++) {
            for (int j = 0; j < cardViews[i].length; j++) {
                if (cardViews[i][j] != null) {
                    allCardViews.add(cardViews[i][j]);
                }
            }
        }

        return allCardViews;
    }

    /**
     * Returns the underlying game model deck this view is bound to.
     *
     * @return the bound {@link Deck}
     */
    public Deck getDeckElem() {
        return this.deckElem;
    }
}
