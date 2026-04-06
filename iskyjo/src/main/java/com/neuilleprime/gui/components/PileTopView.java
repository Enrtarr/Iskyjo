package com.neuilleprime.gui.components;

import com.neuilleprime.game.Card;

import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

public class PileTopView extends VBox {

    private Card topCardElem;

    private Label pileNameLabel;
    private CardView topCardView;

    public PileTopView(Card card, String pileName) {
        this.topCardElem = card;

        // both text and card image
        this.pileNameLabel = new Label(pileName);
        
        this.topCardView = new CardView(this.topCardElem);

        // dynamic size binding
        this.prefWidthProperty().addListener((obs, oldVal, newVal) -> {
            this.updateSize(oldVal, newVal);
        });
        this.prefHeightProperty().addListener((obs, oldVal, newVal) -> {
            this.updateSize(oldVal, newVal);
        });

        this.pileNameLabel.prefHeightProperty().bind(this.prefHeightProperty().multiply(.2));
        this.pileNameLabel.prefWidthProperty().bind(this.prefWidthProperty().multiply(1));

        topCardView.prefHeightProperty().bind(this.prefHeightProperty().multiply(.8));
        topCardView.prefWidthProperty().bind(this.prefWidthProperty().multiply(1));

        // styling
        this.pileNameLabel.setAlignment(Pos.CENTER);
        topCardView.setAlignment(Pos.CENTER);
        this.setAlignment(Pos.CENTER);

        // adding the two, in order (from top to bot)
        this.getChildren().add(this.pileNameLabel);
        this.getChildren().add(this.topCardView);
    }

    private void updateSize(Number oldVal, Number newVal) {
        double labelFontSize = newVal.doubleValue() * .1 / this.pileNameLabel.getText().length() * 15;
        double cardPadding = newVal.doubleValue() * .05;
        double borderRadius = newVal.doubleValue() * .05;
        double borderWidth = newVal.doubleValue() * .02;
        this.pileNameLabel.setStyle(
            "-fx-font-size: " + labelFontSize + "px;" +
            "-fx-font-family: 'VCR OSD Mono';" +
            "-fx-text-fill: #ff0000;"
        );
        this.setStyle(
            "-fx-background-color: #005aa9;" +
            "-fx-background-radius: "+borderRadius+" "+borderRadius+" "+borderRadius+" "+borderRadius+";" +
            "-fx-padding: "+cardPadding+" "+cardPadding+" "+cardPadding+" "+cardPadding+";" +
            "-fx-border-color: #00427c;" +
            "-fx-border-width: "+borderWidth+";" +
            "-fx-border-radius: "+borderRadius+" "+borderRadius+" "+borderRadius+" "+borderRadius+";"
        );
    }

    public CardView getTopCardView() {
        return this.topCardView;
    }

    public Card getTopCard() {
        return this.topCardView.getCardElem();
    }
}
