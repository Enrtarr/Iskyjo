package com.neuilleprime.game.actions;

import com.neuilleprime.game.*;
import com.neuilleprime.jokers.Joker;

/**
 * Action that purchases a joker from the shop for a given player.
 * <p>
 * When executed, delegates to {@link GameController#buyJoker(Player, Joker)}.
 * </p>
 */
public class BuyJokerAction implements Action {

    /** The player who is buying the joker. */
    private final Player player;

    /** The joker to be purchased. */
    private final Joker joker;

    /**
     * Constructs a new {@code BuyJokerAction}.
     *
     * @param player the player who is buying
     * @param joker  the joker to purchase
     */
    public BuyJokerAction(Player player, Joker joker) {
        this.player = player;
        this.joker = joker;
    }

    /**
     * Purchases the joker for the player by invoking
     * {@link GameController#buyJoker(Player, Joker)}.
     *
     * @param game the game controller on which to execute the purchase
     */
    @Override
    public void execute(GameController game) {
        game.buyJoker(this.player, this.joker);
    }
}
