package com.neuilleprime.gui.screens;

// import java.util.ArrayList;

// import com.neuilleprime.game.Card;
// import com.neuilleprime.game.Deck;
// import com.neuilleprime.game.Pile;
// import com.neuilleprime.gui.components.CardView;
// import com.neuilleprime.gui.components.DeckView;
import com.neuilleprime.gui.components.JokerView;
// import com.neuilleprime.gui.components.PileView;
import com.neuilleprime.gui.components.VTextBox;
// import com.neuilleprime.gui.utils.AssetLoader;
import com.neuilleprime.gui.utils.ScreenManager;
import com.neuilleprime.gui.utils.SideBarsHelper;
import com.neuilleprime.jokers.AddXCardJoker;
import com.neuilleprime.jokers.AddXDeckJoker;

import javafx.geometry.Pos;
import javafx.scene.Scene;
// import javafx.scene.control.Button;
// import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

public class DebugScreen {

    private final ScreenManager sm;

    public DebugScreen(ScreenManager sm) {
        this.sm = sm;
    }

    public Scene buildScene() {
        BorderPane root = new BorderPane();

        // Scene scene = new Scene(root, 1280, 720);
        Scene scene = new Scene(root);
        scene.getStylesheets().add(getClass().getResource("/style.css").toExternalForm());

        root.getStyleClass().add("root");

        VBox leftBar = new VBox();
        leftBar.setAlignment(Pos.CENTER);
        leftBar.prefWidthProperty().bind(scene.widthProperty().multiply(SideBarsHelper.leftBarWidth));
        // leftBar.prefHeightProperty().bind(scene.heightProperty().multiply(1));

        VBox rightBar = new VBox();
        rightBar.setAlignment(Pos.CENTER);
        rightBar.prefWidthProperty().bind(scene.widthProperty().multiply(SideBarsHelper.rightBarWidth));
        // rightBar.prefHeightProperty().bind(scene.heightProperty().multiply(1));

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
        // centerBar.prefWidthProperty().bind(scene.widthProperty().multiply(1));
        // centerBar.prefHeightProperty().bind(scene.heightProperty().multiply(1));

        root.setLeft(leftBar);
        root.setRight(rightBar);
        root.setTop(topBar);
        root.setBottom(bottomBar);
        root.setCenter(centerBar);

        // ImageView logo = new ImageView(AssetLoader.NP_LOGO);
        // logo.setPreserveRatio(true);
        // logo.fitHeightProperty().bind(topBar.prefHeightProperty());

        // leftBar.getChildren().clear();

        // Button playBtn = new Button();
        // playBtn.setGraphic(new ImageView(AssetLoader.BUTTON_PLAY));
        // playBtn.setStyle("-fx-background-color: transparent;");
        // playBtn.setOnAction(e -> this.sm.show("game"));

        // leftBar.getChildren().add(playBtn);

        // System.out.println("hello");

        // Card testCard = new Card(6);
        // CardView testCardView = new CardView(testCard);
        // Card testCard2 = new Card(7);
        // CardView testCardView2 = new CardView(testCard2);

        // Deck testDeck = new Deck(4, 3);
        // boolean wholeDeckHidden = false;
        // ArrayList<Card> row1 = new ArrayList<>();
        // row1.add(new Card(1, wholeDeckHidden));
        // row1.add(new Card(2, wholeDeckHidden));
        // row1.add(new Card(3, wholeDeckHidden));
        // row1.add(new Card(4, wholeDeckHidden));
        // testDeck.addRow(row1);
        // ArrayList<Card> row2 = new ArrayList<>();
        // row2.add(new Card(5, wholeDeckHidden));
        // row2.add(new Card(6, wholeDeckHidden));
        // row2.add(new Card(7, wholeDeckHidden));
        // row2.add(new Card(8, wholeDeckHidden));
        // testDeck.addRow(row2);
        // ArrayList<Card> row3 = new ArrayList<>();
        // row3.add(new Card(9, wholeDeckHidden));
        // row3.add(new Card(10, wholeDeckHidden));
        // row3.add(new Card(11, wholeDeckHidden));
        // row3.add(new Card(12, wholeDeckHidden));
        // testDeck.addRow(row3);

        // DeckView testDeckView = new DeckView(testDeck);

        // Pile testPile = new Pile(15);
        // PileView testPileView = new PileView(testPile);
        
        // testPile.showAll();

        // leftBar.getChildren().addAll(testCardView, testCardView2);
        // rightBar.getChildren().add(testPileView);
        // topBar.getChildren().add(logo);

        leftBar.getChildren().clear();

        VTextBox moneyView = new VTextBox("Money");
        moneyView.setText(67+"₣");
        moneyView.setNameColor("#00a6ff");
        moneyView.setContentColor("#000000");
        moneyView.setBackgroundColor("#2da900");
        moneyView.setBorderColor("#217c00");
        moneyView.setContentSize(.1);
        moneyView.prefWidthProperty().bind(leftBar.prefWidthProperty());
        moneyView.prefHeightProperty().bind(leftBar.prefHeightProperty());
        moneyView.setOnMouseClicked(e -> {
            sm.show("menu");
        });

        leftBar.getChildren().add(moneyView);

        bottomBar.getChildren().clear();
        HBox jokerBar = new HBox();
        jokerBar.setAlignment(Pos.CENTER);
        jokerBar.prefWidthProperty().bind(bottomBar.widthProperty().multiply(.7));
        jokerBar.prefHeightProperty().bind(bottomBar.heightProperty().multiply(1));
        HBox consuBar = new HBox();
        consuBar.setAlignment(Pos.CENTER);
        consuBar.prefWidthProperty().bind(bottomBar.widthProperty().multiply(.3));
        consuBar.prefHeightProperty().bind(bottomBar.heightProperty().multiply(1));
        bottomBar.getChildren().addAll(jokerBar, consuBar);

        AddXCardJoker testJoker = new AddXCardJoker(3, false);
        JokerView testJokerView = new JokerView(testJoker);
        testJokerView.prefWidthProperty().bind(jokerBar.prefWidthProperty());
        testJokerView.prefHeightProperty().bind(jokerBar.prefHeightProperty());

        System.out.println(testJoker.getTextureName());

        jokerBar.getChildren().add(testJokerView);

        AddXDeckJoker testJoker2 = new AddXDeckJoker(3, false);
        JokerView testJokerView2 = new JokerView(testJoker2);
        testJokerView2.prefWidthProperty().bind(jokerBar.prefWidthProperty());
        testJokerView2.prefHeightProperty().bind(jokerBar.prefHeightProperty());

        jokerBar.getChildren().add(testJokerView2);

        // root.getChildren().addAll(logo, playBtn, testCardView, testDeckView);

        return scene;
    }
}
