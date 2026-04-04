package com.neuilleprime.gui.game;

import com.neuilleprime.gui.game.GameCanvasLayout.GridMetrics;
import com.neuilleprime.gui.model.Card;
import com.neuilleprime.gui.util.AnimBox;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.image.PixelReader;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;

/** Performs all drawing for the in-game screen. */
public final class GameCanvasRenderer {
    final GameCanvas owner;

    GameCanvasRenderer(GameCanvas owner) {
        this.owner = owner;
    }

    public void render() {
        double w = owner.getWidth();
        double h = owner.getHeight();
        if (w <= 0 || h <= 0) return;

        GraphicsContext gc = owner.getGraphicsContext2D();
        gc.clearRect(0, 0, w, h);
        gc.setGlobalAlpha(1.0);

        drawBackground(gc, w, h);
        drawDrawDiscardPanel(gc);
        drawGrid(gc);
        drawScorePanel(gc);
        drawBottomZones(gc);
        drawDraggedCard(gc);
        drawSettingsOverlay(gc);
    }

    /**
     * Draws the background layer: fills the canvas with the base colour {@code owner.BG},
     * then tiles the scrolling background pattern image (if loaded) at low opacity
     * to create an animated card-pattern wallpaper effect.
     *
     * @param gc the {@link GraphicsContext} to draw onto
     * @param w  current canvas width
     * @param h  current canvas height
     */
    void drawBackground(GraphicsContext gc, double w, double h) {
        gc.setFill(owner.BG);
        gc.fillRect(0, 0, w, h);

        if (owner.backgroundPattern != null) {
            double totalW = owner.BACKGROUND_CARD_W + owner.BACKGROUND_CARD_GAP;
            double totalH = owner.BACKGROUND_CARD_H + owner.BACKGROUND_CARD_GAP;
            double startX = -(totalW) + (owner.animTime * 60.0) % totalW;
            double startY = -(totalH) + (owner.animTime * 24.0) % totalH;

            gc.setGlobalAlpha(owner.BACKGROUND_CARD_OPACITY);
            for (double x = startX; x < w; x += totalW) {
                for (double y = startY; y < h; y += totalH) {
                    gc.drawImage(owner.backgroundPattern, x, y, owner.BACKGROUND_CARD_W, owner.BACKGROUND_CARD_H);
                }
            }
            gc.setGlobalAlpha(1.0);
        }
    }


    void drawGrid(GraphicsContext gc) {
        GridMetrics m = owner.layout.gridMetrics();
        double ox = m.originX();
        double oy = m.originY();
        double pad = 16.0;
        double panelW = m.totalW() + pad * 2.0;
        double panelH = m.totalH() + pad * 2.0;

        boolean dropIntoGridMode = owner.interactions.isDraggingDrawCard() || owner.interactions.isDraggingDiscardCard();

        gc.setFill(owner.GRID_BG);
        gc.fillRoundRect(ox - pad, oy - pad, panelW, panelH, 14, 14);

        if (dropIntoGridMode) {
            gc.setFill(Color.color(owner.ACCENT.getRed(), owner.ACCENT.getGreen(), owner.ACCENT.getBlue(), 0.10));
            gc.fillRoundRect(ox - pad - 4, oy - pad - 4, panelW + 8, panelH + 8, 16, 16);
        }

        gc.setStroke(dropIntoGridMode ? owner.ACCENT : owner.ZONE_BORDER);
        gc.setLineWidth(dropIntoGridMode ? 2.0 : 1.2);
        gc.strokeRoundRect(ox - pad, oy - pad, panelW, panelH, 14, 14);

        for (int c = 0; c < owner.state.grid.cols; c++) {
            for (int r = 0; r < owner.state.grid.rows; r++) {
                boolean hovered = (c == owner.hoverGridCol && r == owner.hoverGridRow)
                    || (c == owner.hoverDropGridCol && r == owner.hoverDropGridRow);
                boolean highlighted = owner.highlightedCells[c][r] > 0;
                drawCardSlot(
                    gc,
                    ox + c * (m.cardW() + m.gap()),
                    oy + r * (m.cardH() + m.gap()),
                    m.cardW(),
                    m.cardH(),
                    owner.state.grid.get(c, r),
                    hovered,
                    highlighted
                );
            }
        }

        if (dropIntoGridMode) {
            Font hintFont = Font.font(owner.fontBold.getFamily(), FontWeight.BOLD, 26);
            gc.setFont(hintFont);
            gc.setFill(Color.color(1, 1, 1, 0.14));
            drawCenteredText(gc, "PLACE CARD", ox - pad, oy + panelH / 2.0 + 10.0, panelW, hintFont);
        }
    }

