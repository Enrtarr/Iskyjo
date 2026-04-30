package com.neuilleprime.gui.components;

import com.neuilleprime.game.Card;

import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

/**
 * JavaFX component that displays the top card of a pile along with the pile's name.
 * <p>
 * Consists of a text label at the top and a {@link CardView} below it.
 * All dimensions are driven by the component's preferred width and height.
 * </p>
 */
public class PileTopView extends VBox {

    /** The top card of the pile being displayed. */
    private Card topCardElem;

    /** Label showing the pile name (e.g. "Draw pile" or "Discard pile"). */
    private Label pileNameLabel;

    /** Visual representation of the top card. */
    private CardView topCardView;

    /**
     * Constructs a {@code PileTopView} for the given card and pile name.
     *
     * @param card     the top card of the pile to display
     * @param pileName the name to display above the card
     */
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

    /**
     * Recalculates and applies font size and padding for the name label based on
     * the new preferred height.
     *
     * @param oldVal previous height (unused)
     * @param newVal new preferred height
     */
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

    /**
     * Returns the {@link CardView} used to render the top card.
     *
     * @return the top card view
     */
    public CardView getTopCardView() {
        return this.topCardView;
    }

    /**
     * Returns the underlying {@link Card} model shown in this view.
     *
     * @return the top card
     */
    public Card getTopCard() {
        return this.topCardView.getCardElem();
    }
}
