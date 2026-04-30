package com.neuilleprime.gui.components;

import java.util.ArrayList;

import com.neuilleprime.game.Deck;
import com.neuilleprime.jokers.Joker;
import com.neuilleprime.jokers.Joker.JokerCategory;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

/**
 * JavaFX component that displays an animated scoring breakdown at round end.
 * <p>
 * The animation plays in four sequential phases:
 * <ol>
 *   <li>Deck joker effects (modify card values in place).</li>
 *   <li>Card value totalling (highlights each card one by one).</li>
 *   <li>Combo detection and step-by-step combo score build-up.</li>
 *   <li>Combo joker effects applied on top of detected combos.</li>
 * </ol>
 * Each phase is implemented as a JavaFX {@link Timeline} and they are chained
 * via {@code setOnFinished} so they run sequentially.
 * </p>
 */
public class ScoreView extends VBox {

    /** The deck view whose cards are animated during scoring. */
    private DeckView deckView;

    /** Joker views of the player's active jokers (used for shake animations). */
    private ArrayList<JokerView> jokerViews;

    /** Upgrade jokers applied silently during combo scanning (no shake). */
    private ArrayList<Joker> upgrades;

    /** Vertical box that holds one label per detected combo. */
    private VBox combosVbox;

    /** One label per detected combo, updated step by step during the animation. */
    private ArrayList<Label> comboLabels;

    /** Running total of points contributed by combos. */
    private Label totalComboLabel;

    /** Running total of points contributed by card face values. */
    private Label totalCardLabel;

    /** Grand total label (sum of combo and card totals). */
    private Label totalLabel;

    /** CSS hex background colour of this panel. */
    private String backgroundColor = "#6600ff";

    /** CSS hex border colour of this panel. */
    private String borderColor = "#5000c8";

    /**
     * Constructs a {@code ScoreView}.
     *
     * @param deckView   the deck view to animate
     * @param jokerViews joker views for the player's active jokers
     * @param upgrades   upgrade jokers applied silently during combo phase
     */
    public ScoreView(DeckView deckView, ArrayList<JokerView> jokerViews, ArrayList<Joker> upgrades) {
        this.deckView = deckView;
        this.jokerViews = jokerViews;
        this.upgrades = upgrades;

        this.combosVbox = new VBox();
        this.comboLabels = new ArrayList<>();
        this.totalComboLabel = new Label("Total from combos: 0");
        this.totalCardLabel = new Label("Total from cards: 0");
        this.totalLabel = new Label("Total: 0");

        this.prefWidthProperty().addListener((obs, oldVal, newVal) -> {
            this.updateSize(oldVal, newVal);
        });
        this.prefHeightProperty().addListener((obs, oldVal, newVal) -> {
            this.updateSize(oldVal, newVal);
        });
        combosVbox.prefWidthProperty().bind(this.prefWidthProperty().multiply(1));
        combosVbox.prefHeightProperty().bind(this.prefHeightProperty().multiply(.6));
        totalComboLabel.prefWidthProperty().bind(this.prefWidthProperty().multiply(1));
        totalComboLabel.prefHeightProperty().bind(this.prefHeightProperty().multiply(.1));
        totalCardLabel.prefWidthProperty().bind(this.prefWidthProperty().multiply(1));
        totalCardLabel.prefHeightProperty().bind(this.prefHeightProperty().multiply(.1));
        totalLabel.prefWidthProperty().bind(this.prefWidthProperty().multiply(1));
        totalLabel.prefHeightProperty().bind(this.prefHeightProperty().multiply(.2));

        this.setAlignment(Pos.CENTER_LEFT);
        this.combosVbox.setAlignment(Pos.CENTER_LEFT);
        this.totalComboLabel.setAlignment(Pos.CENTER_LEFT);
        this.totalComboLabel.setWrapText(true);
        this.totalCardLabel.setAlignment(Pos.CENTER_LEFT);
        this.totalCardLabel.setWrapText(true);
        this.totalLabel.setAlignment(Pos.CENTER_LEFT);
        this.totalLabel.setWrapText(true);

        this.getChildren().add(this.combosVbox);
        this.getChildren().add(this.totalComboLabel);
        this.getChildren().add(this.totalCardLabel);
        this.getChildren().add(this.totalLabel);
    }

