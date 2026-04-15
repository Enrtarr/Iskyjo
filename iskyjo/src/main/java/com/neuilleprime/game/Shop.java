package com.neuilleprime.game;

import java.util.ArrayList;
import java.util.Random;

import com.neuilleprime.jokers.AddXCardJoker;
import com.neuilleprime.jokers.AddXDeckJoker;
import com.neuilleprime.jokers.ComboLeftAllJoker;
import com.neuilleprime.jokers.ComboLeftJoker;
import com.neuilleprime.jokers.ComboRightJoker;
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
                list.add(new AddXDeckJoker(random.nextInt(3), false));
            } else if (jokerType == ComboLeftAllJoker.class) {
                list.add(new ComboLeftAllJoker(random.nextInt(3)));
            } else if (jokerType == ComboLeftJoker.class) {
                list.add(new ComboLeftJoker(random.nextInt(5), random.nextInt(14)-2));
            } else if (jokerType == ComboRightJoker.class) {
                list.add(new ComboRightJoker(random.nextInt(5), random.nextInt(14)-2));
            }
        }

        return list;
    }
}
