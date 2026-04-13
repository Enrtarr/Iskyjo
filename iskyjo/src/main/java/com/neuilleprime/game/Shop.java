package com.neuilleprime.game;

import java.util.ArrayList;
import java.util.Random;

import com.neuilleprime.jokers.AddXCardJoker;
import com.neuilleprime.jokers.AddXDeckJoker;
import com.neuilleprime.jokers.ComboLeftAllJoker;
import com.neuilleprime.jokers.Joker;

public class Shop {

    public Shop() {

    }

    public ArrayList<Joker> getXJokers(int x) {
        ArrayList<Joker> list = new ArrayList<>();
        for (int i=0; i<x; i++) {
            Class<? extends Joker> jokerType = Joker.getRandomType();

            Random random = new Random();

            if (jokerType == AddXCardJoker.class) {
                list.add(new AddXCardJoker(random.nextInt(5), false));
            } else if (jokerType == AddXDeckJoker.class) {
                list.add(new AddXDeckJoker(3, false));
            } else if (jokerType == ComboLeftAllJoker.class) {
                ;
            }
        }

        return list;
    }
}
