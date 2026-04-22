package com.neuilleprime.gui.utils;

import com.neuilleprime.game.Player;
import com.neuilleprime.gui.components.JokerView;
import com.neuilleprime.gui.components.VTextBox;
import com.neuilleprime.jokers.Joker;

import javafx.geometry.Pos;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public class SideBarsHelper {
    public static final double topBarHeight = .1;
    public static final double topBarWidth = 1;
    public static final double bottomBarHeight = .2;
    public static final double bottomBarWidth = 1;
    public static final double leftBarWidth = .1;
    public static final double rightBarWidth = .1;

    public static void loadBottomBar(HBox bottomBar, Player player) {
        HBox jokerBar = new HBox();
        jokerBar.setAlignment(Pos.CENTER);
        jokerBar.prefWidthProperty().bind(bottomBar.prefWidthProperty().multiply(.7));
        jokerBar.prefHeightProperty().bind(bottomBar.prefHeightProperty().multiply(1));
        bottomBar.getChildren().add(jokerBar);
        HBox consuBar = new HBox();
        consuBar.setAlignment(Pos.CENTER);
        consuBar.prefWidthProperty().bind(bottomBar.widthProperty().multiply(.3));
        consuBar.prefHeightProperty().bind(bottomBar.heightProperty().multiply(1));
        bottomBar.getChildren().add(consuBar);

        for (Joker joker : player.getJokers()) {
            JokerView jokerView = new JokerView(joker);
            jokerView.prefWidthProperty().bind(jokerBar.prefWidthProperty());
            jokerView.prefHeightProperty().bind(jokerBar.prefHeightProperty());
            jokerBar.getChildren().add(jokerView);
        }
        for (Joker consu : player.getConsumables()) {
            JokerView jokerView = new JokerView(consu);
            jokerView.prefWidthProperty().bind(jokerBar.prefWidthProperty());
            jokerView.prefHeightProperty().bind(jokerBar.prefHeightProperty());
            jokerBar.getChildren().add(jokerView);
        }
    }

    public static void loadMoneyView(VBox leftBar, int amount) {
        VTextBox moneyView = new VTextBox("Money");
        moneyView.setText(amount+"₣");
        moneyView.setNameColor("#00a6ff");
        moneyView.setContentColor("#000000");
        moneyView.setBackgroundColor("#2da900");
        moneyView.setBorderColor("#217c00");
        moneyView.setContentSize(.1);
        moneyView.prefWidthProperty().bind(leftBar.prefWidthProperty());
        moneyView.prefHeightProperty().bind(leftBar.prefHeightProperty());

        leftBar.getChildren().add(moneyView);
    }
}
