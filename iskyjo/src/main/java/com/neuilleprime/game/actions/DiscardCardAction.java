package com.neuilleprime.game.actions;

import com.neuilleprime.game.*;

/**
 * Action that discards a card to the discard pile.
 * <p>
 * Delegates to {@link GameController#discardCard(Player, Card)} using the
 * current player as determined by the game controller.
 * </p>
 */
public class DiscardCardAction implements Action {

    /** The card to be discarded. */
    private final Card card;

    /**
     * Constructs a new {@code DiscardCardAction}.
     *
     * @param card the card to discard
     */
    public DiscardCardAction(Card card) {
        this.card = card;
    }

    /**
     * Discards the card for the current player.
     *
     * @param game the game controller on which to execute the discard
     */
    @Override
    public void execute(GameController game) {
        game.discardCard(game.getCurrentPlayer(), this.card);
    }
}
