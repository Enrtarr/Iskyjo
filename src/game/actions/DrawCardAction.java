package game.actions;

import game.*;

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