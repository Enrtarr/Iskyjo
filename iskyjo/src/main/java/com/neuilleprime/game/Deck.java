package com.neuilleprime.game;

import java.util.ArrayList;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;

/**
 * Represents a 2-D grid of {@link Card}s with rows and columns.
 * <p>
 * The deck supports dynamic row/column addition and removal, card retrieval
 * by coordinate, and streak-scanning in all four directions (rows, columns,
 * diagonals, anti-diagonals) used during end-of-round scoring.
 * Both the current length (columns) and height (rows) are backed by observable
 * {@link IntegerProperty}s so that UI components can bind to them.
 * </p>
 */
public class Deck {
    /** Default maximum number of columns. */
    private static final int DEFAULT_MAX_LENGTH = 4;

    /** Default maximum number of rows. */
    private static final int DEFAULT_MAX_HEIGHT = 3;

    /** Observable integer property for the current number of columns. */
    private IntegerProperty length;

    /** Observable integer property for the current number of rows. */
    private IntegerProperty height;

    /** Maximum number of columns this deck can hold. */
    private int maxLength;

    /** Maximum number of rows this deck can hold. */
    private int maxHeight;

    /**
     * Row-major matrix storing the cards.
     * {@code matrix.get(row).get(col)} gives the card at that position.
     */
    ArrayList<ArrayList<Card>> matrix = new ArrayList<>();

    /**
     * Constructs a Deck with default maximum length and height.
     */
    public Deck() {
        this(DEFAULT_MAX_LENGTH, DEFAULT_MAX_HEIGHT);
    }

    /**
     * Constructs a Deck with specified maximum length and height.
     *
     * @param l maximum number of columns
     * @param h maximum number of rows
     */
    public Deck(int l, int h) {
        this.length = new SimpleIntegerProperty(0);
        this.height = new SimpleIntegerProperty(0);
        this.maxLength = l;
        this.maxHeight = h;
    }

    /**
     * Removes and returns the row at the given index.
     *
     * @param j index of the row to remove (0-based)
     * @return the removed row, or {@code null} if the deck is already empty
     */
    public ArrayList<Card> removeRow(int j) {
        if (this.height.get() != 0) {
            this.height.set(this.height.get() - 1);
            return this.matrix.remove(j);
        }
        else {
            System.out.println("Can not remove row, deck is already empty");
            return null;
        }
    }

    /**
     * Appends a row of cards to the deck.
     * <p>
     * If this is the first row added, it also fixes the deck's column count.
     * Subsequent rows must have the same size as the first.
     * </p>
     *
     * @param row row of cards to add; must match the existing column count (if any)
     */
    public void addRow(ArrayList<Card> row) {
        if (this.height.get() + 1 <= this.maxHeight) {
            if (this.height.get() == 0) {
                this.length.set(row.size());
            }

            if (row.size() == this.length.get()) {
                this.matrix.add(row);
                this.height.set(this.height.get() + 1);
            }
            else {
                System.out.println("Wrong length (given: "+row.size()+", expected: "+this.length.get()+")");
            }
        }
        else {
            System.out.println("Max height reached ("+this.maxHeight+")");
        }
    }

    /**
     * Removes and returns all cards in the column at the given index.
     *
     * @param i index of the column to remove (0-based)
     * @return the removed column as a flat list, or {@code null} if the deck is empty
     */
    public ArrayList<Card> removeColumn(int i) {
        if (this.length.get() != 0) {
            ArrayList<Card> removed = new ArrayList<>();
            for (int j=0; j<this.matrix.size(); j++) {
                removed.add(this.matrix.get(j).remove(i));
            }
            this.length.set(this.length.get() - 1);
            return removed;
        }
        else {
            System.out.println("Deck is already empty");
            return null;
        }
    }

    /**
     * Appends a column of cards to the right of the deck.
     *
     * @param col column of cards to add; must have {@link #getHeight()} elements
     */
    public void addColumn(ArrayList<Card> col) {
        if (this.length.get() + 1 <= this.maxLength) {
            if (col.size() == this.height.get()) {
                for (int j = 0; j < this.height.get(); j++) {
                    this.matrix.get(j).add(col.get(j));
                }
                this.length.set(this.length.get() + 1);
            }
            else {
                System.out.println("Wrong height (given: "+col.size()+", expected: "+this.height.get()+")");
            }
        }
        else {
            System.out.println("Max length reached ("+this.maxLength+")");
        }
    }