    void drawDrawDiscardPanel(GraphicsContext gc) {
        double x = owner.layout.drawPanelX();
        double y = owner.layout.drawPanelY();
        double w = owner.layout.drawPanelW();
        double h = owner.layout.drawPanelH();

        double innerPad = 12.0;
        double zoneGap = 12.0;
        double zoneW = w - innerPad * 2.0;
        double zoneH = (h - innerPad * 2.0 - zoneGap - 36.0) / 2.0;

        double drawZoneX = x + innerPad;
        double drawZoneY = y + 48.0;
        double discardZoneX = drawZoneX;
        double discardZoneY = drawZoneY + zoneH + zoneGap;

        gc.setFill(Color.rgb(13, 20, 43, 0.88));
        gc.fillRoundRect(x, y, w, h, 18, 18);
        gc.setStroke(Color.web("#4f62a7"));
        gc.setLineWidth(1.2);
        gc.strokeRoundRect(x, y, w, h, 18, 18);

        Font titleFont = Font.font(owner.fontBold.getFamily(), FontWeight.BOLD, 16);
        Font labelFont = Font.font(owner.fontBase.getFamily(), FontWeight.NORMAL, 10);
        Font bigHintFont = Font.font(owner.fontBold.getFamily(), FontWeight.BOLD, 24);

        gc.setFont(titleFont);
        gc.setFill(Color.WHITE);
        drawCenteredText(gc, "DRAW / DISCARD", x, y + 28, w, titleFont);

        gc.setStroke(Color.web("#31457f"));
        gc.setLineWidth(1.0);
        gc.strokeLine(x + 16, y + 40, x + w - 16, y + 40);

        boolean drawActive = owner.interactions.isDraggingDrawCard() || owner.interactions.isDraggingDiscardCard() || owner.hoverDropGridCol >= 0;
        boolean discardActive = owner.interactions.isDraggingGridCard() || owner.hoverDiscardDropZone;

        gc.setFill(Color.rgb(20, 30, 60, drawActive ? 0.95 : 0.72));
        gc.fillRoundRect(drawZoneX, drawZoneY, zoneW, zoneH, 14, 14);
        gc.setStroke(drawActive ? owner.ACCENT : Color.web("#38518b"));
        gc.setLineWidth(drawActive ? 2.0 : 1.0);
        gc.strokeRoundRect(drawZoneX, drawZoneY, zoneW, zoneH, 14, 14);

        gc.setFill(Color.rgb(20, 30, 60, discardActive ? 0.95 : 0.72));
        gc.fillRoundRect(discardZoneX, discardZoneY, zoneW, zoneH, 14, 14);
        gc.setStroke(discardActive ? owner.ACCENT : Color.web("#38518b"));
        gc.setLineWidth(discardActive ? 2.0 : 1.0);
        gc.strokeRoundRect(discardZoneX, discardZoneY, zoneW, zoneH, 14, 14);

        double drawCardAreaX = drawZoneX + (zoneW - owner.SPECIAL_CARD_W) / 2.0;
        double drawCardAreaY = drawZoneY + (zoneH - owner.SPECIAL_CARD_H) / 2.0 + 6.0;
        double discardCardAreaX = discardZoneX + (zoneW - owner.SPECIAL_CARD_W) / 2.0;
        double discardCardAreaY = discardZoneY + (zoneH - owner.SPECIAL_CARD_H) / 2.0 + 6.0;

        if (discardActive) {
            gc.setFont(bigHintFont);
            gc.setFill(Color.color(1, 1, 1, owner.hoverDiscardDropZone ? 0.55 : 0.20));
            drawCenteredText(gc, "DISCARD", discardZoneX, discardZoneY + 28, zoneW, bigHintFont);
        }

        if (drawActive) {
            gc.setFont(bigHintFont);
            gc.setFill(Color.color(1, 1, 1, 0.16));
            drawCenteredText(gc, "PLACE", drawZoneX, drawZoneY + 28, zoneW, bigHintFont);
        }

        drawSideCard(gc, drawCardAreaX, drawCardAreaY, owner.SPECIAL_CARD_W, owner.SPECIAL_CARD_H, owner.state.drawPile, owner.hoverDraw || owner.interactions.isDraggingDrawCard(), "DRAW");
        drawSideCard(gc, discardCardAreaX, discardCardAreaY, owner.SPECIAL_CARD_W, owner.SPECIAL_CARD_H, owner.state.discardPile, owner.hoverDiscard || owner.hoverDiscardDropZone || owner.interactions.isDraggingDiscardCard(), "DISCARD");

        gc.setFont(labelFont);
        gc.setFill(owner.TEXT_DIM);
        drawCenteredText(gc, "DRAW", drawZoneX, drawZoneY + zoneH - 8, zoneW, labelFont);
        drawCenteredText(gc, "DISCARD", discardZoneX, discardZoneY + zoneH - 8, zoneW, labelFont);
    }

