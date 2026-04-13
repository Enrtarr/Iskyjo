package com.neuilleprime.gui.screens;

import java.util.ArrayList;

import com.neuilleprime.game.Card;
import com.neuilleprime.game.Deck;
import com.neuilleprime.game.Pile;
import com.neuilleprime.gui.components.CardView;
import com.neuilleprime.gui.components.DeckView;
import com.neuilleprime.gui.components.PileView;
import com.neuilleprime.gui.utils.AssetLoader;
import com.neuilleprime.gui.utils.ScreenManager;

import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public class DebugScreen {

    private final ScreenManager sm;

    public DebugScreen(ScreenManager sm) {
        this.sm = sm;
    }

    public Scene buildScene() {
        BorderPane root = new BorderPane();

        Scene scene = new Scene(root, 1280, 720);
        scene.getStylesheets().add(getClass().getResource("/style.css").toExternalForm());

        VBox leftBar = new VBox();
        
        leftBar.setAlignment(Pos.CENTER);
        leftBar.prefWidthProperty().bind(scene.widthProperty().multiply(0.1));
        // leftBar.prefHeightProperty().bind(scene.heightProperty().multiply(1));

        VBox rightBar = new VBox();
        rightBar.setAlignment(Pos.CENTER);
        rightBar.prefWidthProperty().bind(scene.widthProperty().multiply(0.1));
        // rightBar.prefHeightProperty().bind(scene.heightProperty().multiply(1));

        HBox topBar = new HBox();
        topBar.setAlignment(Pos.CENTER);
        topBar.prefWidthProperty().bind(scene.widthProperty().multiply(0.8));
        // topBar.prefHeightProperty().bind(scene.heightProperty().multiply(0.1));

        ImageView logo = new ImageView(AssetLoader.NP_LOGO);
        logo.setPreserveRatio(true);
        logo.fitHeightProperty().bind(topBar.prefHeightProperty());

        Button playBtn = new Button();
        playBtn.setGraphic(new ImageView(AssetLoader.BUTTON_PLAY));
        playBtn.setStyle("-fx-background-color: transparent;");
        playBtn.setOnAction(e -> this.sm.show("game"));

        Card testCard = new Card(6);
        CardView testCardView = new CardView(testCard);
        Card testCard2 = new Card(7);
        CardView testCardView2 = new CardView(testCard2);

        Deck testDeck = new Deck(4, 3);
        boolean wholeDeckHidden = false;
        ArrayList<Card> row1 = new ArrayList<>();
        row1.add(new Card(1, wholeDeckHidden));
        row1.add(new Card(2, wholeDeckHidden));
        row1.add(new Card(3, wholeDeckHidden));
        row1.add(new Card(4, wholeDeckHidden));
        testDeck.addRow(row1);
        ArrayList<Card> row2 = new ArrayList<>();
        row2.add(new Card(5, wholeDeckHidden));
        row2.add(new Card(6, wholeDeckHidden));
        row2.add(new Card(7, wholeDeckHidden));
        row2.add(new Card(8, wholeDeckHidden));
        testDeck.addRow(row2);
        ArrayList<Card> row3 = new ArrayList<>();
        row3.add(new Card(9, wholeDeckHidden));
        row3.add(new Card(10, wholeDeckHidden));
        row3.add(new Card(11, wholeDeckHidden));
        row3.add(new Card(12, wholeDeckHidden));
        testDeck.addRow(row3);

        DeckView testDeckView = new DeckView(testDeck);

        Pile testPile = new Pile(15);
        PileView testPileView = new PileView(testPile);
        
        testPile.showAll();

        leftBar.getChildren().addAll(testCardView, testCardView2);
        rightBar.getChildren().add(testPileView);
        topBar.getChildren().add(logo);

        root.setLeft(leftBar);
        root.setRight(rightBar);
        root.setTop(topBar);
        root.setCenter(testDeckView);


        // root.getChildren().addAll(logo, playBtn, testCardView, testDeckView);

        return scene;
    }
}
