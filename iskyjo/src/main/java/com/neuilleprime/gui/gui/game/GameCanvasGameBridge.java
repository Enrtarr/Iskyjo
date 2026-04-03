package com.neuilleprime.gui.game;

import java.util.Collections;
import java.util.Map;

import com.neuilleprime.gui.model.GameState;

/**
 * Synchronises the canvas with gameplay logic and optional multiplayer networking.
 *
 * <p>The bridge deliberately sits between JavaFX code and the real game engine.
 * This keeps rendering and input handling independent from local-controller or
 * client/server decisions.</p>
 */
public final class GameCanvasGameBridge {
    private final GameCanvas owner;
    private GameCanvasGameAdapter adapter;
    private GameCanvasNetworkClient networkClient;

    GameCanvasGameBridge(GameCanvas owner) {
        this.owner = owner;
    }

    /** Binds the gameplay adapter used to read and mutate game state. */
    public void bindAdapter(GameCanvasGameAdapter adapter) {
        this.adapter = adapter;
        refreshFromGame();
    }

    /** Binds the multiplayer client used for outgoing server requests. */
    public void bindNetworkClient(GameCanvasNetworkClient networkClient) {
        this.networkClient = networkClient;
    }

    /** Pulls fresh data from the gameplay layer into the visual state and redraws the canvas. */
    public void refreshFromGame() {
        if (adapter != null) {
            adapter.pullInto(owner.state);
            refreshVisualScores();
        }
        owner.render();
    }

    /** Recomputes the HUD score values from the gameplay adapter. */
    public void refreshVisualScores() {
        if (adapter == null) {
            return;
        }

        owner.state.playerScore = adapter.computePlayerScore(owner.state);
        owner.state.scoreToBeat = adapter.computeScoreToBeat(owner.state);
    }

    /** Notifies the gameplay adapter that the local visual state changed. */
    public void notifyVisualStateChanged() {
        if (adapter != null) {
            adapter.onVisualStateChanged(owner.state);
            refreshVisualScores();
        }
        owner.render();
    }

    /** Dispatches a high-level UI event to the gameplay adapter and, when present, the server. */
    public void dispatchUiEvent(String eventType, Map<String, Object> payload) {
        Map<String, Object> safePayload = payload == null ? Collections.emptyMap() : payload;

        if (adapter != null) {
            adapter.onUiEvent(eventType, owner.state, safePayload);
            refreshVisualScores();
        }

        if (networkClient != null && networkClient.isConnected()) {
            networkClient.sendRequest(eventType, safePayload);
        }

        owner.render();
    }

    /** Returns the current mutable visual state. */
    public GameState getVisualState() {
        return owner.state;
    }
}