    /**
     * Draws the score panel on the right side of the play area.
     * Displays the score to beat, the current chip and multiplier values,
     * the hand name, and the hand level label.
     *
     * @param gc the {@link GraphicsContext} to draw onto
     */
    void drawScorePanel(GraphicsContext gc) {
        double x = owner.layout.scorePanelX();
        double y = owner.layout.playAreaY();
        double yPadding = y + 20;
        double w = owner.SCORE_PANEL_W;
        double h = owner.layout.playAreaH();

        gc.setFill(Color.web("#0d142b", 0.88));
        gc.fillRoundRect(x, y, w, h, 18, 18);
        gc.setStroke(Color.web("#4f62a7"));
        gc.setLineWidth(1.2);
        gc.strokeRoundRect(x, y, w, h, 18, 18);

        Font titleFont = Font.font(owner.fontBold.getFamily(), FontWeight.BOLD, 18);
        Font handFont = Font.font(owner.fontBold.getFamily(), FontWeight.BOLD, 28);
        Font labelFont = Font.font(owner.fontBase.getFamily(), FontWeight.NORMAL, 12);
        Font valueFont = Font.font(owner.fontBold.getFamily(), FontWeight.BOLD, 32);

        gc.setFill(Color.web("#222b47", 0.90));
        gc.fillRoundRect(x + 18, yPadding, 215, 95, 18, 18);
        gc.setStroke(Color.web("#4f62a7"));
        gc.setLineWidth(1);
        gc.strokeRoundRect(x + 18, yPadding, 215, 95, 18, 18);

        gc.setFont(titleFont);
        gc.setFill(owner.TEXT_DIM);
        gc.fillText("SCORE TO BEAT", x + 28, yPadding + 28);

        gc.setFont(valueFont);
        gc.setFill(owner.ACCENT);
        drawCenteredText(gc, String.valueOf(owner.state.scoreToBeat), x, yPadding + 77, owner.SCORE_PANEL_W, valueFont);

        gc.setFill(Color.web("#434855", 0.3));
        gc.fillRoundRect(x + 18, (yPadding + 92) + 20, 215, 50, 18, 18);
        gc.setStroke(Color.web("#4f62a7"));
        gc.setLineWidth(1);
        gc.strokeRoundRect(x + 18, yPadding + 112, 215, 50, 18, 18);

        gc.setFont(valueFont);
        gc.setFill(Color.web("#222b47", 0.8));
        drawCenteredText(gc, "S C O R E", x, yPadding + 112 + 38, owner.SCORE_PANEL_W, Font.font(owner.fontBold.getFamily(), FontWeight.BOLD, 36));

        gc.setFont(valueFont);
        gc.setFill(Color.WHITE);
        drawCenteredText(gc, String.valueOf(owner.state.playerScore), x, yPadding + 112 + 38, owner.SCORE_PANEL_W, valueFont);

        gc.setStroke(Color.web("#31457f"));
        gc.setLineWidth(2.0);
        gc.strokeLine(x + 18, ((yPadding + 92) + 40) + 50, x + w - 18, ((yPadding + 92) + 40) + 50);

        drawComboFeed(gc);

    }

    void drawBottomZones(GraphicsContext gc) {
        double zoneY = owner.layout.bottomZoneY();
        double zoneH = owner.layout.bottomZoneH();
        double bottomPadding = 18.0;
        double topPadding = 18.0;
        double cardY = zoneY + topPadding + 22.0;

        gc.setStroke(owner.ZONE_BORDER);
        gc.setLineWidth(1.0);

        drawBottomZoneShell(gc, owner.layout.jokerZoneX(), zoneY, owner.layout.jokerZoneW(), zoneH, Color.web("#0d0d1c"), Color.web("#4a1070"), "JOKERS", owner.JOKER_LABEL);
        drawBottomZoneShell(gc, owner.layout.consumableZoneX(), zoneY, owner.layout.consumableZoneW(), zoneH, Color.web("#0a1a12"), Color.web("#1a7044"), "CONSUMABLES", owner.CONSU_LABEL);

        if (owner.state.jokers.isEmpty()) {
            drawZoneHint(gc, owner.layout.jokerZoneX(), zoneY, owner.layout.jokerZoneW(), zoneH - bottomPadding, "No jokers", owner.JOKER_LABEL);
        } else {
            double totalW = owner.state.jokers.size() * (owner.SPECIAL_CARD_W + owner.CARD_GAP) - owner.CARD_GAP;
            double startX = owner.layout.jokerZoneX() + (owner.layout.jokerZoneW() - totalW) / 2.0;
            for (int i = 0; i < owner.state.jokers.size(); i++) {
                boolean hovered     = (i == owner.hoverJokerIndex);
                boolean highlighted = (i == owner.highlightedJokerIndex);
                drawSpecialCard(gc, startX + i * (owner.SPECIAL_CARD_W + owner.CARD_GAP), cardY,
                    owner.SPECIAL_CARD_W, owner.SPECIAL_CARD_H, owner.state.jokers.get(i),
                    owner.JOKER_BG, owner.JOKER_BORD, owner.JOKER_LABEL, "J",
                    hovered || highlighted);
            }
        }

        if (owner.state.consumables.isEmpty()) {
            drawZoneHint(gc, owner.layout.consumableZoneX(), zoneY, owner.layout.consumableZoneW(), zoneH - bottomPadding, "No consumables", owner.CONSU_LABEL);
        } else {
            double totalW = owner.state.consumables.size() * (owner.SPECIAL_CARD_W + owner.CARD_GAP) - owner.CARD_GAP;
            double startX = owner.layout.consumableZoneX() + (owner.layout.consumableZoneW() - totalW) / 2.0;
            for (int i = 0; i < owner.state.consumables.size(); i++) {
                boolean hovered     = (i == owner.hoverConsumableIndex);
                boolean highlighted = (i == owner.highlightedConsumableIndex);
                drawSpecialCard(gc, startX + i * (owner.SPECIAL_CARD_W + owner.CARD_GAP), cardY,
                    owner.SPECIAL_CARD_W, owner.SPECIAL_CARD_H, owner.state.consumables.get(i),
                    owner.CONSU_BG, owner.CONSU_BORD, owner.CONSU_LABEL, "C",
                    hovered || highlighted);
            }
        }
    }

    /**
     * Draws the background shell (rounded rectangle + border + title label)
     * for a bottom zone panel such as the joker or consumable area.
     *
     * @param gc         the {@link GraphicsContext} to draw onto
     * @param x          left X of the shell
     * @param y          top Y of the shell
     * @param w          width of the shell
     * @param h          height of the shell
     * @param bg         background fill colour
     * @param border     border stroke colour
     * @param title      label text drawn in the top-left corner of the shell
     * @param titleColor colour used for the title label
     */
    void drawBottomZoneShell(GraphicsContext gc, double x, double y, double w, double h,
                                     Color bg, Color border, String title, Color titleColor) {
        gc.setFill(bg);
        gc.fillRoundRect(x, y, w, h, 12, 12);
        gc.setStroke(border);
        gc.setLineWidth(1.0);
        gc.strokeRoundRect(x, y, w, h, 12, 12);

        Font titleFont = Font.font(owner.fontBase.getFamily(), FontWeight.NORMAL, 10);
        gc.setFont(titleFont);
        gc.setFill(titleColor);
        gc.fillText(title, x + 10, y + 16);
    }

