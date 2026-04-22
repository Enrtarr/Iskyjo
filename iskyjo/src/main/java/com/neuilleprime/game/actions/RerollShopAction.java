package com.neuilleprime.game.actions;

import com.neuilleprime.game.*;

public class RerollShopAction implements Action {
    private final Player player;

    public RerollShopAction(Player player) {
        this.player = player;
    }

    @Override
    public void execute(GameController game) {
        game.rerollShop(this.player);
    }
}
