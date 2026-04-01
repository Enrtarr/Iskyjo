package jokers;

import java.util.ArrayList;

public class ComboRightJoker extends Joker {

    private int valueToAdd;
    private int appliesTo;

    public ComboRightJoker(int valueToAdd, int appliesTo) {
        this.valueToAdd = valueToAdd;
        this.appliesTo = appliesTo;
        this.description = "Adds"+valueToAdd+" to the right (card value) part of combos";
    }

    public static final JokerCategory CATEGORY = JokerCategory.DECK;
    @Override
    public JokerCategory getCategory() { return CATEGORY; }

    @Override
    public void apply(ArrayList<int[]> combos) {
        for (int[] c : combos) {
            if (c[1] == this.appliesTo) {
                c[1] = c[1] + this.valueToAdd;
            }
        }
    }
}