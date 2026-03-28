package jokers;

import game.*;

public class AddXJoker extends Joker {

    private int value;

    public AddXJoker(int x, boolean isConsu) {
        this.consumable = isConsu;
        this.value = x;
        this.description = "Adds "+this.value+" to all cards";
    }

    @Override
    public void apply(Deck deck) {
        for (Card c : deck.getAllCards()) {
            c.setValue(c.getValue()+this.value);
        }
    }

    @Override
    public void apply(Card card) {
        card.setValue(card.getValue()+this.value);
    }
}