package com.neuilleprime.gui.game;

import javafx.scene.paint.Color;

/**
 * Computes all base layout values and hitboxes used by the in-game screen.
 *
 * <p>This helper centralises geometry so that rendering and interactions can
 * both rely on the same positioning rules.</p>
 */
public final class GameCanvasLayout {
    final GameCanvas owner;

    GameCanvasLayout(GameCanvas owner) {
        this.owner = owner;
    }

    public record GridMetrics(
        double cardW,
        double cardH,
        double gap,
        double totalW,
        double totalH,
        double originX,
        double originY
    ) {}

    GridMetrics gridMetrics() {
        double pad = 16.0;

        double availableW = Math.max(40.0, gridZoneW() - pad * 2.0);
        double availableH = Math.max(40.0, playAreaH() - pad * 2.0);

        double baseTotalW = owner.state.grid.cols * owner.CARD_W + (owner.state.grid.cols - 1) * owner.CARD_GAP;
        double baseTotalH = owner.state.grid.rows * owner.CARD_H + (owner.state.grid.rows - 1) * owner.CARD_GAP;

        double scaleX = availableW / baseTotalW;
        double scaleY = availableH / baseTotalH;
        double maxFitScale = Math.min(scaleX, scaleY);

        double windowScaleX = owner.getWidth() / 1400.0;
        double windowScaleY = owner.getHeight() / 900.0;
        double windowScale = Math.min(windowScaleX, windowScaleY);

        double minScale = 1.2 * windowScale;

        double scale = Math.max(minScale, 1.0);
        scale = Math.min(scale, maxFitScale);

        double cardW = owner.CARD_W * scale;
        double cardH = owner.CARD_H * scale;
        double gap = owner.CARD_GAP * scale;

        double totalW = owner.state.grid.cols * cardW + (owner.state.grid.cols - 1) * gap;
        double totalH = owner.state.grid.rows * cardH + (owner.state.grid.rows - 1) * gap;

        double originX = gridZoneX() + (gridZoneW() - totalW) / 2.0;
        double originY = playAreaY() + (playAreaH() - totalH) / 2.0;

        return new GridMetrics(cardW, cardH, gap, totalW, totalH, originX, originY);
    }


    /**
     * Returns the display colour for a card's numeric value.
     * <ul>
     *   <li>Negative values → red</li>
     *   <li>0 → black</li>
     *   <li>1–4 → green</li>
     *   <li>5–8 → pink/magenta</li>
     *   <li>9+ → dark blue</li>
     * </ul>
     *
     * @param value the card value to colourise
     * @return the corresponding {@link Color}
     */
    static Color cardValueColor(int value) {
        if (value < 0) return Color.web("#b11919");
        if (value == 0) return Color.web("#000000");
        if (value <= 4) return Color.web("#27d383");
        if (value <= 8) return Color.web("#f040bb");
        return Color.web("#4020ce");
    }

    /**
     * Returns the tint colour used for the card overlay image, derived from the card's value.
     *
     * @param value the card value to derive the tint from
     * @return the tint {@link Color} matching the card's value range
     */
    Color cardOverlayTint(int value) {
        return cardValueColor(value);
    }

    /**
     * Returns the X coordinate of the left edge of the main play area.
     *
     * @return play area left X, equal to {@code owner.PADDING}
     */
    double playAreaX() {
        return owner.PADDING;
    }

    /**
     * Returns the Y coordinate of the top edge of the main play area,
     * just below the top bar.
     *
     * @return play area top Y
     */
    double playAreaY() {
        return owner.TOP_BAR_H + 16.0;
    }

    /**
     * Returns the total width of the main play area (canvas width minus left and right padding).
     *
     * @return play area width in pixels
     */
    double playAreaW() {
        return owner.getWidth() - owner.PADDING * 2.0;
    }

    /**
     * Returns the height of the main play area, excluding the top bar and bottom bar.
     *
     * @return play area height in pixels
     */
    double playAreaH() {
        return owner.getHeight() - owner.TOP_BAR_H - owner.BOTTOM_BAR_H - 32.0;
    }

