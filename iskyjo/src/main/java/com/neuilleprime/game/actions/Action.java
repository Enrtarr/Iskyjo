package com.neuilleprime.game.actions;

import com.neuilleprime.game.GameController;

/**
 * Represents an action that can be executed against the game controller.
 * <p>
 * All player-initiated commands (drawing, flipping, replacing cards, etc.)
 * implement this interface so they can be dispatched uniformly through
 * {@link GameController#execute(Action)}.
 * </p>
 */
public interface Action {

    /**
     * Executes this action on the given game controller.
     *
     * @param game the {@link GameController} on which to apply the action
     */
    void execute(GameController game);
}
