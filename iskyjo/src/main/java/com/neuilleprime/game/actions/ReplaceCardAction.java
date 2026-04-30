package com.neuilleprime.game.actions;

import com.neuilleprime.game.*;

/**
 * Action that replaces a card in the current player's deck with a new card.
 * <p>
 * Delegates to {@link GameController#replaceCard(Player, int[], Card)}.
 * The replaced card is automatically sent to the discard pile.
 * </p>
 */
public class ReplaceCardAction implements Action {

    /** The {@code [row, col]} coordinates of the card to replace. */
    private final int[] cardCoords;

    /** The new card to place at the target coordinates. */
    private final Card newCard;

    /**
     * Constructs a new {@code ReplaceCardAction}.
     *
     * @param cardCoords {@code [row, col]} coordinates of the card to replace
     * @param newCard    the new card to place at those coordinates
     */
    public ReplaceCardAction(int[] cardCoords, Card newCard) {
        this.cardCoords = cardCoords;
        this.newCard = newCard;
    }

    /**
     * Replaces the card for the current player.
     *
     * @param game the game controller on which to execute the replacement
     */
    @Override
    public void execute(GameController game) {
        game.replaceCard(game.getCurrentPlayer(), this.cardCoords, this.newCard);
    }
}
