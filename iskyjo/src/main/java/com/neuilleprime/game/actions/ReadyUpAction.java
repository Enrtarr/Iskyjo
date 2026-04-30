package com.neuilleprime.game.actions;

import com.neuilleprime.game.GameController;

/**
 * Action that signals a player is ready to start the next round.
 * <p>
 * Delegates to {@link GameController#readyUp()}. Once all players are ready
 * the new round begins automatically.
 * </p>
 */
public class ReadyUpAction implements Action {

    /**
     * Constructs a new {@code ReadyUpAction}.
     */
    public ReadyUpAction() {

    }

    /**
     * Marks one more player as ready.
     *
     * @param game the game controller on which to register the ready state
     */
    @Override
    public void execute(GameController game) {
        game.readyUp();;
    }
}
