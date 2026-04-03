package com.neuilleprime.gui.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Holds the full mutable state required to render and interact with the game screen.
 */
public class GameState {
    public final CardGrid grid;
    public final List<Card> jokers;
    public final List<Card> consumables;
    public Card drawPile;
    public Card discardPile;
    public int scoreToBeat;
    public int playerScore;

    /**
     * Creates a new GameState with an empty grid of the given dimensions.
     * The joker and consumable lists start empty, and {@code scoreToBeat} defaults to 0.
     *
     * @param gridCols number of columns in the card grid
     * @param gridRows number of rows in the card grid
     */
    public GameState(int gridCols, int gridRows) {
        this.grid = new CardGrid(gridCols, gridRows);
        this.jokers = new ArrayList<>();
        this.consumables = new ArrayList<>();
        this.scoreToBeat = 0;
        this.playerScore = 0;
    }
}
