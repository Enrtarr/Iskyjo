package com.neuilleprime.gui.components;

import com.neuilleprime.gui.utils.AssetLoader;

import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

public class VZoneView extends VBox {

    private Label zoneNameLabel;
    private StackPane zoneBox;
    private Label waitingLabel;

    public VZoneView(String zoneName) {
        
        this.setPickOnBounds(true);

        // text
        this.zoneNameLabel = new Label(zoneName);
        this.zoneBox = new StackPane();
        this.waitingLabel = new Label("");


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

        this.waitingLabel.styleProperty().bind(
            this.zoneBox.prefWidthProperty().multiply(0.1)
                .asString("-fx-font-size: %.0fpx; -fx-font-family: 'VCR OSD Mono';")
        );

        // styling
        this.zoneNameLabel.setAlignment(Pos.CENTER);
        this.zoneBox.setAlignment(Pos.CENTER);
        this.waitingLabel.setAlignment(Pos.CENTER);
        this.setAlignment(Pos.CENTER);

        // adding the two, in order (from top to bot)
        this.zoneBox.getChildren().add(zoneBoxTexture);
        this.zoneBox.getChildren().add(waitingLabel);
        this.getChildren().add(this.zoneNameLabel);
        this.getChildren().add(this.zoneBox);
    }

    private void updateSize(Number oldVal, Number newVal) {
        double labelFontSize = newVal.doubleValue() * .1 / this.zoneNameLabel.getText().length() * 15;
        double cardPadding = newVal.doubleValue() * .05;
        double borderRadius = newVal.doubleValue() * .05;
        double borderWidth = newVal.doubleValue() * .02;
        this.zoneNameLabel.setStyle(
            "-fx-font-size: " + labelFontSize + "px;" +
            "-fx-font-family: 'VCR OSD Mono';" +
            "-fx-text-fill: #00a6ff;"
        );
        this.zoneBox.setStyle(
            "-fx-background-color: #a92a00;" +
            "-fx-background-radius: "+borderRadius+" "+borderRadius+" "+borderRadius+" "+borderRadius+";" +
            "-fx-padding: "+cardPadding+" "+cardPadding+" "+cardPadding+" "+cardPadding+";" +
            "-fx-border-color: #7c2300;" +
            "-fx-border-width: "+borderWidth+";" +
            "-fx-border-radius: "+borderRadius+" "+borderRadius+" "+borderRadius+" "+borderRadius+";"
        );
        // this.waitingLabel.se
    }

    public void setWaiting() {
        this.waitingLabel.setText("Please click \non a card to \nreveal it");
    }
}
