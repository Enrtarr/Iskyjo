package jokers;

import game.Deck;

public class MultiplierJoker extends Joker {

    private int multiplier;

    public MultiplierJoker(int multiplier) {
        this.multiplier = multiplier;
        this.description = "Multiplies combos by " + multiplier;
    }

    @Override
    public void apply(Deck deck) {
        System.out.println("Combo multiplier x" + multiplier);
        // integrate with scoring system
    }
}