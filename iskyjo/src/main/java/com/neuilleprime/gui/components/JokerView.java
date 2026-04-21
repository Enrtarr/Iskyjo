package com.neuilleprime.gui.components;

import com.neuilleprime.jokers.Joker;
import com.neuilleprime.gui.utils.AssetLoader;

import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

public class JokerView extends HBox {

    private Joker joker;

    private HBox hBoxContainer;
    private ImageView leftImageView;
    private StackPane rightStackPane;
    private ImageView rightStackPaneRatio;
    private VBox rightStackPaneVBOX;
    private Label jokerName;
    private Label jokerDescription;
    private Label jokerRarity;
    private Label jokerPrice;

    private boolean isRightPaneVisible = false;

    public JokerView(Joker joker) {
        this.joker = joker;

        // binding the size
        // this.prefWidthProperty().addListener((obs, oldVal, newVal) -> {
        //     this.updateSize(oldVal, newVal);
        // });
        this.prefHeightProperty().addListener((obs, oldVal, newVal) -> {
            this.updateSize(oldVal, newVal);
        });
        this.setAlignment(Pos.CENTER);

        // instanciation of all the sub-elems of the class
        this.hBoxContainer = new HBox();
        this.leftImageView = new ImageView(AssetLoader.getAssetFromString(this.joker.getTextureName()));
        this.rightStackPane = new StackPane();
        this.rightStackPaneRatio = new ImageView(AssetLoader.CARD_TRANSPARENT);
        this.rightStackPaneVBOX = new VBox();
        this.jokerName = new Label(this.joker.getName());
        this.jokerDescription = new Label(this.joker.getDescription());
        this.jokerRarity = new Label(this.joker.getRarity().getName());
        this.jokerPrice = new Label(this.joker.getPrice()+"₣");

        // just a bit of styling
        this.rightStackPaneVBOX.setAlignment(Pos.CENTER_LEFT);

        // now we put them all in the right place
        this.rightStackPaneVBOX.getChildren().addAll(
            this.jokerName, this.jokerDescription,
            this.jokerRarity, this.jokerPrice
        );
        this.rightStackPane.getChildren().addAll(
            this.rightStackPaneRatio, this.rightStackPaneVBOX
        );
        this.hBoxContainer.getChildren().addAll(
            this.leftImageView, this.rightStackPane
        );
        this.getChildren().add(this.hBoxContainer);

        // right pane visibility based on click
        this.setRightPaneVisibility(this.isRightPaneVisible);
        this.leftImageView.setOnMouseClicked(e -> {
            this.isRightPaneVisible = !this.isRightPaneVisible;
            this.setRightPaneVisibility(this.isRightPaneVisible);
        });

        // sizing of the other elems
        this.hBoxContainer.setMaxHeight(Double.MAX_VALUE);

        this.leftImageView.setPreserveRatio(true);
        this.leftImageView.setFitWidth(Double.MAX_VALUE);
        this.leftImageView.setFitHeight(Double.MAX_VALUE);
        this.leftImageView.fitWidthProperty().bind(this.prefWidthProperty());
        this.leftImageView.fitHeightProperty().bind(this.prefHeightProperty());

        this.updateSize(0, this.prefHeightProperty().getValue());
    }

