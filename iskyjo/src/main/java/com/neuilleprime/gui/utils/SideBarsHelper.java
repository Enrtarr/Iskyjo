package com.neuilleprime.gui.utils;

import java.util.ArrayList;

import com.neuilleprime.game.Player;
import com.neuilleprime.gui.components.JokerView;
import com.neuilleprime.gui.components.VTextBox;
import com.neuilleprime.jokers.Joker;

import javafx.geometry.Pos;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

/**
 * Utility class providing factory methods that populate the shared side-bar
 * regions (top, bottom, left, right) used across multiple screens.
 * <p>
 * All layout proportions are defined as static constants so screens can bind
 * their bars to consistent fractions of the window dimensions.
 * </p>
 */
public class SideBarsHelper {

    /** Fraction of window height occupied by the top bar. */
    public static final double topBarHeight = .1;

    /** Fraction of window width occupied by the top bar. */
    public static final double topBarWidth = 1;

    /** Fraction of window height occupied by the bottom bar. */
    public static final double bottomBarHeight = .2;

    /** Fraction of window width occupied by the bottom bar. */
    public static final double bottomBarWidth = 1;

    /** Fraction of window width occupied by the left bar. */
    public static final double leftBarWidth = .1;

    /** Fraction of window width occupied by the right bar. */
    public static final double rightBarWidth = .1;

    /**
     * Populates the bottom bar with the player's joker and consumable views.
     * <p>
     * The bar is split 70 / 30 between jokers and consumables. A {@link JokerView}
     * is created for each joker and consumable, bound to the bar's dimensions.
     * </p>
     *
     * @param bottomBar the {@link HBox} to populate
     * @param player    the player whose jokers and consumables to display
     * @return the list of {@link JokerView}s created for the player's jokers
     *         (does not include consumable views)
     */
    public static ArrayList<JokerView> loadBottomBar(HBox bottomBar, Player player) {
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

        ArrayList<JokerView> jokerViews = new ArrayList<>();

        for (Joker joker : player.getJokers()) {
            JokerView jokerView = new JokerView(joker);
            jokerView.prefWidthProperty().bind(jokerBar.prefWidthProperty());
            jokerView.prefHeightProperty().bind(jokerBar.prefHeightProperty());
            jokerBar.getChildren().add(jokerView);
            jokerViews.add(jokerView);
        }
        for (Joker consu : player.getConsumables()) {
            JokerView jokerView = new JokerView(consu);
            jokerView.prefWidthProperty().bind(jokerBar.prefWidthProperty());
            jokerView.prefHeightProperty().bind(jokerBar.prefHeightProperty());
            jokerBar.getChildren().add(jokerView);
        }

        return jokerViews;
    }

    /**
     * Appends a money display widget to the given left bar.
     * <p>
     * The widget is a {@link VTextBox} styled in green showing the player's
     * current money balance with a currency symbol (₣).
     * </p>
     *
     * @param leftBar the {@link VBox} to append the money view to
     * @param amount  the money amount to display
     */
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
