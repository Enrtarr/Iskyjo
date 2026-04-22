package com.neuilleprime.game.events;

import java.util.ArrayList;
import com.neuilleprime.jokers.Joker;

public class ShopRerolledEvent {

    public final ArrayList<Joker> jokers;

    public ShopRerolledEvent(ArrayList<Joker> jokers) {
        this.jokers = jokers;
    }

}
