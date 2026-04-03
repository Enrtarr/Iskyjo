package com.neuilleprime.gui;

import java.time.OffsetDateTime;
import com.google.gson.JsonObject;

import com.jagrosh.discordipc.IPCClient;
import com.jagrosh.discordipc.IPCListener;
import com.jagrosh.discordipc.entities.DiscordBuild;
import com.jagrosh.discordipc.entities.Packet;
import com.jagrosh.discordipc.entities.RichPresence;
import com.jagrosh.discordipc.entities.User;
import com.jagrosh.discordipc.entities.ActivityType;

/**
 * Simple manager for Discord Rich Presence.
 *
 * Usage:
 * - call DiscordRpc.init() once when the app startsS
 * - call DiscordRpc.setMainMenuPresence(), setGamePresence(...), setShopPresence(...)
 * - call DiscordRpc.shutdown() when the app closes
 */
public final class DiscordRpc {

    private static final long CLIENT_ID = 1489598331595264123L;

    private static IPCClient client;
    private static boolean ready = false;
    private static OffsetDateTime appStartTime;

    private DiscordRpc() {
    }

    /**
     * Initializes the Discord IPC connection.
     * Safe to call once at startup.
     */
    public static synchronized void init() {
        if (client != null) {
            return;
        }

        appStartTime = OffsetDateTime.now();
        client = new IPCClient(CLIENT_ID);

        client.setListener(new IPCListener() {
            @Override
            public void onReady(IPCClient ipcClient) {
                ready = true;
                System.out.println("[DiscordRpc] Connected to Discord RPC.");
                setMainMenuPresence();
            }

            @Override
            public void onDisconnect(IPCClient ipcClient, Throwable t) {
                ready = false;
                System.out.println("[DiscordRpc] Disconnected from Discord RPC.");
                if (t != null) {
                    t.printStackTrace();
                }
            }

            @Override
                public void onClose(IPCClient ipcClient, JsonObject json) {
                    ready = false;
                    System.out.println("[DiscordRpc] RPC closed: " + json);
                }

            @Override
            public void onPacketSent(IPCClient ipcClient, Packet packet) {}

            @Override
            public void onPacketReceived(IPCClient ipcClient, Packet packet) {}

            @Override
            public void onActivityJoin(IPCClient ipcClient, String secret) {}

            @Override
            public void onActivitySpectate(IPCClient ipcClient, String secret) {}

            @Override
            public void onActivityJoinRequest(IPCClient ipcClient, String secret, User user) {}
        });

        try {
            client.connect(DiscordBuild.ANY);
        } catch (Exception e) {
            System.err.println("[DiscordRpc] Failed to connect to Discord. Is Discord running?");
        }
    }

    /**
     * Shuts down the Discord IPC connection cleanly.
     */
    public static synchronized void shutdown() {
        ready = false;

        try {
            if (client != null) {
                client.sendRichPresence(null);
                client.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        client = null;
    }

    /**
     * Returns whether Discord RPC is currently connected and ready.
     *
     * @return true if connected
     */
    public static boolean isReady() {
        return ready && client != null;
    }

    /**
     * Presence for the main menu.
     */
    public static void setMainMenuPresence() {
        sendPresence(
            "In the main menu",
            null,
            "logo",
            "by Maaple & Enrtarr",
            null,
            null
        );
    }

    /**
     * Presence for the shop.
     */
    public static void setShopPresence() {
        sendPresence(
            "In the shop",
            null,
            "shop",
            "by Maaple & Enrtarr",
            null,
            null
            
        );
    }

    /**
     * Presence for a live game session.
     *
     * @param scoreToBeat score target for the current round
     * @param currentScore current player score
     */
    public static void setGamePresence(int scoreToBeat, int currentScore) {
        sendPresence(
            "Playing a game",
            "Score: " + currentScore + " / " + scoreToBeat,
            "game",
            "by Maaple & Enrtarr",
            null,
            null
        );
    }


    /**
     * Clears the active rich presence from Discord.
     */
    public static void clearPresence() {
        if (!isReady()) {
            System.out.println("[DiscordRpc] Not ready, skipping clearPresence().");
            return;
        }

        try {
            client.sendRichPresence(null);
        } catch (Exception e) {
            System.err.println("[DiscordRpc] Failed to clear presence.");
            e.printStackTrace();
        }
    }

    /**
     * Low-level helper to send a rich presence payload.
     *
     * @param details main line shown in Discord
     * @param state secondary line shown in Discord
     * @param largeImage asset key for the large image
     * @param largeText tooltip text for the large image
     * @param smallImage asset key for the small image
     * @param smallText tooltip text for the small image
     */
    private static void sendPresence(
        String details,
        String state,
        String largeImage,
        String largeText,
        String smallImage,
        String smallText
    ) {
        if (!isReady()) {
            System.out.println("[DiscordRpc] Not ready, skipping presence update.");
            return;
        }

        try {
            RichPresence.Builder builder = new RichPresence.Builder()
                .setActivityType(ActivityType.Playing)
                .setDetails(details)
                .setState(state)
                .setStartTimestamp(appStartTime.toEpochSecond());

            if (largeImage != null && largeText != null) {
                builder.setLargeImage(largeImage, largeText);
            }

            if (smallImage != null && smallText != null) {
                builder.setSmallImage(smallImage, smallText);
            }

            client.sendRichPresence(builder.build());
        } catch (Exception e) {
            System.err.println("[DiscordRpc] Failed to send presence.");
            e.printStackTrace();
        }
    }
}