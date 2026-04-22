package com.neuilleprime.jokers;

import com.neuilleprime.game.*;

public class AddXDeckJoker extends Joker {

    private int value;

    public AddXDeckJoker(int x, boolean isConsu) {
        this.consumable = isConsu;
        this.value = x;
        this.name = "Add x deck joker";
        this.description = "Adds "+this.value+" to all cards in the deck";
        this.price = 2;
    }

    public static final JokerCategory CATEGORY = JokerCategory.DECK;
    @Override
    public JokerCategory getCategory() { return CATEGORY; }

    public static final JokerRarity RARITY = JokerRarity.COMMON;
    @Override
    public JokerRarity getRarity() { return RARITY; }

    @Override
    public void apply(Deck deck) {
        for (Card c : deck.getAllCards()) {
            c.setValue(c.getValue()+this.value);
        }
    }
}