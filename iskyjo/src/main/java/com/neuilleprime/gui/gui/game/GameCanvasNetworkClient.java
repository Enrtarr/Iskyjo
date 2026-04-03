package com.neuilleprime.gui.game;

import java.util.Map;

/** Abstraction used by the game bridge to send or request multiplayer data. */
public interface GameCanvasNetworkClient {
    /** Returns whether the network client is ready to exchange requests. */
    boolean isConnected();

    /** Sends a high-level request to the remote multiplayer service. */
    void sendRequest(String requestType, Map<String, Object> payload);
}
