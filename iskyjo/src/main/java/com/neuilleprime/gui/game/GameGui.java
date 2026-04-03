package com.neuilleprime.gui.game;

import com.neuilleprime.gui.model.Card;
import com.neuilleprime.gui.model.CardGrid;
import com.neuilleprime.gui.model.GameState;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

/**
 * Standalone JavaFX launcher for the in-game screen.
 * <p>
 * The actual rendering logic now lives in {@link GameCanvas}, while the game data
 * model has been moved to dedicated top-level classes ({@link Card}, {@link CardGrid},
 * and {@link GameState}).
 */
public class GameGui extends Application {

    private static Font mainFont;
    private static Font mainFontBold;

    static {
        try {
            var stream = GameGui.class.getResourceAsStream("/Assets/Fonts/VCR_OSD_MONO.ttf");
            if (stream == null) throw new RuntimeException("Font [VCR OSD MONO] not found");
            mainFont = Font.loadFont(stream, 14);
            if (mainFont == null) throw new RuntimeException("Font load failed");
            mainFontBold = Font.font(mainFont.getFamily(), FontWeight.BOLD, 14);
        } catch (Exception e) {
            mainFont = Font.font("Courier New", FontWeight.NORMAL, 14);
            mainFontBold = Font.font("Courier New", FontWeight.BOLD, 14);
            System.err.println("[GameGui] Font fallback: " + e.getMessage());
        }
    }

    /**
     * Creates a ready-to-use demo state for UI testing and previews.
     *
     * @return a populated demo {@link GameState}
     */
    public static GameState buildDemoState() {
        GameState state = new GameState(4, 3);

        state.grid.set(0, 0, new Card(6, false));
        state.grid.set(0, 1, new Card(6, true));
        state.grid.set(0, 2, new Card(6, true));
        state.grid.set(1, 1, new Card(6, true));
        state.grid.set(2, 1, new Card(6, false));
        state.grid.set(3, 2, new Card(7, false));
        state.grid.set(3, 1, new Card(-2, true));
        state.grid.set(2, 2, new Card(11, true));

        state.jokers.add(new Card(0, true));
        state.jokers.add(new Card(0, true));
        state.consumables.add(new Card(0, true));

        state.drawPile = new Card(5, false);
        state.discardPile = new Card(3, true);
        state.scoreToBeat = 676941;
        state.playerScore = 42087;
        return state;
    }

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Iskyjo - In game");
        primaryStage.setWidth(1400);
        primaryStage.setHeight(900);
        primaryStage.setResizable(true);

        try {
            var iconUrl = getClass().getResource("/Assets/NP_icon.png");
            if (iconUrl != null) {
                primaryStage.getIcons().add(new Image(iconUrl.toExternalForm()));
            }
        } catch (Exception ignored) {
        }

        GameCanvas gameCanvas = new GameCanvas(
            primaryStage,
            buildDemoState(),
            mainFont,
            mainFontBold,
            Platform::exit,
            () -> {},
            true
        );

        StackPane root = new StackPane(gameCanvas);
        root.setStyle("-fx-background-color: #1d2b53;");
        gameCanvas.widthProperty().bind(primaryStage.widthProperty());
        gameCanvas.heightProperty().bind(primaryStage.heightProperty());

        primaryStage.setScene(new Scene(root, 1400, 900));
        primaryStage.centerOnScreen();
        primaryStage.show();
        gameCanvas.render();
    }

    /**
     * Launches the standalone game window.
     *
     * @param args JavaFX command-line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }
}
