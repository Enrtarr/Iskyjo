package com.neuilleprime.gui.utils;

import java.util.ArrayList;

import com.neuilleprime.game.GameController;
import com.neuilleprime.game.Player;

public class GameLogic {

    public static GameController gameController;
    public static Player localPlayer = new Player("Amogus");

    public static GameController setupGame(int nbrPlr, int nbrCard) {

        ArrayList<Player> playerList = new ArrayList<>();
        
        playerList.add(localPlayer);
        // here we should go to nbrPlr+1, but since we already have our local player in, we don't
        for (int i=1;i<nbrPlr;i++) {
            playerList.add(new Player());
        }

        return new GameController(playerList, nbrCard);
    }
}
