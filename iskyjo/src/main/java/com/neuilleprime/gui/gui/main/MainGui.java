package com.neuilleprime.gui.main;

import com.neuilleprime.gui.game.GameCanvas;
import com.neuilleprime.gui.game.GameGui;
import com.neuilleprime.gui.model.GameState;
import com.neuilleprime.gui.shop.ShopGui;
import com.neuilleprime.gui.util.DiscordRpc;

import javafx.animation.AnimationTimer;
import javafx.animation.FadeTransition;
import javafx.animation.Interpolator;
import javafx.animation.ParallelTransition;
import javafx.animation.SequentialTransition;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.image.Image;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import javafx.util.Duration;

/**
 * Main application entry point.
 * <p>
 * This class now focuses on stage lifecycle, screen switching, splash handling,
 * and Discord presence synchronization. Rendering code has been moved to
 * {@link MainCanvas} and {@link SplashCanvas}.
 */
public class MainGui extends Application {

    private static Font customFont;
    private static Font customFontBold;

    static {
        try {
            var stream = MainGui.class.getResourceAsStream("/Assets/Fonts/VCR_OSD_MONO.ttf");
            if (stream == null) {
                throw new RuntimeException("Font file not found: /Assets/Fonts/VCR_OSD_MONO.ttf");
            }
            customFont = Font.loadFont(stream, 14);
            if (customFont == null) {
                throw new RuntimeException("JavaFX could not load the font");
            }
            customFontBold = Font.font(customFont.getFamily(), FontWeight.BOLD, 14);
        } catch (Exception e) {
            customFont = Font.font("Comic Sans MS", FontWeight.NORMAL, 14);
            customFontBold = Font.font("Comic Sans MS", FontWeight.BOLD, 14);
            System.err.println("Failed loading font: " + e.getMessage());
        }
    }

    private Stage mainStage;
    private MainCanvas mainCanvas;
    private SplashCanvas splashCanvas;
    private StackPane root;
    private Rectangle transitionOverlay;
    private GameCanvas gameCanvas;
    private ShopGui.ShopCanvas shopCanvas;

    private AnimationTimer splashTimer;
    private boolean splashSkipped = false;
    private boolean isTransitionRunning = false;
    private GameState activeGameState;
    private AnimationTimer discordSyncTimer;
    private long lastDiscordSyncNs = 0L;
    private String lastPresenceKey = "";

