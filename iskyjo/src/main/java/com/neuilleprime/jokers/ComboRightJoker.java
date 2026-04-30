package com.neuilleprime.jokers;

import java.util.ArrayList;

/**
 * A joker that increases the card value (right side) of combos whose card value
 * exactly matches a target value.
 * <p>
 * Category: {@link Joker.JokerCategory#COMBO}.
 * Rarity: {@link Joker.JokerRarity#COMMON}.
 * </p>
 */
public class ComboRightJoker extends Joker {

    /** The amount to add to the card-value component of matching combos. */
    private int valueToAdd;

    /** Only combos whose card value equals this value are affected. */
    private int appliesTo;

    /**
     * Constructs a new {@code ComboRightJoker}.
     *
     * @param valueToAdd the amount to add to the card-value of matching combos
     * @param appliesTo  the card value that triggers this joker
     */
    public ComboRightJoker(int valueToAdd, int appliesTo) {
        this.valueToAdd = valueToAdd;
        this.appliesTo = appliesTo;
        this.name = "Combo right joker";
        this.description = "Adds "+valueToAdd+" to the right (card value) part of combos for card of value "+appliesTo;
        this.price = 2;
    }

    /** Constant category for this joker type. */
    public static final JokerCategory CATEGORY = JokerCategory.COMBO;

    /**
     * {@inheritDoc}
     *
     * @return {@link JokerCategory#COMBO}
     */
    @Override
    public JokerCategory getCategory() { return CATEGORY; }

    /** Constant rarity for this joker type. */
    public static final JokerRarity RARITY = JokerRarity.COMMON;

    /**
     * {@inheritDoc}
     *
     * @return {@link JokerRarity#COMMON}
     */
    @Override
    public JokerRarity getRarity() { return RARITY; }

    /**
     * Increases the card-value component of every combo whose card value equals
     * {@link #appliesTo}, and returns the indices of affected combos.
     *
     * @param combos list of combo entries (each {@code int[][]} with header + coord rows)
     * @return list of indices into {@code combos} that were modified
     */
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
