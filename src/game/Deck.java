
package game;

import java.util.ArrayList;

/**
 * Represents a 2D deck of cards with rows and columns.
 * Supports adding/removing rows and columns, scanning for streaks in rows, columns, and diagonals.
 */
public class Deck {
    private static final int DEFAULT_MAX_LENGTH = 4;
    private static final int DEFAULT_MAX_HEIGHT = 3;

    private int length;
    private int height;
    private int maxLength;
    private int maxHeight;

    /** Matrix storing the cards row by row */
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
     * @param l Maximum number of columns
     * @param h Maximum number of rows
     */
    public Deck(int l, int h) {
        this.length = 0;
        this.height = 0;
        this.maxLength = l;
        this.maxHeight = h;
    }

    /**
     * Removes the row at the specified index.
     *
     * @param j Index of the row to remove
     */
    public void removeRow(int j) {
        if (this.height != 0) {
            this.matrix.remove(j);
            this.height = this.height - 1;
        }
        else {
            System.out.println("Can not remove row, deck is already empty");
        }
    }

    /**
     * Adds a row of cards to the deck.
     * Initializes deck length if adding the first row.
     *
     * @param row Row of cards to add
     */
    public void addRow(ArrayList<Card> row) {
        if (this.height + 1 <= this.maxHeight) {
            if (this.height == 0) {
                this.length = row.size();
            }

            if (row.size() == this.length) {
                this.matrix.add(row);
                this.height = this.height + 1;
            }
            else {
                System.out.println("Wrong length (given: "+row.size()+", expected: "+this.length+")");
            }
        }
        else {
            System.out.println("Max height reached ("+this.maxHeight+")");
        }
    }

    /**
     * Removes the column at the specified index.
     *
     * @param i Index of the column to remove
     */
    public void removeColumn(int i) {
        if (this.length != 0) {
            for (int j=0; j<this.matrix.size(); j++) {
                this.matrix.get(j).remove(i);
            }
            this.length = this.length - 1;
        }
        else {
            System.out.println("Deck is already empty");
        }
    }

    /**
     * Adds a column of cards to the deck.
     *
     * @param col Column of cards to add
     */
    public void addColumn(ArrayList<Card> col) {
        if (this.length + 1 <= this.maxLength) {
            if (col.size() == this.height) {
                for (int j = 0; j < this.height; j++) {
                    this.matrix.get(j).add(col.get(j));
                }
                this.length = this.length + 1;
            }
            else {
                System.out.println("Wrong height (given: "+col.size()+", expected: "+this.height+")");
            }
        }
        else {
            System.out.println("Max length reached ("+this.maxLength+")");
        }
    }

    /**
     * Returns the current number of columns.
     *
     * @return Current length of the deck
     */
    public int getLength() {
        return this.length;
    }

    /**
     * Returns the current number of rows.
     *
     * @return Current height of the deck
     */
    public int getHeight() {
        return this.height;
    }

        /**
     * Returns the maximum number of columns.
     *
     * @return Maximum length of the deck
     */
    public int getMaxLength() {
        return this.maxLength;
    }

    /**
     * Returns the maximum number of rows.
     *
     * @return Maximum height of the deck
     */
    public int getMaxHeight() {
        return this.maxHeight;
    }

    public boolean hasCardAtCoords(int[] coords) {
        if (coords[0] <= this.height && coords[1] <= this.length) {
            return true;
        }
        else {
            return false;
        }
    }

    public boolean hasCardAtCoords(int h, int l) {
        if (h <= this.height && l <= this.length) {
            return true;
        }
        else {
            return false;
        }
    }

    public Card getCardAtCoords(int[] coords) {
        if (!this.hasCardAtCoords(coords)) {
            throw (new IllegalArgumentException("No card at these coordinates"));
        }
        return this.matrix.get(coords[0]).get(coords[1]);
    }

    public Card getCardAtCoords(int h, int l) {
        if (!this.hasCardAtCoords(h, l)) {
            throw (new IllegalArgumentException("No card at these coordinates"));
        }
        return this.matrix.get(h).get(l);
    }

    public void setCardAtCoords(int[] coords, Card card) {
        if (!this.hasCardAtCoords(coords)) {
            throw (new IllegalArgumentException("No card at these coordinates"));
        }
        this.matrix.get(coords[0]).set(coords[1], card);
    }

    public void setCardAtCoords(int h, int l, Card card) {
        if (!this.hasCardAtCoords(h, l)) {
            throw (new IllegalArgumentException("No card at these coordinates"));
        }
        this.matrix.get(h).set(l, card);
    }

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
     * Returns all cards in the deck as a flat list.
     *
     * @return List of all cards
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

    public void clear() {
        for (ArrayList<Card> row : this.matrix) {
            row.clear();
        }
        this.matrix.clear();
        this.length = 0;
        this.height = 0;
    }

