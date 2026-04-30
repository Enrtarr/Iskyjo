package com.neuilleprime.game.events;

import java.util.Map;

import com.neuilleprime.game.Deck;
import com.neuilleprime.game.Player;

/**
 * Event fired when a round ends, carrying the final scoring data and snapshots
 * of each player's deck and money at the moment the round concluded.
 */
public class RoundEndedEvent {

    /**
     * The score threshold that had to be reached this round.
     * In the next round this value represents the new quota.
     */
    public final int roundScore;

    /**
     * Current game state after round evaluation.
     * <ul>
     *   <li>0 = loss (game over)</li>
     *   <li>1 = next turn</li>
     *   <li>2 = next round / open shop</li>
     * </ul>
     */
    public final int gameState;

    /**
     * {@code true} if this event was fired during initial game setup
     * (i.e. before the first real round was played).
     */
    public final boolean setup;

    /** Snapshot of each player's deck as it was at round end (before cleanup). */
    public final Map<Player, Deck> playerDecks;

    /** Snapshot of each player's money balance at round end. */
    public final Map<Player, Integer> playerMoneys;

    /**
     * Constructs a new {@code RoundEndedEvent}.
     *
     * @param roundScore   the score threshold for the round that just ended
     * @param gameState    the resulting game state (0=loss, 1=next turn, 2=shop)
     * @param setup        whether this is a setup (first-round) event
     * @param playerDecks  map of player to their deck snapshot
     * @param playerMoneys map of player to their money balance snapshot
     */
    public RoundEndedEvent(
            int roundScore, 
            int gameState, 
            boolean setup, 
            Map<Player, Deck> playerDecks,
            Map<Player, Integer> playerMoneys
        ) {
        this.roundScore = roundScore;
        this.gameState = gameState;
        this.setup = setup;
        this.playerDecks = playerDecks;
        this.playerMoneys = playerMoneys;
    }

}
