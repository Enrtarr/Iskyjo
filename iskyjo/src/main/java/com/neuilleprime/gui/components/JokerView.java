package com.neuilleprime.gui.components;

import com.neuilleprime.jokers.Joker;
import com.neuilleprime.gui.utils.AssetLoader;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

/**
 * JavaFX component that visually represents a {@link Joker}.
 * <p>
 * The view consists of two sections:
 * <ul>
 *   <li><b>Left pane</b> — the joker's artwork image, always visible.</li>
 *   <li><b>Right pane</b> — name, description, rarity, and price labels,
 *       toggled by clicking the artwork image.</li>
 * </ul>
 * All dimensions are driven by the component's preferred height so the view
 * scales correctly in any layout.
 * </p>
 */
public class JokerView extends HBox {

    /** The underlying joker model this view represents. */
    private Joker joker;

    /** Container holding the left image and the right info pane side by side. */
    private HBox hBoxContainer;

    /** The joker's artwork displayed on the left. */
    private ImageView leftImageView;

    /** Stack pane that holds the right info panel. */
    private StackPane rightStackPane;

    /** Transparent image used to maintain the aspect ratio of the right pane. */
    private ImageView rightStackPaneRatio;

    /** Vertical box holding name, description, rarity, and price labels. */
    private VBox rightStackPaneVBOX;

    /** Label showing the joker's display name. */
    private Label jokerName;

    /** Label showing the joker's description. */
    private Label jokerDescription;

    /** Label showing the joker's rarity tier. */
    private Label jokerRarity;

    /** Label showing the joker's buy price. */
    private Label jokerPrice;

    /** Whether the right info pane is currently visible. */
    private boolean isRightPaneVisible = false;

    /**
     * Constructs a {@code JokerView} for the given joker.
     * Initialises all sub-components, binds sizes, and wires the click toggle
     * for the right info pane.
     *
     * @param joker the joker to display
     */
    public JokerView(Joker joker) {
        this.joker = joker;

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
        this.hBoxContainer.setAlignment(Pos.CENTER);
        this.rightStackPaneVBOX.setAlignment(Pos.CENTER_LEFT);
        jokerDescription.setWrapText(true);

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

    /**
     * Recalculates and applies font sizes and padding for all labels based on
     * the new preferred height. Temporarily shows the right pane during the
     * calculation if it is currently hidden.
     *
     * @param oldVal previous height (unused)
     * @param newVal new preferred height
     */
    private void updateSize(Number oldVal, Number newVal) {
        if (newVal == null || newVal.doubleValue() <= 0) {
            return;
        }
        
        boolean wasHidden = !this.isRightPaneVisible;
        if (wasHidden) {
            this.isRightPaneVisible = true;
            setRightPaneVisibility(this.isRightPaneVisible);
        }

        double jokerNameFontSize = newVal.doubleValue() * .1 / this.jokerName.getText().length() * 15;
        double jokerDescriptionFontSize = newVal.doubleValue() * .075 / this.jokerName.getText().length() * 15;
        double jokerRarityFontSize = newVal.doubleValue() * .1 / this.jokerName.getText().length() * 15;
        double jokerPriceFontSize = newVal.doubleValue() * .1 / this.jokerName.getText().length() * 15;
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
            setRightPaneVisibility(this.isRightPaneVisible);
        }
    }

    /**
     * Plays a shake animation on the joker's artwork image.
     *
     * @param duration      total duration of the shake effect in milliseconds
     * @param shakeStrength maximum rotation angle in degrees
     * @param numFrames     number of frames for one full back-and-forth cycle
     */
    public void shake(double duration, double shakeStrength, int numFrames) {
        Timeline shakeTimeline = new Timeline();

        int totalFrames = (int)(duration / (1000.0 / 60));
        double frameDuration = duration / totalFrames;

        KeyFrame scaleUp = new KeyFrame(Duration.ZERO,
            event -> {
                this.leftImageView.setScaleX(1.1);
                this.leftImageView.setScaleY(1.1);
            }
        );
        shakeTimeline.getKeyFrames().add(scaleUp);

        for (int i = 0; i < totalFrames; i++) {
            final double angle = shakeStrength * Math.sin(2 * Math.PI * i / numFrames);

            KeyFrame keyFrame = new KeyFrame(
                Duration.millis(i * frameDuration),
                event -> this.leftImageView.setRotate(angle)
            );
            shakeTimeline.getKeyFrames().add(keyFrame);
        }

        shakeTimeline.setOnFinished(event -> {
            this.leftImageView.setRotate(0);
            this.leftImageView.setScaleX(1.0);
            this.leftImageView.setScaleY(1.0);
        });

        shakeTimeline.play();
    }

    /**
     * Shows or hides the right info pane, also toggling its managed state so
     * it does not occupy layout space when invisible.
     *
     * @param visible {@code true} to show the pane, {@code false} to hide it
     */
    private void setRightPaneVisibility(boolean visible) {
        this.rightStackPane.setVisible(visible);
        this.rightStackPane.setManaged(visible);
    }

    /**
     * Returns the underlying joker model this view represents.
     *
     * @return the bound {@link Joker}
     */
    public Joker getJokerElem() {
        return this.joker;
    }

    /**
     * Returns the image view used to display the joker's artwork.
     *
     * @return the left-side artwork {@link ImageView}
     */
    public ImageView getJokerImageView() {
        return this.leftImageView;
    }
}
