package com.neuilleprime.gui.game;

import java.util.Map;

import com.neuilleprime.game.GameController;
import com.neuilleprime.game.Player;
import com.neuilleprime.gui.model.GameState;

/**
 * Starter adapter that connects {@link GameCanvas} to the existing
 * {@link GameController} gameplay layer.
 *
 * <p>This class is intentionally conservative: score values are already wired,
 * while deck and pile synchronisation are left as explicit TODO hooks because
 * the GUI card model and the core game card model are still distinct types.</p>
 */
public class GameControllerCanvasAdapter implements GameCanvasGameAdapter {
    private final GameController controller;

    /**
     * Creates a new adapter backed by the supplied controller.
     *
     * @param controller the gameplay controller driving the current match
     */
    public GameControllerCanvasAdapter(GameController controller) {
        this.controller = controller;
    }

    @Override
    public void pullInto(GameState visualState) {
        Player currentPlayer = controller.getCurrentPlayer();

        visualState.playerScore = computePlayerScore(visualState);
        visualState.scoreToBeat = computeScoreToBeat(visualState);

        // TODO Map the current player's deck to visualState.grid.
        // TODO Map controller.getDrawPileTop() to visualState.drawPile.
        // TODO Map controller.getDiscardPileTop() to visualState.discardPile.
        // TODO Map currentPlayer jokers / consumables / upgrades to the GUI model.
    }

    @Override
    public int computePlayerScore(GameState visualState) {
        return controller.getCurrentPlayer().getPoints();
    }

    @Override
    public int computeScoreToBeat(GameState visualState) {
        // TODO expose roundScore from GameController through a dedicated getter.
        return visualState.scoreToBeat;
    }

    @Override
    public void onUiEvent(String eventType, GameState visualState, Map<String, Object> payload) {
        // TODO Translate GUI events into core actions.
        // Examples:
        // - draw_pile_clicked -> controller.drawCard("draw")
        // - discard_card_placed -> controller.replaceCard(...)
        // - selected_card_discarded -> controller.discardCard(...)
    }

    @Override
    public boolean isMultiplayerSession() {
        return false;
    }
}
