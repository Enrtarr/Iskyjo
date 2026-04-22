package com.neuilleprime.game;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import com.neuilleprime.jokers.AddXCardJoker;
import com.neuilleprime.jokers.AddXDeckJoker;
import com.neuilleprime.jokers.ComboLeftAllJoker;
import com.neuilleprime.jokers.ComboLeftJoker;
import com.neuilleprime.jokers.ComboRightJoker;
import com.neuilleprime.jokers.Joker;

public class Shop {

    private Map<Player, ArrayList<Joker>> shopMap = new HashMap<>();

    public Shop() {

    }

    public void clearShopMap() {
        this.shopMap.clear();
    }

    private ArrayList<Joker> getXJokers(int x) {
        ArrayList<Joker> list = new ArrayList<>();
        for (int i=0; i<x; i++) {
            Class<? extends Joker> jokerType = Joker.getRandomType();

            Random random = new Random();

            if (jokerType == AddXCardJoker.class) {
                list.add(new AddXCardJoker(random.nextInt(5)+1, false));
            } else if (jokerType == AddXDeckJoker.class) {
                list.add(new AddXDeckJoker(random.nextInt(3)+1, false));
            } else if (jokerType == ComboLeftAllJoker.class) {
                list.add(new ComboLeftAllJoker(random.nextInt(3)+1));
            } else if (jokerType == ComboLeftJoker.class) {
                list.add(new ComboLeftJoker(random.nextInt(5)+1, random.nextInt(3)+2));
            } else if (jokerType == ComboRightJoker.class) {
                list.add(new ComboRightJoker(random.nextInt(5)+1, random.nextInt(14)+1-2));
            }
        }

        return list;
    }

    public ArrayList<Joker> rerollShop(Player player) {
        return rerollShop(player, false);
    }

    public ArrayList<Joker> rerollShop(Player player, boolean ignoreCost) {

        ArrayList<Joker> jokers = getXJokers(player.getShopRerollAmount());

        if (!ignoreCost 
                && player.getMoney() - player.getShopRerollPrice() >= 0) {
            int price = player.getShopRerollPrice();
            player.setMoney(player.getMoney() - price);
            player.setShopRerollPrice(price + 2); // maybe change this formula to a more complex one later

            this.shopMap.put(player, jokers);
        } else if (ignoreCost) {
            this.shopMap.put(player, jokers);
        }
        
        return this.shopMap.get(player);
    }

    public void sellJoker(Player player, Joker joker) {
        if (player.getJokers().contains(joker)) {
            player.setMoney(player.getMoney() + joker.getPrice());
            player.getJokers().remove(joker);
        }
        else {
            System.out.println("Player doesn't own the joker");
        }
    }

    public ArrayList<Joker> buyJoker(Player player, Joker joker) {
        // System.out.println("amogosus");
        ArrayList<Joker> playerJokers = this.shopMap.get(player);

        int jokerCost = joker.getPrice()*2;
        if (player.getMoney() - jokerCost >= 0
                && player.getJokers().size() + 1 <= player.getMaxJokers()) {

            player.setMoney(player.getMoney() - jokerCost);

            player.getJokers().add(joker);

            playerJokers.remove(joker);
            
            System.out.println("sold 🤑");
        } else {
            System.out.println("broke ahh");
        }
        
        return playerJokers;
    }
}