    void drawDraggedCard(GraphicsContext gc) {
        if (owner.draggedCard == null) {
            return;
        }

        double cardW = owner.interactions.isDraggingDrawCard() ? owner.SPECIAL_CARD_W : owner.layout.gridMetrics().cardW();
        double cardH = owner.interactions.isDraggingDrawCard() ? owner.SPECIAL_CARD_H : owner.layout.gridMetrics().cardH();

        double x = owner.dragMouseX - owner.dragOffsetX;
        double y = owner.dragMouseY - owner.dragOffsetY;

        gc.save();
        gc.setGlobalAlpha(0.95);
        drawCardShape(gc, x, y, cardW, cardH, owner.draggedCard, true);
        gc.restore();
    }

    /**
     * Draws the settings overlay layer: always renders the settings (hamburger) button,
     * and additionally renders the settings panel drop-down when it is open.
     *
     * @param gc the {@link GraphicsContext} to draw onto
     */
    void drawSettingsOverlay(GraphicsContext gc) {
        drawSettingsButton(gc);
        if (owner.settingsPanelOpen) {
            drawSettingsPanel(gc);
        }
    }

    /**
     * Draws the hamburger-style settings toggle button in the top-left corner.
     * The button background and line colours respond to hover and open/closed owner.state.
     *
     * @param gc the {@link GraphicsContext} to draw onto
     */
    void drawSettingsButton(GraphicsContext gc) {
        double x = owner.SETTINGS_BUTTON_X;
        double y = owner.SETTINGS_BUTTON_Y;
        double s = owner.SETTINGS_BUTTON_SIZE;

        Color bgColor = owner.settingsPanelOpen ? owner.ACCENT
            : owner.hoverSettingsButton ? Color.web("#2a2a55")
            : Color.web("#1a1a3a");
        gc.setFill(bgColor);
        gc.fillRoundRect(x, y, s, s, 6, 6);
        gc.setStroke(owner.settingsPanelOpen || owner.hoverSettingsButton ? owner.ACCENT : owner.ZONE_BORDER);
        gc.setLineWidth(1.5);
        gc.strokeRoundRect(x, y, s, s, 6, 6);

        Color lineColor = owner.settingsPanelOpen ? owner.BG : owner.hoverSettingsButton ? owner.ACCENT : owner.TEXT_DIM;
        gc.setStroke(lineColor);
        gc.setLineWidth(2.2);
        double lx1 = x + 7;
        double lx2 = x + s - 7;
        gc.strokeLine(lx1, y + 8, lx2, y + 8);
        gc.strokeLine(lx1, y + s / 2.0, lx2, y + s / 2.0);
        gc.strokeLine(lx1, y + s - 8, lx2, y + s - 8);
    }

    /**
     * Draws the settings panel drop-down, including a drop shadow, dark background,
     * accent border, title, separator, and either the debug buttons or a
     * "Debug mode disabled" message, followed by the "Leave Game" button.
     *
     * @param gc the {@link GraphicsContext} to draw onto
     */
    void drawSettingsPanel(GraphicsContext gc) {
        double px = owner.PANEL_X;
        double py = owner.PANEL_Y;
        double pw = owner.PANEL_W;
        double ph = owner.PANEL_H;

        gc.setFill(Color.rgb(0, 0, 0, 0.5));
        gc.fillRoundRect(px + 4, py + 4, pw, ph, 12, 12);

        gc.setFill(Color.web("#14142e"));
        gc.fillRoundRect(px, py, pw, ph, 12, 12);

        gc.setStroke(owner.ACCENT);
        gc.setLineWidth(1.5);
        gc.strokeRoundRect(px, py, pw, ph, 12, 12);

        Font titleFont = Font.font(owner.fontBold.getFamily(), FontWeight.BOLD, 11);
        gc.setFont(titleFont);
        gc.setFill(owner.TEXT_DIM);
        gc.fillText("SETTINGS", px + owner.LEAVE_PAD, py + 20);

        gc.setStroke(Color.web("#2a2a55"));
        gc.setLineWidth(1.0);
        gc.strokeLine(px + owner.LEAVE_PAD, py + 28, px + pw - owner.LEAVE_PAD, py + 28);

        if (owner.debugMode) {
            drawDebugButtons(gc);
        } else {
            Font infoFont = Font.font(owner.fontBase.getFamily(), FontWeight.NORMAL, 10);
            gc.setFont(infoFont);
            gc.setFill(owner.TEXT_DIM);
            gc.fillText("Debug mode disabled", px + owner.LEAVE_PAD, py + 48);
        }

        drawLeaveButton(gc);
    }

