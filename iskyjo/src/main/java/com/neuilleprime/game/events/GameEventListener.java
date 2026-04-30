package com.neuilleprime.game.events;

/**
 * Listener interface for receiving game lifecycle events from the
 * {@link com.neuilleprime.game.GameController}.
 * <p>
 * All methods have default no-op implementations so that implementors only
 * need to override the events they care about.
 * </p>
 */
public interface GameEventListener {

    /**
     * Called when a new player's turn begins.
     *
     * @param event details about the turn that just started
     */
    default void onTurnStarted(TurnStartedEvent event) {}

    /**
     * Called when the current round ends (either by a player revealing all
     * their cards, or after the last player has taken their final turn).
     *
     * @param event details about the round result and game state
     */
    default void onRoundEnded(RoundEndedEvent event) {}

    /**
     * Called when the shop inventory has been rerolled (or refreshed at round start).
     *
     * @param event the new set of jokers available in the shop
     */
    default void onShopRerolledEvent(ShopRerolledEvent event) {}

    /**
     * Called after a player successfully sells one of their jokers.
     *
     * @param event details about the player who sold a joker
     */
    default void onJokerSold(JokerSoldEvent event) {}
}
