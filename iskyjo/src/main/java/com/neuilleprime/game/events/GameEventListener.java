package com.neuilleprime.game.events;

public interface GameEventListener {
    // void onTurnStarted(TurnStartedEvent event);
    // void onRoundEnded(RoundEndedEvent event);
    default void onTurnStarted(TurnStartedEvent event) {}
    default void onRoundEnded(RoundEndedEvent event) {}
    default void onShopRerolledEvent(ShopRerolledEvent event) {}
    default void onJokerSold(JokerSoldEvent event) {}
}