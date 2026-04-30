package com.neuilleprime.game;

import java.util.ArrayList;

import com.neuilleprime.jokers.*;

/**
 * Represents a player in the game.
 * <p>
 * A player owns a {@link Deck}, a collection of {@link Joker}s, consumables,
 * and upgrades, and tracks statistics such as points, money, interest rates,
 * and shop reroll state.
 * </p>
 */
public class Player {
    /** Default maximum number of jokers a player can hold. */
    private static final int DEFAULT_MAX_JOKERS = 5;

    /** Default maximum number of consumables a player can hold. */
    private static final int DEFAULT_MAX_CONSUMABLES = 2;

    /** Default number of jokers returned per shop reroll. */
    private static final int DEFAULT_SHOP_REROLL_AMOUNT = 3;

    /** Default base price for a shop reroll. */
    private static final int DEFAULT_SHOP_REROLL_BASE_PRICE = 2;

    /** Default player name when none is provided. */
    private static final String DEFAULT_NAME = "Unnamed";

    /** Default starting point total. */
    private static final int DEFAULT_POINTS = 0;

    /** Default starting money. */
    private static final int DEFAULT_MONEY = 0;

    /** Default bonus money multiplier. */
    private static final int DEFAULT_BONUS_MONEY_RATE = 1;

    /**
     * Default interest thresholds: {@code {amount_gained, per_slice_of_X, Y_times_max}}.
     */
    private static final int[] DEFAULT_INTERESTS = new int[] {1, 5, 5};

    /** The player's card deck. */
    private Deck deck;

    /** The player's currently held jokers. */
    private ArrayList<Joker> jokers;

    /** Maximum number of jokers the player can hold simultaneously. */
    private int maxJokers;

    /** The player's consumable jokers. */
    private ArrayList<Joker> consumables;

    /** Maximum number of consumables the player can hold simultaneously. */
    private int maxConsumables;

    /** The player's passive upgrade jokers. */
    private ArrayList<Joker> upgrades;

    /** Number of jokers shown per shop reroll. */
    private int shopRerollAmount;

    /** Base price for a shop reroll (resets each round). */
    private int shopRerollBasePrice;

    /** Current price for the next shop reroll (increases after each reroll). */
    private int shopRerollPrice;

    /** Display name of the player. */
    private String name;

    /** Total accumulated points across all rounds. */
    private int points;

    /** Current money balance. */
    private int money;

    /** Multiplier applied to bonus money earned by exceeding the round quota. */
    private int bonusMoneyRate;

    /**
     * Interest thresholds in the form {@code {amount_gained, per_slice_of_X, Y_times_max}}.
     */
    private int[] interests;

    /**
     * Constructs a player with all default values.
     */
    public Player() {
        this(
            new Deck(), null, DEFAULT_MAX_JOKERS, null, DEFAULT_MAX_CONSUMABLES, null,
            DEFAULT_SHOP_REROLL_AMOUNT, DEFAULT_SHOP_REROLL_BASE_PRICE,
            DEFAULT_NAME, DEFAULT_POINTS, DEFAULT_MONEY, DEFAULT_BONUS_MONEY_RATE, DEFAULT_INTERESTS
        );
    }

    /**
     * Constructs a player with the given name and all other values at defaults.
     *
     * @param name the player's display name
     */
    public Player(String name) {
        this(
            new Deck(), null, DEFAULT_MAX_JOKERS, null, DEFAULT_MAX_CONSUMABLES, null,
            DEFAULT_SHOP_REROLL_AMOUNT, DEFAULT_SHOP_REROLL_BASE_PRICE,
            name, DEFAULT_POINTS, DEFAULT_MONEY, DEFAULT_BONUS_MONEY_RATE, DEFAULT_INTERESTS
        );
    }

