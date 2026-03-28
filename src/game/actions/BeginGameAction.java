package game.actions;

import game.*;

public class BeginGameAction implements Action {

    public BeginGameAction() {}

    @Override
    public void execute(GameController game) {
        game.beginGame();
    }
}
