package com.neuilleprime.game.events;

import java.util.ArrayList;
import com.neuilleprime.jokers.Joker;

/**
 * Event fired when the shop inventory is refreshed, either because a player
 * paid to reroll or because a new round began.
 */
public class ShopRerolledEvent {

    /** The new list of jokers available for purchase in the shop. */
    public final ArrayList<Joker> jokers;

    /**
     * Constructs a new {@code ShopRerolledEvent}.
     *
     * @param jokers the updated list of jokers on offer
     */
    public ShopRerolledEvent(ArrayList<Joker> jokers) {
        this.jokers = jokers;
    }

}
