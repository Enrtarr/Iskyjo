package com.neuilleprime.game;
import java.util.ArrayList;
import java.util.Collections;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;

/**
 * Represents a pile of cards with utility methods to manage, display,
 * and manipulate the collection.
 */
public class Pile {
    private static final int DEFAULT_SIZE = 150;

    private ArrayList<Card> pile = new ArrayList<Card>();
    private IntegerProperty size;

    /**
     * Creates a pile with the default size.
     */
    public Pile() {
        this(DEFAULT_SIZE);
    }

    /**
     * Creates a pile with a specific size and initializes
     * the distribution of card values.
     *
     * @param size total number of cards in the pile
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

        System.out.println(this.size.get());
    }

    public Card getTop() {
        return this.pile.get(this.size.get()-1);
    }

    /**
     * Adds multiple cards with values in the range [k, l].
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
     * Adds a single card with a given value.
     *
     * @param k value of the card
     */
    public void addCard(int k) {
        this.pile.add(new Card(k));
        this.size.set(this.size.get()+1);
    }

    /**
     * Adds a single card with given by reference.
     *
     * @param c reference of the card
     */
    public void addCard(Card c) {
        this.pile.add(c);
        this.size.set(this.size.get()+1);
    }

    /**
     * Returns the upper card from the pile, while removing it
     * 
     * @return The reference to the last card in the pile
     */
    public Card pop() {
        return this.pop(0);
    }

    /**
     * Returns the upper card from the pile, while removing it
     * 
     * @param n Returns the n-th card from the last
     * 
     * @return The reference to the last card in the pile
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
     * Returns the amount of cards currently present in the pile
     */
    public int size() {
        return this.size.get();
    }

    public IntegerProperty sizeProperty() {
        return this.size;
    }

    /**
     * Prints all cards without additional information.
     */
    public void printAll() {
        printAll(false);
    }

    /**
     * Prints all card values. Optionally displays metadata.
     *
     * @param showNumberAndText if true, prints size and label text
     */
    public void printAll(boolean showNumberAndText) { 
        ArrayList<Integer> valuesList = new ArrayList<>();
        for (Card c : this.pile) {
            // boolean wasHidden = c.isHidden();
            // if (wasHidden) {
            //     c.show();
            // }
            valuesList.add(c.getValue());
            // if (wasHidden) {
            //     c.hide();
            // }
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
     * Reveals the last x cards in the pile.
     *
     * @param x number of cards to reveal from the end
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
     * Randomly shuffles the pile.
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
     * Returns all of the cards in the pile.
     */
    public ArrayList<Card> getAllCards() {
        return this.pile;
    }

    /**
     * Entry point for testing the Pile behavior.
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