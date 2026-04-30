package com.neuilleprime.gui.utils;

import java.util.ArrayList;

import com.neuilleprime.game.GameController;
import com.neuilleprime.game.Player;

/**
 * Singleton-style utility class that holds shared game state for the GUI.
 * <p>
 * Stores the single {@link GameController} instance and the local player so
 * that all screens can access them without passing them through constructors.
 * </p>
 */
public class GameLogic {

    /**
     * The active game controller.
     * Set to {@code null} between games and initialized before the game screen
     * is shown.
     */
    public static GameController gameController;

    /**
     * The local human player.
     * Created once at startup with a fixed test name.
     */
    public static Player localPlayer = new Player("Amogus");

    /**
     * Creates and returns a {@link GameController} configured for the given
     * number of players and cards.
     * <p>
     * The {@link #localPlayer} is always placed first in the player list; the
     * remaining {@code nbrPlr - 1} slots are filled with anonymous {@link Player}
     * instances.
     * </p>
     *
     * @param nbrPlr  total number of players (including the local player)
     * @param nbrCard total number of cards in the draw pile
     * @return a freshly created {@link GameController}
     */
    public static GameController setupGame(int nbrPlr, int nbrCard) {

        ArrayList<Player> playerList = new ArrayList<>();
        
        playerList.add(localPlayer);
        for (int i=1;i<nbrPlr;i++) {
            playerList.add(new Player());
        }

        return new GameController(playerList, nbrCard);
    }
}
