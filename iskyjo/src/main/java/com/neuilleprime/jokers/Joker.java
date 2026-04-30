package com.neuilleprime.jokers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import java.util.Random;

import com.neuilleprime.game.*;

/**
 * Abstract base class for all jokers in the game.
 * <p>
 * Jokers are special items players can buy from the shop to gain passive bonuses.
 * Each joker belongs to a {@link JokerCategory} that determines when it is
 * applied (on the deck, on individual cards, or on combo calculations), and has
 * a {@link JokerRarity} that controls how often it appears.
 * </p>
 * <p>
 * Concrete subclasses must implement {@link #getCategory()} and {@link #getRarity()},
 * and should override the appropriate {@code apply} / {@code applyWithPos} overload
 * for their category.
 * </p>
 */
public abstract class Joker {

    /** Human-readable description shown in the shop and joker tooltip. */
    protected String description;

    /** Display name of this joker. */
    protected String name;

    /** Whether this joker is a consumable (used once then discarded). */
    protected boolean consumable;

    /** Base sell/buy price of this joker. */
    protected int price;

    /**
     * Defines when in the scoring pipeline a joker's effect is applied.
     */
    public enum JokerCategory {
        /** Applied to the entire deck (e.g. modify all card values). */
        DECK,
        /** Applied to the list of detected combos. */
        COMBO,
        /** Applied to a single card. */
        CARD,
        /** Miscellaneous effects that don't fit other categories. */
        MISC
    }

    /**
     * Defines the rarity tier of a joker, each with a weighted probability of
     * appearing in the shop.
     */
    public enum JokerRarity {
        /** Very common; high weight. */
        COMMON(50),
        /** Somewhat common. */
        UNCOMMON(25),
        /** Hard to find. */
        RARE(15),
        /** Very hard to find. */
        EPIC(9),
        /** Extremely rare. */
        LEGENDARY(1);

        /** Weight used in weighted-random rarity selection. */
        public final int weight;

        /**
         * Constructs a rarity tier with the given selection weight.
         *
         * @param weight relative probability weight
         */
        JokerRarity(int weight) { this.weight = weight; }

        /**
         * Returns the display name of this rarity.
         *
         * @return human-readable rarity name
         */
        public String getName() {
            return switch (this) {
                case COMMON    -> "Common";
                case UNCOMMON  -> "Uncommon";
                case RARE      -> "Rare";
                case EPIC      -> "Epic";
                case LEGENDARY -> "Legendary";
            };
        }

        /**
         * Returns the CSS hex background colour associated with this rarity.
         *
         * @return background colour as a {@code #RRGGBB} string
         */
        public String getBackgroundColor() {
            return switch (this) {
                case COMMON    -> "#9e9e9e"; // grey
                case UNCOMMON  -> "#4caf50"; // green
                case RARE      -> "#2196f3"; // blue
                case EPIC      -> "#9c27b0"; // purple
                case LEGENDARY -> "#ff9800"; // orange/gold
            };
        }

        /**
         * Returns the CSS hex outline/border colour associated with this rarity.
         *
         * @return border colour as a {@code #RRGGBB} string
         */
        public String getOutlineColor() {
            return switch (this) {
                case COMMON    -> "#747474"; // grey
                case UNCOMMON  -> "#3b893d"; // green
                case RARE      -> "#1972bc"; // blue
                case EPIC      -> "#711d80"; // purple
                case LEGENDARY -> "#c97800"; // orange/gold
            };
        }
    }

    /**
     * Returns the category of this joker.
     *
     * @return the {@link JokerCategory} this joker belongs to
     */
    public abstract JokerCategory getCategory();

    /**
     * Returns {@code true} if the given joker belongs to the given category.
     *
     * @param joker    the joker to test
     * @param category the category to check against
     * @return {@code true} if the joker's category matches
     */
    public static boolean isOfCategory(Joker joker, JokerCategory category) {
        return joker.getCategory() == category;
    }

    /**
     * Returns the rarity of this joker.
     *
     * @return the {@link JokerRarity} of this joker
     */
    public abstract JokerRarity getRarity();

    /**
     * Returns {@code true} if the given joker has the given rarity.
     *
     * @param joker  the joker to test
     * @param rarity the rarity to check against
     * @return {@code true} if the joker's rarity matches
     */
    public static boolean isOfRarity(Joker joker, JokerRarity rarity) {
        return joker.getRarity() == rarity;
    }

    /**
     * Returns the asset texture name for this joker.
     * Defaults to the simple class name, which matches the file name convention
     * used by {@link com.neuilleprime.gui.utils.AssetLoader}.
     *
     * @return texture name string (case-insensitive key for the asset map)
     */
    public String getTextureName() {
        return this.getClass().getSimpleName();
    }

