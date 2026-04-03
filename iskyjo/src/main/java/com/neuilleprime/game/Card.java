package com.neuilleprime.game;
/**
 * Represents a card with a numeric value and a visibility state.
 */
public class Card {

    /** Default value assigned to a card */
    private static final int DEFAULT_VALUE = 0;

    /** Default visibility state (hidden by default) */
    private static final boolean DEFAULT_HIDDEN = true;

    /** The numeric value of the card */
    private int value;

    /** Whether the card is hidden */
    private boolean hidden;

    /**
     * Constructs a card with default value and visibility.
     * Value = 0, Hidden = true.
     */
    public Card() {
        this(DEFAULT_VALUE, DEFAULT_HIDDEN);
    }

    /**
     * Constructs a card with a specified value and default visibility.
     *
     * @param v the value of the card
     */
    public Card(int v) {
        this(v, DEFAULT_HIDDEN);
    }

    /**
     * Constructs a card with a specified value and visibility.
     *
     * @param v the value of the card
     * @param h the visibility state (true if hidden, false otherwise)
     */
    public Card(int v, boolean h) {
        this.value = v;
        this.hidden = h;
    }

    /**
     * Sets the value of the card.
     *
     * @param v the new value of the card
     */
    public void setValue(int v) {
        this.value = v;
    }

    /**
     * Returns the value of the card.
     *
     * @return the card's value
     */
    public int getValue() {
        // if (this.isHidden()) {
        //     throw (new IllegalStateException("The card is hidden and can therefor not be shown"));
        // }
        return this.value;
    }

    /**
     * Checks whether the card is hidden.
     *
     * @return true if the card is hidden, false otherwise
     */
    public boolean isHidden() {
        return this.hidden;
    }

    /**
     * Makes the card visible.
     */
    public void show() {
        this.hidden = false;
    }

    /**
     * Hides the card.
     */
    public void hide() {
        this.hidden = true;
    }
}