    /**
     * Full constructor for {@code Player}.
     *
     * @param deck                 the player's deck ({@code null} creates a new empty deck)
     * @param jokers               the player's jokers ({@code null} creates an empty list)
     * @param maxJokers            maximum number of jokers allowed
     * @param consumables          the player's consumables ({@code null} creates an empty list)
     * @param maxConsumables       maximum number of consumables allowed
     * @param upgrades             the player's upgrades ({@code null} creates an empty list)
     * @param shopRerollAmount     number of jokers shown per reroll
     * @param shopRerollBasePrice  base price per reroll
     * @param name                 display name ({@code null} falls back to {@value #DEFAULT_NAME})
     * @param points               starting point total
     * @param money                starting money balance
     * @param bonusMoneyRate       bonus money multiplier
     * @param interests            interest thresholds {@code {gained, per_X, max_Y}}
     */
    public Player(Deck deck, ArrayList<Joker> jokers, int maxJokers,
                ArrayList<Joker> consumables, int maxConsumables,
                ArrayList<Joker> upgrades, int shopRerollAmount,
                int shopRerollBasePrice, String name, int points, 
                int money, int bonusMoneyRate, int[] interests) {
        this.deck = (deck != null) ? deck : new Deck();
        this.jokers = (jokers != null) ? jokers : new ArrayList<>();
        this.maxJokers = maxJokers;
        this.consumables = (consumables != null) ? consumables : new ArrayList<>();
        this.maxConsumables = maxConsumables;
        this.upgrades = (upgrades != null) ? upgrades : new ArrayList<>();
        this.shopRerollAmount = shopRerollAmount;
        this.shopRerollBasePrice = shopRerollBasePrice;
        this.shopRerollPrice = shopRerollBasePrice;
        this.name = (name != null) ? name : DEFAULT_NAME;
        this.points = points;
        this.money = money;
        this.bonusMoneyRate = bonusMoneyRate;
        this.interests = interests;
    }

    /**
     * Resets all player state to default values, preserving the player's name.
     */
    public void resetPlayer() {
        this.deck = new Deck();
        this.jokers = new ArrayList<>();
        this.maxJokers = DEFAULT_MAX_JOKERS;
        this.consumables = new ArrayList<>();
        this.maxConsumables = DEFAULT_MAX_CONSUMABLES;
        this.upgrades = new ArrayList<>();
        this.shopRerollAmount = DEFAULT_SHOP_REROLL_AMOUNT;
        this.shopRerollBasePrice = DEFAULT_SHOP_REROLL_BASE_PRICE;
        this.shopRerollPrice = shopRerollBasePrice;
        this.name = (name != null) ? name : DEFAULT_NAME;
        this.points = DEFAULT_POINTS;
        this.money = DEFAULT_MONEY;
        this.bonusMoneyRate = DEFAULT_BONUS_MONEY_RATE;
        this.interests = DEFAULT_INTERESTS;
    }

    /**
     * Returns the player's deck.
     *
     * @return the {@link Deck}
     */
    public Deck getDeck() { return deck; }

    /**
     * Sets the player's deck.
     *
     * @param deck the new deck ({@code null} creates a new empty deck)
     */
    public void setDeck(Deck deck) { this.deck = (deck != null) ? deck : new Deck(); }

    /**
     * Returns the player's list of jokers.
     *
     * @return the joker list
     */
    public ArrayList<Joker> getJokers() { return this.jokers; }

    /**
     * Sets the player's joker list.
     *
     * @param jokers the new list ({@code null} creates an empty list)
     */
    public void setJokers(ArrayList<Joker> jokers) { this.jokers = (jokers != null) ? jokers : new ArrayList<>(); }

    /**
     * Returns the maximum number of jokers the player can hold.
     *
     * @return max joker count
     */
    public int getMaxJokers() { return maxJokers; }

    /**
     * Sets the maximum number of jokers allowed.
     *
     * @param maxJokers new maximum
     */
    public void setMaxJokers(int maxJokers) { this.maxJokers = maxJokers; }

    /**
     * Returns the player's list of consumable jokers.
     *
     * @return the consumable list
     */
    public ArrayList<Joker> getConsumables() { return this.consumables; }