    /**
     * Returns whether this joker is a consumable.
     *
     * @return {@code true} if the joker is consumed after use
     */
    public boolean isConsumable() {
        return this.consumable;
    }

    /**
     * Returns the description of this joker.
     *
     * @return human-readable description
     */
    public String getDescription() {
        return this.description;
    }

    /**
     * Returns the display name of this joker.
     *
     * @return joker name
     */
    public String getName() {
        return this.name;
    }

    /**
     * Returns the base price of this joker (buy price = 2×, sell price = 1×).
     *
     * @return base price
     */
    public int getPrice() {
        return this.price;
    }

    /**
     * Applies this joker's effect to the given deck.
     * Override in {@link JokerCategory#DECK} jokers.
     *
     * @param deck the player's deck to modify
     */
    public void apply(Deck deck) {};

    /**
     * Applies this joker's effect to the given card.
     * Override in {@link JokerCategory#CARD} jokers.
     *
     * @param card the card to modify
     */
    public void apply(Card card) {};

    /**
     * Applies this joker's effect to the given pile.
     * Override in jokers that target piles.
     *
     * @param pile the pile to modify
     */
    public void apply(Pile pile) {};

    /**
     * Applies this joker's effect to the given list of combo entries.
     * Each entry is {@code {streak_length, card_value}}.
     * Override in {@link JokerCategory#COMBO} jokers.
     *
     * @param combos list of combo entries to modify in place
     */
    public void apply(ArrayList<int[]> combos) {};

    /**
     * Applies this joker's deck effect and returns the coordinates and new values
     * of every modified card, for use in score animations.
     * <p>
     * Each entry is {@code {row, col, newValue}}.
     * </p>
     *
     * @param deck the deck to modify
     * @return list of {@code Integer[]{row, col, newValue}} for each changed card
     */
    public ArrayList<Integer[]> applyWithPos(Deck deck) {return new ArrayList<>();};

    /**
     * Applies this joker's combo effect and returns the indices of all modified
     * combo entries, for use in score animations.
     *
     * @param combos list of combo entries (each is {@code int[][] {header, coords...}})
     * @return list of indices into {@code combos} that were modified
     */
    public ArrayList<Integer> applyWithPos(ArrayList<int[][]> combos) {return new ArrayList<>();};

    /**
     * Maps each registered joker class to its rarity.
     * <p>
     * <strong>Note:</strong> every new joker type must be added here, in the
     * shop, and in its own class.
     * </p>
     */
    // BIEN PENSER A RAJOUTER LE JOKER ICI, DANS LE SHOP, ET DANS LE JOKER LUI-MÊME !
    private static final Map<Class<? extends Joker>, JokerRarity> RARITY_MAP = Map.of(
        AddXCardJoker.class, JokerRarity.COMMON,
        AddXDeckJoker.class, JokerRarity.COMMON,
        ComboLeftJoker.class, JokerRarity.COMMON,
        ComboRightJoker.class, JokerRarity.COMMON,
        ComboLeftAllJoker.class, JokerRarity.UNCOMMON
    );

    /**
     * Randomly selects a joker class using weighted rarity sampling.
     * <p>
     * A rarity tier is first chosen proportional to its weight, then a random
     * joker of that rarity is picked. Falls back to {@link JokerRarity#COMMON}
     * if no jokers exist for the rolled rarity.
     * </p>
     * <p>
     * Note: this method was AI-generated.
     * </p>
     *
     * @return a randomly chosen {@link Joker} subclass
     */
    public static Class<? extends Joker> getRandomType() {
        Random rand = new Random();

        // Step 1 — pick a rarity by weight
        int totalWeight = Arrays.stream(JokerRarity.values())
            .mapToInt(r -> r.weight)
            .sum();
        int roll = rand.nextInt(totalWeight);
        int cumulative = 0;
        JokerRarity pickedRarity = JokerRarity.COMMON;
        for (JokerRarity rarity : JokerRarity.values()) {
            cumulative += rarity.weight;
            if (roll < cumulative) {
                pickedRarity = rarity;
                break;
            }
        }
        final JokerRarity finalRarity = pickedRarity;

        // Step 2 — filter jokers by that rarity and pick one
        var pool = RARITY_MAP.entrySet().stream()
            .filter(e -> e.getValue() == finalRarity)
            .map(e -> e.getKey())
            .toList();

        // Fallback to COMMON if no jokers exist for the picked rarity yet
        if (pool.isEmpty()) {
            pool = RARITY_MAP.entrySet().stream()
                .filter(e -> e.getValue() == JokerRarity.COMMON)
                .map(Map.Entry::getKey)
                .toList();
        }

        return pool.get(rand.nextInt(pool.size()));
    }
}
