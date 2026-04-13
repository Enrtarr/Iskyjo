package com.neuilleprime.game.events;

import com.neuilleprime.game.Card;
import com.neuilleprime.game.Player;

public class TurnStartedEvent {
    public final Player currentPlayer;
    public final Card drawPileTop;
    public final Card discardPileTop;
    public final int round;
    public final int roundScore;

    public TurnStartedEvent(Player currentPlayer, Card drawPileTop, Card discardPileTop, int round, int roundScore) {
        this.currentPlayer = currentPlayer;
        this.drawPileTop = drawPileTop;
        this.discardPileTop = discardPileTop;
        this.round = round;
        this.roundScore = roundScore;
    }
}