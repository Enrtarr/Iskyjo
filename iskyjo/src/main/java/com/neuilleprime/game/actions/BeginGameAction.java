package com.neuilleprime.game.actions;

import com.neuilleprime.game.*;

/**
 * Action that starts a new game.
 * <p>
 * When executed, delegates to {@link GameController#beginGame()}, which
 * performs the initial round setup and deals cards to all players.
 * </p>
 */
public class BeginGameAction implements Action {

    /**
     * Constructs a new {@code BeginGameAction}.
     */
    public BeginGameAction() {}

    /**
     * Starts the game by invoking {@link GameController#beginGame()}.
     *
     * @param game the game controller on which to begin the game
     */
    @Override
    public void execute(GameController game) {
        game.beginGame();
    }
}