    /**
     * Starts the main application window, initialises the menu and splash screens,
     * and configures Discord Rich Presence synchronisation.
     *
     * @param primaryStage the primary JavaFX stage
     */
    @Override
    public void start(Stage primaryStage) {
        this.mainStage = primaryStage;

        mainStage.setTitle("Iskyjo");
        mainStage.setWidth(1400);
        mainStage.setHeight(900);
        mainStage.setResizable(true);

        try {
            var iconUrl = getClass().getResource("/Assets/NP_icon.png");
            if (iconUrl != null) {
                primaryStage.getIcons().add(new Image(iconUrl.toExternalForm()));
            }
        } catch (Exception ignored) {
        }

        mainCanvas = new MainCanvas(
            mainStage,
            customFont,
            customFontBold,
            () -> {
                GameState demoState = GameGui.buildDemoState();
                startGameSession(demoState, true);
            }
        );
        splashCanvas = new SplashCanvas(customFont, customFontBold);

        mainCanvas.setRevealProgress(0.0f);

        transitionOverlay = new Rectangle();
        transitionOverlay.setManaged(false);
        transitionOverlay.setMouseTransparent(true);
        transitionOverlay.setVisible(false);
        transitionOverlay.setOpacity(0.0);
        transitionOverlay.widthProperty().bind(mainStage.widthProperty());
        transitionOverlay.heightProperty().bind(mainStage.heightProperty());

        root = new StackPane(mainCanvas, splashCanvas, transitionOverlay);
        root.setStyle("-fx-background-color: black;");

        bindCanvasToStage(mainCanvas);
        bindCanvasToStage(splashCanvas);

        Scene scene = new Scene(root, 1400, 900);
        mainStage.setScene(scene);
        mainStage.centerOnScreen();
        mainStage.show();

        mainStage.setOnCloseRequest(e -> {
            try {
                if (discordSyncTimer != null) {
                    discordSyncTimer.stop();
                }

                DiscordRpc.clearPresence();
                DiscordRpc.shutdown();
            } catch (Exception ex) {
                ex.printStackTrace();
            }

            Platform.exit();
            System.exit(0);
        });

        DiscordRpc.init();
        startDiscordAutoSync();
        syncDiscordPresence();

        showSplashOverlay();
    }

/**
     * Opens the game screen for the provided state.
     *
     * @param state the game state to display
     * @param debug whether debug controls should be enabled
     */
    private void startGameSession(GameState state, boolean debug) {
        if (isTransitionRunning || gameCanvas != null) return;

        activeGameState = state;
        lastPresenceKey = "";

        gameCanvas = new GameCanvas(
            mainStage,
            state,
            customFont,
            customFontBold,
            this::endGameSession,
            this::openShopSession,
            debug
        );
        bindCanvasToStage(gameCanvas);
        gameCanvas.setOpacity(0.0);
        root.getChildren().add(gameCanvas);
        gameCanvas.render();

        mainCanvas.pauseForGame();
        animateSwitch(mainCanvas, gameCanvas, true, Color.web("#243764"), () -> {
            mainCanvas.setVisible(false);
            gameCanvas.requestFocus();
        });
    }

/**
     * Switches from the game screen to the shop screen.
     */
    private void openShopSession() {
        if (isTransitionRunning || gameCanvas == null) return;

        if (shopCanvas == null) {
            shopCanvas = new ShopGui.ShopCanvas(
                mainStage,
                customFont,
                customFontBold,
                this::closeShopSession,
                this::endGameSession,
                true
            );
            bindCanvasToStage(shopCanvas);
        }

        if (!root.getChildren().contains(shopCanvas)) {
            shopCanvas.setOpacity(0.0);
            root.getChildren().add(shopCanvas);
        }
        shopCanvas.render();
        animateSwitch(gameCanvas, shopCanvas, false, Color.BLACK, shopCanvas::requestFocus);
    }

/**
     * Closes the shop screen and returns to the game screen.
     */
    private void closeShopSession() {
        if (isTransitionRunning || shopCanvas == null || gameCanvas == null) return;

        if (!root.getChildren().contains(gameCanvas)) {
            gameCanvas.setOpacity(0.0);
            root.getChildren().add(gameCanvas);
        }
        gameCanvas.render();
        animateSwitch(shopCanvas, gameCanvas, false, Color.BLACK, gameCanvas::requestFocus);
    }

/**
     * Ends the current game session and returns to the main menu.
     */
    private void endGameSession() {
        if (isTransitionRunning) return;

        Platform.runLater(() -> {
            if (shopCanvas != null) {
                root.getChildren().remove(shopCanvas);
                shopCanvas = null;
            }
            if (gameCanvas != null) {
                root.getChildren().remove(gameCanvas);
                gameCanvas = null;
            }

            activeGameState = null;
            lastPresenceKey = "";

            syncDiscordPresence();

            mainCanvas.setVisible(true);
            mainCanvas.setOpacity(1.0);
            mainCanvas.restartMain();
            mainCanvas.requestFocus();
        });
    }

/**
     * Plays a fade transition between two screens.
     *
     * @param oldNode the currently visible node
     * @param newNode the node that should become visible
     * @param keepOldNode whether to keep the old node in the scene graph after the switch
     * @param transitionTint the tint applied during the transition overlay
     * @param afterTransition callback executed once the transition finishes
     */
    private void animateSwitch(Node oldNode, Node newNode, boolean keepOldNode, Color transitionTint, Runnable afterTransition) {
        isTransitionRunning = true;

        if (newNode == mainCanvas) {
            mainCanvas.setVisible(true);
        }

        newNode.toFront();
        transitionOverlay.setFill(transitionTint);
        transitionOverlay.setOpacity(0.0);
        transitionOverlay.setVisible(true);
        transitionOverlay.toFront();

        FadeTransition coverIn = new FadeTransition(Duration.millis(120), transitionOverlay);
        coverIn.setFromValue(0.0);
        coverIn.setToValue(0.12);
        coverIn.setInterpolator(Interpolator.EASE_BOTH);

        FadeTransition fadeOut = new FadeTransition(Duration.millis(240), oldNode);
        fadeOut.setFromValue(oldNode.getOpacity());
        fadeOut.setToValue(0.0);
        fadeOut.setInterpolator(Interpolator.EASE_BOTH);

        FadeTransition fadeIn = new FadeTransition(Duration.millis(240), newNode);
        fadeIn.setFromValue(newNode.getOpacity());
        fadeIn.setToValue(1.0);
        fadeIn.setInterpolator(Interpolator.EASE_BOTH);

        ParallelTransition swap = new ParallelTransition(fadeOut, fadeIn);

        FadeTransition coverOut = new FadeTransition(Duration.millis(160), transitionOverlay);
        coverOut.setFromValue(0.12);
        coverOut.setToValue(0.0);
        coverOut.setInterpolator(Interpolator.EASE_BOTH);

        SequentialTransition transition = new SequentialTransition(coverIn, swap, coverOut);
        transition.setOnFinished(e -> {
            if (!keepOldNode) {
                root.getChildren().remove(oldNode);
            } else {
                oldNode.setOpacity(1.0);
                if (oldNode == mainCanvas) {
                    mainCanvas.setVisible(false);
                }
            }

            newNode.setOpacity(1.0);
            transitionOverlay.setOpacity(0.0);
            transitionOverlay.setVisible(false);
            isTransitionRunning = false;

            if (afterTransition != null) {
                afterTransition.run();
            }
        });
        transition.play();
    }

/**
     * Binds a canvas size to the current stage dimensions.
     *
     * @param canvas the canvas to bind
     */
    private void bindCanvasToStage(Canvas canvas) {
        canvas.widthProperty().bind(mainStage.widthProperty());
        canvas.heightProperty().bind(mainStage.heightProperty());
    }

/**
     * Synchronises the Discord Rich Presence with the currently visible screen.
     */
    private void syncDiscordPresence() {
        if (!DiscordRpc.isReady()) {
            return;
        }

        String newPresenceKey;

        if (shopCanvas != null && root.getChildren().contains(shopCanvas)) {
            newPresenceKey = "shop";
            if (!newPresenceKey.equals(lastPresenceKey)) {
                DiscordRpc.setShopPresence();
                lastPresenceKey = newPresenceKey;
            }
            return;
        }

        if (gameCanvas != null && activeGameState != null) {
            newPresenceKey = "game:" + activeGameState.scoreToBeat + ":" + activeGameState.playerScore;
            if (!newPresenceKey.equals(lastPresenceKey)) {
                DiscordRpc.setGamePresence(activeGameState.scoreToBeat, activeGameState.playerScore);
                lastPresenceKey = newPresenceKey;
            }
            return;
        }

        newPresenceKey = "menu";
        if (!newPresenceKey.equals(lastPresenceKey)) {
            DiscordRpc.setMainMenuPresence();
            lastPresenceKey = newPresenceKey;
        }
    }

/**
     * Starts a periodic timer that refreshes Discord presence state.
     */
    private void startDiscordAutoSync() {
        discordSyncTimer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                if (now - lastDiscordSyncNs < 1_500_000_000L) {
                    return;
                }
                lastDiscordSyncNs = now;
                syncDiscordPresence();
            }
        };
        discordSyncTimer.start();
    }