    /**
     * Prints the deck to the console in a 2D format.
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

    public Card replaceCard(int[] cardCoords, Card newCard) {
        if(!this.hasCardAtCoords(cardCoords)) {
            throw (new IllegalArgumentException("No card at these coordinates"));
        }

        Card oldCard = this.getCardAtCoords(cardCoords);
        this.setCardAtCoords(cardCoords, newCard);

        return oldCard;
    }

    /**
     * Scans all rows for consecutive identical card values and returns streaks.
     *
     * @return List of streaks, each as {streak length, card value}
     */
    private ArrayList<int[]> scanRows() {
        ArrayList<int[]> finalList = new ArrayList<>();
        for (int i=0;i<this.height;i++) {
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
     * Scans all columns for consecutive identical card values and returns streaks.
     *
     * @return List of streaks, each as {streak length, card value}
     */
    private ArrayList<int[]> scanColumns() {
        ArrayList<int[]> finalList = new ArrayList<>();
        for (int j=0;j<this.length;j++) {
            int cur = 0, prev = 0, streak = 0;
            for (int i=0;i<this.height;i++) {
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
     * Checks if a specific coordinate has already been counted in streaks.
     *
     * @param toIgnore List of coordinates to ignore
     * @param i Row index
     * @param j Column index
     * @return true if coordinates were already counted, false otherwise
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
     * Scans diagonals (top-left to bottom-right) for consecutive identical card values.
     *
     * @return List of streaks, each as {streak length, card value}
     */
    private ArrayList<int[]> scanDiagonals() {
        ArrayList<int[]> finalList = new ArrayList<>();
        ArrayList<int[]> toIgnore = new ArrayList<>();

        int i0 = 0;
        while (i0<this.height) {
            int j0 = 0;
            while (j0<this.length) {
                int i = i0, j = j0;
                int cur = 0, prev = 0, streak = 0;
                while (i<this.height && j<this.length) {
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
     * Scans anti-diagonals (bottom-left to top-right) for consecutive identical card values.
     *
     * @return List of streaks, each as {streak length, card value}
     */
    private ArrayList<int[]> scanAntiDiagonals() {
        ArrayList<int[]> finalList = new ArrayList<>();
        ArrayList<int[]> toIgnore = new ArrayList<>();

        int i0 = this.height-1;
        while (i0>=0) {
            int j0 = 0;
            while (j0<this.length) {
                int i = i0, j = j0;
                int cur = 0, prev = 0, streak = 0;
                while (i>=0 && j<this.length) {
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
     * Scans the whole deck for consecutive identical card values.
     *
     * @return List of all streaks, each as {streak length, card value}
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
     * Removes the full columns made of the same values (by default ignores columns with hidden cards)
     */
    public void removeColumns() {
        removeColumns(false);
    }

    /**
     * Removes the full columns made of the same values
     * 
     * @param ignoreHidden Whether or not to remove columns with hidden cards (true=remove)
     */
    public void removeColumns(boolean ignoreHidden) {
        // ArrayList<int[]> finalList = new ArrayList<>();
        for (int j=0;j<this.length;j++) {
            int cur = 0, prev = 0, streak = 0;
            for (int i=0;i<this.height;i++) {
                cur = this.matrix.get(i).get(j).getValue();
                if (cur == prev) {
                    streak++;
                }
                else {
                    if (streak+1 == this.height) {
                        // finalList.add(new int[]{streak+1,prev});
                        this.removeColumn(j);
                    }
                    streak = 0;
                }
                if (this.matrix.get(i).get(j).isHidden() && !ignoreHidden) {
                    streak = 0;
                }
                prev = cur;
            }
            if (streak+1 == this.height) {
                // finalList.add(new int[]{streak+1,prev});
                this.removeColumn(j);
            }
        }
    }

    // public void clearFullLines() {
        
    // }

    /**
     * Main method used to test Deck operations and streak scanning.
     *
     * @param args Command-line arguments (not used)
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

        ArrayList<Card> col2 = new ArrayList<>();
        col2.add(new Card(13, false));
        col2.add(new Card(14, false));
        col2.add(new Card(15, false));

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
        r1.add(new Card(1));
        r1.add(new Card(1));
        r1.add(new Card(2));
        r1.add(new Card(2));
        r1.add(new Card(2));

        ArrayList<Card> r2 = new ArrayList<>();
        r2.add(new Card(1));
        r2.add(new Card(3));
        r2.add(new Card(3));
        r2.add(new Card(2));
        r2.add(new Card(7));

        ArrayList<Card> r3 = new ArrayList<>();
        r3.add(new Card(1));
        r3.add(new Card(7));
        r3.add(new Card(2));
        r3.add(new Card(7));
        r3.add(new Card(2));

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