package com.neuilleprime.jokers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import java.util.Random;

import com.neuilleprime.game.*;

public abstract class Joker {
    protected String description;
    protected String name;
    protected boolean consumable;
    protected int price;

    public enum JokerCategory {
        DECK,
        COMBO,
        CARD,
        MISC
    }

    public enum JokerRarity {
        COMMON(50),
        UNCOMMON(25),
        RARE(15),
        EPIC(9),
        LEGENDARY(1);

        public final int weight;
        JokerRarity(int weight) { this.weight = weight; }

        public String getName() {
            return switch (this) {
                case COMMON    -> "Common";
                case UNCOMMON  -> "Uncommon";
                case RARE      -> "Rare";
                case EPIC      -> "Epic";
                case LEGENDARY -> "Legendary";
            };
        }

        public String getBackgroundColor() {
            return switch (this) {
                case COMMON    -> "#9e9e9e"; // grey
                case UNCOMMON  -> "#4caf50"; // green
                case RARE      -> "#2196f3"; // blue
                case EPIC      -> "#9c27b0"; // purple
                case LEGENDARY -> "#ff9800"; // orange/gold
            };
        }

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

    public abstract JokerCategory getCategory();
    public static boolean isOfCategory(Joker joker, JokerCategory category) {
        return joker.getCategory() == category;
    }

    public abstract JokerRarity getRarity();
    public static boolean isOfRarity(Joker joker, JokerRarity rarity) {
        return joker.getRarity() == rarity;
    }

    public String getTextureName() {
        return this.getClass().getSimpleName();
    }

    public boolean isConsumable() {
        return this.consumable;
    }

    public String getDescription() {
        return this.description;
    }

    public String getName() {
        return this.name;
    }

    public int getPrice() {
        return this.price;
    }

    public void apply(Deck deck) {};
    public void apply(Card card) {};
    public void apply(Pile pile) {};
    public void apply(ArrayList<int[]> combos) {};

    // BIEN PENSER A RAJOUTER LE JOKER ICI, DANS LE SHOP, ET DANS LE JOKER LUI-MÊME !
    private static final Map<Class<? extends Joker>, JokerRarity> RARITY_MAP = Map.of(
        AddXCardJoker.class, JokerRarity.COMMON,
        AddXDeckJoker.class, JokerRarity.COMMON,
        ComboLeftJoker.class, JokerRarity.COMMON,
        ComboRightJoker.class, JokerRarity.COMMON,
        ComboLeftAllJoker.class, JokerRarity.UNCOMMON
    );

    // Ok this method was actually AI-generated, because I had no clue how to do this
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
