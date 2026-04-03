package com.neuilleprime.gui;

import com.neuilleprime.gui.DiscordRpc;

import javafx.animation.AnimationTimer;
import javafx.animation.FadeTransition;
import javafx.animation.Interpolator;
import javafx.animation.ParallelTransition;
import javafx.animation.SequentialTransition;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;

/**
 * Main entry point and top-level GUI controller for the Iskyjo application.
 *
 * <p>This class extends {@link Application} and is responsible for bootstrapping the
 * JavaFX window, loading fonts, orchestrating the splash screen, main menu, game session,
 * and shop session, and keeping the Discord Rich Presence in sync with the current
 * application state.</p>
 *
 * <p>Application flow:</p>
 * <ol>
 *   <li>Static initializer loads the custom VCR OSD Mono font (falls back to Comic Sans MS).</li>
 *   <li>{@link #start(Stage)} builds the scene graph and shows the splash overlay.</li>
 *   <li>After the splash finishes (or is skipped), the main menu becomes interactive.</li>
 *   <li>Clicking "Play" calls {@link #startGameSession(GameGui.GameState, boolean)}.</li>
 *   <li>From within the game the shop can be opened via {@link #openShopSession()} and
 *       closed via {@link #closeShopSession()}.</li>
 *   <li>Returning to the menu is handled by {@link #endGameSession()}.</li>
 * </ol>
 */
public class MainGui extends Application {

    /** Base (regular-weight) instance of the custom application font at 14 pt. */
    private static Font customFont;

    /** Bold-weight instance of the custom application font at 14 pt. */
    private static Font customFontBold;

    /*
     * Static font loader.
     * Attempts to load VCR_OSD_MONO.ttf from the classpath.
     * Falls back to Comic Sans MS if the resource is missing or JavaFX rejects the file.
     */
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

    /** The primary application window. */
    private Stage mainStage;

    /** Canvas that renders the main menu. */
    private MainCanvas mainCanvas;

    /** Canvas that renders the startup splash screen. */
    private SplashCanvas splashCanvas;

    /** Root layout node; children are layered via a {@link StackPane}. */
    private StackPane root;

    /**
     * Full-screen overlay rectangle used during animated scene transitions.
     * It is normally invisible and is brought to the front only for the duration
     * of a fade transition to soften the swap between canvases.
     */
    private Rectangle transitionOverlay;

    /** Canvas responsible for rendering the active game session, or {@code null} when no game is running. */
    private GameGui.GameCanvas gameCanvas;

    /** Canvas responsible for rendering the shop overlay, or {@code null} when the shop is closed. */
    private ShopGui.ShopCanvas shopCanvas;

    /** Timer that drives the splash-screen frame loop. Stopped once the splash finishes or is skipped. */
    private AnimationTimer splashTimer;

    /** {@code true} once the user has clicked to skip the splash animation. */
    private boolean splashSkipped = false;

    /** Guards against overlapping scene transitions; set to {@code true} while an animation is in progress. */
    private boolean isTransitionRunning = false;

    /** The {@link GameGui.GameState} for the currently active game session, or {@code null} when idle. */
    private GameGui.GameState activeGameState;

    /** Timer that periodically calls {@link #syncDiscordPresence()} (~every 1.5 s). */
    private AnimationTimer discordSyncTimer;

    /** Timestamp of the last Discord sync in nanoseconds, used to throttle updates. */
    private long lastDiscordSyncNs = 0L;

    /**
     * Cache key representing the presence state that was last pushed to Discord.
     * Compared before each sync to avoid redundant API calls.
     */
    private String lastPresenceKey = "";

    // Startup ----------------------------------------------------------------------------

