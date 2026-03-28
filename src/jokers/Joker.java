package jokers;

import game.*;

public abstract class Joker {
    protected String description;
    protected boolean consumable;

    public boolean isConsumable() {
        return this.consumable;
    }

    public String getDescription() {
        return description;
    }

    public void apply(Deck deck) {};
    public void apply(Card card) {};
    public void apply(Pile pile) {};
}