package com.neuilleprime.jokers;

import java.util.ArrayList;

import com.neuilleprime.game.*;

public class AddXDeckJoker extends Joker {

    private int value;

    public AddXDeckJoker(int x, boolean isConsu) {
        this.consumable = isConsu;
        this.value = x;
        this.name = "Add x deck joker";
        this.description = "Adds "+this.value+" to all cards in the deck";
        this.price = 2;
    }

    public static final JokerCategory CATEGORY = JokerCategory.DECK;
    @Override
    public JokerCategory getCategory() { return CATEGORY; }

    public static final JokerRarity RARITY = JokerRarity.UNCOMMON;
    @Override
    public JokerRarity getRarity() { return RARITY; }

    @Override
    public void apply(Deck deck) {
        for (int i=0;i<deck.getHeight();i++) {
            for (int j=0;j<deck.getLength();j++) {
                int newVal = deck.getCardAtCoords(i, j).getValue() + this.value;
                deck.getCardAtCoords(i, j).setValue(newVal);
            }
        }
    }

    @Override
    public ArrayList<Integer[]> applyWithPos(Deck deck) {
        ArrayList<Integer[]> coords = new ArrayList<>();
        for (int i=0;i<deck.getHeight();i++) {
            for (int j=0;j<deck.getLength();j++) {
                int newVal = deck.getCardAtCoords(i, j).getValue() + this.value;
                coords.add(new Integer[] {i, j, newVal});
            }
        }
        return coords;
    }
}