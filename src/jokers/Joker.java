package jokers;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import game.*;

public abstract class Joker {
    protected String description;
    protected String name;
    protected boolean consumable;

    public enum JokerCategory {
        DECK,
        COMBO,
        CARD,
        MISC
    }

    public enum JokerRarity {
        COMMON,
        UNCOMMON,
        RARE,
        EPIC,
        LEGENDARY
    }

    public abstract JokerCategory getCategory();
    public static boolean isOfCategory(Joker joker, JokerCategory category) {
        return joker.getCategory() == category;
    }

    public abstract JokerRarity getRarity();
    public static boolean isOfRarity(Joker joker, JokerRarity rarity) {
        return joker.getRarity() == rarity;
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

    public void apply(Deck deck) {};
    public void apply(Card card) {};
    public void apply(Pile pile) {};
    public void apply(ArrayList<int[]> combos) {};

    public static Class<? extends Joker> getRandomType() {
        // BIEN PENSER À LE RAJOUTER DANS LA LISTE QUAND ON IMPLÉMENTE UN NOUVEAU JOKER !
        List<Class<? extends Joker>> types = List.of(
            AddXCardJoker.class, 
            ComboLeftJoker.class,
            ComboRightJoker.class,
            ComboLeftAllJoker.class
        );
        return types.get(new Random().nextInt(types.size()));
    }
}