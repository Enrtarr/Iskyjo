package game.actions;

import game.*;

public class ReplaceCardAction implements Action {
    private final int[] cardCoords;
    private final Card newCard;

    public ReplaceCardAction(int[] cardCoords, Card newCard) {
        this.cardCoords = cardCoords;
        this.newCard = newCard;
    }

    @Override
    public void execute(GameController game) {
        game.replaceCard(game.getCurrentPlayer(), this.cardCoords, this.newCard);
    }
}