    /**
     * Draws all debug action buttons inside the settings panel.
     * Each button is highlighted when hovered and labelled from {@link #owner.DEBUG_BUTTON_LABELS}.
     *
     * @param gc the {@link GraphicsContext} to draw onto
     */
    void drawDebugButtons(GraphicsContext gc) {
        Font bf = Font.font(owner.fontBold.getFamily(), FontWeight.BOLD, 10);
        gc.setFont(bf);

        for (int i = 0; i < owner.DEBUG_BUTTON_LABELS.length; i++) {
            double x = owner.layout.getDebugButtonX();
            double y = owner.layout.getDebugButtonY(i);
            double w = owner.layout.getDebugButtonWidth();
            double h = owner.DEBUG_BUTTON_HEIGHT;
            boolean hover = owner.hoverDebugAction == i;

            gc.setFill(hover ? Color.web("#2a2a55") : Color.web("#1b1b3f"));
            gc.fillRoundRect(x, y, w, h, 6, 6);
            gc.setStroke(hover ? owner.ACCENT : owner.ZONE_BORDER);
            gc.setLineWidth(1.2);
            gc.strokeRoundRect(x, y, w, h, 6, 6);

            gc.setFill(hover ? owner.ACCENT : Color.WHITE);
            String label = owner.DEBUG_BUTTON_LABELS[i];
            double tw = measureText(label, bf);
            gc.fillText(label, x + (w - tw) / 2.0, y + h / 2.0 + 3.5);
        }
    }

    /**
     * Draws the "← LEAVE GAME" button at the bottom of the settings panel.
     * The button background and text colour respond to hover owner.state.
     *
     * @param gc the {@link GraphicsContext} to draw onto
     */
    void drawLeaveButton(GraphicsContext gc) {
        double x = owner.layout.getLeaveButtonX();
        double y = owner.layout.getLeaveButtonY();
        double w = owner.layout.getLeaveButtonWidth();
        double h = owner.LEAVE_BUTTON_HEIGHT;

        gc.setFill(owner.hoverLeaveButton ? Color.web("#5a2130") : Color.web("#391522"));
        gc.fillRoundRect(x, y, w, h, 6, 6);
        gc.setStroke(owner.hoverLeaveButton ? Color.web("#ff9db6") : Color.web("#8a4b61"));
        gc.setLineWidth(1.2);
        gc.strokeRoundRect(x, y, w, h, 6, 6);

        Font bf = Font.font(owner.fontBold.getFamily(), FontWeight.BOLD, 10);
        gc.setFont(bf);
        gc.setFill(owner.hoverLeaveButton ? Color.WHITE : Color.web("#ffaaaa"));
        String label = "<- LEAVE GAME";
        double tw = measureText(label, bf);
        gc.fillText(label, x + (w - tw) / 2.0, y + h / 2.0 + 4.0);
    }


    // Card drawing ----------------------------------------------------------------------------