    /**
     * JavaFX lifecycle entry point. Builds the scene graph, configures the primary stage,
     * starts Discord Rich Presence, and shows the splash screen.
     *
     * @param primaryStage the window provided by the JavaFX runtime
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
            GameGui.GameState demoState = GameGui.buildDemoState();
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

    // Session flow ----------------------------------------------------------------------------

    /**
     * Creates a new game canvas from the given state, fades out the main menu, and fades in
     * the game view. Does nothing if a transition is already running or a game is already active.
     *
     * @param state the initial {@link GameGui.GameState} to play
     * @param debug {@code true} to enable debug overlays inside the game canvas
     */
    private void startGameSession(GameGui.GameState state, boolean debug) {
        if (isTransitionRunning || gameCanvas != null) return;

        activeGameState = state;
        lastPresenceKey = "";

        gameCanvas = new GameGui.GameCanvas(
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
     * Opens the shop overlay on top of the current game session.
     * Creates the {@link ShopGui.ShopCanvas} on first call and reuses it on subsequent calls.
     * Does nothing if a transition is running or there is no active game session.
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
        animateSwitch(gameCanvas, shopCanvas, false, Color.BLACK, () -> {
            shopCanvas.requestFocus();
        });
    }

    /**
     * Closes the shop overlay and returns focus to the game canvas.
     * Does nothing if a transition is running, or if either the shop or game canvas is absent.
     */
    private void closeShopSession() {
        if (isTransitionRunning || shopCanvas == null || gameCanvas == null) return;

        if (!root.getChildren().contains(gameCanvas)) {
            gameCanvas.setOpacity(0.0);
            root.getChildren().add(gameCanvas);
        }
        gameCanvas.render();
        animateSwitch(shopCanvas, gameCanvas, false, Color.BLACK, () -> {
            gameCanvas.requestFocus();
        });
    }

    /**
     * Terminates the active game (and shop, if open), removes their canvases from the scene,
     * and returns the application to the animated main menu.
     * Schedules UI work on the JavaFX application thread via {@link Platform#runLater(Runnable)}.
     * Does nothing if a transition is already running.
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

            // Ensure Discord presence is updated when returning to menu
            syncDiscordPresence();

            mainCanvas.setVisible(true);
            mainCanvas.setOpacity(1.0);
            mainCanvas.restartMain();
            mainCanvas.requestFocus();
        });

    }

    /**
     * Performs an animated cross-fade between two scene nodes, optionally tinting the screen
     * with a semi-transparent colour overlay during the swap.
     *
     * <p>Sequence:</p>
     * <ol>
     *   <li>Fade the tint overlay in (120 ms).</li>
     *   <li>Fade {@code oldNode} out and {@code newNode} in simultaneously (240 ms each).</li>
     *   <li>Fade the tint overlay out (160 ms).</li>
     * </ol>
     *
     * @param oldNode        the node currently visible that should fade out
     * @param newNode        the node that should fade in
     * @param keepOldNode    if {@code true} the old node remains in the scene graph (opacity
     *                       restored to 1.0, but hidden if it is the main canvas);
     *                       if {@code false} it is removed from {@code root}
     * @param transitionTint the fill colour of the full-screen tint overlay
     * @param afterTransition optional callback invoked on the JavaFX thread after the animation
     *                        completes; may be {@code null}
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
     * Binds the width and height of {@code canvas} to the current dimensions of
     * {@link #mainStage} so the canvas always fills the window.
     *
     * @param canvas the canvas whose size properties should track the stage
     */
    private void bindCanvasToStage(Canvas canvas) {
        canvas.widthProperty().bind(mainStage.widthProperty());
        canvas.heightProperty().bind(mainStage.heightProperty());
    }

    /**
     * Determines the correct Discord Rich Presence state based on which canvas is currently
     * active and pushes an update only when the state has changed since the last call.
     *
     * <p>Priority order:</p>
     * <ol>
     *   <li>Shop view → {@link DiscordRpc#setShopPresence()}</li>
     *   <li>Game view → {@link DiscordRpc#setGamePresence(int, int)}</li>
     *   <li>Main menu → {@link DiscordRpc#setMainMenuPresence()}</li>
     * </ol>
     *
     * <p>The method is a no-op when {@link DiscordRpc#isReady()} returns {@code false}.</p>
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
     * Starts an {@link AnimationTimer} that calls {@link #syncDiscordPresence()} approximately
     * every 1.5 seconds (1 500 000 000 ns). This keeps the Discord presence reasonably fresh
     * even when game score changes are not explicitly triggering a sync.
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

    // Splash ----------------------------------------------------------------------------

    /**
     * Attaches the splash canvas animation loop to the scene and starts playing it.
     * The splash consists of a fade-in, hold, and fade-out phase driven by
     * {@link SplashCanvas#nextFrame()}. During the fade-out phase the main canvas is
     * gradually revealed via {@link MainCanvas#setRevealProgress(float)}.
     *
     * <p>A mouse press on the root pane at any point skips the animation immediately.</p>
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
     * Immediately jumps past the splash animation: stops the splash timer, fully reveals the
     * main canvas, removes the splash canvas from the scene, and starts the main menu loop.
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
     * Clamps a {@code double} value to the range [0.0, 1.0].
     *
     * @param v the value to clamp
     * @return the clamped value
     */
    private static double clamp(double v) {
        return Math.max(0.0, Math.min(1.0, v));
    }

    /**
     * Application entry point. Delegates to {@link Application#launch(String...)}.
     *
     * @param args command-line arguments (not used)
     */
    public static void main(String[] args) {
        launch(args);
    }

    // Splash canvas ----------------------------------------------------------------------------

    /**
     * Full-screen canvas that renders the studio splash screen shown at application startup.
     *
     * <p>The animation is divided into three phases driven by an external frame counter:</p>
     * <ol>
     *   <li><b>Fade-in</b> ({@value #FADE_IN_FRAMES} frames) – alpha goes from 0 → 1.</li>
     *   <li><b>Hold</b> ({@value #HOLD_FRAMES} frames) – alpha stays at 1.</li>
     *   <li><b>Fade-out</b> ({@value #FADE_OUT_FRAMES} frames) – alpha goes from 1 → 0.</li>
     * </ol>
     *
     * <p>The caller advances the frame counter by calling {@link #nextFrame()} once per
     * render tick and then calls {@link #render()} to draw the current frame.</p>
     */
    static class SplashCanvas extends Canvas {
        /** Number of frames for the alpha fade-in phase. */
        private static final int FADE_IN_FRAMES = 40;

        /** Number of frames the splash is held at full opacity. */
        private static final int HOLD_FRAMES = 100;

        /** Number of frames for the alpha fade-out phase. */
        private static final int FADE_OUT_FRAMES = 40;

        /** Current frame index, incremented by {@link #nextFrame()}. */
        private int frame = 0;

        /** Studio logo image, or {@code null} if the asset failed to load. */
        private final Image logo;

        /** Large bold font used for the studio/team name labels. */
        private final Font mainFont;

        /** Smaller regular font used for the subtitle labels beneath the studio names. */
        private final Font subFont;

        /**
         * Constructs the splash canvas and loads the NP_logo asset.
         *
         * @param baseFont     the application base font (used to derive the splash fonts)
         * @param baseFontBold the bold variant of the application base font
         */
        SplashCanvas(Font baseFont, Font baseFontBold) {
            Image img = null;
            try {
                var url = MainGui.class.getResource("/Assets/NP_logo.png");
                if (url == null) throw new RuntimeException("Missing asset: /Assets/NP_logo.png");
                img = new Image(url.toExternalForm());
            } catch (Exception e) {
                System.out.println("Failed loading splash logo: " + e.getMessage());
            }

            logo = img;
            mainFont = Font.font(baseFont.getFamily(), FontWeight.BOLD, 42);
            subFont = Font.font(baseFont.getFamily(), FontWeight.NORMAL, 18);
        }

        /**
         * Advances the internal frame counter by one. Must be called exactly once per
         * render tick before calling {@link #render()}.
         */
        public void nextFrame() {
            frame++;
        }

        /**
         * Calculates the current opacity of the splash overlay based on the frame counter.
         *
         * @return a value in [0.0, 1.0] representing the current alpha
         */
        public float getAlpha() {
            if (frame <= FADE_IN_FRAMES) {
                return (float) frame / (float) FADE_IN_FRAMES;
            }
            if (frame <= FADE_IN_FRAMES + HOLD_FRAMES) {
                return 1.0f;
            }
            int fadeOutFrame = frame - FADE_IN_FRAMES - HOLD_FRAMES;
            if (fadeOutFrame <= FADE_OUT_FRAMES) {
                return 1.0f - (float) fadeOutFrame / (float) FADE_OUT_FRAMES;
            }
            return 0.0f;
        }

        /**
         * Returns {@code true} once all three animation phases have elapsed and the
         * splash screen can be removed from the scene.
         *
         * @return {@code true} if the animation has completed
         */
        public boolean isFinished() {
            return frame > FADE_IN_FRAMES + HOLD_FRAMES + FADE_OUT_FRAMES;
        }

        /**
         * Returns {@code true} during the fade-out phase, indicating that the main canvas
         * should start being revealed in parallel with the splash fading away.
         *
         * @return {@code true} if the splash is currently in the fade-out (main-reveal) phase
         */
        public boolean isMainFrameRevealPhase() {
            return frame > FADE_IN_FRAMES + HOLD_FRAMES;
        }

        /**
         * Draws the current splash frame onto this canvas. Renders a black background, the
         * studio logo centred on screen, and two flanking text labels (left and right) at the
         * opacity returned by {@link #getAlpha()}.
         */
        public void render() {
            double w = getWidth();
            double h = getHeight();
            double cx = w / 2.0;
            double cy = h / 2.0;

            GraphicsContext canva = getGraphicsContext2D();
            canva.clearRect(0, 0, w, h);
            canva.setFill(Color.BLACK);
            canva.fillRect(0, 0, w, h);

            float alpha = Math.max(0.0f, Math.min(1.0f, getAlpha()));
            canva.setGlobalAlpha(alpha);

            double logoW = 0;
            double logoH = 0;
            if (logo != null) {
                logoW = logo.getWidth();
                logoH = logo.getHeight();
                canva.drawImage(logo, cx - logoW / 2.0, cy - logoH / 2.0);
            }

            canva.setFill(Color.WHITE);
            int spacing = 160;

            String leftMain = "Enrtarr";
            String leftSub = "Game";
            double leftMainW = measureText(leftMain, mainFont);
            double leftSubW = measureText(leftSub, subFont);
            double leftX = cx - logoW / 2.0 - spacing - Math.max(leftMainW, leftSubW);

            canva.setFont(mainFont);
            canva.fillText(leftMain, leftX, cy - 5);
            canva.setFont(subFont);
            canva.fillText(leftSub, leftX + (leftMainW - leftSubW) / 2.0, cy + 17);

            String rightMain = "Maaple";
            String rightSub = "GUI";
            double rightMainW = measureText(rightMain, mainFont);
            double rightSubW = measureText(rightSub, subFont);
            double rightX = cx + logoW / 2.0 + spacing;

            canva.setFont(mainFont);
            canva.fillText(rightMain, rightX, cy - 5);
            canva.setFont(subFont);
            canva.fillText(rightSub, rightX + (rightMainW - rightSubW) / 2.0, cy + 17);

            canva.setGlobalAlpha(1.0);
        }

        /**
         * Measures the rendered pixel width of the given string in the given font.
         *
         * @param s the string to measure
         * @param f the font to use for measurement
         * @return the width in pixels
         */
        private double measureText(String s, Font f) {
            Text t = new Text(s);
            t.setFont(f);
            return t.getBoundsInLocal().getWidth();
        }
    }

    // Main canvas ----------------------------------------------------------------------------

    /**
     * Canvas that renders the main menu screen, including the animated card-pattern background,
     * the game title with a gentle float animation, and the Play / Leaderboard buttons.
     *
     * <p>The canvas manages its own {@link AnimationTimer} which is started by
     * {@link #startMain()}, paused by {@link #pauseForGame()}, and restarted by
     * {@link #restartMain()}. Hover states are tracked internally and drive smooth colour
     * interpolations via {@link #approach(double, double, double)}.</p>
     *
     * <p>Window-control buttons (minimise and close) are drawn in the top-right corner and
     * handled through hit-test helpers. The close button also shuts down Discord RPC.</p>
     */
    static class MainCanvas extends Canvas {
        /** Pixel width of each menu button. */
        private static final int MENU_BUTTON_W = 240;

        /** Pixel height of each menu button. */
        private static final int MENU_BUTTON_H = 65;

        /** Horizontal gap between the two menu buttons. */
        private static final int MENU_BUTTON_GAP = 70;

        /** Width of each card tile used in the scrolling background pattern. */
        private static final int CARD_WIDTH = 82;

        /** Height of each card tile used in the scrolling background pattern. */
        private static final int CARD_HEIGHT = 114;

        /** Gap between card tiles in the scrolling background. */
        private static final int SPACING = 20;

        /** Speed factor applied to the background card scroll animation (pixels per frame). */
        private static final float ANIMATION_SPEED = 1.5f;

        /** Base opacity of the card-pattern background layer. */
        private static final float CARD_OPACITY_BASE = 0.05f;

        /** Hit-box size (width and height) for the window control buttons. */
        private static final int BUTTON_HIT_SIZE = 28;

        /** Top margin from the window edge to the window control buttons. */
        private static final int BUTTON_TOP = 8;

        /** Right margin from the window edge to the close button. */
        private static final int BUTTON_RIGHT = 10;

        /** Horizontal gap between the minimise and close window control buttons. */
        private static final int BUTTON_GAP = 6;

        /** Default background colour of the main menu. */
        private static final Color BG_BASE = Color.web("#1d2b53");

        /** Overlay colour blended in when the Play button is hovered. */
        private static final Color BG_PLAY = Color.web("#1e532c");

        /** Overlay colour blended in when the Leaderboard button is hovered. */
        private static final Color BG_LEADERBOARD = Color.web("#534b1f");

        /** Reference to the primary stage; used for minimise/close actions. */
        private final Stage stage;

        /** Regular-weight application font. */
        private final Font fontBase;

        /** Bold-weight application font. */
        private final Font fontBaseBold;

        /** Callback invoked when the user clicks the Play button. */
        private final Runnable onPlay;

        /** Timer driving the main menu render loop; {@code null} when paused. */
        private AnimationTimer mainTimer;

        /** Frame counter incremented every tick of {@link #mainTimer}. */
        private int mainCounter = 0;

        /** Current reveal progress (0–1) controlling how much of the menu is visible after the splash. */
        private float revealProgress = 0.0f;

        /**
         * Alpha multiplier for the background layer, animated from 0 → 1 during the menu intro.
         */
        private float backgroundAlpha = 0.0f;

        /**
         * Alpha multiplier for the title text, animated from 0 → 1 during the menu intro.
         */
        private float titleAlpha = 0.0f;

        /**
         * Alpha multiplier for the menu buttons, animated from 0 → 1 during the menu intro.
         */
        private float buttonsAlpha = 0.0f;

        /** Card tile image used for the scrolling background, or {@code null} on load failure. */
        private Image cardPattern;

        /** Play button image, or {@code null} on load failure. */
        private Image buttonPlayImg;

        /** Leaderboard button image, or {@code null} on load failure. */
        private Image buttonLeaderboardImg;

        /** {@code true} while the mouse is over the Play button. */
        private boolean hoverPlay = false;

        /** {@code true} while the mouse is over the Leaderboard button. */
        private boolean hoverLeaderboard = false;

        /** {@code true} if the menu intro animation has been skipped by the user. */
        private boolean mainAnimSkipped = false;

        /**
         * Smoothed hover intensity for the Play button in [0.0, 1.0]; used to blend the
         * background colour overlay gradually rather than snapping.
         */
        private double playHoverMix = 0.0;

        /**
         * Smoothed hover intensity for the Leaderboard button in [0.0, 1.0]; used to blend
         * the background colour overlay gradually rather than snapping.
         */
        private double leaderboardHoverMix = 0.0;

        /**
         * Constructs the main menu canvas, loads assets, and wires up mouse interaction handlers.
         *
         * @param stage        the primary stage (used for minimise/close)
         * @param fontBase     the regular application font
         * @param fontBaseBold the bold application font
         * @param onPlay       callback invoked when the Play button is clicked
         */
        MainCanvas(Stage stage, Font fontBase, Font fontBaseBold, Runnable onPlay) {
            this.stage = stage;
            this.fontBase = fontBase;
            this.fontBaseBold = fontBaseBold;
            this.onPlay = onPlay;

            loadAssets();
            initInteraction();

            widthProperty().addListener(o -> render());
            heightProperty().addListener(o -> render());
        }

        /**
         * Instantly completes the menu intro animation by jumping all alpha values to 1.0
         * and re-rendering. Calling this method more than once has no effect.
         */
        public void skipMainAnim() {
            if (mainAnimSkipped) return;
            mainAnimSkipped = true;
            backgroundAlpha = 1.0f;
            titleAlpha = 1.0f;
            buttonsAlpha = 1.0f;
            render();
        }

        /**
         * Starts the main menu animation timer. Subsequent calls while the timer is already
         * running are silently ignored.
         *
         * <p>The timer increments {@link #mainCounter} every ~25 ms and smoothly updates alpha
         * and hover-mix values before calling {@link #render()}.</p>
         */
        public void startMain() {
            if (mainTimer != null) return;

            final long[] last = {0L};
            final long frameNs = 25_000_000L;

            mainTimer = new AnimationTimer() {
                @Override
                public void handle(long now) {
                    if (now - last[0] < frameNs) return;
                    last[0] = now;
                    mainCounter++;

                    if (!mainAnimSkipped) {
                        backgroundAlpha = Math.min(1.0f, mainCounter / 40.0f);
                        titleAlpha = Math.max(0.0f, Math.min(1.0f, (mainCounter - 16) / 34.0f));
                        buttonsAlpha = Math.max(0.0f, Math.min(1.0f, (mainCounter - 40) / 30.0f));
                    }

                    playHoverMix = approach(playHoverMix, hoverPlay ? 1.0 : 0.0, 0.14);
                    leaderboardHoverMix = approach(leaderboardHoverMix, hoverLeaderboard ? 1.0 : 0.0, 0.14);
                    render();
                }
            };
            mainTimer.start();
        }

        /**
         * Stops the animation timer so the canvas does not consume CPU while a game session
         * is active. The canvas remains visible but static.
         */
        public void pauseForGame() {
            if (mainTimer != null) {
                mainTimer.stop();
                mainTimer = null;
            }
        }

        /**
         * Resets all animation state to its initial values and then restarts the menu loop,
         * producing the full intro animation again. Called when returning from a game session.
         */
        public void restartMain() {
            pauseForGame();

            mainCounter = 0;
            backgroundAlpha = 0.0f;
            titleAlpha = 0.0f;
            buttonsAlpha = 0.0f;
            mainAnimSkipped = false;
            hoverPlay = false;
            hoverLeaderboard = false;
            playHoverMix = 0.0;
            leaderboardHoverMix = 0.0;

            render();
            startMain();
        }

        /**
         * Sets the reveal progress, which is a master multiplier applied to all animated
         * alpha values. Used by {@link MainGui} to blend the canvas in while the splash
         * screen is fading out.
         *
         * @param v a value in [0.0, 1.0]; values outside this range are clamped
         */
        public void setRevealProgress(float v) {
            this.revealProgress = clamp(v);
            render();
        }

        /**
         * Loads all image assets required by the main menu (card background pattern, play
         * button, and leaderboard button) from the classpath. Missing or unreadable assets
         * are logged to {@code System.out} and the corresponding fields remain {@code null};
         * the render method handles a {@code null} image gracefully.
         */
        private void loadAssets() {
            try {
                var url = MainGui.class.getResource("/Assets/Cards/card_bg.png");
                if (url == null) throw new RuntimeException("Missing asset: /Assets/Cards/card_bg.png");
                cardPattern = new Image(url.toExternalForm());
            } catch (Exception e) {
                System.out.println("Failed loading background cards: " + e.getMessage());
            }
            try {
                var url = MainGui.class.getResource("/Assets/Buttons/button_play.png");
                if (url == null) throw new RuntimeException("Missing asset: /Assets/Buttons/button_play.png");
                buttonPlayImg = new Image(url.toExternalForm());
            } catch (Exception e) {
                System.out.println("Failed loading play button: " + e.getMessage());
            }
            try {
                var url = MainGui.class.getResource("/Assets/Buttons/button_leaderboard.png");
                if (url == null) throw new RuntimeException("Missing asset: /Assets/Buttons/button_leaderboard.png");
                buttonLeaderboardImg = new Image(url.toExternalForm());
            } catch (Exception e) {
                System.out.println("Failed loading leaderboard button: " + e.getMessage());
            }
        }

        // Interaction ----------------------------------------------------------------------------

        /**
         * Registers all mouse event handlers on this canvas:
         * <ul>
         *   <li>{@code onMouseMoved} – updates hover flags and cursor.</li>
         *   <li>{@code onMouseExited} – clears hover flags.</li>
         *   <li>{@code onMousePressed} – reserved for future use.</li>
         *   <li>{@code onMouseClicked} – handles button actions (skip intro, minimise,
         *       close, play, leaderboard).</li>
         * </ul>
         */
        private void initInteraction() {
            setOnMouseMoved(e -> {
                double x = e.getX();
                double y = e.getY();

                boolean mouseInPlay = inPlayBounds(x, y);
                boolean mouseInLeaderboard = inLeaderboardBounds(x, y);

                if (mouseInPlay != hoverPlay
                    || mouseInLeaderboard != hoverLeaderboard) {
                    hoverPlay = mouseInPlay;
                    hoverLeaderboard = mouseInLeaderboard;
                    render();
                }

                setCursor((mouseInPlay || mouseInLeaderboard) ? Cursor.HAND : Cursor.DEFAULT);
            });

            setOnMouseExited(e -> {
                hoverPlay = false;
                hoverLeaderboard = false;
                render();
            });

            setOnMousePressed(e -> {
                double x = e.getX();
                double y = e.getY();
            });


            setOnMouseClicked(e -> {
                double x = e.getX();
                double y = e.getY();

                if (!mainAnimSkipped && buttonsAlpha < 1.0f) {
                    skipMainAnim();
                    return;
                }

                if (inMinBounds(x, y)) {
                    stage.setIconified(true);
                } else if (inCloseBounds(x, y)) {
                    DiscordRpc.shutdown();
                    Platform.exit();
                } else if (inPlayBounds(x, y)) {
                    onPlay.run();
                } else if (inLeaderboardBounds(x, y)) {
                    System.out.println("Leaderboard clicked");
                }
            });
        }

        // Hitboxes ----------------------------------------------------------------------------

        /**
         * Returns the left edge X coordinate of the close button's hit area.
         *
         * @return pixel X of the close button
         */
        private double closeX() {
            return getWidth() - BUTTON_RIGHT - BUTTON_HIT_SIZE;
        }

        /**
         * Returns the left edge X coordinate of the minimise button's hit area.
         *
         * @return pixel X of the minimise button
         */
        private double minX() {
            return getWidth() - BUTTON_RIGHT - BUTTON_HIT_SIZE - BUTTON_GAP - BUTTON_HIT_SIZE;
        }

        /**
         * Tests whether the point ({@code x}, {@code y}) is within the close button's hit area.
         *
         * @param x mouse X coordinate in canvas space
         * @param y mouse Y coordinate in canvas space
         * @return {@code true} if the point is inside the close button
         */
        private boolean inCloseBounds(double x, double y) {
            return x >= closeX() && x <= closeX() + BUTTON_HIT_SIZE
                && y >= BUTTON_TOP && y <= BUTTON_TOP + BUTTON_HIT_SIZE;
        }

        /**
         * Tests whether the point ({@code x}, {@code y}) is within the minimise button's hit area.
         *
         * @param x mouse X coordinate in canvas space
         * @param y mouse Y coordinate in canvas space
         * @return {@code true} if the point is inside the minimise button
         */
        private boolean inMinBounds(double x, double y) {
            return x >= minX() && x <= minX() + BUTTON_HIT_SIZE
                && y >= BUTTON_TOP && y <= BUTTON_TOP + BUTTON_HIT_SIZE;
        }

        /**
         * Tests whether the point ({@code x}, {@code y}) is within the Play button's hit area.
         *
         * @param x mouse X coordinate in canvas space
         * @param y mouse Y coordinate in canvas space
         * @return {@code true} if the point is inside the Play button
         */
        private boolean inPlayBounds(double x, double y) {
            double totalW = MENU_BUTTON_W * 2 + MENU_BUTTON_GAP;
            double startX = (getWidth() - totalW) / 2.0;
            double startY = getHeight() / 2.0 + 80;
            return x >= startX && x <= startX + MENU_BUTTON_W
                && y >= startY && y <= startY + MENU_BUTTON_H;
        }

        /**
         * Tests whether the point ({@code x}, {@code y}) is within the Leaderboard button's
         * hit area.
         *
         * @param x mouse X coordinate in canvas space
         * @param y mouse Y coordinate in canvas space
         * @return {@code true} if the point is inside the Leaderboard button
         */
        private boolean inLeaderboardBounds(double x, double y) {
            double totalW = MENU_BUTTON_W * 2 + MENU_BUTTON_GAP;
            double startX = (getWidth() - totalW) / 2.0 + MENU_BUTTON_W + MENU_BUTTON_GAP;
            double startY = getHeight() / 2.0 + 80;
            return x >= startX && x <= startX + MENU_BUTTON_W
                && y >= startY && y <= startY + MENU_BUTTON_H;
        }

        // Render ----------------------------------------------------------------------------

        /**
         * Draws a single frame of the main menu onto this canvas. Renders (in order):
         * <ol>
         *   <li>Solid base background colour.</li>
         *   <li>Hover colour overlays for the Play and Leaderboard buttons.</li>
         *   <li>Scrolling card-tile background pattern.</li>
         *   <li>Animated, gently bobbing title text.</li>
         *   <li>Play and Leaderboard buttons (with hover scale effect).</li>
         * </ol>
         * All layers respect the current {@link #revealProgress} multiplier.
         */
        public void render() {
            double w = getWidth();
            double h = getHeight();
            double cx = w / 2.0;
            double cy = h / 2.0;

            GraphicsContext canva = getGraphicsContext2D();
            canva.clearRect(0, 0, w, h);

            canva.setFill(BG_BASE);
            canva.fillRect(0, 0, w, h);

            Color playOverlay = blend(Color.TRANSPARENT, BG_PLAY, playHoverMix * 0.75);
            Color leaderboardOverlay = blend(Color.TRANSPARENT, BG_LEADERBOARD, leaderboardHoverMix * 0.75);
            drawColorOverlay(canva, playOverlay, w, h);
            drawColorOverlay(canva, leaderboardOverlay, w, h);

            float finalBackgroundAlpha = backgroundAlpha * revealProgress;
            drawPatternBackground(canva, w, h, finalBackgroundAlpha);

            float finalTitleAlpha = titleAlpha * revealProgress;
            if (finalTitleAlpha > 0.001f) {
                double scale = 0.92 + 0.08 * finalTitleAlpha;
                Font titleFont = Font.font(fontBaseBold.getFamily(), FontWeight.BOLD, 120);
                String title = "Iskyjo";
                double titleW = measureText(title, titleFont);

                canva.save();
                canva.translate(cx, cy);
                canva.rotate(Math.toDegrees(Math.sin(mainCounter * 0.03) * 0.025));
                canva.scale(scale, scale);
                canva.setGlobalAlpha(finalTitleAlpha);
                canva.setFill(Color.rgb(255, 200, 0));
                canva.setFont(titleFont);
                canva.fillText(title, -titleW / 2.0, 0);
                canva.restore();
            }


            float finalButtonsAlpha = buttonsAlpha * revealProgress;
            if (finalButtonsAlpha > 0.001f) {
                double totalW = MENU_BUTTON_W * 2 + MENU_BUTTON_GAP;
                double startX = (w - totalW) / 2.0;
                double startY = h / 2.0 + 80;

                drawMenuButton(canva, startX, startY, MENU_BUTTON_W, MENU_BUTTON_H,
                    buttonPlayImg, hoverPlay, finalButtonsAlpha);
                drawMenuButton(canva, startX + MENU_BUTTON_W + MENU_BUTTON_GAP, startY, MENU_BUTTON_W, MENU_BUTTON_H,
                    buttonLeaderboardImg, hoverLeaderboard, finalButtonsAlpha);
            }
        }

        // Drawing helpers ----------------------------------------------------------------------------

        /**
         * Draws the scrolling card-tile background pattern. Tiles scroll diagonally using
         * {@link #mainCounter} as the time base and wrap seamlessly.
         *
         * @param canva the graphics context to draw into
         * @param w     canvas width in pixels
         * @param h     canvas height in pixels
         * @param alpha overall opacity multiplier; drawing is skipped when below 0.001
         */
        private void drawPatternBackground(GraphicsContext canva, double w, double h, float alpha) {
            if (cardPattern == null || alpha <= 0.001f) return;

            double totalW = CARD_WIDTH + SPACING;
            double totalH = CARD_HEIGHT + SPACING;
            double startX = -(totalW) + (mainCounter * ANIMATION_SPEED) % totalW;
            double startY = -(totalH) + (mainCounter * ANIMATION_SPEED * 0.4f) % totalH;

            canva.setGlobalAlpha(clamp(CARD_OPACITY_BASE * alpha));
            for (double x = startX; x < w; x += totalW) {
                for (double y = startY; y < h; y += totalH) {
                    canva.drawImage(cardPattern, x, y, CARD_WIDTH, CARD_HEIGHT);
                }
            }
            canva.setGlobalAlpha(1.0);
        }

        /**
         * Fills the entire canvas with a solid colour at the colour's own alpha value.
         * Skips drawing if the colour's opacity is below 0.01 to avoid unnecessary state changes.
         *
         * @param canva the graphics context to draw into
         * @param c     the fill colour (opacity is used as the global alpha)
         * @param w     canvas width in pixels
         * @param h     canvas height in pixels
         */
        private void drawColorOverlay(GraphicsContext canva, Color c, double w, double h) {
            double a = c.getOpacity();
            if (a < 0.01) return;
            canva.setGlobalAlpha(a);
            canva.setFill(Color.color(c.getRed(), c.getGreen(), c.getBlue()));
            canva.fillRect(0, 0, w, h);
            canva.setGlobalAlpha(1.0);
        }

        /**
         * Draws a single menu button at the specified position. If an image is available it is
         * drawn; otherwise a rounded-rectangle placeholder is rendered. When hovered, the button
         * is scaled up by 6 % around its centre.
         *
         * @param canva  the graphics context to draw into
         * @param rx     left edge X of the button's logical bounding box
         * @param ry     top edge Y of the button's logical bounding box
         * @param rw     width of the button's logical bounding box
         * @param rh     height of the button's logical bounding box
         * @param img    the button image to draw, or {@code null} for the placeholder
         * @param hover  {@code true} if the mouse is currently over this button
         * @param alpha  overall opacity applied to the button
         */
        private void drawMenuButton(GraphicsContext canva, double rx, double ry, double rw, double rh,
                                    Image img, boolean hover, float alpha) {
            double finalAlpha = clamp(alpha);
            double scale = hover ? 1.06 : 1.0;
            double drawW = rw * scale;
            double drawH = rh * scale;
            double drawX = rx + (rw - drawW) / 2.0;
            double drawY = ry + (rh - drawH) / 2.0;

            canva.setGlobalAlpha(finalAlpha);
            if (img != null) {
                canva.drawImage(img, drawX, drawY, drawW, drawH);
            } else {
                canva.setFill(Color.rgb(255, 200, 0, 200 / 255.0));
                canva.fillRoundRect(drawX, drawY, drawW, drawH, 12, 12);
                canva.setStroke(Color.rgb(255, 200, 0));
                canva.setLineWidth(2);
                canva.strokeRoundRect(drawX, drawY, drawW, drawH, 12, 12);
            }
            canva.setGlobalAlpha(1.0);
        }

        /**
         * Linearly interpolates between two {@link Color} values.
         *
         * @param from the colour at {@code t = 0}
         * @param to   the colour at {@code t = 1}
         * @param t    interpolation factor; clamped to [0.0, 1.0]
         * @return the blended colour
         */
        private Color blend(Color from, Color to, double t) {
            double k = clamp(t);
            return new Color(
                from.getRed() + (to.getRed() - from.getRed()) * k,
                from.getGreen() + (to.getGreen() - from.getGreen()) * k,
                from.getBlue() + (to.getBlue() - from.getBlue()) * k,
                from.getOpacity() + (to.getOpacity() - from.getOpacity()) * k
            );
        }

        /**
         * Measures the rendered pixel width of the given string in the given font.
         *
         * @param s the string to measure
         * @param f the font to use for measurement
         * @return the width in pixels
         */
        private double measureText(String s, Font f) {
            Text t = new Text(s);
            t.setFont(f);
            return t.getBoundsInLocal().getWidth();
        }

        /**
         * Clamps a {@code float} value to the range [0.0, 1.0].
         *
         * @param v the value to clamp
         * @return the clamped value
         */
        private float clamp(float v) {
            return Math.max(0f, Math.min(1f, v));
        }

        /**
         * Clamps a {@code double} value to the range [0.0, 1.0].
         *
         * @param v the value to clamp
         * @return the clamped value
         */
        private double clamp(double v) {
            return Math.max(0.0, Math.min(1.0, v));
        }

        /**
         * Moves {@code value} toward {@code target} by at most {@code step} per call,
         * without overshooting. Used to produce smooth hover colour transitions.
         *
         * @param value  the current value
         * @param target the desired value
         * @param step   the maximum change per call
         * @return the updated value
         */
        private double approach(double value, double target, double step) {
            if (value < target) return Math.min(target, value + step);
            return Math.max(target, value - step);
        }
    }
}
