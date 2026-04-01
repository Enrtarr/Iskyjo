package jokers;

import game.*;

public class AddXCardJoker extends Joker {

    private int value;

    public AddXCardJoker(int x, boolean isConsu) {
        this.consumable = isConsu;
        this.value = x;
        this.description = "Adds "+this.value+" to all cards";
    }

    public static final JokerCategory CATEGORY = JokerCategory.CARD;
    @Override
    public JokerCategory getCategory() { return CATEGORY; }

    @Override
    public void apply(Card card) {
        card.setValue(card.getValue()+this.value);
    }
}