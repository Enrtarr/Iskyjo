package com.neuilleprime.gui.components;

import java.util.ArrayList;
import java.util.List;

import com.neuilleprime.game.Deck;
import javafx.geometry.Pos;
import javafx.scene.layout.GridPane;

public class DeckView extends GridPane {

    private Deck deckElem;
    private CardView[][] cardViews;

    private Runnable onRebuild = null;
    
    public void setOnRebuild(Runnable callback) {
        this.onRebuild = callback;
    }

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

    // Note that this isn't the cleanest design because we rebuild the deck from the ground up
    // Also, we should try to change it so we can add an effect when a card is being removed
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

    // get the CardView at the specified coordinates (row, col)
    public CardView getCardViewAtCoords(int row, int col) {
        if (row < 0 || col < 0 || row >= cardViews.length || col >= cardViews[0].length) {
            return null;
        }
        return cardViews[row][col];
    }
    
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

    public Deck getDeckElem() {
        return this.deckElem;
    }
}