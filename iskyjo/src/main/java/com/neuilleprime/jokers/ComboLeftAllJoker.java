package com.neuilleprime.jokers;

import java.util.ArrayList;

public class ComboLeftAllJoker extends Joker {

    private int valueToAdd;

    public ComboLeftAllJoker(int valueToAdd) {
        this.valueToAdd = valueToAdd;
        this.description = "Adds"+valueToAdd+" to the left (amount) part of combos";
    }

    public static final JokerCategory CATEGORY = JokerCategory.DECK;
    @Override
    public JokerCategory getCategory() { return CATEGORY; }

    public static final JokerRarity RARITY = JokerRarity.UNCOMMON;
    @Override
    public JokerRarity getRarity() { return RARITY; }

    @Override
    public void apply(ArrayList<int[]> combos) {
        for (int[] c : combos) {
            c[0] = c[0] + this.valueToAdd;
        }
    }
}