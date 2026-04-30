package com.neuilleprime.gui.components;

import javafx.geometry.Pos;
import javafx.scene.control.Label;

/**
 * A self-scaling JavaFX {@link Label} used for prominent text in the top bar.
 * <p>
 * The font size is recomputed automatically whenever the component's preferred
 * width or height changes, keeping the text proportional to the available space.
 * </p>
 */
public class TopTextView extends Label {

    /**
     * Ratio used to compute the font size from the preferred height:
     * {@code fontSize = preferredHeight * textSize}.
     */
    private double textSize;

    /**
     * Constructs a {@code TopTextView} with the given text and scaling factor.
     *
     * @param text     the text to display
     * @param textSize ratio of font size to preferred height (e.g. {@code 0.3})
     */
    public TopTextView(String text, double textSize) {
        this.setText(text);
        this.textSize = textSize;
        
        this.prefWidthProperty().addListener((obs, oldVal, newVal) -> {
            this.updateTextSize(oldVal, newVal);
        });
        this.prefHeightProperty().addListener((obs, oldVal, newVal) -> {
            this.updateTextSize(oldVal, newVal);
        });

        this.setAlignment(Pos.CENTER);
    }

    /**
     * Recalculates and applies the font size based on the new preferred dimension.
     *
     * @param oldVal previous dimension (unused)
     * @param newVal new preferred dimension used for font size computation
     */
    private void updateTextSize(Number oldVal, Number newVal) {
        double labelFontSize = newVal.doubleValue() * this.textSize;
        this.setStyle(
            "-fx-font-size: " + labelFontSize + "px;" +
            "-fx-font-family: 'VCR OSD Mono';" +
            "-fx-text-fill: #1a1818;"
        );
    }
}