    /**
     * Returns a tinted copy of the card overlay image coloured with the given tint,
     * using a cache keyed on the RGBA components to avoid redundant pixel processing.
     * Transparent pixels in the source image are preserved as transparent in the output.
     *
     * @param tint the {@link Color} to apply as the overlay tint
     * @return the tinted {@link Image}, or {@code null} if the overlay image is unavailable
     */
    Image getTintedOverlay(Color tint) {
        if (owner.cardOverlayImg == null) return null;

        String key = String.format("%.4f-%.4f-%.4f-%.4f",
            tint.getRed(), tint.getGreen(), tint.getBlue(), tint.getOpacity());

        Image cached = owner.tintedOverlayCache.get(key);
        if (cached != null) return cached;

        int width = (int) Math.round(owner.cardOverlayImg.getWidth());
        int height = (int) Math.round(owner.cardOverlayImg.getHeight());
        if (width <= 0 || height <= 0) return owner.cardOverlayImg;

        WritableImage tinted = new WritableImage(width, height);
        PixelReader pr = owner.cardOverlayImg.getPixelReader();
        PixelWriter pw = tinted.getPixelWriter();
        if (pr == null) return owner.cardOverlayImg;

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                Color src = pr.getColor(x, y);
                double alpha = src.getOpacity();
                if (alpha <= 0.001) {
                    pw.setColor(x, y, Color.TRANSPARENT);
                    continue;
                }
                Color out = new Color(tint.getRed(), tint.getGreen(), tint.getBlue(), alpha);
                pw.setColor(x, y, out);
            }
        }

        owner.tintedOverlayCache.put(key, tinted);
        return tinted;
    }

    /**
     * Draws the tinted overlay image on top of a face-up card, clipped to the card's
     * rounded-rectangle bounds. Does nothing if the overlay image is missing,
     * the card is {@code null}, or the card is face-down.
     *
     * @param gc   the {@link GraphicsContext} to draw onto
     * @param x    left X of the card
     * @param y    top Y of the card
     * @param w    card width
     * @param h    card height
     * @param card the card whose value drives the overlay tint colour
     */
    void drawCardOverlay(GraphicsContext gc, double x, double y, double w, double h, Card card) {
        if (owner.cardOverlayImg == null || card == null || !card.faceUp) return;

        Image tintedOverlay = getTintedOverlay(owner.layout.cardOverlayTint(card.value));
        if (tintedOverlay == null) return;

        gc.save();
        clipRoundedRect(gc, x, y, w, h, owner.CARD_RADIUS);
        gc.drawImage(tintedOverlay, x, y, w, h);
        gc.restore();
    }

    /**
     * Draws the visual shape of a single card at the given position and size.
     * Face-down cards show the card-back image (or a fallback pattern) with a coloured border.
     * Face-up cards show a light front, the tinted overlay, the centred value in the
     * appropriate colour, and small corner labels.
     *
     * @param gc          the {@link GraphicsContext} to draw onto
     * @param x           left X of the card
     * @param y           top Y of the card
     * @param w           card width
     * @param h           card height
     * @param card        the {@link Card} to render
     * @param highlighted {@code true} to draw the accent-coloured border (hover or selected)
     */
    void drawCardShape(GraphicsContext gc, double x, double y, double w, double h,
                               Card card, boolean highlighted) {
        if (!card.faceUp) {
            if (owner.cardBackImg != null) {
                gc.save();
                clipRoundedRect(gc, x, y, w, h, owner.CARD_RADIUS);
                gc.drawImage(owner.cardBackImg, x, y, w, h);
                gc.restore();
            } else {
                gc.setFill(owner.CARD_BACK);
                gc.fillRoundRect(x, y, w, h, owner.CARD_RADIUS, owner.CARD_RADIUS);
                gc.setFill(Color.web("#152c5e"));
                gc.fillRoundRect(x + 5, y + 5, w - 10, h - 10, 5, 5);
            }

            gc.setStroke(highlighted ? owner.ACCENT : Color.web("#3355aa"));
            gc.setLineWidth(highlighted ? 2.5 : 1.5);
            gc.strokeRoundRect(x, y, w, h, owner.CARD_RADIUS, owner.CARD_RADIUS);
            return;
        }

        gc.setFill(owner.CARD_FRONT);
        gc.fillRoundRect(x, y, w, h, owner.CARD_RADIUS, owner.CARD_RADIUS);
        drawCardOverlay(gc, x, y, w, h, card);

        gc.setStroke(highlighted ? owner.ACCENT : Color.web("#cccccc"));
        gc.setLineWidth(highlighted ? 2.5 : 1.2);
        gc.strokeRoundRect(x, y, w, h, owner.CARD_RADIUS, owner.CARD_RADIUS);

        String label = String.valueOf(card.value);
        Color valueColor = GameCanvasLayout.cardValueColor(card.value);
        Font valueFont = Font.font(owner.fontBold.getFamily(), FontWeight.BOLD, 32);
        gc.setFont(valueFont);

        AnimBox.CardFlipAnimation flipAnimation = owner.activeFlipAnimations.get(card);
        double textScaleX = 1.0;
        double textAlpha = 1.0;

        if (flipAnimation != null) {
            textScaleX = Math.max(0.05, flipAnimation.getScaleX());
            textAlpha = Math.max(0.0, Math.min(1.0, flipAnimation.getScaleX() * 1.4));
        }

        double tw = measureText(label, valueFont);
        double scaledTextW = tw * textScaleX;
        double textX = x + (w - scaledTextW) / 2.0;
        double textY = y + h / 2.0 + 11.0;

        gc.save();
        gc.setGlobalAlpha(textAlpha);
        gc.translate(x + w / 2.0, 0);
        gc.scale(textScaleX, 1.0);
        gc.translate(-(x + w / 2.0), 0);
        gc.setFill(valueColor);
        gc.fillText(label, textX, textY);
        gc.restore();
    }

    /**
     * Overload without the explicit {@code highlighted} parameter.
     * Delegates to {@link #drawCardSlot(GraphicsContext, double, double, double, double, Card, boolean, boolean)}
     * with {@code highlighted = false}.
     */
    void drawCardSlot(GraphicsContext gc, double x, double y,
                              double w, double h, Card card, boolean hovered) {
        drawCardSlot(gc, x, y, w, h, card, hovered, false);
    }

    /**
     * Draws a single card slot in the grid, applying hover, selection, flip, and highlight
     * scale effects.  When {@code highlighted} is {@code true}, the card is scaled up
     * slightly and its border glows in the accent colour, independently of the hover state.
     *
     * @param gc          the {@link GraphicsContext} to draw onto
     * @param x           left X of the slot
     * @param y           top Y of the slot
     * @param w           nominal slot width
     * @param h           nominal slot height
     * @param card        the {@link Card} to draw, or {@code null} for an empty slot
     * @param hovered     {@code true} if the mouse is currently over this slot
     * @param highlighted {@code true} if this card belongs to an active combo highlight
     */
    void drawCardSlot(GraphicsContext gc, double x, double y,
                              double w, double h, Card card, boolean hovered, boolean highlighted) {
        if (card == null) {
            double scale = (hovered || highlighted) ? 1.06 : 1.0;
            double nw = w * scale;
            double nh = h * scale;
            double nx = x + (w - nw) / 2.0;
            double ny = y + (h - nh) / 2.0;

            gc.setFill(owner.CARD_EMPTY);
            gc.fillRoundRect(nx, ny, nw, nh, owner.CARD_RADIUS, owner.CARD_RADIUS);
            gc.setStroke((hovered || highlighted) ? owner.ACCENT : Color.web("#2a2a48"));
            gc.setLineWidth((hovered || highlighted) ? 1.8 : 1.0);
            gc.strokeRoundRect(nx, ny, nw, nh, owner.CARD_RADIUS, owner.CARD_RADIUS);
            return;
        }

        boolean effectiveHighlight = card.selected || hovered || highlighted;
        double scale = card.selected ? (hovered ? 1.12 : 1.08)
                     : highlighted  ? 1.10
                     : hovered      ? 1.06
                     : 1.0;
        double nw = w * scale;
        double nh = h * scale;
        double shakeX = hovered && !card.selected ? Math.sin(owner.animTime * 6.0) * 1.5 : 0.0;
        double shakeY = hovered && !card.selected ? Math.cos(owner.animTime * 5.0) * 1.2 : 0.0;
        double nx = x + (w - nw) / 2.0 + shakeX;
        double ny = y + (h - nh) / 2.0 + shakeY;

        if (owner.interactions.isDraggingGridCard() && card == owner.draggedCard) {
            gc.setFill(owner.CARD_EMPTY);
            gc.fillRoundRect(x, y, w, h, owner.CARD_RADIUS, owner.CARD_RADIUS);
            gc.setStroke(Color.web("#2a2a48"));
            gc.setLineWidth(1.0);
            gc.strokeRoundRect(x, y, w, h, owner.CARD_RADIUS, owner.CARD_RADIUS);
            return;
        }

        AnimBox.CardFlipAnimation flipAnimation = owner.activeFlipAnimations.get(card);
        if (flipAnimation != null) {
            double flipScaleX = Math.max(0.06, flipAnimation.getScaleX());
            double flippedW = nw * flipScaleX;
            double flippedX = nx + (nw - flippedW) / 2.0;
            drawCardShape(gc, flippedX, ny, flippedW, nh, card, effectiveHighlight);
        } else {
            drawCardShape(gc, nx, ny, nw, nh, card, effectiveHighlight);
        }
    }

    /**
     * Draws a side card (draw pile or discard pile) within a given bounding box.
     * Applies a hover scale effect and reduces opacity slightly when not hovered.
     * If no card is provided, renders an empty placeholder with a centred tag label.
     *
     * @param gc      the {@link GraphicsContext} to draw onto
     * @param x       left X of the bounding box
     * @param y       top Y of the bounding box
     * @param w       bounding box width
     * @param h       bounding box height
     * @param topCard the top {@link Card} to display, or {@code null} for an empty pile
     * @param hover   {@code true} if the cursor is over this card
     * @param tag     fallback label shown when the pile is empty (e.g. {@code "DRAW"})
     */
    void drawSideCard(GraphicsContext gc, double x, double y, double w, double h,
                              Card topCard, boolean hover, String tag) {
        double scale = hover ? 1.06 : 1.0;
        double dw = w * scale;
        double dh = h * scale;
        double dx = x + (w - dw) / 2.0;
        double dy = y + (h - dh) / 2.0;

        gc.setGlobalAlpha(hover ? 1.0 : 0.90);
        if (topCard != null) {
            drawCardShape(gc, dx, dy, dw, dh, topCard, hover);
        } else {
            gc.setFill(owner.CARD_EMPTY);
            gc.fillRoundRect(dx, dy, dw, dh, owner.CARD_RADIUS, owner.CARD_RADIUS);
            gc.setStroke(hover ? owner.ACCENT : owner.ZONE_BORDER);
            gc.setLineWidth(1.5);
            gc.strokeRoundRect(dx, dy, dw, dh, owner.CARD_RADIUS, owner.CARD_RADIUS);

            Font tf = Font.font(owner.fontBold.getFamily(), FontWeight.BOLD, 9);
            gc.setFont(tf);
            gc.setFill(owner.TEXT_DIM);
            drawCenteredText(gc, tag, dx, dy + dh / 2.0 + 5, dw, tf);
        }
        gc.setGlobalAlpha(1.0);
    }

    /**
     * Draws a special card (joker or consumable) with custom colours and a symbol label.
     * Applies hover scaling and an accent glow halo when hovered or selected.
     *
     * @param gc         the {@link GraphicsContext} to draw onto
     * @param x          left X of the card bounding box
     * @param y          top Y of the card bounding box
     * @param w          bounding box width
     * @param h          bounding box height
     * @param card       the {@link Card} to render (used for selected owner.state)
     * @param bg         background fill colour of the card face
     * @param border     border stroke colour
     * @param labelColor colour of the centred symbol when not hovered/selected
     * @param symbol     the text symbol displayed on the card face (e.g. {@code "J"} or {@code "C"})
     * @param hovered    {@code true} if the cursor is currently over this card
     */
    void drawSpecialCard(GraphicsContext gc, double x, double y, double w, double h,
                                 Card card, Color bg, Color border, Color labelColor,
                                 String symbol, boolean hovered) {
        boolean selected = card.selected;
        double scale = hovered ? 1.06 : 1.0;
        double nw = w * scale;
        double nh = h * scale;
        double nx = x + (w - nw) / 2.0;
        double ny = y + (h - nh) / 2.0;

        if (selected || hovered) {
            gc.setFill(Color.color(owner.ACCENT.getRed(), owner.ACCENT.getGreen(), owner.ACCENT.getBlue(), hovered ? 0.16 : 0.20));
            gc.fillRoundRect(nx - 5, ny - 5, nw + 10, nh + 10, owner.CARD_RADIUS + 3, owner.CARD_RADIUS + 3);
        }

        gc.setFill(bg);
        gc.fillRoundRect(nx, ny, nw, nh, owner.CARD_RADIUS, owner.CARD_RADIUS);
        gc.setStroke((selected || hovered) ? owner.ACCENT : border);
        gc.setLineWidth((selected || hovered) ? 2.5 : 1.5);
        gc.strokeRoundRect(nx, ny, nw, nh, owner.CARD_RADIUS, owner.CARD_RADIUS);

        Font sf = Font.font(owner.fontBold.getFamily(), FontWeight.BOLD, 28);
        gc.setFont(sf);
        gc.setFill((selected || hovered) ? owner.ACCENT : labelColor);
        double tw = measureText(symbol, sf);
        gc.fillText(symbol, nx + (nw - tw) / 2.0, ny + nh / 2.0 + 10.0);
    }

    /**
     * Applies a rounded-rectangle clipping path to the given {@link GraphicsContext}.
     * Subsequent draw calls will be clipped to this region until {@link GraphicsContext#restore()}
     * is called. Uses quadratic Bézier curves to form the rounded corners.
     *
     * @param gc     the {@link GraphicsContext} to clip
     * @param x      left X of the clip rectangle
     * @param y      top Y of the clip rectangle
     * @param w      width of the clip rectangle
     * @param h      height of the clip rectangle
     * @param radius corner radius in pixels
     */
    void clipRoundedRect(GraphicsContext gc, double x, double y, double w, double h, double radius) {
        gc.beginPath();
        gc.moveTo(x + radius, y);
        gc.lineTo(x + w - radius, y);
        gc.quadraticCurveTo(x + w, y, x + w, y + radius);
        gc.lineTo(x + w, y + h - radius);
        gc.quadraticCurveTo(x + w, y + h, x + w - radius, y + h);
        gc.lineTo(x + radius, y + h);
        gc.quadraticCurveTo(x, y + h, x, y + h - radius);
        gc.lineTo(x, y + radius);
        gc.quadraticCurveTo(x, y, x + radius, y);
        gc.closePath();
        gc.clip();
    }

    /**
     * Draws the animated combo feed inside the score panel.
     *
     * <p>Each {@link ComboEntry} in {@link GameCanvasAnimations#comboFeed} is
     * rendered as a single line of text.  The newest entry (slot 0) appears just
     * below the score separator line, is fully white, and slides in from the
     * right edge of the panel.  Older entries are pushed down, progressively
     * dimmed, and rendered in a smaller font until they fade out entirely.</p>
     *
     * @param gc the {@link GraphicsContext} to draw onto
     */
    void drawComboFeed(GraphicsContext gc) {
        if (owner.animations.comboFeed.isEmpty()) return;

        double panelX = owner.layout.scorePanelX();
        double panelW = owner.SCORE_PANEL_W;

        // Y just below the separator drawn in drawScorePanel()
        double yPadding   = owner.layout.playAreaY() + 20.0;
        double separatorY = ((yPadding + 92) + 40) + 50;
        double feedStartY = separatorY + 40.0;

        double slotH      = 44.0;
        double baseFontSz = 16.0;

        for (ComboEntry entry : owner.animations.comboFeed) {
            double opacity = entry.computeOpacity();
            if (opacity <= 0.01) continue;

            double fontSize = entry.computeFontSize(baseFontSz);
            double slotY    = feedStartY + entry.slotIndex * slotH;

            // Slide-in: ease from right edge toward centre
            double ease      = easeOutQuad(entry.slideProgress);
            double slideOffX = (1.0 - ease) * (panelW * 0.6);

            Font entryFont = Font.font(
                owner.fontBase.getFamily(),
                FontWeight.BOLD,
                fontSize
            );

            gc.save();
            gc.setGlobalAlpha(opacity);
            gc.setFont(entryFont);
            gc.setFill(entry.slotIndex == 0
                ? Color.WHITE
                : owner.TEXT_DIM);

            String label = entry.displayLabel();
            double textW = measureText(label, entryFont);

            // Centre within the panel, shifted right during slide-in
            double textX = panelX + (panelW - textW) / 2.0 + slideOffX;
            // Clamp so text never slides fully off the left edge
            textX = Math.max(panelX + 10.0, textX);

            gc.fillText(label, textX, slotY);
            gc.restore();
        }
    }

    /**
     * Ease-out quadratic function used to smooth the combo-entry slide-in animation.
     *
     * @param t input progress in {@code [0.0, 1.0]}
     * @return eased output value in {@code [0.0, 1.0]}
     */
    private double easeOutQuad(double t) {
        return 1.0 - (1.0 - t) * (1.0 - t);
    }

    // Toolbox ----------------------------------------------------------------------------

    /**
     * Draws a faint centred hint message inside a zone, typically used when
     * the zone is empty (e.g. "No jokers"). The text is rendered at low opacity
     * using the provided colour.
     *
     * @param gc    the {@link GraphicsContext} to draw onto
     * @param x     left X of the zone bounding box
     * @param y     top Y of the zone bounding box
     * @param w     width of the zone bounding box
     * @param h     height of the zone bounding box
     * @param msg   the hint message to display
     * @param color the base colour of the hint text (drawn at 35% opacity)
     */
    void drawZoneHint(GraphicsContext gc, double x, double y, double w, double h,
                              String msg, Color color) {
        Font f = Font.font(owner.fontBase.getFamily(), FontWeight.NORMAL, 10);
        gc.setFont(f);
        gc.setFill(Color.color(color.getRed(), color.getGreen(), color.getBlue(), 0.35));
        double tw = measureText(msg, f);
        gc.fillText(msg, x + (w - tw) / 2.0, y + h / 2.0 + 5.0);
    }

    /**
     * Draws a string centred horizontally within the given width at the specified Y position.
     * The font must be set on the {@link GraphicsContext} fill colour before calling this method,
     * as it only sets the font and position.
     *
     * @param gc    the {@link GraphicsContext} to draw onto
     * @param text  the string to render
     * @param x     left X of the container used for centering
     * @param y     baseline Y of the text
     * @param width width of the container used for centering
     * @param font  the {@link Font} to apply before drawing
     */
    void drawCenteredText(GraphicsContext gc, String text, double x, double y,
                                  double width, Font font) {
        double tw = measureText(text, font);
        gc.setFont(font);
        gc.fillText(text, x + (width - tw) / 2.0, y);
    }

    /**
     * Measures and returns the rendered pixel width of a string in the given font.
     * Uses a temporary off-screen {@link Text} node to obtain accurate layout bounds.
     *
     * @param s the string to measure
     * @param f the {@link Font} to use for measurement
     * @return the width of the string in pixels
     */
    double measureText(String s, Font f) {
        Text t = new Text(s);
        t.setFont(f);
        return t.getBoundsInLocal().getWidth();
    }

}