    /**
     * Returns the X coordinate of the draw/discard panel's left edge.
     *
     * @return draw panel left X
     */
    double drawPanelX() {
        return playAreaX();
    }

    /**
     * Returns the Y coordinate of the draw/discard panel's top edge.
     *
     * @return draw panel top Y
     */
    double drawPanelY() {
        return playAreaY();
    }

    /**
     * Returns the fixed width of the draw/discard panel.
     *
     * @return draw panel width in pixels
     */
    double drawPanelW() {
        return owner.DRAW_ZONE_W;
    }

    /**
     * Returns the height of the draw/discard panel, matching the play area height.
     *
     * @return draw panel height in pixels
     */
    double drawPanelH() {
        return playAreaH();
    }

    /**
     * Returns the X coordinate of the card grid zone's left edge,
     * positioned immediately to the right of the draw/discard panel.
     *
     * @return grid zone left X
     */
    double gridZoneX() {
        return drawPanelX() + drawPanelW() + owner.SECTION_GAP;
    }

    /**
     * Returns the width allocated to the card grid zone,
     * computed from the remaining space between the draw panel and the score panel.
     *
     * @return grid zone width in pixels
     */
    double gridZoneW() {
        return playAreaW() - drawPanelW() - owner.SCORE_PANEL_W - owner.SECTION_GAP * 2.0;
    }

    /**
     * Returns the X coordinate of the score panel's left edge,
     * anchored to the right side of the grid zone.
     *
     * @return score panel left X
     */
    double scorePanelX() {
        return gridZoneX() + gridZoneW() + owner.SECTION_GAP;
    }


    double[] gridOrigin() {
        GridMetrics m = gridMetrics();
        return new double[] {m.originX(), m.originY()};
    }

    /**
     * Returns the Y coordinate of the top edge of the bottom zone (jokers + consumables bar).
     *
     * @return bottom zone top Y
     */
    double bottomZoneY() {
        return owner.getHeight() - owner.BOTTOM_BAR_H + 10.0;
    }

    /**
     * Returns the height of the bottom zone panel.
     *
     * @return bottom zone height in pixels
     */
    double bottomZoneH() {
        return owner.BOTTOM_BAR_H - 20.0;
    }

    /**
     * Returns the total available width within the bottom zone,
     * accounting for left and right padding.
     *
     * @return available width in pixels
     */
    double bottomAvailableW() {
        return owner.getWidth() - owner.PADDING * 2.0;
    }

    /**
     * Returns the width of the joker zone, which takes 60% of the available bottom width.
     *
     * @return joker zone width in pixels
     */
    double jokerZoneW() {
        return (bottomAvailableW() - owner.SECTION_GAP) * 0.60;
    }

    /**
     * Returns the width of the consumable zone, which occupies the remainder of the
     * bottom bar after the joker zone and section gap.
     *
     * @return consumable zone width in pixels
     */
    double consumableZoneW() {
        return bottomAvailableW() - jokerZoneW() - owner.SECTION_GAP;
    }

    /**
     * Returns the X coordinate of the joker zone's left edge.
     *
     * @return joker zone left X
     */
    double jokerZoneX() {
        return owner.PADDING;
    }

    /**
     * Returns the X coordinate of the consumable zone's left edge,
     * positioned right after the joker zone.
     *
     * @return consumable zone left X
     */
    double consumableZoneX() {
        return jokerZoneX() + jokerZoneW() + owner.SECTION_GAP;
    }

    /**
     * Returns the X coordinate of the draw pile card, centred horizontally in the draw panel.
     *
     * @return draw card left X
     */
    double drawCardX() {
        return drawPanelX() + (drawPanelW() - owner.SPECIAL_CARD_W) / 2.0;
    }

    /**
     * Returns the X coordinate of the discard pile card.
     * Shares the same horizontal centre as the draw card.
     *
     * @return discard card left X
     */
    double discardCardX() {
        return drawCardX();
    }

