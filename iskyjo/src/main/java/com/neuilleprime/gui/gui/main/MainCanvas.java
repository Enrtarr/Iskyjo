package com.neuilleprime.gui.main;

import com.neuilleprime.gui.util.DiscordRpc;

import javafx.animation.AnimationTimer;
import javafx.application.Platform;
import javafx.scene.Cursor;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

/**
 * JavaFX canvas used for the main menu screen.
 * It draws the animated background, title, and menu buttons.
 */
public class MainCanvas extends Canvas {
    private static final int MENU_BUTTON_W = 240;
    private static final int MENU_BUTTON_H = 65;
    private static final int MENU_BUTTON_GAP = 70;
    private static final int CARD_WIDTH = 82;
    private static final int CARD_HEIGHT = 114;
    private static final int SPACING = 20;
    private static final float ANIMATION_SPEED = 1.5f;
    private static final float CARD_OPACITY_BASE = 0.05f;
    private static final int BUTTON_HIT_SIZE = 28;
    private static final int BUTTON_TOP = 8;
    private static final int BUTTON_RIGHT = 10;
    private static final int BUTTON_GAP = 6;

    private static final Color BG_BASE = Color.web("#1d2b53");
    private static final Color BG_PLAY = Color.web("#1e532c");
    private static final Color BG_LEADERBOARD = Color.web("#534b1f");

    private final Stage stage;
    private final Font fontBase;
    private final Font fontBaseBold;
    private final Runnable onPlay;

    private AnimationTimer mainTimer;
    private int mainCounter = 0;

    private float revealProgress = 0.0f;
    private float backgroundAlpha = 0.0f;
    private float titleAlpha = 0.0f;
    private float buttonsAlpha = 0.0f;

    private Image cardPattern;
    private Image buttonPlayImg;
    private Image buttonLeaderboardImg;

    private boolean hoverPlay = false;
    private boolean hoverLeaderboard = false;
    private boolean mainAnimSkipped = false;

    private double playHoverMix = 0.0;
    private double leaderboardHoverMix = 0.0;

    /**
     * Creates the main menu canvas and wires its assets and interactions.
     *
     * @param stage the owning application stage
     * @param fontBase the base font used for regular labels
     * @param fontBaseBold the bold variant used for titles and emphasis
     * @param onPlay callback invoked when the Play button is clicked
     */
    public MainCanvas(Stage stage, Font fontBase, Font fontBaseBold, Runnable onPlay) {
        this.stage = stage;
        this.fontBase = fontBase;
        this.fontBaseBold = fontBaseBold;
        this.onPlay = onPlay;

        loadAssets();
        initInteraction();

        widthProperty().addListener(o -> render());
        heightProperty().addListener(o -> render());
    }

/**
     * Immediately completes the menu entrance animation and shows all UI elements.
     */
    public void skipMainAnim() {
        if (mainAnimSkipped) return;
        mainAnimSkipped = true;
        backgroundAlpha = 1.0f;
        titleAlpha = 1.0f;
        buttonsAlpha = 1.0f;
        render();
    }

/**
     * Starts the main menu animation loop if it is not already running.
     */
    public void startMain() {
        if (mainTimer != null) return;

        final long[] last = {0L};
        final long frameNs = 25_000_000L;

        mainTimer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                if (now - last[0] < frameNs) return;
                last[0] = now;
                mainCounter++;

                if (!mainAnimSkipped) {
                    backgroundAlpha = Math.min(1.0f, mainCounter / 40.0f);
                    titleAlpha = Math.max(0.0f, Math.min(1.0f, (mainCounter - 16) / 34.0f));
                    buttonsAlpha = Math.max(0.0f, Math.min(1.0f, (mainCounter - 40) / 30.0f));
                }

                playHoverMix = approach(playHoverMix, hoverPlay ? 1.0 : 0.0, 0.14);
                leaderboardHoverMix = approach(leaderboardHoverMix, hoverLeaderboard ? 1.0 : 0.0, 0.14);
                render();
            }
        };
        mainTimer.start();
    }

/**
     * Stops the menu animation loop while another screen is displayed.
     */
    public void pauseForGame() {
        if (mainTimer != null) {
            mainTimer.stop();
            mainTimer = null;
        }
    }

/**
     * Resets all menu animation state and restarts the main menu sequence.
     */
    public void restartMain() {
        pauseForGame();

        mainCounter = 0;
        backgroundAlpha = 0.0f;
        titleAlpha = 0.0f;
        buttonsAlpha = 0.0f;
        mainAnimSkipped = false;
        hoverPlay = false;
        hoverLeaderboard = false;
        playHoverMix = 0.0;
        leaderboardHoverMix = 0.0;

        render();
        startMain();
    }

/**
     * Updates the reveal progress used while transitioning from the splash screen.
     *
     * @param v reveal amount in the {@code [0, 1]} range
     */
    public void setRevealProgress(float v) {
        this.revealProgress = clamp(v);
        render();
    }

