package com.neuilleprime.game;
/**
 * Represents a card with a numeric value and a visibility state.
 * <p>
 * Both the value and the hidden flag are backed by JavaFX observable properties
 * so that UI components can bind to them and react automatically to changes.
 * </p>
 */

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;

public class Card {

    /** The default card value used when none is specified. */
    private static final int DEFAULT_VALUE = 6;

    /** The default hidden state used when none is specified. */
    private static final boolean DEFAULT_HIDDEN = true;

    /** Observable integer property holding the card's numeric value. */
    private IntegerProperty value;

    /** Observable boolean property holding the card's visibility state. */
    private BooleanProperty hidden;

    /**
     * Constructs a card with default value and visibility.
     * Value = {@value #DEFAULT_VALUE}, Hidden = {@value #DEFAULT_HIDDEN}.
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
     * @param h the visibility state ({@code true} if hidden, {@code false} otherwise)
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
        return this.value.get();
    }

    /**
     * Checks whether the card is hidden.
     *
     * @return {@code true} if the card is hidden, {@code false} otherwise
     */
    public boolean isHidden() {
        return this.hidden.get();
    }

    /**
     * Makes the card visible (sets hidden to {@code false}).
     */
    public void show() {
        this.hidden.set(false);;
    }

    /**
     * Hides the card (sets hidden to {@code true}).
     */
    public void hide() {
        this.hidden.set(true);;
    }

    /**
     * Returns the observable integer property for the card's value.
     * Useful for binding UI labels directly to this property.
     *
     * @return the {@link IntegerProperty} backing the card's value
     */
    public IntegerProperty valueProperty() {
        return this.value;
    }

    /**
     * Returns the observable boolean property for the card's hidden state.
     * Useful for binding UI components that should react to flip events.
     *
     * @return the {@link BooleanProperty} backing the card's visibility
     */
    public BooleanProperty hiddenProperty() {
        return this.hidden;
    }
}