/**
     * Displays and animates the startup splash overlay.
     */
    private void showSplashOverlay() {
        splashSkipped = false;

        root.setOnMousePressed(e -> {
            if (!splashSkipped) {
                splashSkipped = true;
                skipSplash();
            }
        });

        final long[] lastTick = {0L};
        final long frameNs = 30_000_000L;

        splashTimer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                if (splashSkipped) {
                    stop();
                    return;
                }
                if (now - lastTick[0] < frameNs) return;
                lastTick[0] = now;

                splashCanvas.nextFrame();
                splashCanvas.render();

                if (splashCanvas.isMainFrameRevealPhase()) {
                    double reveal = 1.0 - splashCanvas.getAlpha();
                    mainCanvas.setRevealProgress((float) clamp(reveal));
                }

                if (splashCanvas.isFinished()) {
                    mainCanvas.setRevealProgress(1.0f);
                    stop();
                    root.getChildren().remove(splashCanvas);
                    splashCanvas = null;
                    mainCanvas.startMain();
                    mainCanvas.requestFocus();
                }
            }
        };
        splashTimer.start();
    }

/**
     * Immediately removes the splash screen and reveals the main menu.
     */
    private void skipSplash() {
        if (splashTimer != null) {
            splashTimer.stop();
            splashTimer = null;
        }
        mainCanvas.setRevealProgress(1.0f);
        if (splashCanvas != null) {
            root.getChildren().remove(splashCanvas);
            splashCanvas = null;
        }
        mainCanvas.startMain();
        mainStage.toFront();
        mainStage.requestFocus();
    }

/**
     * Clamps the provided value to the {@code [0, 1]} range.
     *
     * @param v the value to clamp
     * @return the clamped value
     */
    private static double clamp(double v) {
        return Math.max(0.0, Math.min(1.0, v));
    }

/**
     * Launches the JavaFX application.
     *
     * @param args command-line arguments passed to JavaFX
     */
    public static void main(String[] args) {
        launch(args);
    }
}
