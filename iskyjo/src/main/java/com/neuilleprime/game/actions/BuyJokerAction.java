package com.neuilleprime.game.actions;

import com.neuilleprime.game.*;
import com.neuilleprime.jokers.Joker;

public class BuyJokerAction implements Action {
    private final Player player;
    private final Joker joker;

    public BuyJokerAction(Player player, Joker joker) {
        this.player = player;
        this.joker = joker;
    }

    @Override
    public void execute(GameController game) {
        game.buyJoker(this.player, this.joker);
    }
}
