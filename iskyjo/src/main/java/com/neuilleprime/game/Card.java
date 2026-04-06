package com.neuilleprime.game;
/**
 * Represents a card with a numeric value and a visibility state.
 */

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;

public class Card {

    private static final int DEFAULT_VALUE = 6;
    private static final boolean DEFAULT_HIDDEN = true;

    // private int value;
    // private boolean hidden;
    private IntegerProperty value;
    private BooleanProperty hidden;

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
        this.value = new SimpleIntegerProperty(v);
        this.hidden = new SimpleBooleanProperty(h);
    }

    /**
     * Sets the value of the card.
     *
     * @param v the new value of the card
     */
    public void setValue(int v) {
        this.value.set(v);;
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
        return this.value.get();
    }

    /**
     * Checks whether the card is hidden.
     *
     * @return true if the card is hidden, false otherwise
     */
    public boolean isHidden() {
        return this.hidden.get();
    }

    /**
     * Makes the card visible.
     */
    public void show() {
        this.hidden.set(false);;
    }

    /**
     * Hides the card.
     */
    public void hide() {
        this.hidden.set(true);;
    }

    public IntegerProperty valueProperty() {
        return this.value;
    }

    public BooleanProperty hiddenProperty() {
        return this.hidden;
    }
}