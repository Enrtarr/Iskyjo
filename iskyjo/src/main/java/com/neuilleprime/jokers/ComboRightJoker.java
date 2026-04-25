package com.neuilleprime.jokers;

import java.util.ArrayList;

public class ComboRightJoker extends Joker {

    private int valueToAdd;
    private int appliesTo;

    public ComboRightJoker(int valueToAdd, int appliesTo) {
        this.valueToAdd = valueToAdd;
        this.appliesTo = appliesTo;
        this.name = "Combo right joker";
        this.description = "Adds "+valueToAdd+" to the right (card value) part of combos for card of value "+appliesTo;
        this.price = 2;
    }

    public static final JokerCategory CATEGORY = JokerCategory.COMBO;
    @Override
    public JokerCategory getCategory() { return CATEGORY; }

    public static final JokerRarity RARITY = JokerRarity.COMMON;
    @Override
    public JokerRarity getRarity() { return RARITY; }

    @Override
    public ArrayList<Integer> applyWithPos(ArrayList<int[][]> combos) {
        ArrayList<Integer> indexes = new ArrayList<>();
        for (int[][] c : combos) {
            if (c[0][1] == this.appliesTo) {
                c[0][1] = c[0][1] + this.valueToAdd;
                indexes.add(combos.indexOf(c));
            }
        }
        return indexes;
    }
}