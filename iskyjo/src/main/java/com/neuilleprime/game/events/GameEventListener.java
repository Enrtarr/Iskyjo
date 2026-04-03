package com.neuilleprime.game.events;

public interface GameEventListener {
    void onTurnStarted(TurnStartedEvent event);
    void onRoundEnded(RoundEndedEvent event);
    // ...
}