    /**
     * Returns the Y coordinate of the draw pile card,
     * placed in the upper half of the draw panel.
     *
     * @return draw card top Y
     */
    double drawCardY() {
        return drawPanelY() + drawPanelH() / 2.0 - owner.SPECIAL_CARD_H - 12.0;
    }

    /**
     * Returns the Y coordinate of the discard pile card,
     * placed in the lower half of the draw panel.
     *
     * @return discard card top Y
     */
    double discardCardY() {
        return drawPanelY() + drawPanelH() / 2.0 + 12.0;
    }



    /**
     * Returns the X coordinate of the "Leave Game" button inside the settings panel.
     *
     * @return leave button left X
     */
    double getLeaveButtonX() {
        return owner.PANEL_X + owner.LEAVE_PAD;
    }

    /**
     * Returns the Y coordinate of the "Leave Game" button,
     * pinned to the bottom of the settings panel.
     *
     * @return leave button top Y
     */
    double getLeaveButtonY() {
        return owner.PANEL_Y + owner.PANEL_H - owner.LEAVE_BUTTON_HEIGHT - owner.LEAVE_PAD;
    }

    /**
     * Returns the width of the "Leave Game" button,
     * spanning the full panel width minus padding on both sides.
     *
     * @return leave button width in pixels
     */
    double getLeaveButtonWidth() {
        return owner.PANEL_W - owner.LEAVE_PAD * 2.0;
    }

    /**
     * Returns the X coordinate shared by all debug action buttons in the settings panel.
     *
     * @return debug buttons left X
     */
    double getDebugButtonX() {
        return owner.PANEL_X + owner.LEAVE_PAD;
    }

    /**
     * Returns the width shared by all debug action buttons in the settings panel.
     *
     * @return debug button width in pixels
     */
    double getDebugButtonWidth() {
        return owner.PANEL_W - owner.LEAVE_PAD * 2.0;
    }

    /**
     * Returns the Y coordinate of the first debug button, just below the settings panel header.
     *
     * @return first debug button top Y
     */
    double debugStartY() {
        return owner.PANEL_Y + 42.0;
    }

    /**
     * Returns the Y coordinate of the debug button at the given index,
     * stacking buttons vertically with a fixed spacing.
     *
     * @param index zero-based index of the debug button
     * @return top Y of the button at {@code index}
     */
    double getDebugButtonY(int index) {
        return debugStartY() + index * (owner.DEBUG_BUTTON_HEIGHT + owner.DEBUG_WINDOW_BUTTON_SPACING);
    }

    // Hitboxes ----------------------------------------------------------------------------

    boolean isInsideDrawCard(double x, double y) {
        double panelX = drawPanelX();
        double panelY = drawPanelY();
        double panelW = drawPanelW();
        double panelH = drawPanelH();

        double innerPad = 12.0;
        double zoneGap = 12.0;
        double zoneW = panelW - innerPad * 2.0;
        double zoneH = (panelH - innerPad * 2.0 - zoneGap - 36.0) / 2.0;

        double drawZoneX = panelX + innerPad;
        double drawZoneY = panelY + 48.0;

        return x >= drawZoneX && x <= drawZoneX + zoneW
            && y >= drawZoneY && y <= drawZoneY + zoneH;
    }

    boolean isInsideDiscardCard(double x, double y) {
        double panelX = drawPanelX();
        double panelY = drawPanelY();
        double panelW = drawPanelW();
        double panelH = drawPanelH();

        double innerPad = 12.0;
        double zoneGap = 12.0;
        double zoneW = panelW - innerPad * 2.0;
        double zoneH = (panelH - innerPad * 2.0 - zoneGap - 36.0) / 2.0;

        double drawZoneY = panelY + 48.0;
        double discardZoneY = drawZoneY + zoneH + zoneGap;
        double zoneX = panelX + innerPad;

        return x >= zoneX && x <= zoneX + zoneW
            && y >= discardZoneY && y <= discardZoneY + zoneH;
    }

