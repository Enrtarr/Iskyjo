package com.neuilleprime.game.events;

import com.neuilleprime.game.Player;

/**
 * Event fired after a player successfully sells one of their jokers.
 */
public class JokerSoldEvent {

    /** The player who sold the joker. */
    public final Player player;

    /**
     * Constructs a new {@code JokerSoldEvent}.
     *
     * @param player the player who performed the sale
     */
    public JokerSoldEvent(Player player) {
        this.player = player;
    }

}
