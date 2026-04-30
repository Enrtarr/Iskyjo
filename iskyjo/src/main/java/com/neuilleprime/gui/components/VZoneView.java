package com.neuilleprime.gui.components;

import com.neuilleprime.gui.utils.AssetLoader;

import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

/**
 * A titled, coloured drop-zone component used as a drag-and-drop target.
 * <p>
 * Structurally identical to {@link VTextBox} but intended specifically as a
 * drop target (e.g. the discard zone on the game screen or the buy/sell zones
 * on the shop screen). All colours, font sizes, and padding scale with the
 * component's preferred dimensions.
 * </p>
 */
public class VZoneView extends VBox {

    /** Label displayed above the content box showing the zone name. */
    private Label zoneNameLabel;

    /** Bordered stack pane that acts as the visual drop target. */
    private StackPane zoneBox;

    /** Label inside the box showing any instructional content text. */
    private Label contentLabel;

    /** CSS hex colour for the zone name label text. */
    private String nameColor;

    /** CSS hex colour for the content label text. */
    private String contentColor;

    /** CSS hex background colour for the drop zone box. */
    private String backgroundColor;

    /** CSS hex border colour for the drop zone box. */
    private String borderColor;

    /** Ratio of name label font size to preferred height. */
    private double nameSize;

    /** Ratio of content label font size to preferred height. */
    private double contentSize;

    /**
     * Constructs a {@code VZoneView} with the given zone name and default styling.
     *
     * @param zoneName text shown in the name label above the drop zone
     */
    public VZoneView(String zoneName) {
        
        this.setPickOnBounds(true);

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

        this.zoneNameLabel.setAlignment(Pos.CENTER);
        this.zoneBox.setAlignment(Pos.CENTER);
        this.contentLabel.setAlignment(Pos.CENTER);
        this.setAlignment(Pos.CENTER);

        this.zoneBox.getChildren().add(zoneBoxTexture);
        this.zoneBox.getChildren().add(contentLabel);
        this.getChildren().add(this.zoneNameLabel);
        this.getChildren().add(this.zoneBox);
    }

    /**
     * Recalculates and applies all styles based on the new preferred dimension.
     *
     * @param oldVal previous dimension (unused)
     * @param newVal new preferred dimension
     */
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

    /**
     * Sets the text displayed in the content label and triggers a size update.
     *
     * @param text the new content text
     */
    public void setText(String text) {
        this.contentLabel.setText(text);
        updateSize(this.prefWidthProperty().getValue(), this.prefWidthProperty().getValue());
    }

    /**
     * Sets the CSS hex colour for the zone name label text.
     *
     * @param color CSS hex colour string
     */
    public void setNameColor(String color) { this.nameColor = color; }

    /**
     * Sets the CSS hex colour for the content label text.
     *
     * @param color CSS hex colour string
     */
    public void setContentColor(String color) { this.contentColor = color; }

    /**
     * Sets the CSS hex background colour for the drop zone box.
     *
     * @param color CSS hex colour string
     */
    public void setBackgroundColor(String color) { this.backgroundColor = color; }

    /**
     * Sets the CSS hex border colour for the drop zone box.
     *
     * @param color CSS hex colour string
     */
    public void setBorderColor(String color) { this.borderColor = color; }

    /**
     * Sets the name label font size ratio and triggers a size update.
     *
     * @param size ratio relative to preferred height
     */
    public void setNameSize(double size) {
        this.nameSize = size;
        updateSize(this.prefWidthProperty().getValue(), this.prefWidthProperty().getValue());
    }

    /**
     * Sets the content label font size ratio and triggers a size update.
     *
     * @param size ratio relative to preferred height
     */
    public void setContentSize(double size) {
        this.contentSize = size;
        updateSize(this.prefWidthProperty().getValue(), this.prefWidthProperty().getValue());
    }
}
