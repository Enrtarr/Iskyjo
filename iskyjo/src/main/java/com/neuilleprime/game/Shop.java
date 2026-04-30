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

/**
 * Manages the in-game shop where players can buy, sell, and reroll jokers.
 * <p>
 * Each player has their own shop inventory stored in {@link #shopMap}.
 * Rerolling populates (or refreshes) that inventory; buying removes a joker
 * from it and adds it to the player; selling removes a joker from the player
 * and refunds its sell price.
 * </p>
 */
public class Shop {

    /** Maps each player to their current list of available shop jokers. */
    private Map<Player, ArrayList<Joker>> shopMap = new HashMap<>();

    /**
     * Constructs a new empty {@code Shop}.
     */
    public Shop() {

    }

    /**
     * Clears all shop inventories for all players.
     */
    public void clearShopMap() {
        this.shopMap.clear();
    }

    /**
     * Generates a list of {@code x} randomly selected jokers for sale.
     * Each joker type is chosen via {@link Joker#getRandomType()} and its
     * constructor parameters are randomised.
     *
     * @param x the number of jokers to generate
     * @return a list of freshly created {@link Joker} instances
     */
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

    /**
     * Rerolls the shop for the given player, deducting the reroll cost.
     * Delegates to {@link #rerollShop(Player, boolean)} with {@code ignoreCost = false}.
     *
     * @param player the player requesting the reroll
     * @return the new list of available jokers, or the existing list if the player
     *         cannot afford the reroll
     */
    public ArrayList<Joker> rerollShop(Player player) {
        return rerollShop(player, false);
    }

    /**
     * Rerolls the shop for the given player.
     * <p>
     * If {@code ignoreCost} is {@code false}, the reroll price is deducted from
     * the player's money and the price increases for subsequent rerolls. If the
     * player cannot afford it, the shop inventory is left unchanged.
     * If {@code ignoreCost} is {@code true}, the inventory is always replaced.
     * </p>
     *
     * @param player     the player requesting the reroll
     * @param ignoreCost {@code true} to bypass cost checking (e.g. at round start)
     * @return the current list of available jokers for the player
     */
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

    /**
     * Sells a joker owned by the given player.
     * The joker's sell price is added to the player's money and the joker is
     * removed from their collection. If the player does not own the joker,
     * an error message is printed and nothing happens.
     *
     * @param player the player selling the joker
     * @param joker  the joker to sell
     */
    public void sellJoker(Player player, Joker joker) {
        if (player.getJokers().contains(joker)) {
            player.setMoney(player.getMoney() + joker.getPrice());
            player.getJokers().remove(joker);
        }
        else {
            System.out.println("Player doesn't own the joker");
        }
    }

    /**
     * Purchases a joker from the shop for the given player.
     * <p>
     * The buy price is twice the joker's base price. The purchase succeeds only if
     * the player has enough money and has not reached their joker capacity.
     * On success the joker is removed from the shop inventory and added to the
     * player's collection.
     * </p>
     *
     * @param player the player buying the joker
     * @param joker  the joker to purchase (must be present in the player's shop inventory)
     * @return the updated shop inventory for the player after the purchase
     */
    public ArrayList<Joker> buyJoker(Player player, Joker joker) {
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
