package com.neuilleprime.game.actions;

import com.neuilleprime.game.*;

/**
 * Action that toggles the visibility of a card in the current player's deck.
 * <p>
 * Delegates to {@link GameController#flipCard(Player, int[])}.
 * </p>
 */
public class FlipCardAction implements Action {

    /** The {@code [row, col]} coordinates of the card to flip. */
    private final int[] cardCoords;

    /**
     * Constructs a new {@code FlipCardAction}.
     *
     * @param cardCoords {@code [row, col]} coordinates of the card to flip
     */
    public FlipCardAction(int[] cardCoords) {
        this.cardCoords = cardCoords;
    }

    /**
     * Flips the card at the stored coordinates for the current player.
     *
     * @param game the game controller on which to execute the flip
     */
    @Override
    public void execute(GameController game) {
        game.flipCard(game.getCurrentPlayer(), this.cardCoords);
    }
}