    /**
     * Recalculates and applies font sizes and padding for all labels based on
     * the new preferred height.
     *
     * @param oldVal previous height (unused)
     * @param newVal new preferred height
     */
    private void updateSize(Number oldVal, Number newVal) {
        double totalComboLabelFontSize = newVal.doubleValue() * .085 / this.totalComboLabel.getText().length() * 15;
        double totalCardLabelFontSize = newVal.doubleValue() * .085 / this.totalCardLabel.getText().length() * 15;
        double totalLabelFontSize = newVal.doubleValue() * .085 / this.totalLabel.getText().length() * 15;
        double cardPadding = newVal.doubleValue() * .05;
        double borderRadius = newVal.doubleValue() * .05;
        double borderWidth = newVal.doubleValue() * .02;

        this.setStyle(
            "-fx-background-color: "+this.backgroundColor+";" +
            "-fx-background-radius: "+borderRadius+" "+borderRadius+" "+borderRadius+" "+borderRadius+";" +
            "-fx-padding: "+cardPadding+" "+cardPadding+" "+cardPadding+" "+cardPadding+";" +
            "-fx-border-color: "+this.borderColor+";" +
            "-fx-border-width: "+borderWidth+";" +
            "-fx-border-radius: "+borderRadius+" "+borderRadius+" "+borderRadius+" "+borderRadius+";"
        );
        this.totalComboLabel.setStyle(
            "-fx-font-size: " + totalComboLabelFontSize + "px;" +
            "-fx-font-family: 'VCR OSD Mono';" +
            "-fx-text-fill: "+"#d7d7d7"+";"
        );
        this.totalCardLabel.setStyle(
            "-fx-font-size: " + totalCardLabelFontSize + "px;" +
            "-fx-font-family: 'VCR OSD Mono';" +
            "-fx-text-fill: "+"#d7d7d7"+";"
        );
        this.totalLabel.setStyle(
            "-fx-font-size: " + totalLabelFontSize + "px;" +
            "-fx-font-family: 'VCR OSD Mono';" +
            "-fx-text-fill: "+"#d7d7d7"+";"
        );
    }

    /**
     * Resets all labels and combo entries to their initial zero state.
     * Must be called before {@link #startAnims()} to avoid stale data.
     */
    public void clear() {
        this.combosVbox.getChildren().clear();
        this.totalComboLabel.setText("Total from combos: 0");
        this.totalCardLabel.setText("Total from cardss: 0");
        this.totalLabel.setText("Total: 0");
    }

    /**
     * Starts the full four-phase scoring animation sequence.
     * The phases run sequentially: deck jokers → card totals → combos → combo jokers.
     */
    public void startAnims() {
        Timeline cardsJokerTL = this.getCardsJokerTimeline();
        cardsJokerTL.setOnFinished(e2 -> {

            Timeline cardsTL = this.getCardsTimeline();
            cardsTL.setOnFinished(e3 -> {

                Timeline combosTL = this.getCombosTimeline();
                combosTL.setOnFinished(e4 -> {

                    Timeline comboJokersTL = this.getCombosJokerTimeline();
                    comboJokersTL.setOnFinished(e5 -> {
                        
                    });
                    comboJokersTL.play();
                });
                combosTL.play();
            });
            cardsTL.play();
        });
        cardsJokerTL.play();
    }

