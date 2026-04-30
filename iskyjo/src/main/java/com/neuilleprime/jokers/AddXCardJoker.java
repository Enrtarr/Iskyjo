package com.neuilleprime.jokers;

import com.neuilleprime.game.*;

/**
 * A joker that adds a fixed value to a single card.
 * <p>
 * Category: {@link Joker.JokerCategory#CARD} — applied to one card at a time.
 * Rarity: {@link Joker.JokerRarity#COMMON}.
 * </p>
 */
public class AddXCardJoker extends Joker {

    /** The value to add to the target card. */
    private int value;

    /**
     * Constructs a new {@code AddXCardJoker}.
     *
     * @param x       the amount to add to a card's value
     * @param isConsu {@code true} if this joker is a consumable
     */
    public AddXCardJoker(int x, boolean isConsu) {
        this.consumable = isConsu;
        this.value = x;
        this.name = "Add x card joker";
        this.description = "Adds "+this.value+" to one card";
        this.price = 1;
    }

    /** Constant category for this joker type. */
    public static final JokerCategory CATEGORY = JokerCategory.CARD;

    /**
     * {@inheritDoc}
     *
     * @return {@link JokerCategory#CARD}
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
     * Increases the given card's value by {@link #value}.
     *
     * @param card the card to modify
     */
    @Override
    public void apply(Card card) {
        card.setValue(card.getValue()+this.value);
    }
}
