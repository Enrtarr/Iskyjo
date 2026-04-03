package com.neuilleprime.game.actions;

import com.neuilleprime.game.*;

public class BeginGameAction implements Action {

    public BeginGameAction() {}

    @Override
    public void execute(GameController game) {
        game.beginGame();
    }
}
