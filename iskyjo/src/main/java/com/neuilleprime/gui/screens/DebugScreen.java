package com.neuilleprime.gui.screens;

import java.util.ArrayList;

import com.neuilleprime.game.Card;
import com.neuilleprime.game.Deck;
import com.neuilleprime.game.Player;
import com.neuilleprime.gui.components.DeckView;
import com.neuilleprime.gui.components.JokerView;
import com.neuilleprime.gui.components.ScoreView;
import com.neuilleprime.gui.utils.AssetLoader;
import com.neuilleprime.gui.utils.ScreenManager;
import com.neuilleprime.gui.utils.SideBarsHelper;
import com.neuilleprime.jokers.AddXDeckJoker;
import com.neuilleprime.jokers.ComboLeftAllJoker;
import com.neuilleprime.jokers.ComboRightJoker;
import com.neuilleprime.jokers.Joker;

import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

/**
 * Development-only screen used to test the scoring animation pipeline.
 * <p>
 * Builds a hard-coded 5×3 deck and a fixed set of jokers so that the
 * {@link ScoreView} animation can be triggered repeatedly without starting a
 * full game. The "Play" button clears the score view and restarts the animation.
 * </p>
 * <p>
 * This screen should not be shipped in a production build.
 * </p>
 */
public class DebugScreen {

    /** Screen manager used to navigate between scenes. */
    private final ScreenManager sm;

    /**
     * Constructs a {@code DebugScreen}.
     *
     * @param sm the application screen manager
     */
    public DebugScreen(ScreenManager sm) {
        this.sm = sm;
    }

    /**
     * Builds and returns the debug {@link Scene}.
     * <p>
     * Creates a hard-coded deck and joker set, wires up the {@link ScoreView},
     * and attaches a "Play" button that replays the scoring animation.
     * </p>
     *
     * @return the constructed {@link Scene}
     */
    public Scene buildScene() {
        BorderPane root = new BorderPane();

        Scene scene = new Scene(root);
        scene.getStylesheets().add(getClass().getResource("/style.css").toExternalForm());

        root.getStyleClass().add("root");

        VBox leftBar = new VBox();
        leftBar.setAlignment(Pos.CENTER);
        leftBar.prefWidthProperty().bind(scene.widthProperty().multiply(SideBarsHelper.leftBarWidth));

        VBox rightBar = new VBox();
        rightBar.setAlignment(Pos.CENTER);
        rightBar.prefWidthProperty().bind(scene.widthProperty().multiply(.15));

        HBox topBar = new HBox();
        topBar.setAlignment(Pos.CENTER);
        topBar.prefWidthProperty().bind(scene.widthProperty().multiply(SideBarsHelper.topBarWidth));
        topBar.prefHeightProperty().bind(scene.heightProperty().multiply(SideBarsHelper.topBarHeight));

        HBox bottomBar = new HBox();
        bottomBar.setAlignment(Pos.CENTER);
        bottomBar.prefWidthProperty().bind(scene.widthProperty().multiply(SideBarsHelper.bottomBarWidth));
        bottomBar.prefHeightProperty().bind(scene.heightProperty().multiply(SideBarsHelper.bottomBarHeight));

        StackPane centerBar = new StackPane();
        centerBar.setAlignment(Pos.CENTER);

        root.setLeft(leftBar);
        root.setRight(rightBar);
        root.setTop(topBar);
        root.setBottom(bottomBar);
        root.setCenter(centerBar);

        Deck testDeck = new Deck(5, 3);

        ArrayList<Card> r1 = new ArrayList<>();
        r1.add(new Card(1, false)); r1.add(new Card(1, false)); r1.add(new Card(2, false));
        r1.add(new Card(2, false)); r1.add(new Card(2, false));
        testDeck.addRow(r1);

        ArrayList<Card> r2 = new ArrayList<>();
        r2.add(new Card(1, false)); r2.add(new Card(3, false)); r2.add(new Card(3, false));
        r2.add(new Card(2, false)); r2.add(new Card(7, false));
        testDeck.addRow(r2);

        ArrayList<Card> r3 = new ArrayList<>();
        r3.add(new Card(1, false)); r3.add(new Card(7, false)); r3.add(new Card(2, false));
        r3.add(new Card(7, false)); r3.add(new Card(2, false));
        testDeck.addRow(r3);

        ArrayList<Joker> testJokers = new ArrayList<>();
        testJokers.add(new ComboLeftAllJoker(2));
        testJokers.add(new ComboRightJoker(2, 7));
        testJokers.add(new AddXDeckJoker(2, false));

        Player testPlayer = new Player(
            testDeck, testJokers, 5, null, 2, null, 3, 2,
            "Test player", 0, 0, 1, new int[] {1, 5, 5}
        );

        DeckView testDeckView = new DeckView(testDeck);
        testDeckView.prefWidthProperty().bind(centerBar.widthProperty().multiply(0.4));
        testDeckView.prefHeightProperty().bind(centerBar.heightProperty().multiply(0.6));

        leftBar.getChildren().clear();
        SideBarsHelper.loadMoneyView(leftBar, 67);

        bottomBar.getChildren().clear();
        ArrayList<JokerView> bottomBarJokers = SideBarsHelper.loadBottomBar(bottomBar, testPlayer);

        rightBar.getChildren().clear();

        ScoreView testScoreView = new ScoreView(testDeckView, bottomBarJokers, testPlayer.getUpgrades());
        testScoreView.prefWidthProperty().bind(leftBar.prefWidthProperty());
        testScoreView.prefHeightProperty().bind(leftBar.prefHeightProperty());
        rightBar.getChildren().add(testScoreView);

        centerBar.getChildren().add(testDeckView);

        topBar.getChildren().clear();
        Button playBtn = new Button();
        playBtn.setGraphic(new ImageView(AssetLoader.BUTTON_PLAY));
        playBtn.setStyle("-fx-background-color: transparent;");
        playBtn.setOnAction(e -> {
            testScoreView.clear();
            testScoreView.startAnims();
        });
        topBar.getChildren().add(playBtn);

        return scene;
    }
}
