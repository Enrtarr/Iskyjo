package com.neuilleprime.game.actions;

import com.neuilleprime.game.GameController;

public class ReadyUpAction implements Action {

    public ReadyUpAction() {

    }

    @Override
    public void execute(GameController game) {
        game.readyUp();;
    }
}
