package com.neuilleprime.gui.components;

import javafx.geometry.Pos;
import javafx.scene.control.Label;

public class TopTextView extends Label {

    private double textSize;

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

    private void updateTextSize(Number oldVal, Number newVal) {
        double labelFontSize = newVal.doubleValue() * this.textSize;
        this.setStyle(
            "-fx-font-size: " + labelFontSize + "px;" +
            "-fx-font-family: 'VCR OSD Mono';" +
            "-fx-text-fill: #1a1818;"
        );
    }
}
