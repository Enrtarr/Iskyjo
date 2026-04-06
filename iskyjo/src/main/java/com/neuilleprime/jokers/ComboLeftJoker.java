package com.neuilleprime.jokers;

import java.util.ArrayList;

public class ComboLeftJoker extends Joker {

    private int valueToAdd;
    private int appliesTo;

    public ComboLeftJoker(int valueToAdd, int appliesTo) {
        this.valueToAdd = valueToAdd;
        this.appliesTo = appliesTo;
        this.description = "Adds"+valueToAdd+" to the left (amount) part of combos";
        this.name = "lorem ipsum";
    }

    public static final JokerCategory CATEGORY = JokerCategory.DECK;
    @Override
    public JokerCategory getCategory() { return CATEGORY; }

    public static final JokerRarity RARITY = JokerRarity.COMMON;
    @Override
    public JokerRarity getRarity() { return RARITY; }

    @Override
    public void apply(ArrayList<int[]> combos) {
        for (int[] c : combos) {
            if (c[0] == this.appliesTo) {
                c[0] = c[0] + this.valueToAdd;
            }
        }
    }
}