    /**
     * Builds the combo-scoring timeline.
     * <p>
     * Scans the deck for combos, creates one label per combo, and animates
     * each contributing card one step at a time, updating the running totals.
     * </p>
     *
     * @return the constructed {@link Timeline}
     */
    private Timeline getCombosTimeline() {
        ArrayList<int[][]> combos = this.deckView.getDeckElem().scanCombosWithPos();

        // silently apply the combo upgrades
        for (Joker j : this.upgrades) {
                if (j.getCategory() == JokerCategory.COMBO) {
                    j.applyWithPos(combos);
                }
            }

        comboLabels.clear();
        combosVbox.getChildren().clear();

        Timeline timeline = new Timeline();
        double time = 1000;

        final int deltaT = 300;
        final int shakeDuration = 300;

        for (int[][] combo : combos) {

            Label comboLabel = new Label(" ");
            comboLabel.setWrapText(true);
            comboLabel.setAlignment(Pos.CENTER_LEFT);

            comboLabel.prefWidthProperty().bind(this.combosVbox.prefWidthProperty());
            comboLabel.prefHeightProperty().bind(this.combosVbox.prefHeightProperty());

            combosVbox.getChildren().add(comboLabel);
            comboLabels.add(comboLabel);

            int comboLength = combo[0][0];
            int comboValue = combo[0][1];

            for (int i = 1; i <= comboLength; i++) {
                int finalI = i;

                timeline.getKeyFrames().add(new KeyFrame(
                    Duration.millis(time),
                    e -> {
                        int oldNumber = 0;
                        if (finalI > 1) {
                            oldNumber = Integer.parseInt(comboLabel.getText().split("=")[1].trim());
                        }
                        int newNumber = finalI*combo[0][1];
                        comboLabel.setText(finalI+" * "+comboValue+" = "+newNumber);

                        int oldTotal = Integer.parseInt(this.totalComboLabel.getText().split(":")[1].trim());
                        int oldTotalTotal = Integer.parseInt(this.totalLabel.getText().split(":")[1].trim());
                        this.totalComboLabel.setText("Total from combos: "+(oldTotal+newNumber-oldNumber));
                        this.totalLabel.setText("Total: "+(oldTotalTotal+newNumber-oldNumber));

                        deckView.getCardViewAtCoords(
                            combo[finalI][0],
                            combo[finalI][1]
                        ).shake(shakeDuration, 5, 20);
                    }
                ));

                time += deltaT;
            }
        }

        combosVbox.prefWidthProperty().addListener((obs, oldVal, newVal) -> {
            for (Label label : comboLabels) {
                double fontSize = newVal.doubleValue() * .125 / Math.max(1, totalComboLabel.getText().length()) * 15;
                label.setStyle(
                    "-fx-font-size: " + fontSize + "px;" +
                    "-fx-font-family: 'VCR OSD Mono';" +
                    "-fx-text-fill: #d7d7d7;"
                );
            }
        });
        for (Label label : comboLabels) {
            double fontSize = combosVbox.prefWidthProperty().doubleValue() * .125 
                / Math.max(1, totalComboLabel.getText().length()) * 15;
            label.setStyle(
                "-fx-font-size: " + fontSize + "px;" +
                "-fx-font-family: 'VCR OSD Mono';" +
                "-fx-text-fill: #d7d7d7;"
            );
        }

        return timeline;
    }

