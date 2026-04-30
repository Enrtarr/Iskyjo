package com.neuilleprime.game.events;

import com.neuilleprime.game.Card;
import com.neuilleprime.game.Player;

/**
 * Event fired at the start of each player's turn, providing a snapshot of the
 * information the player needs to make their decision.
 */
public class TurnStartedEvent {

    /** The player whose turn it now is. */
    public final Player currentPlayer;

    /**
     * The top card of the draw pile (visible to all), or {@code null} if empty.
     */
    public final Card drawPileTop;

    /**
     * The top card of the discard pile (visible to all), or {@code null} if empty.
     */
    public final Card discardPileTop;

    /** The current round number (1-based). */
    public final int round;

    /** The combined score all players must reach to survive this round. */
    public final int roundScore;

    /**
     * Constructs a new {@code TurnStartedEvent}.
     *
     * @param currentPlayer  the player whose turn is starting
     * @param drawPileTop    the top card of the draw pile, or {@code null}
     * @param discardPileTop the top card of the discard pile, or {@code null}
     * @param round          the current round number
     * @param roundScore     the score threshold for the current round
     */
    public TurnStartedEvent(Player currentPlayer, Card drawPileTop, Card discardPileTop, int round, int roundScore) {
        this.currentPlayer = currentPlayer;
        this.drawPileTop = drawPileTop;
        this.discardPileTop = discardPileTop;
        this.round = round;
        this.roundScore = roundScore;
    }
}
