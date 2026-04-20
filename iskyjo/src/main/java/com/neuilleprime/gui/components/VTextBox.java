package com.neuilleprime.gui.components;

import com.neuilleprime.gui.utils.AssetLoader;

import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

public class VTextBox extends VBox {

    private Label zoneNameLabel;
    private StackPane zoneBox;
    private Label contentLabel;
    private String nameColor;
    private String contentColor;
    private String backgroundColor;
    private String borderColor;
    private double nameSize;
    private double contentSize;

    public VTextBox(String zoneName) {
        
        this.setPickOnBounds(true);

        // text
        this.zoneNameLabel = new Label(zoneName);
        this.zoneBox = new StackPane();
        this.contentLabel = new Label("");

        this.nameColor = "#00a6ff";
        this.contentColor = "#000000";
        this.backgroundColor = "#a92a00";
        this.borderColor = "#7c2300";

        this.nameSize = .1;
        this.contentSize = .3;

        contentLabel.setWrapText(true);

        ImageView zoneBoxTexture = new ImageView(AssetLoader.CARD_TRANSPARENT);

        // dynamic size binding
        this.prefWidthProperty().addListener((obs, oldVal, newVal) -> {
            this.updateSize(oldVal, newVal);
        });
        this.prefHeightProperty().addListener((obs, oldVal, newVal) -> {
            this.updateSize(oldVal, newVal);
        });

        this.zoneNameLabel.prefHeightProperty().bind(this.prefHeightProperty().multiply(.2));
        this.zoneNameLabel.prefWidthProperty().bind(this.prefWidthProperty().multiply(1));

        this.zoneBox.prefHeightProperty().bind(this.prefHeightProperty().multiply(1));
        this.zoneBox.prefWidthProperty().bind(this.prefWidthProperty().multiply(1));

        zoneBoxTexture.setPreserveRatio(true);
        zoneBoxTexture.setFitWidth(Double.MAX_VALUE);
        zoneBoxTexture.setFitHeight(Double.MAX_VALUE);
        zoneBoxTexture.fitWidthProperty().bind(this.zoneBox.prefWidthProperty());
        zoneBoxTexture.fitHeightProperty().bind(this.zoneBox.prefHeightProperty());

        // styling
        this.zoneNameLabel.setAlignment(Pos.CENTER);
        this.zoneBox.setAlignment(Pos.CENTER);
        this.contentLabel.setAlignment(Pos.CENTER);
        this.setAlignment(Pos.CENTER);

        // adding the two, in order (from top to bot)
        this.zoneBox.getChildren().add(zoneBoxTexture);
        this.zoneBox.getChildren().add(contentLabel);
        this.getChildren().add(this.zoneNameLabel);
        this.getChildren().add(this.zoneBox);
    }

    private void updateSize(Number oldVal, Number newVal) {
        double nameLabelFontSize = newVal.doubleValue() * this.nameSize / this.zoneNameLabel.getText().length() * 15;
        boolean isContentEmpty = (this.contentLabel.getText().equals(""));
        double textLabelFontSize = newVal.doubleValue() * this.contentSize / this.contentLabel.getText().length() * 15;
        double cardPadding = newVal.doubleValue() * .05;
        double borderRadius = newVal.doubleValue() * .05;
        double borderWidth = newVal.doubleValue() * .02;
        this.zoneNameLabel.setStyle(
            "-fx-font-size: " + nameLabelFontSize + "px;" +
            "-fx-font-family: 'VCR OSD Mono';" +
            "-fx-text-fill: "+this.nameColor+";"
        );
        if (!isContentEmpty) {
            this.contentLabel.setStyle(
            "-fx-font-size: " + textLabelFontSize + "px;" +
            "-fx-font-family: 'VCR OSD Mono';" +
            "-fx-text-fill: "+this.contentColor+";"
            )
        ;}
        this.zoneBox.setStyle(
            "-fx-background-color: "+this.backgroundColor+";" +
            "-fx-background-radius: "+borderRadius+" "+borderRadius+" "+borderRadius+" "+borderRadius+";" +
            "-fx-padding: "+cardPadding+" "+cardPadding+" "+cardPadding+" "+cardPadding+";" +
            "-fx-border-color: "+this.borderColor+";" +
            "-fx-border-width: "+borderWidth+";" +
            "-fx-border-radius: "+borderRadius+" "+borderRadius+" "+borderRadius+" "+borderRadius+";"
        );
    }

    public void setText(String text) {
        this.contentLabel.setText(text);
        updateSize(this.prefWidthProperty().getValue(), this.prefWidthProperty().getValue());
    }

    public void setNameColor(String color) {
        this.nameColor = color;
    }

    public void setContentColor(String color) {
        this.contentColor = color;
    }

    public void setBackgroundColor(String color) {
        this.backgroundColor = color;
    }

    public void setBorderColor(String color) {
        this.borderColor = color;
    }

    public void setNameSize(double size) {
        this.nameSize = size;
        updateSize(this.prefWidthProperty().getValue(), this.prefWidthProperty().getValue());
    }

    public void setContentSize(double size) {
        this.contentSize = size;
        updateSize(this.prefWidthProperty().getValue(), this.prefWidthProperty().getValue());
    }
}