    /**
     * Returns the current number of columns.
     *
     * @return current column count
     */
    public int getLength() {
        return this.length.get();
    }

    /**
     * Returns the current number of rows.
     *
     * @return current row count
     */
    public int getHeight() {
        return this.height.get();
    }

    /**
     * Returns the maximum number of columns this deck can hold.
     *
     * @return maximum column count
     */
    public int getMaxLength() {
        return this.maxLength;
    }

    /**
     * Returns the maximum number of rows this deck can hold.
     *
     * @return maximum row count
     */
    public int getMaxHeight() {
        return this.maxHeight;
    }

    /**
     * Returns the observable integer property for the current column count.
     *
     * @return length property
     */
    public IntegerProperty lengthProperty() {
        return this.length;
    }

    /**
     * Returns the observable integer property for the current row count.
     *
     * @return height property
     */
    public IntegerProperty heightProperty() {
        return this.height;
    }

    /**
     * Creates a deep copy of this deck with all card values and hidden states
     * preserved. The copy has the same max dimensions.
     *
     * @return a new {@code Deck} instance with cloned cards
     */
    public Deck getFreshDeck() {
        Deck newDeck = new Deck(this.maxLength, this.maxHeight);
        for (ArrayList<Card> row : this.matrix) {
            ArrayList<Card> newRow = new ArrayList<>();
            for (Card c : row) {
                newRow.add(new Card(c.getValue(), c.isHidden()));
            }
            newDeck.addRow(newRow);
        }
        return newDeck;
    }

    /**
     * Returns {@code true} if there is a card at the given coordinates.
     *
     * @param coords {@code [row, col]} coordinates to check
     * @return {@code true} if the coordinates are within bounds
     */
    public boolean hasCardAtCoords(int[] coords) {
        if (coords[0] <= this.height.get() && coords[1] <= this.length.get()) {
            return true;
        }
        else {
            return false;
        }
    }

    /**
     * Returns {@code true} if there is a card at the given row and column.
     *
     * @param h row index (0-based)
     * @param l column index (0-based)
     * @return {@code true} if the coordinates are within bounds
     */
    public boolean hasCardAtCoords(int h, int l) {
        if (h <= this.height.get() && l <= this.length.get()) {
            return true;
        }
        else {
            return false;
        }
    }

    /**
     * Returns the card at the given {@code [row, col]} coordinates.
     *
     * @param coords {@code [row, col]} coordinates
     * @return the {@link Card} at those coordinates
     * @throws IllegalArgumentException if no card exists at the coordinates
     */
    public Card getCardAtCoords(int[] coords) {
        if (!this.hasCardAtCoords(coords)) {
            throw (new IllegalArgumentException("No card at these coordinates"));
        }
        return this.matrix.get(coords[0]).get(coords[1]);
    }

    /**
     * Returns the card at the given row and column.
     *
     * @param h row index (0-based)
     * @param l column index (0-based)
     * @return the {@link Card} at that position
     * @throws IllegalArgumentException if no card exists at the coordinates
     */
    public Card getCardAtCoords(int h, int l) {
        if (!this.hasCardAtCoords(h, l)) {
            throw (new IllegalArgumentException("No card at these coordinates"));
        }
        return this.matrix.get(h).get(l);
    }

    /**
     * Replaces the card at the given {@code [row, col]} coordinates.
     *
     * @param coords {@code [row, col]} coordinates
     * @param card   the card to place at those coordinates
     * @throws IllegalArgumentException if no card exists at the coordinates
     */
    public void setCardAtCoords(int[] coords, Card card) {
        if (!this.hasCardAtCoords(coords)) {
            throw (new IllegalArgumentException("No card at these coordinates"));
        }
        this.matrix.get(coords[0]).set(coords[1], card);
    }

    /**
     * Replaces the card at the given row and column.
     *
     * @param h    row index (0-based)
     * @param l    column index (0-based)
     * @param card the card to place at that position
     * @throws IllegalArgumentException if no card exists at the coordinates
     */
    public void setCardAtCoords(int h, int l, Card card) {
        if (!this.hasCardAtCoords(h, l)) {
            throw (new IllegalArgumentException("No card at these coordinates"));
        }
        this.matrix.get(h).set(l, card);
    }