    int[] getGridCellAt(double x, double y) {
        GridMetrics m = gridMetrics();

        for (int c = 0; c < owner.state.grid.cols; c++) {
            for (int r = 0; r < owner.state.grid.rows; r++) {
                double cx = m.originX() + c * (m.cardW() + m.gap());
                double cy = m.originY() + r * (m.cardH() + m.gap());

                if (x >= cx && x <= cx + m.cardW()
                && y >= cy && y <= cy + m.cardH()) {
                    return new int[]{c, r};
                }
            }
        }
        return null;
    }

    int getJokerIndexAt(double x, double y) {
        if (owner.state.jokers.isEmpty()) return -1;

        double totalW = owner.state.jokers.size() * (owner.SPECIAL_CARD_W + owner.CARD_GAP) - owner.CARD_GAP;
        double startX = jokerZoneX() + (jokerZoneW() - totalW) / 2.0;
        double cardY = bottomZoneY() + 40.0;

        for (int i = 0; i < owner.state.jokers.size(); i++) {
            double cx = startX + i * (owner.SPECIAL_CARD_W + owner.CARD_GAP);
            if (x >= cx && x <= cx + owner.SPECIAL_CARD_W
            && y >= cardY && y <= cardY + owner.SPECIAL_CARD_H) {
                return i;
            }
        }
        return -1;
    }

    int getConsumableIndexAt(double x, double y) {
        if (owner.state.consumables.isEmpty()) return -1;

        double totalW = owner.state.consumables.size() * (owner.SPECIAL_CARD_W + owner.CARD_GAP) - owner.CARD_GAP;
        double startX = consumableZoneX() + (consumableZoneW() - totalW) / 2.0;
        double cardY = bottomZoneY() + 40.0;

        for (int i = 0; i < owner.state.consumables.size(); i++) {
            double cx = startX + i * (owner.SPECIAL_CARD_W + owner.CARD_GAP);
            if (x >= cx && x <= cx + owner.SPECIAL_CARD_W
            && y >= cardY && y <= cardY + owner.SPECIAL_CARD_H) {
                return i;
            }
        }
        return -1;
    }

    boolean isInsideSettingsButton(double x, double y) {
        return x >= GameCanvas.SETTINGS_BUTTON_X
            && x <= GameCanvas.SETTINGS_BUTTON_X + GameCanvas.SETTINGS_BUTTON_SIZE
            && y >= GameCanvas.SETTINGS_BUTTON_Y
            && y <= GameCanvas.SETTINGS_BUTTON_Y + GameCanvas.SETTINGS_BUTTON_SIZE;
    }

    boolean inPanel(double x, double y) {
        return owner.settingsPanelOpen
            && x >= GameCanvas.PANEL_X
            && x <= GameCanvas.PANEL_X + GameCanvas.PANEL_W
            && y >= GameCanvas.PANEL_Y
            && y <= GameCanvas.PANEL_Y + GameCanvas.PANEL_H;
    }

    boolean isInsideLeaveButton(double x, double y) {
        return owner.settingsPanelOpen
            && x >= getLeaveButtonX()
            && x <= getLeaveButtonX() + getLeaveButtonWidth()
            && y >= getLeaveButtonY()
            && y <= getLeaveButtonY() + GameCanvas.LEAVE_BUTTON_HEIGHT;
    }

    int hitTestDebugButton(double x, double y) {
        if (!owner.settingsPanelOpen) return -1;

        double bx = getDebugButtonX();
        double bw = getDebugButtonWidth();

        for (int i = 0; i < GameCanvas.DEBUG_BUTTON_LABELS.length; i++) {
            double by = getDebugButtonY(i);

            if (x >= bx && x <= bx + bw
            && y >= by && y <= by + GameCanvas.DEBUG_BUTTON_HEIGHT) {
                return i;
            }
        }
        return -1;
    }

    



}
