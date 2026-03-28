package game;

import java.util.ArrayList;

import jokers.*;

public class Player {
    private static final int DEFAULT_MAX_JOKERS = 5;
    private static final int DEFAULT_MAX_CONSUMABLES = 2;
    private static final String DEFAULT_NAME = "Unnamed";
    private static final int DEFAULT_POINTS = 0;
    private static final int DEFAULT_MONEY = 0;

    private Deck deck;
    private ArrayList<Joker> jokers;
    private int maxJokers;
    private ArrayList<Joker> consumables;
    private int maxConsumables;
    private String name;
    private int points;
    private int money;

    public Player() {
        this(
            new Deck(), 
            null, 
            DEFAULT_MAX_JOKERS, 
            null, 
            DEFAULT_MAX_CONSUMABLES, 
            DEFAULT_NAME, 
            DEFAULT_POINTS, 
            DEFAULT_MONEY
        );
    }

    public Player(String name) {
        this(
            new Deck(), 
            null, 
            DEFAULT_MAX_JOKERS, 
            null, 
            DEFAULT_MAX_CONSUMABLES, 
            name, 
            DEFAULT_POINTS, 
            DEFAULT_MONEY
        );
    }

    public Player(Deck deck, ArrayList<Joker> jokers, int maxJokers,
                ArrayList<Joker> consumables, int maxConsumables,
                String name, int points, int money) {
        this.deck = (deck != null) ? deck : new Deck();
        this.jokers = (jokers != null) ? jokers : new ArrayList<>();
        this.maxJokers = maxJokers;
        this.consumables = (consumables != null) ? consumables : new ArrayList<>();
        this.maxConsumables = maxConsumables;
        this.name = (name != null) ? name : DEFAULT_NAME;
        this.points = points;
        this.money = money;
    }

    /**
     * Returns the player's deck.
     * @return the deck
     */
    public Deck getDeck() {
        return deck;
    }

    /**
     * Sets the player's deck.
     * @param deck the deck to set
     */
    public void setDeck(Deck deck) {
        this.deck = (deck != null) ? deck : new Deck();
    }

    /**
     * Returns the list of jokers.
     * @return the jokers
     */
    public ArrayList<Joker> getJokers() {
        return jokers;
    }

    /**
     * Sets the list of jokers.
     * @param jokers the jokers to set
     */
    public void setJokers(ArrayList<Joker> jokers) {
        this.jokers = (jokers != null) ? jokers : new ArrayList<>();
    }

    /**
     * Returns the maximum number of jokers allowed.
     * @return maxJokers
     */
    public int getMaxJokers() {
        return maxJokers;
    }

    /**
     * Sets the maximum number of jokers allowed.
     * @param maxJokers the maximum to set
     */
    public void setMaxJokers(int maxJokers) {
        this.maxJokers = maxJokers;
    }

    /**
     * Returns the list of consumables.
     * @return the consumables
     */
    public ArrayList<Joker> getConsumables() {
        return consumables;
    }

    /**
     * Sets the list of consumables.
     * @param consumables the consumables to set
     */
    public void setConsumables(ArrayList<Joker> consumables) {
        this.consumables = (consumables != null) ? consumables : new ArrayList<>();
    }

    /**
     * Returns the maximum number of consumables allowed.
     * @return maxConsumables
     */
    public int getMaxConsumables() {
        return maxConsumables;
    }

    /**
     * Sets the maximum number of consumables allowed.
     * @param maxConsumables the maximum to set
     */
    public void setMaxConsumables(int maxConsumables) {
        this.maxConsumables = maxConsumables;
    }

    /**
     * Returns the player's name.
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the player's name.
     * @param name the name to set
     */
    public void setName(String name) {
        this.name = (name != null) ? name : DEFAULT_NAME;
    }

    /**
     * Returns the player's points.
     * @return the points
     */
    public int getPoints() {
        return points;
    }

    /**
     * Sets the player's points.
     * @param points the points to set
     */
    public void setPoints(int points) {
        this.points = points;
    }

    /**
     * Returns the player's money.
     * @return the money
     */
    public int getMoney() {
        return money;
    }

    /**
     * Sets the player's money.
     * @param money the money to set
     */
    public void setMoney(int money) {
        this.money = money;
    }
}