    /**
     * Returns {@code true} if at least one card in the deck is still hidden.
     *
     * @return {@code true} if any hidden card exists
     */
    public boolean hasHiddenCard() {
        for (ArrayList<Card> row : this.matrix) {
            for (Card c : row) {
                if (c.isHidden()) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Returns all cards in the deck as a flat list in row-major order.
     *
     * @return list of all {@link Card}s
     */
    public ArrayList<Card> getAllCards() {
        ArrayList<Card> cards = new ArrayList<>();
        for (ArrayList<Card> row : this.matrix) {
            for (Card c : row) {
                cards.add(c);
            }
        }
        return cards;
    }

    /**
     * Removes all rows and columns, resetting the deck to an empty state.
     */
    public void clear() {
        for (ArrayList<Card> row : this.matrix) {
            row.clear();
        }
        this.matrix.clear();
        this.length.set(0);
        this.height.set(0);
    }

    /**
     * Prints the deck to the console in a 2-D format.
     * Hidden cards are suffixed with {@code h}.
     */
    public void printAll() {
        System.out.println("[");
        for (int i = 0; i < matrix.size(); i++) {
            System.out.print("[");
            for (int j = 0; j < matrix.get(i).size(); j++) {
                if (!matrix.get(i).get(j).isHidden()) {
                    System.out.print(matrix.get(i).get(j).getValue() + " ");
                }
                else {
                    System.out.print(matrix.get(i).get(j).getValue() + "h ");
                }
            }
            System.out.println("]");
        }
        System.out.println("]");
    }

    /**
     * Toggles the visibility of the card at the given coordinates.
     * If the card is currently hidden it is shown, and vice-versa.
     *
     * @param cardCoords {@code [row, col]} coordinates of the card to flip
     * @throws IllegalArgumentException if no card exists at the coordinates
     */
    public void flipCard(int[] cardCoords) {
        if(!this.hasCardAtCoords(cardCoords)) {
            throw (new IllegalArgumentException("No card at these coordinates"));
        }

        if (this.getCardAtCoords(cardCoords).isHidden()) {
            this.getCardAtCoords(cardCoords).show();
        }
        else {
            this.getCardAtCoords(cardCoords).hide();
        }
    }

    /**
     * Replaces the card at the given coordinates with {@code newCard} and
     * returns the old card.
     *
     * @param cardCoords {@code [row, col]} coordinates of the card to replace
     * @param newCard    the new card to place at those coordinates
     * @return the card that was previously at those coordinates
     * @throws IllegalArgumentException if no card exists at the coordinates
     */
    public Card replaceCard(int[] cardCoords, Card newCard) {
        if(!this.hasCardAtCoords(cardCoords)) {
            throw (new IllegalArgumentException("No card at these coordinates"));
        }

        Card oldCard = this.getCardAtCoords(cardCoords);
        this.setCardAtCoords(cardCoords, newCard);

        return oldCard;
    }

    /**
     * Scans all rows for consecutive identical card values and returns the streaks.
     * Each entry is {@code {streak_length, card_value}}.
     *
     * @return list of streaks found across all rows
     */
    private ArrayList<int[]> scanRows() {
        ArrayList<int[]> finalList = new ArrayList<>();
        for (int i=0;i<this.height.get();i++) {
            int cur = 0, prev = 0, streak = 0;
            for (Card c : this.matrix.get(i)) {
                cur = c.getValue();
                if (cur == prev) {
                    streak++;
                }
                else {
                    if (streak > 0) {
                        finalList.add(new int[]{streak+1,prev});
                    }
                    streak = 0;
                }
                prev = cur;
            }
            if (streak > 0) {
                finalList.add(new int[]{streak+1,prev});
            }
        }
        return finalList;
    }

    /**
     * Scans all columns for consecutive identical card values and returns the streaks.
     * Each entry is {@code {streak_length, card_value}}.
     *
     * @return list of streaks found across all columns
     */
    private ArrayList<int[]> scanColumns() {
        ArrayList<int[]> finalList = new ArrayList<>();
        for (int j=0;j<this.length.get();j++) {
            int cur = 0, prev = 0, streak = 0;
            for (int i=0;i<this.height.get();i++) {
                cur = this.matrix.get(i).get(j).getValue();
                if (cur == prev) {
                    streak++;
                }
                else {
                    if (streak > 0) {
                        finalList.add(new int[]{streak+1,prev});
                    }
                    streak = 0;
                }
                prev = cur;
            }
            if (streak > 0) {
                finalList.add(new int[]{streak+1,prev});
            }
        }
        return finalList;
    }

    /**
     * Returns {@code true} if the coordinate {@code (i, j)} is already present
     * in the {@code toIgnore} list (used by diagonal scanners to avoid double-counting).
     *
     * @param toIgnore list of already-recorded end coordinates
     * @param i        row index to check
     * @param j        column index to check
     * @return {@code true} if the coordinate was already found
     */
    private boolean wasAlreadyFound(ArrayList<int[]> toIgnore, int i, int j) {
        boolean wasAlrFound = false;
        for (int[] coords : toIgnore) {
            if (coords[0] == i && coords[1] == j) {
                wasAlrFound = true;
            }
        }
        return wasAlrFound;
    }

    /**
     * Scans top-left-to-bottom-right diagonals for consecutive identical card values.
     * Each entry is {@code {streak_length, card_value}}.
     *
     * @return list of streaks found on the main diagonals
     */
    private ArrayList<int[]> scanDiagonals() {
        ArrayList<int[]> finalList = new ArrayList<>();
        ArrayList<int[]> toIgnore = new ArrayList<>();

        int i0 = 0;
        while (i0<this.height.get()) {
            int j0 = 0;
            while (j0<this.length.get()) {
                int i = i0, j = j0;
                int cur = 0, prev = 0, streak = 0;
                while (i<this.height.get() && j<this.length.get()) {
                    cur = this.matrix.get(i).get(j).getValue();
                    if (cur == prev) {
                        streak++;
                    }
                    else {
                        if (streak > 0) {
                            if (!wasAlreadyFound(toIgnore, i, j)) {
                                finalList.add(new int[]{streak+1,prev});
                                toIgnore.add(new int[]{i,j});
                            }
                        }
                        streak = 0;
                    }
                    prev = cur;
                    i++; j++;
                }
                if (streak > 0) {
                    if (!wasAlreadyFound(toIgnore, i, j)) {
                        finalList.add(new int[]{streak+1,prev});
                        toIgnore.add(new int[]{i,j});
                    }
                }
                j0++;
            }
            i0++; 
        }
        return finalList;
    }

    /**
     * Scans bottom-left-to-top-right anti-diagonals for consecutive identical card values.
     * Each entry is {@code {streak_length, card_value}}.
     *
     * @return list of streaks found on the anti-diagonals
     */
    private ArrayList<int[]> scanAntiDiagonals() {
        ArrayList<int[]> finalList = new ArrayList<>();
        ArrayList<int[]> toIgnore = new ArrayList<>();

        int i0 = this.height.get()-1;
        while (i0>=0) {
            int j0 = 0;
            while (j0<this.length.get()) {
                int i = i0, j = j0;
                int cur = 0, prev = 0, streak = 0;
                while (i>=0 && j<this.length.get()) {
                    cur = this.matrix.get(i).get(j).getValue();
                    if (cur == prev) {
                        streak++;
                    }
                    else {
                        if (streak > 0) {
                            if (!wasAlreadyFound(toIgnore, i, j)) {
                                finalList.add(new int[]{streak+1,prev});
                                toIgnore.add(new int[]{i,j});
                            }
                        }
                        streak = 0;
                    }
                    prev = cur;
                    i--; j++;
                }
                if (streak > 0) {
                    if (!wasAlreadyFound(toIgnore, i, j)) {
                        finalList.add(new int[]{streak+1,prev});
                        toIgnore.add(new int[]{i,j});
                    }
                }
                j0++;
            }
            i0--; 
        }
        return finalList;
    }

    /**
     * Scans the entire deck for consecutive identical card values in all four
     * directions and returns all streaks as a flat list.
     * Each entry is {@code {streak_length, card_value}}.
     *
     * @return list of all streaks found in the deck
     */
    public ArrayList<int[]> scanCombos() {
        ArrayList<int[]> list = new ArrayList<>();

        list.addAll(this.scanRows());
        list.addAll(this.scanColumns());
        list.addAll(this.scanDiagonals());
        list.addAll(this.scanAntiDiagonals());

        return list;
    }

    /**
     * Builds a single combo entry with positional data.
     * <p>
     * The returned array has the following structure:
     * <ul>
     *   <li>{@code entry[0] = {streak_length, card_value}}</li>
     *   <li>{@code entry[1..n] = {row, col}} for each card in the streak</li>
     * </ul>
     * </p>
     *
     * @param length the streak length
     * @param value  the card value shared by all cards in the streak
     * @param coords list of {@code [row, col]} coordinates for each card
     * @return the assembled combo entry
     */
    private int[][] buildEntry(int length, int value, ArrayList<int[]> coords) {
        int[][] entry = new int[coords.size() + 1][];
        entry[0] = new int[]{length, value};
        for (int i = 0; i < coords.size(); i++) {
            entry[i + 1] = coords.get(i);
        }
        return entry;
    }

    /**
     * Scans all rows for consecutive identical card values, returning streaks
     * together with the coordinates of every contributing card.
     *
     * @return list of entries where {@code entry[0]={length, value}} and
     *         {@code entry[1..n]={row, col}}
     */
    private ArrayList<int[][]> scanRowsWithPos() {
        ArrayList<int[][]> finalList = new ArrayList<>();
        for (int i = 0; i < this.height.get(); i++) {
            int prev = 0, streak = 0;
            for (int j = 0; j < this.length.get(); j++) {
                int cur = this.matrix.get(i).get(j).getValue();
                if (cur == prev) {
                    streak++;
                } else {
                    if (streak > 0) {
                        ArrayList<int[]> coords = new ArrayList<>();
                        for (int k = j - streak - 1; k < j; k++) coords.add(new int[]{i, k});
                        finalList.add(buildEntry(streak + 1, prev, coords));
                    }
                    streak = 0;
                }
                prev = cur;
            }
            if (streak > 0) {
                int j = this.length.get();
                ArrayList<int[]> coords = new ArrayList<>();
                for (int k = j - streak - 1; k < j; k++) coords.add(new int[]{i, k});
                finalList.add(buildEntry(streak + 1, prev, coords));
            }
        }
        return finalList;
    }

    /**
     * Scans all columns for consecutive identical card values, returning streaks
     * together with the coordinates of every contributing card.
     *
     * @return list of entries where {@code entry[0]={length, value}} and
     *         {@code entry[1..n]={row, col}}
     */
    private ArrayList<int[][]> scanColumnsWithPos() {
        ArrayList<int[][]> finalList = new ArrayList<>();
        for (int j = 0; j < this.length.get(); j++) {
            int prev = 0, streak = 0;
            for (int i = 0; i < this.height.get(); i++) {
                int cur = this.matrix.get(i).get(j).getValue();
                if (cur == prev) {
                    streak++;
                } else {
                    if (streak > 0) {
                        ArrayList<int[]> coords = new ArrayList<>();
                        for (int k = i - streak - 1; k < i; k++) coords.add(new int[]{k, j});
                        finalList.add(buildEntry(streak + 1, prev, coords));
                    }
                    streak = 0;
                }
                prev = cur;
            }
            if (streak > 0) {
                int i = this.height.get();
                ArrayList<int[]> coords = new ArrayList<>();
                for (int k = i - streak - 1; k < i; k++) coords.add(new int[]{k, j});
                finalList.add(buildEntry(streak + 1, prev, coords));
            }
        }
        return finalList;
    }

    /**
     * Scans top-left-to-bottom-right diagonals for consecutive identical card values,
     * returning streaks together with the coordinates of every contributing card.
     *
     * @return list of entries where {@code entry[0]={length, value}} and
     *         {@code entry[1..n]={row, col}}
     */
    private ArrayList<int[][]> scanDiagonalsWithPos() {
        ArrayList<int[][]> finalList = new ArrayList<>();
        ArrayList<int[]> toIgnore = new ArrayList<>();

        for (int i0 = 0; i0 < this.height.get(); i0++) {
            for (int j0 = 0; j0 < this.length.get(); j0++) {
                int i = i0, j = j0;
                int prev = 0, streak = 0;
                int si = i0, sj = j0;

                while (i < this.height.get() && j < this.length.get()) {
                    int cur = this.matrix.get(i).get(j).getValue();
                    if (cur == prev) {
                        streak++;
                    } else {
                        if (streak > 0) {
                            int[] endCoord = {i - 1, j - 1};
                            if (!wasAlreadyFound(toIgnore, endCoord[0], endCoord[1])) {
                                ArrayList<int[]> coords = new ArrayList<>();
                                for (int k = 0; k <= streak; k++) coords.add(new int[]{si + k, sj + k});
                                finalList.add(buildEntry(streak + 1, prev, coords));
                                toIgnore.add(endCoord);
                            }
                        }
                        streak = 0;
                        si = i; sj = j;
                    }
                    prev = cur;
                    i++; j++;
                }
                if (streak > 0) {
                    int[] endCoord = {i - 1, j - 1};
                    if (!wasAlreadyFound(toIgnore, endCoord[0], endCoord[1])) {
                        ArrayList<int[]> coords = new ArrayList<>();
                        for (int k = 0; k <= streak; k++) coords.add(new int[]{si + k, sj + k});
                        finalList.add(buildEntry(streak + 1, prev, coords));
                        toIgnore.add(endCoord);
                    }
                }
            }
        }
        return finalList;
    }

    /**
     * Scans bottom-left-to-top-right anti-diagonals for consecutive identical card
     * values, returning streaks together with the coordinates of every contributing card.
     *
     * @return list of entries where {@code entry[0]={length, value}} and
     *         {@code entry[1..n]={row, col}}
     */
    private ArrayList<int[][]> scanAntiDiagonalsWithPos() {
        ArrayList<int[][]> finalList = new ArrayList<>();
        ArrayList<int[]> toIgnore = new ArrayList<>();

        for (int i0 = this.height.get() - 1; i0 >= 0; i0--) {
            for (int j0 = 0; j0 < this.length.get(); j0++) {
                int i = i0, j = j0;
                int prev = 0, streak = 0;
                int si = i0, sj = j0;

                while (i >= 0 && j < this.length.get()) {
                    int cur = this.matrix.get(i).get(j).getValue();
                    if (cur == prev) {
                        streak++;
                    } else {
                        if (streak > 0) {
                            int[] endCoord = {i + 1, j - 1};
                            if (!wasAlreadyFound(toIgnore, endCoord[0], endCoord[1])) {
                                ArrayList<int[]> coords = new ArrayList<>();
                                for (int k = 0; k <= streak; k++) coords.add(new int[]{si - k, sj + k});
                                finalList.add(buildEntry(streak + 1, prev, coords));
                                toIgnore.add(endCoord);
                            }
                        }
                        streak = 0;
                        si = i; sj = j;
                    }
                    prev = cur;
                    i--; j++;
                }
                if (streak > 0) {
                    int[] endCoord = {i + 1, j - 1};
                    if (!wasAlreadyFound(toIgnore, endCoord[0], endCoord[1])) {
                        ArrayList<int[]> coords = new ArrayList<>();
                        for (int k = 0; k <= streak; k++) coords.add(new int[]{si - k, sj + k});
                        finalList.add(buildEntry(streak + 1, prev, coords));
                        toIgnore.add(endCoord);
                    }
                }
            }
        }
        return finalList;
    }

    /**
     * Scans the entire deck for consecutive identical card values in all four
     * directions, returning each streak together with the grid coordinates of
     * every card that belongs to it.
     * <p>
     * Each entry is an {@code int[][]} where:
     * <ul>
     *   <li>{@code entry[0] = {streak_length, card_value}}</li>
     *   <li>{@code entry[1..n] = {row, col}} for each card in the streak</li>
     * </ul>
     * </p>
     *
     * @return list of all combo entries with their card coordinates
     */
    public ArrayList<int[][]> scanCombosWithPos() {
        ArrayList<int[][]> list = new ArrayList<>();
        list.addAll(this.scanRowsWithPos());
        list.addAll(this.scanColumnsWithPos());
        list.addAll(this.scanDiagonalsWithPos());
        list.addAll(this.scanAntiDiagonalsWithPos());
        return list;
    }

    /**
     * Removes all columns whose cards all share the same value (ignoring columns
     * that contain hidden cards).
     *
     * @return the flat list of cards removed
     */
    public ArrayList<Card> removeColumns() {
        return removeColumns(false);
    }

    /**
     * Removes all columns whose cards all share the same value.
     *
     * @param ignoreHidden {@code true} to also remove columns that contain hidden cards;
     *                     {@code false} to skip columns with at least one hidden card
     * @return the flat list of cards that were removed
     */
    public ArrayList<Card> removeColumns(boolean ignoreHidden) {
        ArrayList<Card> removed = new ArrayList<>();

        // iterate backwards to account for shifting indices after each removal
        for (int j = this.length.get() - 1; j >= 0; j--) {
            boolean allSame = true;
            boolean hasHidden = false;
            int firstValue = this.matrix.get(0).get(j).getValue();

            for (int i = 0; i < this.height.get(); i++) {
                Card c = this.matrix.get(i).get(j);

                if (c.isHidden()) {
                    hasHidden = true;
                }

                if (c.getValue() != firstValue) {
                    allSame = false;
                    break;
                }
            }
            if (allSame && (ignoreHidden || !hasHidden)) {
                removed.addAll(this.removeColumn(j));
            }
        }
        return removed;
    }

    /**
     * Main method used to test {@code Deck} operations and streak scanning.
     *
     * @param args command-line arguments (not used)
     */
    public static void main(String[] args) {

        Deck deck = new Deck(4, 3);

        ArrayList<Card> row1 = new ArrayList<>();
        row1.add(new Card(1, false));
        row1.add(new Card(2, false));
        row1.add(new Card(3, false));

        deck.addRow(row1);

        ArrayList<Card> row2 = new ArrayList<>();
        row2.add(new Card(4, false));
        row2.add(new Card(5, false));
        row2.add(new Card(6, false));

        deck.addRow(row2);

        ArrayList<Card> row3 = new ArrayList<>();
        row3.add(new Card(7, false));
        row3.add(new Card(8, false));
        row3.add(new Card(9, false));

        deck.addRow(row3);

        System.out.println("Initial deck:");
        deck.printAll();

        System.out.println("\nTrying to add extra row...");
        deck.addRow(row3);

        ArrayList<Card> col1 = new ArrayList<>();
        col1.add(new Card(10, false));
        col1.add(new Card(11, false));
        col1.add(new Card(12, false));

        deck.addColumn(col1);

        System.out.println("Adding a column...");
        deck.printAll();

        System.out.println("Removing column at index 1...");
        deck.removeColumn(1);
        deck.printAll();

        System.out.println("Removing row at index 0...");
        deck.removeRow(0);
        deck.printAll();

        ArrayList<Card> badColumn = new ArrayList<>();
        badColumn.add(new Card(99, false));

        System.out.println("Trying to add invalid column...");
        deck.addColumn(badColumn);

        ArrayList<Card> badRow = new ArrayList<>();
        badRow.add(new Card(100, false));

        System.out.println("Trying to add invalid row...");
        deck.addRow(badRow);

        deck = new Deck(5, 3);

        ArrayList<Card> r1 = new ArrayList<>();
        r1.add(new Card(1)); r1.add(new Card(1)); r1.add(new Card(2));
        r1.add(new Card(2)); r1.add(new Card(2));

        ArrayList<Card> r2 = new ArrayList<>();
        r2.add(new Card(1)); r2.add(new Card(3)); r2.add(new Card(3));
        r2.add(new Card(2)); r2.add(new Card(7));

        ArrayList<Card> r3 = new ArrayList<>();
        r3.add(new Card(1)); r3.add(new Card(7)); r3.add(new Card(2));
        r3.add(new Card(7)); r3.add(new Card(2));

        deck.addRow(r1);
        deck.addRow(r2);
        deck.addRow(r3);

        System.out.println("Combo testing deck:");
        deck.printAll();

        System.out.println("Row streaks:");
        for (int[] arr : deck.scanRows()) {
            System.out.println("Value: " + arr[1] + ", Times: " + arr[0]);
        }

        System.out.println("Column streaks:");
        for (int[] arr : deck.scanColumns()) {
            System.out.println("Value: " + arr[1] + ", Times: " + arr[0]);
        }

        System.out.println("Diagonal streaks:");
        for (int[] arr : deck.scanDiagonals()) {
            System.out.println("Value: " + arr[1] + ", Times: " + arr[0]);
        }

        System.out.println("Antidiagonal streaks:");
        for (int[] arr : deck.scanAntiDiagonals()) {
            System.out.println("Value: " + arr[1] + ", Times: " + arr[0]);
        }

        System.out.println("Removing full columns...");
        deck.removeColumns();
        deck.printAll();

        System.out.println("Clearing deck...");
        deck.clear();
        deck.printAll();
    }
}
