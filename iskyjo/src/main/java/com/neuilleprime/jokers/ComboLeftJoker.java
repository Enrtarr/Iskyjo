package com.neuilleprime.jokers;

import java.util.ArrayList;

/**
 * A joker that increases the multiplier (left side) of combos whose streak
 * length exactly matches a target size.
 * <p>
 * Category: {@link Joker.JokerCategory#COMBO}.
 * Rarity: {@link Joker.JokerRarity#COMMON}.
 * </p>
 */
public class ComboLeftJoker extends Joker {

    /** The amount to add to the streak-length of matching combos. */
    private int valueToAdd;

    /** Only combos whose current streak length equals this value are affected. */
    private int appliesTo;

    /**
     * Constructs a new {@code ComboLeftJoker}.
     *
     * @param valueToAdd the amount to add to the streak-length of matching combos
     * @param appliesTo  the streak length that triggers this joker
     */
    public ComboLeftJoker(int valueToAdd, int appliesTo) {
        this.valueToAdd = valueToAdd;
        this.appliesTo = appliesTo;
        this.name = "Combo left joker";
        this.description = "Adds "+valueToAdd+" to the left (amount) part of combos made of "+appliesTo+" cards";
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
     * Increases the streak-length component of every combo whose current streak
     * length equals {@link #appliesTo}, and returns the indices of affected combos.
     *
     * @param combos list of combo entries (each {@code int[][]} with header + coord rows)
     * @return list of indices into {@code combos} that were modified
     */
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
