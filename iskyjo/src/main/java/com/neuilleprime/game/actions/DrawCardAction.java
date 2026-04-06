package com.neuilleprime.game.actions;

import com.neuilleprime.game.*;

public class DrawCardAction implements Action {
    private final String pileName;

    public DrawCardAction(String pileName) {
        this.pileName = pileName;
    }

    @Override
    public void execute(GameController game) {
        game.drawCard(this.pileName);
    }
}