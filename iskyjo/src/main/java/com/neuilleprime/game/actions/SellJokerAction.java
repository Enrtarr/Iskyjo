package com.neuilleprime.game.actions;

import com.neuilleprime.game.*;
import com.neuilleprime.jokers.Joker;

public class SellJokerAction implements Action {
    private final Player player;
    private final Joker joker;

    public SellJokerAction(Player player, Joker joker) {
        this.player = player;
        this.joker = joker;
    }

    @Override
    public void execute(GameController game) {
        game.sellJoker(this.player, this.joker);
    }
}
