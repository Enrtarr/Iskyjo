package com.neuilleprime.game.actions;

import com.neuilleprime.game.*;

public class FlipCardAction implements Action {
    private final int[] cardCoords;

    public FlipCardAction(int[] cardCoords) {
        this.cardCoords = cardCoords;
    }

    @Override
    public void execute(GameController game) {
        game.flipCard(game.getCurrentPlayer(), this.cardCoords);
    }
}