    /**
     * Builds the combo-joker timeline.
     * <p>
     * For each {@link JokerCategory#COMBO} joker, applies its positional effect
     * to the combo list and animates the updated totals, shaking both the joker
     * view and affected card views.
     * </p>
     *
     * @return the constructed {@link Timeline}
     */
    private Timeline getCombosJokerTimeline() {
        ArrayList<int[][]> combos = this.deckView.getDeckElem().scanCombosWithPos();

        Timeline timeline = new Timeline();
        double time = 1000;

        final int deltaT = 300;
        final int shakeDuration = 300;

        for (JokerView jokerView : this.jokerViews) {
            Joker joker = jokerView.getJokerElem();

            if (joker.getCategory() == JokerCategory.COMBO) {

                ArrayList<Integer> changedIndexes = joker.applyWithPos(combos);

                for (int i=0;i<changedIndexes.size();i++) {
                    int ci = changedIndexes.get(i);
                    Label label = comboLabels.get(ci);
                    int[][] combo = combos.get(ci);

                    for (int j=1;j<combo.length;j++) {
                        int finalI = j;

                        if (finalI <= deckView.getDeckElem().getHeight()) {

                            timeline.getKeyFrames().add(new KeyFrame(
                            Duration.millis(time),
                            e2 -> {
                                    int oldNumber = Integer.parseInt(label.getText().split("=")[1].trim());
                                    int newNumber = combo[0][0]*combo[0][1];
                                    label.setText(combo[0][0]+" * "+combo[0][1]+" = "+newNumber);

                                    int oldTotal = Integer.parseInt(this.totalComboLabel.getText().split(":")[1].trim());
                                    int oldTotalTotal = Integer.parseInt(this.totalLabel.getText().split(":")[1].trim());
                                    this.totalComboLabel.setText("Total from combos: "+(oldTotal+newNumber-oldNumber));
                                    this.totalLabel.setText("Total: "+(oldTotalTotal+newNumber-oldNumber));

                                    jokerView.shake(shakeDuration, 5, 20);

                                    deckView.getCardViewAtCoords(
                                        combo[finalI][0],
                                        combo[finalI][1]
                                    ).shake(shakeDuration, 5, 20);
                                }
                            ));
                        }
                    }
                    time += deltaT;
                }
            }
        }

        return timeline;
    }

    /**
     * Builds the card-value timeline.
     * <p>
     * Iterates over all card views in the deck, adding each card's value to the
     * running total one at a time with a shake animation on the card.
     * </p>
     *
     * @return the constructed {@link Timeline}
     */
    private Timeline getCardsTimeline() {
        Timeline timeline = new Timeline();
        double time = 1000;

        final int deltaT = 200;
        final int shakeDuration = 300;

        int totalCardValue = 0;

        for (CardView cardView : this.deckView.getAllCardViews()) {
            totalCardValue += cardView.getCardElem().getValue();
            final int finalTotalCardValue = totalCardValue;

            timeline.getKeyFrames().add(new KeyFrame(
            Duration.millis(time),
                e2 -> {
                    this.totalCardLabel.setText("Total from cards: "+finalTotalCardValue);

                    int oldTotalTotal = Integer.parseInt(this.totalLabel.getText().split(":")[1].trim());
                    this.totalLabel.setText("Total: "+(oldTotalTotal+cardView.getCardElem().getValue()));

                    cardView.shake(shakeDuration, 5, 20);
                }
            ));
            time += deltaT;
        }

        return timeline;
    }

    /**
     * Builds the deck-joker timeline.
     * <p>
     * For each {@link JokerCategory#DECK} joker, applies its positional effect
     * to the deck and animates the card value updates, shaking both the joker
     * view and the affected card views.
     * </p>
     *
     * @return the constructed {@link Timeline}
     */
    private Timeline getCardsJokerTimeline() {
        Timeline timeline = new Timeline();
        double time = 1000;

        final int deltaT = 300;
        final int shakeDuration = 300;

        for (JokerView jokerView : this.jokerViews) {
            Joker joker = jokerView.getJokerElem();

            if (joker.getCategory() == JokerCategory.DECK) {
            
                ArrayList<Integer[]> changedCoords = joker.applyWithPos(this.deckView.getDeckElem());

                for (int i=0;i<changedCoords.size();i++) {
                    int finalI = i;

                    timeline.getKeyFrames().add(new KeyFrame(
                        Duration.millis(time),
                        e2 -> {
                            deckView.getDeckElem().getCardAtCoords(
                                changedCoords.get(finalI)[0],
                                changedCoords.get(finalI)[1]
                            ).setValue(changedCoords.get(finalI)[2]);

                            jokerView.shake(shakeDuration, 5, 20);

                            deckView.getCardViewAtCoords(
                                changedCoords.get(finalI)[0],
                                changedCoords.get(finalI)[1]
                            ).shake(shakeDuration, 5, 20);
                        }
                    ));
                    time += deltaT;
                }
            }
        }

        return timeline;
    }
}