    /**
     * Sets the player's consumable list.
     *
     * @param consumables the new list ({@code null} creates an empty list)
     */
    public void setConsumables(ArrayList<Joker> consumables) { this.consumables = (consumables != null) ? consumables : new ArrayList<>(); }

    /**
     * Returns the maximum number of consumables the player can hold.
     *
     * @return max consumable count
     */
    public int getMaxConsumables() { return this.maxConsumables; }

    /**
     * Sets the maximum number of consumables allowed.
     *
     * @param maxConsumables new maximum
     */
    public void setMaxConsumables(int maxConsumables) { this.maxConsumables = maxConsumables; }

    /**
     * Returns the player's list of upgrade jokers.
     *
     * @return the upgrade list
     */
    public ArrayList<Joker> getUpgrades() { return this.upgrades; }

    /**
     * Sets the player's upgrade list.
     *
     * @param upgrades the new list ({@code null} creates an empty list)
     */
    public void setUpgrades(ArrayList<Joker> upgrades) { this.upgrades = (upgrades != null) ? upgrades : new ArrayList<>(); }

    /**
     * Returns the number of jokers shown per shop reroll.
     *
     * @return shop reroll amount
     */
    public int getShopRerollAmount() { return this.shopRerollAmount; }

    /**
     * Sets the number of jokers shown per shop reroll.
     *
     * @param amount the new reroll amount
     */
    public void setShopRerollAmount(int amount) { this.shopRerollAmount = amount; }

    /**
     * Returns the base price for a shop reroll.
     *
     * @return shop reroll base price
     */
    public int getShopRerollBasePrice() { return this.shopRerollBasePrice; }

    /**
     * Sets the base price for a shop reroll.
     *
     * @param price the new base price
     */
    public void setShopRerollBasePrice(int price) { this.shopRerollBasePrice = price; }

    /**
     * Returns the current price of the next shop reroll.
     * This increases after each reroll within a round.
     *
     * @return current reroll price
     */
    public int getShopRerollPrice() { return this.shopRerollPrice; }

    /**
     * Sets the current reroll price.
     *
     * @param price the new reroll price
     */
    public void setShopRerollPrice(int price) { this.shopRerollPrice = price; }

    /**
     * Returns the player's display name.
     *
     * @return the name
     */
    public String getName() { return this.name; }

    /**
     * Sets the player's display name.
     *
     * @param name the new name ({@code null} falls back to {@value #DEFAULT_NAME})
     */
    public void setName(String name) { this.name = (name != null) ? name : DEFAULT_NAME; }

    /**
     * Returns the player's total accumulated points.
     *
     * @return point total
     */
    public int getPoints() { return this.points; }

    /**
     * Sets the player's total accumulated points.
     *
     * @param points the new point total
     */
    public void setPoints(int points) { this.points = points; }

    /**
     * Returns the player's current money balance.
     *
     * @return money balance
     */
    public int getMoney() { return this.money; }

    /**
     * Sets the player's money balance.
     *
     * @param money the new balance
     */
    public void setMoney(int money) { this.money = money; }

    /**
     * Returns the bonus money rate multiplier applied when the player exceeds
     * their share of the round quota.
     *
     * @return bonus money rate
     */
    public int getBonusMoneyRate() { return this.bonusMoneyRate; }

    /**
     * Sets the bonus money rate multiplier.
     *
     * @param bonusMoney the new multiplier
     */
    public void setBonusMoneyRate(int bonusMoney) { this.bonusMoneyRate = bonusMoney; }

    /**
     * Returns the interest thresholds as {@code {amount_gained, per_slice_of_X, Y_times_max}}.
     *
     * @return the interests array
     */
    public int[] getInterests() { return this.interests; }

    /**
     * Sets the interest thresholds.
     *
     * @param interests the new thresholds array
     */
    public void setInterests(int[] interests) { this.interests = interests; }
}
