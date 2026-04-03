package com.neuilleprime.gui.model;

/**
 * Represents the rectangular grid used by the game board.
 *
 * <p>The grid stores cards by column and row and provides a small helper
 * to retrieve the first currently selected card.
 */
public class CardGrid {
    public final int cols;
    public final int rows;
    private final Card[][] slots;

    /**
     * Creates a new CardGrid with the specified dimensions.
     * All slots are initialised to {@code null} (empty).
     *
     * @param cols number of columns in the grid
     * @param rows number of rows in the grid
     */
    public CardGrid(int cols, int rows) {
        this.cols = cols;
        this.rows = rows;
        this.slots = new Card[cols][rows];
    }

    /**
     * Returns the card at the given grid position, or {@code null} if the slot is empty.
     *
     * @param col zero-based column index
     * @param row zero-based row index
     * @return the {@link Card} at {@code (col, row)}, or {@code null}
     */
    public Card get(int col, int row) {
        return slots[col][row];
    }

    /**
     * Places a card at the given grid position.
     * Pass {@code null} to clear the slot.
     *
     * @param col  zero-based column index
     * @param row  zero-based row index
     * @param card the {@link Card} to place, or {@code null} to empty the slot
     */
    public void set(int col, int row, Card card) {
        slots[col][row] = card;
    }

    /**
     * Searches the entire grid and returns the first card that is currently selected.
     *
     * @return the first selected {@link Card}, or {@code null} if none is selected
     */
    public Card getSelected() {
        for (int c = 0; c < cols; c++) {
            for (int r = 0; r < rows; r++) {
                if (slots[c][r] != null && slots[c][r].selected) {
                    return slots[c][r];
                }
            }
        }
        return null;
    }
}
