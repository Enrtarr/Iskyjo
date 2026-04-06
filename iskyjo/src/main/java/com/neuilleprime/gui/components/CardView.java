package com.neuilleprime.gui.components;

import com.neuilleprime.game.Card;
import com.neuilleprime.gui.utils.AssetLoader;

import javafx.scene.Cursor;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.PixelReader;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;

public class CardView extends StackPane {

    private Card cardElem;

    private Label valueLabel;
    private ImageView cardBackImage;
    private ImageView cardFrontBg;
    private ImageView cardFrontOverlay;

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
        // this.cardBackImage.fitWidthProperty().bind(this.widthProperty());
        // this.cardBackImage.fitHeightProperty().bind(this.heightProperty());
        this.cardBackImage.fitWidthProperty().bind(this.prefWidthProperty());
        this.cardBackImage.fitHeightProperty().bind(this.prefHeightProperty());

        this.cardFrontBg.setPreserveRatio(true);
        this.cardFrontBg.setFitWidth(Double.MAX_VALUE);
        this.cardFrontBg.setFitHeight(Double.MAX_VALUE);
        // this.cardFrontBg.fitWidthProperty().bind(this.widthProperty());
        // this.cardFrontBg.fitHeightProperty().bind(this.heightProperty());
        this.cardFrontBg.fitWidthProperty().bind(this.prefWidthProperty());
        this.cardFrontBg.fitHeightProperty().bind(this.prefHeightProperty());

        this.cardFrontOverlay.setPreserveRatio(true);
        this.cardFrontOverlay.setFitWidth(Double.MAX_VALUE);
        this.cardFrontOverlay.setFitHeight(Double.MAX_VALUE);
        // this.cardFrontOverlay.fitWidthProperty().bind(this.widthProperty());
        // this.cardFrontOverlay.fitHeightProperty().bind(this.heightProperty());
        this.cardFrontOverlay.fitWidthProperty().bind(this.prefWidthProperty());
        this.cardFrontOverlay.fitHeightProperty().bind(this.prefHeightProperty());

        // this.valueLabel.styleProperty().bind(
        //     this.widthProperty().multiply(0.15)
        //         .asString("-fx-font-size: %.0fpx; -fx-font-family: 'VCR OSD Mono';")
        // );
        this.valueLabel.styleProperty().bind(
            this.prefWidthProperty().multiply(0.35)
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

    public void setCursorTo(Cursor cursor) {
        this.cardBackImage.setCursor(cursor);
        this.cardFrontBg.setCursor(cursor);
        this.cardFrontOverlay.setCursor(cursor);
        this.valueLabel.setCursor(cursor);
    }

    private void show() {
        this.cardBackImage.setVisible(false);

        this.cardFrontBg.setVisible(true);
        this.cardFrontOverlay.setVisible(true);
        this.valueLabel.setVisible(true);
    }

    private void hide() {
        this.cardBackImage.setVisible(true);

        this.cardFrontBg.setVisible(false);
        this.cardFrontOverlay.setVisible(false);
        this.valueLabel.setVisible(false);
    }

    private void updateTint(int value) {
        Color color = null;

        if (value < 0) {
            color = Color.RED;
        } else if (value == 0) {
            color = Color.BLACK;
        } else if (0 < value && value < 5) {
            color = Color.BLUE;
        } else if (4 < value && value < 8) {
            color = Color.GREEN;
        } else if (7 < value) {
            color = Color.PURPLE;
        }

        Image newImage = this.getTintedOverlay(this.cardFrontOverlay.getImage(), color);
        this.cardFrontOverlay.setImage(newImage);
        this.valueLabel.setTextFill(color);
    }

    Image getTintedOverlay(Image image, Color tint) {

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
                Color out = new Color(tint.getRed(), tint.getGreen(), tint.getBlue(), alpha);
                pw.setColor(x, y, out);
            }
        }

        return tinted;
    }

    public Card getCardElem() {
        return this.cardElem;
    }
}
 