package com.neuilleprime.game.actions;

import com.neuilleprime.game.*;

/**
 * Action that draws the top card from a named pile.
 * <p>
 * Delegates to {@link GameController#drawCard(String)}.
 * </p>
 */
public class DrawCardAction implements Action {

    /** The name of the pile to draw from ({@code "draw"} or {@code "discard"}). */
    private final String pileName;

    /**
     * Constructs a new {@code DrawCardAction}.
     *
     * @param pileName {@code "draw"} or {@code "discard"} (case-insensitive)
     */
    public DrawCardAction(String pileName) {
        this.pileName = pileName;
    }

    /**
     * Draws from the specified pile.
     *
     * @param game the game controller on which to execute the draw
     */
    @Override
    public void execute(GameController game) {
        game.drawCard(this.pileName);
    }
}