/**
     * Loads the background and button images used by the main menu.
     */
    private void loadAssets() {
        try {
            var url = MainGui.class.getResource("/Assets/Cards/card_bg.png");
            if (url == null) throw new RuntimeException("Missing asset: /Assets/Cards/card_bg.png");
            cardPattern = new Image(url.toExternalForm());
        } catch (Exception e) {
            System.out.println("Failed loading background cards: " + e.getMessage());
        }
        try {
            var url = MainGui.class.getResource("/Assets/Buttons/button_play.png");
            if (url == null) throw new RuntimeException("Missing asset: /Assets/Buttons/button_play.png");
            buttonPlayImg = new Image(url.toExternalForm());
        } catch (Exception e) {
            System.out.println("Failed loading play button: " + e.getMessage());
        }
        try {
            var url = MainGui.class.getResource("/Assets/Buttons/button_leaderboard.png");
            if (url == null) throw new RuntimeException("Missing asset: /Assets/Buttons/button_leaderboard.png");
            buttonLeaderboardImg = new Image(url.toExternalForm());
        } catch (Exception e) {
            System.out.println("Failed loading leaderboard button: " + e.getMessage());
        }
    }

    // Interaction ----------------------------------------------------------------------------

/**
     * Registers mouse interactions for the main menu buttons and window controls.
     */
    private void initInteraction() {
        setOnMouseMoved(e -> {
            double x = e.getX();
            double y = e.getY();

            boolean mouseInPlay = inPlayBounds(x, y);
            boolean mouseInLeaderboard = inLeaderboardBounds(x, y);

            if (mouseInPlay != hoverPlay
                || mouseInLeaderboard != hoverLeaderboard) {
                hoverPlay = mouseInPlay;
                hoverLeaderboard = mouseInLeaderboard;
                render();
            }

            setCursor((mouseInPlay || mouseInLeaderboard) ? Cursor.HAND : Cursor.DEFAULT);
        });

        setOnMouseExited(e -> {
            hoverPlay = false;
            hoverLeaderboard = false;
            render();
        });

        setOnMousePressed(e -> {
            double x = e.getX();
            double y = e.getY();
        });


        setOnMouseClicked(e -> {
            double x = e.getX();
            double y = e.getY();

            if (!mainAnimSkipped && buttonsAlpha < 1.0f) {
                skipMainAnim();
                return;
            }

            if (inMinBounds(x, y)) {
                stage.setIconified(true);
            } else if (inCloseBounds(x, y)) {
                DiscordRpc.shutdown();
                Platform.exit();
            } else if (inPlayBounds(x, y)) {
                onPlay.run();
            } else if (inLeaderboardBounds(x, y)) {
                System.out.println("Leaderboard clicked");
            }
        });
    }

    // Hitboxes ----------------------------------------------------------------------------

    private double closeX() {
        return getWidth() - BUTTON_RIGHT - BUTTON_HIT_SIZE;
    }

    private double minX() {
        return getWidth() - BUTTON_RIGHT - BUTTON_HIT_SIZE - BUTTON_GAP - BUTTON_HIT_SIZE;
    }

    private boolean inCloseBounds(double x, double y) {
        return x >= closeX() && x <= closeX() + BUTTON_HIT_SIZE
            && y >= BUTTON_TOP && y <= BUTTON_TOP + BUTTON_HIT_SIZE;
    }

    private boolean inMinBounds(double x, double y) {
        return x >= minX() && x <= minX() + BUTTON_HIT_SIZE
            && y >= BUTTON_TOP && y <= BUTTON_TOP + BUTTON_HIT_SIZE;
    }

    private boolean inPlayBounds(double x, double y) {
        double totalW = MENU_BUTTON_W * 2 + MENU_BUTTON_GAP;
        double startX = (getWidth() - totalW) / 2.0;
        double startY = getHeight() / 2.0 + 80;
        return x >= startX && x <= startX + MENU_BUTTON_W
            && y >= startY && y <= startY + MENU_BUTTON_H;
    }

    private boolean inLeaderboardBounds(double x, double y) {
        double totalW = MENU_BUTTON_W * 2 + MENU_BUTTON_GAP;
        double startX = (getWidth() - totalW) / 2.0 + MENU_BUTTON_W + MENU_BUTTON_GAP;
        double startY = getHeight() / 2.0 + 80;
        return x >= startX && x <= startX + MENU_BUTTON_W
            && y >= startY && y <= startY + MENU_BUTTON_H;
    }

    // Render ----------------------------------------------------------------------------

