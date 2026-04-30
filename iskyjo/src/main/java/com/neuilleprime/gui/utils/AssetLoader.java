package com.neuilleprime.gui.utils;

import javafx.scene.image.Image;
import java.util.Map;
import java.util.HashMap;

/**
 * Centralized loader for all image assets used in the GUI.
 * <p>
 * All images are loaded once in a static initializer block and stored both as
 * typed constants (e.g. {@link #CARD_BACK}) and in a case-insensitive
 * {@link #ASSET_MAP} keyed by short name. Use {@link #getAssetFromString(String)}
 * to look up an asset by name at runtime (e.g. from a joker's texture name).
 * </p>
 * <p>
 * <strong>Note:</strong> every new joker must be registered here alongside its
 * constant and map entry.
 * </p>
 */
public class AssetLoader {

    /** Card back-face image. */
    public static final Image CARD_BACK;

    /** Generic card background image. */
    public static final Image CARD_BG;

    /** Blank card front image used as the base layer. */
    public static final Image CARD_BLANK;

    /** First card overlay variant (unused in current build). */
    public static final Image CARD_OVERLAY_1;

    /** Second card overlay variant used for tinting. */
    public static final Image CARD_OVERLAY_2;

    /** Fully transparent card-shaped image used for aspect-ratio maintenance. */
    public static final Image CARD_TRANSPARENT;

    /** Neuilleprime studio logo. */
    public static final Image NP_LOGO;

    /** "Play" button image. */
    public static final Image BUTTON_PLAY;

    /** "Leaderboard" button image. */
    public static final Image BUTTON_LEADERBOARD;

    /** "Shop" button image. */
    public static final Image BUTTON_SHOP;

    /** "Reroll" button image. */
    public static final Image BUTTON_REROLL;

    /** "Next Round" button image. */
    public static final Image BUTTON_NEXT_ROUND;

    // Joker images
    /** Image for {@link com.neuilleprime.jokers.AddXCardJoker}. */
    public static final Image J_ADD_X_CARD;

    /** Image for {@link com.neuilleprime.jokers.AddXDeckJoker}. */
    public static final Image J_ADD_X_DECK;

    /** Image for {@link com.neuilleprime.jokers.ComboLeftAllJoker}. */
    public static final Image J_COMBO_LEFT_ALL;

    /** Image for {@link com.neuilleprime.jokers.ComboLeftJoker}. */
    public static final Image J_COMBO_LEFT;

    /** Image for {@link com.neuilleprime.jokers.ComboRightJoker}. */
    public static final Image J_COMBO_RIGHT;

    /**
     * Case-insensitive map from asset short name to its loaded {@link Image}.
     * Populated in the static initializer alongside the constants above.
     */
    private static final Map<String, Image> ASSET_MAP = new HashMap<>();

    static {
        CARD_BACK          = load("/Assets/Cards/card_back.png");
        CARD_BG            = load("/Assets/Cards/card_bg.png");
        CARD_BLANK         = load("/Assets/Cards/card_blank.png");
        CARD_OVERLAY_1     = load("/Assets/Cards/card_overlay-1.png");
        CARD_OVERLAY_2     = load("/Assets/Cards/card_overlay-2.png");
        CARD_TRANSPARENT   = load("/Assets/Cards/card_transparent.png");
        NP_LOGO            = load("/Assets/NP_logo.png");
        BUTTON_PLAY        = load("/Assets/Buttons/button_play.png");
        BUTTON_LEADERBOARD = load("/Assets/Buttons/button_leaderboard.png");
        BUTTON_SHOP        = load("/Assets/Buttons/button_shop.png");
        BUTTON_REROLL      = load("/Assets/Buttons/button_reroll.png");
        BUTTON_NEXT_ROUND  = load("/Assets/Buttons/button_nextround.png");

        ASSET_MAP.put("card_back", CARD_BACK);
        ASSET_MAP.put("card_bg", CARD_BG);
        ASSET_MAP.put("card_blank", CARD_BLANK);
        ASSET_MAP.put("card_overlay_1", CARD_OVERLAY_1);
        ASSET_MAP.put("card_overlay_2", CARD_OVERLAY_2);
        ASSET_MAP.put("card_transparent", CARD_TRANSPARENT);
        ASSET_MAP.put("np_logo", NP_LOGO);
        ASSET_MAP.put("button_play", BUTTON_PLAY);
        ASSET_MAP.put("button_leaderboard", BUTTON_LEADERBOARD);
        ASSET_MAP.put("button_shop", BUTTON_SHOP);
        ASSET_MAP.put("button_reroll", BUTTON_REROLL);
        ASSET_MAP.put("button_next_round", BUTTON_NEXT_ROUND);

        J_ADD_X_CARD = load("/Assets/Jokers/AddXCardJoker.png");
        ASSET_MAP.put("addxcardjoker", J_ADD_X_CARD);
        J_ADD_X_DECK = load("/Assets/Jokers/AddXDeckJoker.png");
        ASSET_MAP.put("addxdeckjoker", J_ADD_X_DECK);
        J_COMBO_LEFT_ALL = load("/Assets/Jokers/ComboLeftAllJoker.png");
        ASSET_MAP.put("comboleftalljoker", J_COMBO_LEFT_ALL);
        J_COMBO_LEFT = load("/Assets/Jokers/ComboLeftJoker.png");
        ASSET_MAP.put("comboleftjoker", J_COMBO_LEFT);
        J_COMBO_RIGHT = load("/Assets/Jokers/ComboRightJoker.png");
        ASSET_MAP.put("comborightjoker", J_COMBO_RIGHT);
    }

    /**
     * Loads an image from the classpath.
     *
     * @param path classpath-relative path to the image resource
     * @return the loaded {@link Image}
     * @throws RuntimeException if the resource cannot be found
     */
    private static Image load(String path) {
        var stream = AssetLoader.class.getResourceAsStream(path);
        if (stream == null) {
            throw new RuntimeException("Ressource manquante: " + path);
        }
        return new Image(stream);
    }

    /**
     * Returns the image associated with the given asset name (case-insensitive).
     *
     * @param str the short name of the asset (e.g. {@code "AddXCardJoker"})
     * @return the corresponding {@link Image}
     * @throws IllegalArgumentException if {@code str} is {@code null} or unknown
     */
    public static Image getAssetFromString(String str) {
        if (str == null) {
            throw new IllegalArgumentException("Nom d'asset null");
        }

        Image img = ASSET_MAP.get(str.toLowerCase());
        if (img == null) {
            throw new IllegalArgumentException("Asset inconnu: " + str);
        }

        return img;
    }
}
