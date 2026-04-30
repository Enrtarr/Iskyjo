package com.neuilleprime.jokers;

import java.util.ArrayList;

import com.neuilleprime.game.*;

/**
 * A joker that adds a fixed value to every card in the player's deck.
 * <p>
 * Category: {@link Joker.JokerCategory#DECK} — applied to the entire deck
 * during the scoring phase.
 * Rarity: {@link Joker.JokerRarity#UNCOMMON}.
 * </p>
 */
public class AddXDeckJoker extends Joker {

    /** The value to add to each card in the deck. */
    private int value;

    /**
     * Constructs a new {@code AddXDeckJoker}.
     *
     * @param x       the amount to add to every card's value
     * @param isConsu {@code true} if this joker is a consumable
     */
    public AddXDeckJoker(int x, boolean isConsu) {
        this.consumable = isConsu;
        this.value = x;
        this.name = "Add x deck joker";
        this.description = "Adds "+this.value+" to all cards in the deck";
        this.price = 2;
    }

    /** Constant category for this joker type. */
    public static final JokerCategory CATEGORY = JokerCategory.DECK;

    /**
     * {@inheritDoc}
     *
     * @return {@link JokerCategory#DECK}
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
     * Increases every card in the deck by {@link #value}.
     *
     * @param deck the deck to modify
     */
    @Override
    public void apply(Deck deck) {
        for (int i=0;i<deck.getHeight();i++) {
            for (int j=0;j<deck.getLength();j++) {
                int newVal = deck.getCardAtCoords(i, j).getValue() + this.value;
                deck.getCardAtCoords(i, j).setValue(newVal);
            }
        }
    }

    /**
     * Increases every card in the deck by {@link #value} and returns the
     * coordinates and new values for animation purposes.
     * <p>
     * Each entry in the returned list is {@code {row, col, newValue}}.
     * </p>
     *
     * @param deck the deck to modify
     * @return list of {@code Integer[]{row, col, newValue}} for every modified card
     */
    @Override
    public ArrayList<Integer[]> applyWithPos(Deck deck) {
        ArrayList<Integer[]> coords = new ArrayList<>();
        for (int i=0;i<deck.getHeight();i++) {
            for (int j=0;j<deck.getLength();j++) {
                int newVal = deck.getCardAtCoords(i, j).getValue() + this.value;
                coords.add(new Integer[] {i, j, newVal});
            }
        }
        return coords;
    }
}
