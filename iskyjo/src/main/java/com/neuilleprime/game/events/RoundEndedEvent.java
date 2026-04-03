package com.neuilleprime.game.events;


public class RoundEndedEvent {

    public final int roundScore;
    public final int gameState;

    public RoundEndedEvent(int roundScore, int gameState) {
        this.roundScore = roundScore;
        this.gameState = gameState;
    }

}
