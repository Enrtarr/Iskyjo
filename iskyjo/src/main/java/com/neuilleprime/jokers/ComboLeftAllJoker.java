package com.neuilleprime.jokers;

import java.util.ArrayList;

/**
 * A joker that increases the multiplier (left side) of every detected combo.
 * <p>
 * Category: {@link Joker.JokerCategory#COMBO} — applied to the full combo list
 * during the scoring phase.
 * Rarity: {@link Joker.JokerRarity#UNCOMMON}.
 * </p>
 */
public class ComboLeftAllJoker extends Joker {

    /** The amount added to the streak-length component of every combo. */
    private int valueToAdd;

    /**
     * Constructs a new {@code ComboLeftAllJoker}.
     *
     * @param valueToAdd the amount to add to the left (streak-length) of all combos
     */
    public ComboLeftAllJoker(int valueToAdd) {
        this.valueToAdd = valueToAdd;
        this.name = "Combo left all joker";
        this.description = "Adds "+valueToAdd+" to the left (amount) part of all combos";
        this.price = 3;
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
    public static final JokerRarity RARITY = JokerRarity.UNCOMMON;

    /**
     * {@inheritDoc}
     *
     * @return {@link JokerRarity#UNCOMMON}
     */
    @Override
    public JokerRarity getRarity() { return RARITY; }

    /**
     * Increases the streak-length component ({@code c[0]}) of every combo entry
     * by {@link #valueToAdd}.
     *
     * @param combos list of {@code {streak_length, card_value}} entries to modify
     */
    @Override
    public void apply(ArrayList<int[]> combos) {
        for (int[] c : combos) {
            c[0] = c[0] + this.valueToAdd;
        }
    }

    /**
     * Increases the streak-length component of every combo entry and returns the
     * indices of all modified entries for animation purposes.
     *
     * @param combos list of combo entries (each {@code int[][]} with header + coord rows)
     * @return list of indices into {@code combos} that were modified
     */
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
