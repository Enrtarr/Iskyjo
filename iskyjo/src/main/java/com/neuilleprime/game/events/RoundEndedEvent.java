package com.neuilleprime.game.events;

import java.util.Map;

import com.neuilleprime.game.Deck;
import com.neuilleprime.game.Player;

public class RoundEndedEvent {

    public final int roundScore;
    public final int gameState;
    public final boolean setup;
    public final Map<Player, Deck> playerDecks;
    public final Map<Player, Integer> playerMoneys;

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
