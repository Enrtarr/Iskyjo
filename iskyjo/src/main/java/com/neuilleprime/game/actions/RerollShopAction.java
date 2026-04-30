package com.neuilleprime.game.actions;

import com.neuilleprime.game.*;

/**
 * Action that rerolls the shop inventory for a given player.
 * <p>
 * Delegates to {@link GameController#rerollShop(Player)}.
 * </p>
 */
public class RerollShopAction implements Action {

    /** The player requesting the shop reroll. */
    private final Player player;

    /**
     * Constructs a new {@code RerollShopAction}.
     *
     * @param player the player who wants to reroll the shop
     */
    public RerollShopAction(Player player) {
        this.player = player;
    }

    /**
     * Rerolls the shop for the stored player.
     *
     * @param game the game controller on which to execute the reroll
     */
    @Override
    public void execute(GameController game) {
        game.rerollShop(this.player);
    }
}
