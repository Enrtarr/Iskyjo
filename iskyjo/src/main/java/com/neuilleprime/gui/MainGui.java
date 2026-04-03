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
    private GameGui.GameCanvas gameCanvas;
    private ShopGui.ShopCanvas shopCanvas;

    private AnimationTimer splashTimer;
    private boolean splashSkipped = false;
    private boolean isTransitionRunning = false;
    private GameGui.GameState activeGameState;
    private AnimationTimer discordSyncTimer;
    private long lastDiscordSyncNs = 0L;
    private String lastPresenceKey = "";

    // Startup ----------------------------------------------------------------------------

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

    private void bindCanvasToStage(Canvas canvas) {
        canvas.widthProperty().bind(mainStage.widthProperty());
        canvas.heightProperty().bind(mainStage.heightProperty());
    }

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

    private static double clamp(double v) {
        return Math.max(0.0, Math.min(1.0, v));
    }

    public static void main(String[] args) {
        launch(args);
    }

    // Splash canvas ----------------------------------------------------------------------------

    static class SplashCanvas extends Canvas {
        private static final int FADE_IN_FRAMES = 40;
        private static final int HOLD_FRAMES = 100;
        private static final int FADE_OUT_FRAMES = 40;

        private int frame = 0;

        private final Image logo;
        private final Font mainFont;
        private final Font subFont;

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

        public void nextFrame() {
            frame++;
        }

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

        public boolean isFinished() {
            return frame > FADE_IN_FRAMES + HOLD_FRAMES + FADE_OUT_FRAMES;
        }

        public boolean isMainFrameRevealPhase() {
            return frame > FADE_IN_FRAMES + HOLD_FRAMES;
        }

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

        private double measureText(String s, Font f) {
            Text t = new Text(s);
            t.setFont(f);
            return t.getBoundsInLocal().getWidth();
        }
    }

    // Main canvas ----------------------------------------------------------------------------

    static class MainCanvas extends Canvas {
        private static final int MENU_BUTTON_W = 240;
        private static final int MENU_BUTTON_H = 65;
        private static final int MENU_BUTTON_GAP = 70;
        private static final int CARD_WIDTH = 82;
        private static final int CARD_HEIGHT = 114;
        private static final int SPACING = 20;
        private static final float ANIMATION_SPEED = 1.5f;
        private static final float CARD_OPACITY_BASE = 0.05f;
        private static final int BUTTON_HIT_SIZE = 28;
        private static final int BUTTON_TOP = 8;
        private static final int BUTTON_RIGHT = 10;
        private static final int BUTTON_GAP = 6;

        private static final Color BG_BASE = Color.web("#1d2b53");
        private static final Color BG_PLAY = Color.web("#1e532c");
        private static final Color BG_LEADERBOARD = Color.web("#534b1f");

        private final Stage stage;
        private final Font fontBase;
        private final Font fontBaseBold;
        private final Runnable onPlay;

        private AnimationTimer mainTimer;
        private int mainCounter = 0;

        private float revealProgress = 0.0f;
        private float backgroundAlpha = 0.0f;
        private float titleAlpha = 0.0f;
        private float buttonsAlpha = 0.0f;

        private Image cardPattern;
        private Image buttonPlayImg;
        private Image buttonLeaderboardImg;

        private boolean hoverPlay = false;
        private boolean hoverLeaderboard = false;
        private boolean mainAnimSkipped = false;

        private double playHoverMix = 0.0;
        private double leaderboardHoverMix = 0.0;

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

        public void skipMainAnim() {
            if (mainAnimSkipped) return;
            mainAnimSkipped = true;
            backgroundAlpha = 1.0f;
            titleAlpha = 1.0f;
            buttonsAlpha = 1.0f;
            render();
        }

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

        public void pauseForGame() {
            if (mainTimer != null) {
                mainTimer.stop();
                mainTimer = null;
            }
        }

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

        public void setRevealProgress(float v) {
            this.revealProgress = clamp(v);
            render();
        }

        private void loadAssets() {
            try {
                var url = MainGui.class.getResource("/Assets/Cards/Card_bg.png");
                if (url == null) throw new RuntimeException("Missing asset: /Assets/Cards/Card_bg.png");
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

        private double closeX() {
            return getWidth() - BUTTON_RIGHT - BUTTON_HIT_SIZE;
        }

        private double minX() {
            return getWidth() - BUTTON_RIGHT - BUTTON_HIT_SIZE - BUTTON_GAP - BUTTON_HIT_SIZE;
        }

        private boolean inCloseBounds(double x, double y) {
            return x >= closeX() && x <= closeX() + BUTTON_HIT_SIZE
                && y >= BUTTON_TOP && y <= BUTTON_TOP + BUTTON_HIT_SIZE;
        }

        private boolean inMinBounds(double x, double y) {
            return x >= minX() && x <= minX() + BUTTON_HIT_SIZE
                && y >= BUTTON_TOP && y <= BUTTON_TOP + BUTTON_HIT_SIZE;
        }

        private boolean inPlayBounds(double x, double y) {
            double totalW = MENU_BUTTON_W * 2 + MENU_BUTTON_GAP;
            double startX = (getWidth() - totalW) / 2.0;
            double startY = getHeight() / 2.0 + 80;
            return x >= startX && x <= startX + MENU_BUTTON_W
                && y >= startY && y <= startY + MENU_BUTTON_H;
        }

        private boolean inLeaderboardBounds(double x, double y) {
            double totalW = MENU_BUTTON_W * 2 + MENU_BUTTON_GAP;
            double startX = (getWidth() - totalW) / 2.0 + MENU_BUTTON_W + MENU_BUTTON_GAP;
            double startY = getHeight() / 2.0 + 80;
            return x >= startX && x <= startX + MENU_BUTTON_W
                && y >= startY && y <= startY + MENU_BUTTON_H;
        }

        // Render ----------------------------------------------------------------------------

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

        private void drawColorOverlay(GraphicsContext canva, Color c, double w, double h) {
            double a = c.getOpacity();
            if (a < 0.01) return;
            canva.setGlobalAlpha(a);
            canva.setFill(Color.color(c.getRed(), c.getGreen(), c.getBlue()));
            canva.fillRect(0, 0, w, h);
            canva.setGlobalAlpha(1.0);
        }

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

        private Color blend(Color from, Color to, double t) {
            double k = clamp(t);
            return new Color(
                from.getRed() + (to.getRed() - from.getRed()) * k,
                from.getGreen() + (to.getGreen() - from.getGreen()) * k,
                from.getBlue() + (to.getBlue() - from.getBlue()) * k,
                from.getOpacity() + (to.getOpacity() - from.getOpacity()) * k
            );
        }

        private double measureText(String s, Font f) {
            Text t = new Text(s);
            t.setFont(f);
            return t.getBoundsInLocal().getWidth();
        }

        private float clamp(float v) {
            return Math.max(0f, Math.min(1f, v));
        }

        private double clamp(double v) {
            return Math.max(0.0, Math.min(1.0, v));
        }

        private double approach(double value, double target, double step) {
            if (value < target) return Math.min(target, value + step);
            return Math.max(target, value - step);
        }
    }
}