    private void updateSize(Number oldVal, Number newVal) {

        // this.leftImageView.fitWidthProperty().set(this.prefWidthProperty().doubleValue());
        // this.leftImageView.fitHeightProperty().set(this.prefHeightProperty().doubleValue());
        
        boolean wasHidden = this.isRightPaneVisible;
        if (wasHidden) {
            this.isRightPaneVisible = true;
        }
        // if (!this.isRightPaneVisible) {return;}

        double jokerNameFontSize = newVal.doubleValue() * .1 / this.jokerName.getText().length() * 15;
        double cardPadding = newVal.doubleValue() * .05;
        double borderRadius = newVal.doubleValue() * .05;
        double borderWidth = newVal.doubleValue() * .01;
        this.jokerName.setStyle(
            "-fx-font-size: " + jokerNameFontSize + "px;" +
            "-fx-font-family: 'VCR OSD Mono';" +
            "-fx-text-fill: "+"#2c2121"+";" +
            "-fx-background-color: "+"#cdc0c0"+";" +
            "-fx-background-radius: "+borderRadius+" "+borderRadius+" "+borderRadius+" "+borderRadius+";" +
            "-fx-padding: "+cardPadding+" "+cardPadding+" "+cardPadding+" "+cardPadding+";" +
            "-fx-border-color: "+"#8f8787"+";" +
            "-fx-border-width: "+borderWidth+";" +
            "-fx-border-radius: "+borderRadius+" "+borderRadius+" "+borderRadius+" "+borderRadius+";"
        );
        double jokerDescriptionFontSize = newVal.doubleValue() * .1 / this.jokerName.getText().length() * 15;
        this.jokerDescription.setStyle(
            "-fx-font-size: " + jokerDescriptionFontSize + "px;" +
            "-fx-font-family: 'VCR OSD Mono';" +
            "-fx-text-fill: "+"#2c2121"+";" +
            "-fx-background-color: "+"#cdc0c0"+";" +
            "-fx-background-radius: "+borderRadius+" "+borderRadius+" "+borderRadius+" "+borderRadius+";" +
            "-fx-padding: "+cardPadding+" "+cardPadding+" "+cardPadding+" "+cardPadding+";" +
            "-fx-border-color: "+"#8f8787"+";" +
            "-fx-border-width: "+borderWidth+";" +
            "-fx-border-radius: "+borderRadius+" "+borderRadius+" "+borderRadius+" "+borderRadius+";"
        );
        double jokerRarityFontSize = newVal.doubleValue() * .1 / this.jokerName.getText().length() * 15;
        this.jokerRarity.setStyle(
            "-fx-font-size: " + jokerRarityFontSize + "px;" +
            "-fx-font-family: 'VCR OSD Mono';" +
            "-fx-text-fill: "+"#2c2121"+";" +
            "-fx-background-color: "+this.joker.getRarity().getBackgroundColor()+";" +
            "-fx-background-radius: "+borderRadius+" "+borderRadius+" "+borderRadius+" "+borderRadius+";" +
            "-fx-padding: "+cardPadding+" "+cardPadding+" "+cardPadding+" "+cardPadding+";" +
            "-fx-border-color: "+this.joker.getRarity().getOutlineColor()+";" +
            "-fx-border-width: "+borderWidth+";" +
            "-fx-border-radius: "+borderRadius+" "+borderRadius+" "+borderRadius+" "+borderRadius+";"
        );
        double jokerPriceFontSize = newVal.doubleValue() * .1 / this.jokerName.getText().length() * 15;
        this.jokerPrice.setStyle(
            "-fx-font-size: " + jokerPriceFontSize + "px;" +
            "-fx-font-family: 'VCR OSD Mono';" +
            "-fx-text-fill: "+"#2c2121"+";" +
            "-fx-background-color: "+"#cdc0c0"+";" +
            "-fx-background-radius: "+borderRadius+" "+borderRadius+" "+borderRadius+" "+borderRadius+";" +
            "-fx-padding: "+cardPadding+" "+cardPadding+" "+cardPadding+" "+cardPadding+";" +
            "-fx-border-color: "+"#8f8787"+";" +
            "-fx-border-width: "+borderWidth+";" +
            "-fx-border-radius: "+borderRadius+" "+borderRadius+" "+borderRadius+" "+borderRadius+";"
        );

        if (wasHidden) {
            this.isRightPaneVisible = false;
        }
    }

    private void setRightPaneVisibility(boolean visible) {
        this.rightStackPane.setVisible(visible);
        this.rightStackPane.setManaged(visible);
    }
}
