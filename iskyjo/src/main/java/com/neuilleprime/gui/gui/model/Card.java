package com.neuilleprime.gui.model;

/**
 * Represents a single game card.
 * A card stores its numeric value, whether it is currently selected,
 * and whether its front face is visible.
 */
public class Card {
    public final int value;
    public boolean selected;
    public boolean faceUp;

    /**
     * Creates a new Card with the given value and face orientation.
     *
     * @param value  the numeric value of the card (can be negative)
     * @param faceUp {@code true} if the card should be visible face-up; {@code false} for face-down
     */
    public Card(int value, boolean faceUp) {
        this.value = value;
        this.faceUp = faceUp;
        this.selected = false;
    }
}
