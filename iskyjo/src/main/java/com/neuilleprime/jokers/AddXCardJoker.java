package com.neuilleprime.jokers;

import com.neuilleprime.game.*;

public class AddXCardJoker extends Joker {

    private int value;

    public AddXCardJoker(int x, boolean isConsu) {
        this.consumable = isConsu;
        this.value = x;
        this.description = "Adds "+this.value+" to all cards";
    }

    public static final JokerCategory CATEGORY = JokerCategory.CARD;
    @Override
    public JokerCategory getCategory() { return CATEGORY; }

    public static final JokerRarity RARITY = JokerRarity.COMMON;
    @Override
    public JokerRarity getRarity() { return RARITY; }

    @Override
    public void apply(Card card) {
        card.setValue(card.getValue()+this.value);
    }
}