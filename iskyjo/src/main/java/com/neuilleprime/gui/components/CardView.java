package com.neuilleprime.gui.components;

import com.neuilleprime.game.Card;
import com.neuilleprime.gui.utils.AssetLoader;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.scene.Cursor;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.PixelReader;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.util.Duration;

/**
 * JavaFX component that visually represents a single {@link Card}.
 * <p>
 * The view automatically reacts to changes in the underlying card's value
 * and hidden state via JavaFX property bindings:
 * <ul>
 *   <li>When hidden, the card back image is shown.</li>
 *   <li>When revealed, the front background, tinted overlay, and value label
 *       are displayed instead.</li>
 *   <li>The overlay tint colour changes based on the card's numeric value
 *       to give a quick visual cue of the card's range.</li>
 * </ul>
 * </p>
 */
public class CardView extends StackPane {

    /** The underlying game model card this view is bound to. */
    private Card cardElem;

    /** Label displaying the card's numeric value. */
    private Label valueLabel;

    /** Image shown when the card is face-down. */
    private ImageView cardBackImage;

    /** Background image shown when the card is face-up. */
    private ImageView cardFrontBg;

    /** Coloured overlay image shown on top of the front background. */
    private ImageView cardFrontOverlay;

    /**
     * Constructs a {@code CardView} bound to the given {@link Card}.
     * All images are sized to the component's preferred dimensions and all
     * property listeners are wired up immediately.
     *
     * @param card the game model card to display
     */
    public CardView(Card card) {
        this.cardElem = card;

        this.cardBackImage = new ImageView(AssetLoader.CARD_BACK);
        this.cardFrontBg = new ImageView(AssetLoader.CARD_BLANK);
        this.cardFrontOverlay = new ImageView(AssetLoader.CARD_OVERLAY_2);

        this.valueLabel = new Label();

        // bind the Card value to auto change the text
        this.valueLabel.textProperty().bind(this.cardElem.valueProperty().asString());
        
        // basically the same thing as above but with the color
        this.cardElem.valueProperty().addListener((obs, oldVal, newVal) -> {
            this.updateTint(newVal.intValue());
        });

        // same thing but for visibility
        this.cardElem.hiddenProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal) {
                this.hide();
            }
            else {
                this.show();
            }
        });

        // apply both at least once
        if (cardElem.isHidden()) {
            this.hide();
        } else {
            this.show();
        }
        this.updateTint(cardElem.getValue());
        
        // binding all sizes (text and images) to the one of cardBack
        this.cardBackImage.setPreserveRatio(true);
        this.cardBackImage.setFitWidth(Double.MAX_VALUE);
        this.cardBackImage.setFitHeight(Double.MAX_VALUE);
        this.cardBackImage.fitWidthProperty().bind(this.prefWidthProperty());
        this.cardBackImage.fitHeightProperty().bind(this.prefHeightProperty());

        this.cardFrontBg.setPreserveRatio(true);
        this.cardFrontBg.setFitWidth(Double.MAX_VALUE);
        this.cardFrontBg.setFitHeight(Double.MAX_VALUE);
        this.cardFrontBg.fitWidthProperty().bind(this.prefWidthProperty());
        this.cardFrontBg.fitHeightProperty().bind(this.prefHeightProperty());

        this.cardFrontOverlay.setPreserveRatio(true);
        this.cardFrontOverlay.setFitWidth(Double.MAX_VALUE);
        this.cardFrontOverlay.setFitHeight(Double.MAX_VALUE);
        this.cardFrontOverlay.fitWidthProperty().bind(this.prefWidthProperty());
        this.cardFrontOverlay.fitHeightProperty().bind(this.prefHeightProperty());

        this.valueLabel.styleProperty().bind(
            this.prefWidthProperty().multiply(0.3)
                .asString("-fx-font-size: %.0fpx; -fx-font-family: 'VCR OSD Mono';")
        );

        // add all the card's elements to the pane
        this.getChildren().addAll(
            this.cardBackImage,
            this.cardFrontBg,
            this.cardFrontOverlay, 
            this.valueLabel
        );
    }

    /**
     * Sets the cursor for all visual sub-components of this card view.
     *
     * @param cursor the cursor to apply
     */
    public void setCursorTo(Cursor cursor) {
        this.cardBackImage.setCursor(cursor);
        this.cardFrontBg.setCursor(cursor);
        this.cardFrontOverlay.setCursor(cursor);
        this.valueLabel.setCursor(cursor);
    }

    /**
     * Switches the card to its face-up appearance.
     */
    private void show() {
        this.cardBackImage.setVisible(false);

        this.cardFrontBg.setVisible(true);
        this.cardFrontOverlay.setVisible(true);
        this.valueLabel.setVisible(true);
    }

    /**
     * Switches the card to its face-down appearance.
     */
    private void hide() {
        this.cardBackImage.setVisible(true);

        this.cardFrontBg.setVisible(false);
        this.cardFrontOverlay.setVisible(false);
        this.valueLabel.setVisible(false);
    }

    /**
     * Updates the overlay tint and label colour to reflect the given card value.
     * <ul>
     *   <li>Negative → red</li>
     *   <li>Zero → dark red</li>
     *   <li>1–4 → green</li>
     *   <li>5–7 → blue</li>
     *   <li>8–10 → purple</li>
     *   <li>11+ → gold</li>
     * </ul>
     *
     * @param value the card value to determine the tint colour
     */
    private void updateTint(int value) {
        String color = null;

        if (value < 0) {
            color = "#da3b3b";
        } else if (value == 0) {
            color = "#3c2f2f";
        } else if (0 < value && value < 5) {
            color = "#3ca903";
        } else if (4 < value && value < 8) {
            color = "#0387a9";
        } else if (7 < value && value < 11) {
            color = "#5b1baf";
        } else if (10 < value) {
            color = "#efbf04";
        }

        double[] colorAsDouble = hexToDoubleArray(color);
        Image newImage = this.getTintedOverlay(this.cardFrontOverlay.getImage(), colorAsDouble);
        this.cardFrontOverlay.setImage(newImage);
        this.valueLabel.setTextFill(new Color(colorAsDouble[0], colorAsDouble[1], colorAsDouble[2], 1));
    }

    /**
     * Produces a new {@link WritableImage} by replacing the non-transparent
     * pixels of {@code image} with the given RGB tint while preserving alpha.
     *
     * @param image the source image to tint
     * @param tint  RGB components in the range {@code [0.0, 1.0]}
     * @return a new tinted image, or the original if it has no pixels
     */
    Image getTintedOverlay(Image image, double[] tint) {

        int width = (int) Math.round(image.getWidth());
        int height = (int) Math.round(image.getHeight());
        if (width <= 0 || height <= 0) return image;

        WritableImage tinted = new WritableImage(width, height);
        PixelReader pr = image.getPixelReader();
        PixelWriter pw = tinted.getPixelWriter();
        if (pr == null) return image;

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                Color src = pr.getColor(x, y);
                double alpha = src.getOpacity();
                if (alpha <= 0.001) {
                    pw.setColor(x, y, Color.TRANSPARENT);
                    continue;
                }
                Color out = new Color(tint[0], tint[1], tint[2], alpha);
                pw.setColor(x, y, out);
            }
        }

        return tinted;
    }

    /**
     * Converts a {@code #RRGGBB} hex colour string to a {@code double[3]} array
     * with each channel normalised to {@code [0.0, 1.0]}.
     *
     * @param hexString a colour in {@code #RRGGBB} format
     * @return array {@code [r, g, b]} with values in {@code [0.0, 1.0]}
     * @throws IllegalArgumentException if the string (after stripping {@code #}) is not exactly 6 characters
     */
    public static double[] hexToDoubleArray(String hexString) {
        hexString = hexString.replaceAll("#", "");
        
        if (hexString.length() != 6) {
            throw new IllegalArgumentException("Hex string must have exactly 6 characters for #RRGGBB format.");
        }

        double[] result = new double[3];

        for (int i = 0; i < 3; i++) {
            String hexPair = hexString.substring(i * 2, i * 2 + 2);
            
            int intValue = Integer.parseInt(hexPair, 16);
            
            result[i] = (double) intValue / 0xFF;
        }

        return result;
    }

    /**
     * Plays a shake animation on this card view.
     *
     * @param duration      the total duration of the shake effect in milliseconds
     * @param shakeStrength the maximum rotation angle in degrees
     * @param numFrames     the number of frames for one full back-and-forth cycle
     */
    public void shake(double duration, double shakeStrength, int numFrames) {
        Timeline shakeTimeline = new Timeline();

        int totalFrames = (int)(duration / (1000.0 / 60));
        double frameDuration = duration / totalFrames;

        KeyFrame scaleUp = new KeyFrame(Duration.ZERO,
            event -> {
                this.setScaleX(1.05);
                this.setScaleY(1.05);
            }
        );
        shakeTimeline.getKeyFrames().add(scaleUp);

        for (int i = 0; i < totalFrames; i++) {
            final double angle = shakeStrength * Math.sin(2 * Math.PI * i / numFrames);

            KeyFrame keyFrame = new KeyFrame(
                Duration.millis(i * frameDuration),
                event -> this.setRotate(angle)
            );
            shakeTimeline.getKeyFrames().add(keyFrame);
        }

        shakeTimeline.setOnFinished(event -> {
            this.setRotate(0);
            this.setScaleX(1.0);
            this.setScaleY(1.0);
        });

        shakeTimeline.play();
    }

    /**
     * Returns the underlying game model card this view represents.
     *
     * @return the bound {@link Card}
     */
    public Card getCardElem() {
        return this.cardElem;
    }
}
