package com.neuilleprime.game.events;

import java.util.ArrayList;
import com.neuilleprime.jokers.Joker;

public class ShopRerolledEvent {

    public final ArrayList<Joker> jokers;
    public final int newRerollPrice;

    public ShopRerolledEvent(ArrayList<Joker> jokers, int newRerollPrice) {
        this.jokers = jokers;
        this.newRerollPrice = newRerollPrice;
    }

}
