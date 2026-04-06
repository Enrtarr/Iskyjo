package com.neuilleprime.game.actions;

import com.neuilleprime.game.*;

public class DiscardCardAction implements Action {
    private final Card card;

    public DiscardCardAction(Card card) {
        this.card = card;
    }

    @Override
    public void execute(GameController game) {
        game.discardCard(game.getCurrentPlayer(), this.card);
    }
}
