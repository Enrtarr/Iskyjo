package com.neuilleprime.gui.main;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;

/**
 * JavaFX canvas used for the startup splash screen.
 */
public class SplashCanvas extends Canvas {
    private static final int FADE_IN_FRAMES = 40;
    private static final int HOLD_FRAMES = 100;
    private static final int FADE_OUT_FRAMES = 40;

    private int frame = 0;

    private final Image logo;
    private final Font mainFont;
    private final Font subFont;

    /**
     * Creates the splash canvas and prepares the logo and display fonts.
     *
     * @param baseFont the base font used to derive the splash typography
     * @param baseFontBold unused bold font placeholder kept for constructor symmetry
     */
    public SplashCanvas(Font baseFont, Font baseFontBold) {
        Image img = null;
        try {
            var url = MainGui.class.getResource("/Assets/NP_logo.png");
            if (url == null) throw new RuntimeException("Missing asset: /Assets/NP_logo.png");
            img = new Image(url.toExternalForm());
        } catch (Exception e) {
            System.out.println("Failed loading splash logo: " + e.getMessage());
        }

        logo = img;
        mainFont = Font.font(baseFont.getFamily(), FontWeight.BOLD, 42);
        subFont = Font.font(baseFont.getFamily(), FontWeight.NORMAL, 18);
    }

/**
     * Advances the splash animation by one frame.
     */
    public void nextFrame() {
        frame++;
    }

/**
     * Returns the current global opacity of the splash content.
     *
     * @return the current alpha value in the {@code [0, 1]} range
     */
    public float getAlpha() {
        if (frame <= FADE_IN_FRAMES) {
            return (float) frame / (float) FADE_IN_FRAMES;
        }
        if (frame <= FADE_IN_FRAMES + HOLD_FRAMES) {
            return 1.0f;
        }
        int fadeOutFrame = frame - FADE_IN_FRAMES - HOLD_FRAMES;
        if (fadeOutFrame <= FADE_OUT_FRAMES) {
            return 1.0f - (float) fadeOutFrame / (float) FADE_OUT_FRAMES;
        }
        return 0.0f;
    }

/**
     * Indicates whether the splash animation has fully completed.
     *
     * @return {@code true} when the splash should be removed
     */
    public boolean isFinished() {
        return frame > FADE_IN_FRAMES + HOLD_FRAMES + FADE_OUT_FRAMES;
    }

/**
     * Indicates whether the splash is currently revealing the main menu underneath.
     *
     * @return {@code true} during the final reveal phase
     */
    public boolean isMainFrameRevealPhase() {
        return frame > FADE_IN_FRAMES + HOLD_FRAMES;
    }

/**
     * Renders the current splash frame on the canvas.
     */
    public void render() {
        double w = getWidth();
        double h = getHeight();
        double cx = w / 2.0;
        double cy = h / 2.0;

        GraphicsContext canva = getGraphicsContext2D();
        canva.clearRect(0, 0, w, h);
        canva.setFill(Color.BLACK);
        canva.fillRect(0, 0, w, h);

        float alpha = Math.max(0.0f, Math.min(1.0f, getAlpha()));
        canva.setGlobalAlpha(alpha);

        double logoW = 0;
        double logoH = 0;
        if (logo != null) {
            logoW = logo.getWidth();
            logoH = logo.getHeight();
            canva.drawImage(logo, cx - logoW / 2.0, cy - logoH / 2.0);
        }

        canva.setFill(Color.WHITE);
        int spacing = 160;

        String leftMain = "Enrtarr";
        String leftSub = "Game";
        double leftMainW = measureText(leftMain, mainFont);
        double leftSubW = measureText(leftSub, subFont);
        double leftX = cx - logoW / 2.0 - spacing - Math.max(leftMainW, leftSubW);

        canva.setFont(mainFont);
        canva.fillText(leftMain, leftX, cy - 5);
        canva.setFont(subFont);
        canva.fillText(leftSub, leftX + (leftMainW - leftSubW) / 2.0, cy + 17);

        String rightMain = "Maaple";
        String rightSub = "GUI";
        double rightMainW = measureText(rightMain, mainFont);
        double rightSubW = measureText(rightSub, subFont);
        double rightX = cx + logoW / 2.0 + spacing;

        canva.setFont(mainFont);
        canva.fillText(rightMain, rightX, cy - 5);
        canva.setFont(subFont);
        canva.fillText(rightSub, rightX + (rightMainW - rightSubW) / 2.0, cy + 17);

        canva.setGlobalAlpha(1.0);
    }

/**
     * Measures the width of a text string with the given font.
     *
     * @param s the text to measure
     * @param f the font used for measurement
     * @return the text width in local coordinates
     */
    private double measureText(String s, Font f) {
        Text t = new Text(s);
        t.setFont(f);
        return t.getBoundsInLocal().getWidth();
    }
}