/**
     * Renders the full main menu frame on the canvas.
     */
    public void render() {
        double w = getWidth();
        double h = getHeight();
        double cx = w / 2.0;
        double cy = h / 2.0;

        GraphicsContext canva = getGraphicsContext2D();
        canva.clearRect(0, 0, w, h);

        canva.setFill(BG_BASE);
        canva.fillRect(0, 0, w, h);

        Color playOverlay = blend(Color.TRANSPARENT, BG_PLAY, playHoverMix * 0.75);
        Color leaderboardOverlay = blend(Color.TRANSPARENT, BG_LEADERBOARD, leaderboardHoverMix * 0.75);
        drawColorOverlay(canva, playOverlay, w, h);
        drawColorOverlay(canva, leaderboardOverlay, w, h);

        float finalBackgroundAlpha = backgroundAlpha * revealProgress;
        drawPatternBackground(canva, w, h, finalBackgroundAlpha);

        float finalTitleAlpha = titleAlpha * revealProgress;
        if (finalTitleAlpha > 0.001f) {
            double scale = 0.92 + 0.08 * finalTitleAlpha;
            Font titleFont = Font.font(fontBaseBold.getFamily(), FontWeight.BOLD, 120);
            String title = "Iskyjo";
            double titleW = measureText(title, titleFont);

            canva.save();
            canva.translate(cx, cy);
            canva.rotate(Math.toDegrees(Math.sin(mainCounter * 0.03) * 0.025));
            canva.scale(scale, scale);
            canva.setGlobalAlpha(finalTitleAlpha);
            canva.setFill(Color.rgb(255, 200, 0));
            canva.setFont(titleFont);
            canva.fillText(title, -titleW / 2.0, 0);
            canva.restore();
        }


        float finalButtonsAlpha = buttonsAlpha * revealProgress;
        if (finalButtonsAlpha > 0.001f) {
            double totalW = MENU_BUTTON_W * 2 + MENU_BUTTON_GAP;
            double startX = (w - totalW) / 2.0;
            double startY = h / 2.0 + 80;

            drawMenuButton(canva, startX, startY, MENU_BUTTON_W, MENU_BUTTON_H,
                buttonPlayImg, hoverPlay, finalButtonsAlpha);
            drawMenuButton(canva, startX + MENU_BUTTON_W + MENU_BUTTON_GAP, startY, MENU_BUTTON_W, MENU_BUTTON_H,
                buttonLeaderboardImg, hoverLeaderboard, finalButtonsAlpha);
        }
    }

    // Drawing helpers ----------------------------------------------------------------------------

    private void drawPatternBackground(GraphicsContext canva, double w, double h, float alpha) {
        if (cardPattern == null || alpha <= 0.001f) return;

        double totalW = CARD_WIDTH + SPACING;
        double totalH = CARD_HEIGHT + SPACING;
        double startX = -(totalW) + (mainCounter * ANIMATION_SPEED) % totalW;
        double startY = -(totalH) + (mainCounter * ANIMATION_SPEED * 0.4f) % totalH;

        canva.setGlobalAlpha(clamp(CARD_OPACITY_BASE * alpha));
        for (double x = startX; x < w; x += totalW) {
            for (double y = startY; y < h; y += totalH) {
                canva.drawImage(cardPattern, x, y, CARD_WIDTH, CARD_HEIGHT);
            }
        }
        canva.setGlobalAlpha(1.0);
    }

    private void drawColorOverlay(GraphicsContext canva, Color c, double w, double h) {
        double a = c.getOpacity();
        if (a < 0.01) return;
        canva.setGlobalAlpha(a);
        canva.setFill(Color.color(c.getRed(), c.getGreen(), c.getBlue()));
        canva.fillRect(0, 0, w, h);
        canva.setGlobalAlpha(1.0);
    }

    private void drawMenuButton(GraphicsContext canva, double rx, double ry, double rw, double rh,
                                Image img, boolean hover, float alpha) {
        double finalAlpha = clamp(alpha);
        double scale = hover ? 1.06 : 1.0;
        double drawW = rw * scale;
        double drawH = rh * scale;
        double drawX = rx + (rw - drawW) / 2.0;
        double drawY = ry + (rh - drawH) / 2.0;

        canva.setGlobalAlpha(finalAlpha);
        if (img != null) {
            canva.drawImage(img, drawX, drawY, drawW, drawH);
        } else {
            canva.setFill(Color.rgb(255, 200, 0, 200 / 255.0));
            canva.fillRoundRect(drawX, drawY, drawW, drawH, 12, 12);
            canva.setStroke(Color.rgb(255, 200, 0));
            canva.setLineWidth(2);
            canva.strokeRoundRect(drawX, drawY, drawW, drawH, 12, 12);
        }
        canva.setGlobalAlpha(1.0);
    }

    private Color blend(Color from, Color to, double t) {
        double k = clamp(t);
        return new Color(
            from.getRed() + (to.getRed() - from.getRed()) * k,
            from.getGreen() + (to.getGreen() - from.getGreen()) * k,
            from.getBlue() + (to.getBlue() - from.getBlue()) * k,
            from.getOpacity() + (to.getOpacity() - from.getOpacity()) * k
        );
    }

    private double measureText(String s, Font f) {
        Text t = new Text(s);
        t.setFont(f);
        return t.getBoundsInLocal().getWidth();
    }

    private float clamp(float v) {
        return Math.max(0f, Math.min(1f, v));
    }

    private double clamp(double v) {
        return Math.max(0.0, Math.min(1.0, v));
    }

    private double approach(double value, double target, double step) {
        if (value < target) return Math.min(target, value + step);
        return Math.max(target, value - step);
    }
}
