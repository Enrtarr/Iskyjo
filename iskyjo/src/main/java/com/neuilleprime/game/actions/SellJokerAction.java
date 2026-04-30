package com.neuilleprime.game.actions;

import com.neuilleprime.game.*;
import com.neuilleprime.jokers.Joker;

/**
 * Action that sells a joker from a player's collection back to the shop.
 * <p>
 * Delegates to {@link GameController#sellJoker(Player, Joker)}.
 * </p>
 */
public class SellJokerAction implements Action {

    /** The player selling the joker. */
    private final Player player;

    /** The joker to sell. */
    private final Joker joker;

    /**
     * Constructs a new {@code SellJokerAction}.
     *
     * @param player the player who is selling
     * @param joker  the joker to sell
     */
    public SellJokerAction(Player player, Joker joker) {
        this.player = player;
        this.joker = joker;
    }

    /**
     * Sells the joker for the stored player.
     *
     * @param game the game controller on which to execute the sale
     */
    @Override
    public void execute(GameController game) {
        game.sellJoker(this.player, this.joker);
    }
}
