package com.neuilleprime.gui.utils;

import javafx.scene.image.Image;

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
    }

    private static Image load(String path) {
        var s = AssetLoader.class.getResourceAsStream(path);
        if (s == null) throw new RuntimeException("Ressource manquante: " + path);
        return new Image(s);
    }
}
