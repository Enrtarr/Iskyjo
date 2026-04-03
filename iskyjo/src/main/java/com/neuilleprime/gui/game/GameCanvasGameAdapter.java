package com.neuilleprime.gui.game;

import java.util.Map;

import com.neuilleprime.gui.model.GameState;

/**
 * Contract between the in-game canvas and the gameplay layer.
 *
 * <p>An implementation can be backed by a local controller, a server-driven
 * match, or a hybrid mode. The bridge calls into this adapter whenever the
 * visual state must be refreshed or a user action must be propagated.</p>
 */
public interface GameCanvasGameAdapter {
    /** Pulls the latest gameplay state into the mutable visual state. */
    void pullInto(GameState visualState);

    /** Computes the score currently displayed for the active player. */
    default int computePlayerScore(GameState visualState) {
        return visualState.playerScore;
    }

    /** Computes the target score shown by the HUD. */
    default int computeScoreToBeat(GameState visualState) {
        return visualState.scoreToBeat;
    }

    /** Receives a generic visual-state change notification from the GUI. */
    default void onVisualStateChanged(GameState visualState) {
    }

    /** Receives a high-level GUI event with an optional payload. */
    default void onUiEvent(String eventType, GameState visualState, Map<String, Object> payload) {
    }

    /** Indicates whether the backing implementation is multiplayer-aware. */
    default boolean isMultiplayerSession() {
        return false;
    }
}
