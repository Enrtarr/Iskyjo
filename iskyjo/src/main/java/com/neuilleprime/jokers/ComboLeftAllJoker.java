package com.neuilleprime.jokers;

import java.util.ArrayList;

public class ComboLeftAllJoker extends Joker {

    private int valueToAdd;

    public ComboLeftAllJoker(int valueToAdd) {
        this.valueToAdd = valueToAdd;
        this.name = "Combo left all joker";
        this.description = "Adds "+valueToAdd+" to the left (amount) part of all combos";
        this.price = 3;
    }

    public static final JokerCategory CATEGORY = JokerCategory.COMBO;
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

    @Override
    public ArrayList<Integer> applyWithPos(ArrayList<int[][]> combos) {
        ArrayList<Integer> indexes = new ArrayList<>();
        for (int[][] c : combos) {
            c[0][0] = c[0][0] + this.valueToAdd;
            indexes.add(combos.indexOf(c));
        }
        return indexes;
    }
}