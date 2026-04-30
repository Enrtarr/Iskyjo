package com.neuilleprime.game;
import java.util.ArrayList;
import java.util.Collections;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;

/**
 * Represents a pile of cards with utility methods to manage, display,
 * and manipulate the collection.
 * <p>
 * The pile size is backed by an observable {@link IntegerProperty} so that
 * UI components (e.g. {@link com.neuilleprime.gui.components.PileView}) can
 * react automatically whenever cards are added or removed.
 * </p>
 */
public class Pile {
    /** Default number of cards when size is not specified. */
    private static final int DEFAULT_SIZE = 150;

    /** The ordered list of cards, with the top card at the end. */
    private ArrayList<Card> pile = new ArrayList<Card>();

    /** Observable integer property tracking the current number of cards. */
    private IntegerProperty size;

    /**
     * Creates a pile with the default size ({@value #DEFAULT_SIZE} cards).
     */
    public Pile() {
        this(DEFAULT_SIZE);
    }

    /**
     * Creates a pile with a specific size and initializes the distribution of
     * card values to roughly match the game's intended proportions.
     *
     * @param size total number of cards to populate the pile with
     */
    public Pile(int size) {
        this.size = new SimpleIntegerProperty(0);

        int nbrMinusTwo = (int) Math.floor(size*(0.5/15));
        int nbrZero = (int) Math.ceil(size*(1.5/15));
        int nbrOther = (int) Math.round((size/15));

        for (int i=0; i<nbrMinusTwo; i++) {
            addCard(-2);
        }
        for (int i=0; i<nbrZero; i++) {
            addCard(0);
        }
        for (int i=0; i<nbrOther; i++) {
            addCard(-1);
            addCards(1, 12);
        }
    }

    /**
     * Returns the top card of the pile (the last card in the list) without
     * removing it.
     *
     * @return the top {@link Card}, or the last element if the pile is non-empty
     */
    public Card getTop() {
        return this.pile.get(this.size.get()-1);
    }

    /**
     * Adds multiple cards with values in the inclusive range {@code [k, l]}.
     *
     * @param k starting value (inclusive)
     * @param l ending value (inclusive)
     */
    public void addCards(int k, int l) {
        for (int i=k; i<l+1; i++) {
            this.pile.add(new Card(i));
            this.size.set(this.size.get()+1);
        }
    }

    /**
     * Adds all cards from the given list to the bottom of this pile.
     *
     * @param cards the list of cards to add
     */
    public void addCards(ArrayList<Card> cards) {
        for (Card c : cards) {
            this.addCard(c);
        }
    }

    /**
     * Adds a single new card with the given value.
     *
     * @param k value of the card to add
     */
    public void addCard(int k) {
        this.pile.add(new Card(k));
        this.size.set(this.size.get()+1);
    }

    /**
     * Adds an existing card instance to the pile.
     *
     * @param c the card to add
     */
    public void addCard(Card c) {
        this.pile.add(c);
        this.size.set(this.size.get()+1);
    }

    /**
     * Removes and returns the top card of the pile.
     *
     * @return the top {@link Card}, or {@code null} if the pile is empty
     */
    public Card pop() {
        return this.pop(0);
    }

    /**
     * Removes and returns the {@code n}-th card from the top of the pile
     * (0-based; {@code 0} means the top card).
     *
     * @param n offset from the top (must be &ge; 0)
     * @return the requested {@link Card}, or {@code null} if the pile is empty
     * @throws IllegalArgumentException if {@code n} is negative
     */
    public Card pop(int n) {
        if (n < 0) {throw (new IllegalArgumentException("n must be greater or equal than 0"));}
        if (this.size.get() <= 0) {
            return null;
        }
        Card card = this.pile.get(this.size.get()-n-1);
        this.pile.remove(this.size.get()-n-1);
        this.size.set(this.size.get()-1);
        return card;
    }

    /**
     * Returns the number of cards currently in the pile.
     *
     * @return current pile size
     */
    public int size() {
        return this.size.get();
    }

    /**
     * Returns the observable integer property for the pile's size.
     * Useful for binding UI components that should react to size changes.
     *
     * @return the {@link IntegerProperty} backing the pile size
     */
    public IntegerProperty sizeProperty() {
        return this.size;
    }

    /**
     * Prints all card values without additional metadata.
     */
    public void printAll() {
        printAll(false);
    }

    /**
     * Prints all card values. Optionally displays the total count and a header label.
     *
     * @param showNumberAndText if {@code true}, prints the size and a header before the values
     */
    public void printAll(boolean showNumberAndText) { 
        ArrayList<Integer> valuesList = new ArrayList<>();
        for (Card c : this.pile) {
            valuesList.add(c.getValue());
        }
        if (showNumberAndText) {
            System.out.println("Total size of the pile: "+valuesList.size());
            System.out.println("Content of the pile: ");
        }
        System.out.println(valuesList);
    }

    /**
     * Reveals all cards in the pile.
     */
    public void showAll() {
        showAll(this.size.get());
    }

    /**
     * Reveals the last {@code x} cards (top of the pile).
     *
     * @param x number of cards to reveal, counting from the top
     */
    public void showAll(int x) {
        for (int i=(this.size.get()-x);i<this.size.get();i++) {
            this.pile.get(i).show();
        }
    }

    /**
     * Hides all cards in the pile.
     */
    public void hideAll() {
        for (int i=0;i<this.size.get();i++) {
            this.pile.get(i).hide();
        }
    }

    /**
     * Randomly shuffles the pile using {@link Collections#shuffle}.
     */
    public void shuffle() {
        Collections.shuffle(this.pile);
    }

    /**
     * Removes all cards from the pile.
     */
    public void clear() {
        this.pile.clear();
    }

    /**
     * Returns the internal list of all cards in the pile.
     * The top card is at index {@code size - 1}.
     *
     * @return the list of all {@link Card}s
     */
    public ArrayList<Card> getAllCards() {
        return this.pile;
    }

    /**
     * Entry point for testing {@code Pile} behaviour.
     *
     * @param args command-line arguments (unused)
     */
    public static void main(String[] args) {
        int nbrOfCards = 150;
        System.out.println("Generating a draw pile with " + nbrOfCards + " cards...");
        Pile drawPile = new Pile(nbrOfCards);
        drawPile.printAll();

        System.out.println("Shuffling draw pile...");
        drawPile.shuffle();
        drawPile.printAll();

        System.out.println("Revealing all the 15 firsts cards...");
        drawPile.showAll(15);

        System.out.println("Hiding all the cards...");
        drawPile.hideAll();

        System.out.println("Emptying the draw pile...");
        drawPile.clear();
        drawPile.printAll(true);
    }
}
