package com.neuilleprime.jokers;

import java.util.ArrayList;

public class ComboLeftJoker extends Joker {

    private int valueToAdd;
    private int appliesTo;

    public ComboLeftJoker(int valueToAdd, int appliesTo) {
        this.valueToAdd = valueToAdd;
        this.appliesTo = appliesTo;
        this.name = "Combo left joker";
        this.description = "Adds "+valueToAdd+" to the left (amount) part of combos made of "+appliesTo+" cards";
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
            if (c[0][0] == this.appliesTo) {
                c[0][0] = c[0][0] + this.valueToAdd;
                indexes.add(combos.indexOf(c));
            }
        }
        return indexes;
    }
}