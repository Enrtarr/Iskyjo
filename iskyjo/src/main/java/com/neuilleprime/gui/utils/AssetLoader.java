package com.neuilleprime.gui.utils;

import javafx.scene.image.Image;
import java.util.Map;
import java.util.HashMap;

public class AssetLoader {

    public static final Image CARD_BACK;
    public static final Image CARD_BG;
    public static final Image CARD_BLANK;
    public static final Image CARD_OVERLAY_1;
    public static final Image CARD_OVERLAY_2;
    public static final Image CARD_TRANSPARENT;
    public static final Image BUTTON_PLAY;
    public static final Image BUTTON_LEADERBOARD;
    public static final Image NP_LOGO;

    // jokers
    public static final Image J_ADD_X_CARD;
    public static final Image J_ADD_X_DECK;
    public static final Image J_COMBO_LEFT_ALL;
    public static final Image J_COMBO_LEFT;
    public static final Image J_COMBO_RIGHT;

    private static final Map<String, Image> ASSET_MAP = new HashMap<>();

    static {
        CARD_BACK          = load("/Assets/Cards/card_back.png");
        CARD_BG            = load("/Assets/Cards/card_bg.png");
        CARD_BLANK         = load("/Assets/Cards/card_blank.png");
        CARD_OVERLAY_1     = load("/Assets/Cards/card_overlay-1.png");
        CARD_OVERLAY_2     = load("/Assets/Cards/card_overlay-2.png");
        CARD_TRANSPARENT   = load("/Assets/Cards/card_transparent.png");
        BUTTON_PLAY        = load("/Assets/Buttons/button_play.png");
        BUTTON_LEADERBOARD = load("/Assets/Buttons/button_leaderboard.png");
        NP_LOGO            = load("/Assets/NP_logo.png");

        ASSET_MAP.put("card_back", CARD_BACK);
        ASSET_MAP.put("card_bg", CARD_BG);
        ASSET_MAP.put("card_blank", CARD_BLANK);
        ASSET_MAP.put("card_overlay_1", CARD_OVERLAY_1);
        ASSET_MAP.put("card_overlay_2", CARD_OVERLAY_2);
        ASSET_MAP.put("card_transparent", CARD_TRANSPARENT);
        ASSET_MAP.put("button_play", BUTTON_PLAY);
        ASSET_MAP.put("button_leaderboard", BUTTON_LEADERBOARD);
        ASSET_MAP.put("np_logo", NP_LOGO);

        // All the jokers (here we do them 2 by 2 cause it looks cleaner to me)
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

    private static Image load(String path) {
        var stream = AssetLoader.class.getResourceAsStream(path);
        if (stream == null) {
            throw new RuntimeException("Ressource manquante: " + path);
        }
        return new Image(stream);
    }

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
