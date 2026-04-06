package com.neuilleprime.game.events;

import com.neuilleprime.game.Card;
import com.neuilleprime.game.Player;

public class TurnStartedEvent {
    public final Player currentPlayer;
    public final Card drawPileTop;
    public final Card discardPileTop;
    public final int round;

    public TurnStartedEvent(Player currentPlayer, Card drawPileTop, Card discardPileTop, int round) {
        this.currentPlayer = currentPlayer;
        this.drawPileTop = drawPileTop;
        this.discardPileTop = discardPileTop;
        this.round = round;
    }
}