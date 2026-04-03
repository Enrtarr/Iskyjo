package com.neuilleprime.gui.shop;

import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Stage;

public class ShopGui extends Application {

    private static Font customFont;
    private static Font customFontBold;

    static {
        try {
            var stream = ShopGui.class.getResourceAsStream("/Assets/Fonts/VCR_OSD_MONO.ttf");
            if (stream == null) throw new RuntimeException("Font not found");
            customFont = Font.loadFont(stream, 14);
            if (customFont == null) throw new RuntimeException("Font load failed");
            customFontBold = Font.font(customFont.getFamily(), FontWeight.BOLD, 14);
        } catch (Exception e) {
            customFont = Font.font("Courier New", FontWeight.NORMAL, 14);
            customFontBold = Font.font("Courier New", FontWeight.BOLD, 14);
            System.err.println("[Shop] Font fallback: " + e.getMessage());
        }
    }

    /**
     * Starts the standalone shop window.
     *
     * @param primaryStage the primary JavaFX stage
     */
    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Iskyjo - Shop");
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

        ShopCanvas shopCanvas = new ShopCanvas(
            primaryStage,
            customFont,
            customFontBold,
            () -> {},
            Platform::exit,
            true
        );

        StackPane root = new StackPane(shopCanvas);
        root.setStyle("-fx-background-color: #c59b2d;");
        shopCanvas.widthProperty().bind(primaryStage.widthProperty());
        shopCanvas.heightProperty().bind(primaryStage.heightProperty());

        primaryStage.setScene(new Scene(root, 1400, 900));
        primaryStage.centerOnScreen();
        primaryStage.show();
        shopCanvas.render();
    }

/**
     * Launches the standalone shop screen.
     *
     * @param args command-line arguments passed to JavaFX
     */
    public static void main(String[] args) {
        launch(args);
    }

    // Shop canvas ----------------------------------------------------------------------------

    public static class ShopCanvas extends Canvas {
        private static final double TOP_BAR_H = 64.0;

        private static final double SETTINGS_BUTTON_X = 30.0;
        private static final double SETTINGS_BUTTON_SIZE = 28.0;
        private static final double SETTINGS_BUTTON_Y = 30.0;

        private static final double PANEL_X = SETTINGS_BUTTON_X + SETTINGS_BUTTON_SIZE + 10.0;
        private static final double PANEL_Y = SETTINGS_BUTTON_Y - 2.0;
        private static final double PANEL_W = 260.0;
        private static final double PANEL_H = 210.0;
        private static final double LEAVE_PAD = 10.0;
        private static final double LEAVE_BUTTON_HEIGHT = 32.0;
        private static final double DEBUG_BUTTON_HEIGHT = 26.0;
        private static final double DEBUG_WINDOW_BUTTON_SPACING = 8.0;

        private static final double ITEM_W = 250.0;
        private static final double ITEM_H = 340.0;
        private static final double ITEM_GAP = 26.0;

        private static final int BACKGROUND_CARD_W = 82;
        private static final int BACKGROUND_CARD_H = 114;
        private static final int BACKGROUND_CARD_GAP = 20;
        private static final double BACKGROUND_CARD_OPACITY = 0.05;

        private static final Color BG = Color.web("#4e481d");
        private static final Color TEXT_DIM = Color.web("#a0acd8");
        private static final Color ACCENT = Color.web("#ffc800");
        private static final Color PANEL_BG = Color.web("#151d3c");
        private static final Color PANEL_BORDER = Color.web("#435794");

        private static final String[] DEBUG_BUTTON_LABELS = {
            "OPEN GAME"
        };

        private final Stage stage;
        private final Font fontBase;
        private final Font fontBold;
        private final Runnable onBackToGame;
        private final Runnable onLeave;
        private final boolean debugMode;

        private boolean hoverSettingsButton = false;
        private boolean hoverLeaveButton = false;
        private int hoverDebugAction = -1;
        private boolean settingsPanelOpen = false;
        private boolean dragging = false;

        private double dragOffX = 0.0;
        private double dragOffY = 0.0;
        private double animTime = 0.0;
        private long lastFrameTime = -1L;

        private Image backgroundPattern = null;
        private AnimationTimer animationTimer;

        /**
         * Creates the shop canvas and initialises its assets, interactions, and animation loop.
         *
         * @param stage the owning stage
         * @param fontBase the base font used for labels
         * @param fontBold the bold font used for titles
         * @param onBackToGame callback invoked when the user returns to the game
         * @param onLeave callback invoked when the user leaves the shop/application
         * @param debugMode whether debug controls should be shown
         */
        public ShopCanvas(Stage stage, Font fontBase, Font fontBold,
                          Runnable onBackToGame, Runnable onLeave, boolean debugMode) {
            this.stage = stage;
            this.fontBase = fontBase;
            this.fontBold = fontBold;
            this.onBackToGame = onBackToGame;
            this.onLeave = onLeave;
            this.debugMode = debugMode;

            tryLoadImages();
            setupInteractions();
            startAnimation();

            widthProperty().addListener((o, a, b) -> render());
            heightProperty().addListener((o, a, b) -> render());
        }

        // Assets ----------------------------------------------------------------------------

        /**
         * Attempts to load the decorative background pattern used by the shop screen.
         */
        private void tryLoadImages() {
            try {
                var resource = ShopCanvas.class.getResource("/Assets/Cards/Card_bg.png");
                if (resource != null) backgroundPattern = new Image(resource.toExternalForm());
            } catch (Exception ignored) {
            }
        }

        /**
         * Starts the animation timer used to refresh the shop screen.
         */
        private void startAnimation() {
            animationTimer = new AnimationTimer() {
                @Override
                public void handle(long now) {
                    if (lastFrameTime < 0) {
                        lastFrameTime = now;
                        return;
                    }

                    long elapsed = now - lastFrameTime;
                    if (elapsed < 25_000_000L) return;

                    lastFrameTime = now;
                    animTime += elapsed / 1_000_000_000.0;
                    render();
                }
            };
            animationTimer.start();
        }

        // Layout ----------------------------------------------------------------------------


        private double getLeaveButtonX() {
            return PANEL_X + LEAVE_PAD;
        }

        private double getLeaveButtonY() {
            return PANEL_Y + PANEL_H - LEAVE_BUTTON_HEIGHT - LEAVE_PAD;
        }

        private double getLeaveButtonWidth() {
            return PANEL_W - LEAVE_PAD * 2.0;
        }

        private double getDebugButtonX() {
            return PANEL_X + LEAVE_PAD;
        }

        private double getDebugButtonWidth() {
            return PANEL_W - LEAVE_PAD * 2.0;
        }

        private double debugStartY() {
            return PANEL_Y + 42.0;
        }

        private double getDebugButtonY(int index) {
            return debugStartY() + index * (DEBUG_BUTTON_HEIGHT + DEBUG_WINDOW_BUTTON_SPACING);
        }


        private boolean isInsideSettingsButton(double x, double y) {
            return x >= SETTINGS_BUTTON_X && x <= SETTINGS_BUTTON_X + SETTINGS_BUTTON_SIZE
                && y >= SETTINGS_BUTTON_Y && y <= SETTINGS_BUTTON_Y + SETTINGS_BUTTON_SIZE;
        }

        private boolean inPanel(double x, double y) {
            return settingsPanelOpen
                && x >= PANEL_X && x <= PANEL_X + PANEL_W
                && y >= PANEL_Y && y <= PANEL_Y + PANEL_H;
        }

        private boolean isInsideLeaveButton(double x, double y) {
            return settingsPanelOpen
                && x >= getLeaveButtonX() && x <= getLeaveButtonX() + getLeaveButtonWidth()
                && y >= getLeaveButtonY() && y <= getLeaveButtonY() + LEAVE_BUTTON_HEIGHT;
        }

        private int hitTestDebugButton(double x, double y) {
            if (!settingsPanelOpen || !debugMode) return -1;

            double bx = getDebugButtonX();
            double bw = getDebugButtonWidth();
            for (int i = 0; i < DEBUG_BUTTON_LABELS.length; i++) {
                double by = getDebugButtonY(i);
                if (x >= bx && x <= bx + bw && y >= by && y <= by + DEBUG_BUTTON_HEIGHT) {
                    return i;
                }
            }
            return -1;
        }

        // Interaction ----------------------------------------------------------------------------

        /**
         * Registers mouse interactions for the shop settings panel and debug actions.
         */
        private void setupInteractions() {
            setOnMouseMoved(e -> {
                double x = e.getX();
                double y = e.getY();

                boolean mouseOverSettings = isInsideSettingsButton(x, y);
                boolean mouseOverLeave = isInsideLeaveButton(x, y);
                int hoveredDebug = hitTestDebugButton(x, y);

                boolean changed = mouseOverSettings != hoverSettingsButton
                    || mouseOverLeave != hoverLeaveButton
                    || hoveredDebug != hoverDebugAction;

                if (changed) {
                    hoverSettingsButton = mouseOverSettings;
                    hoverLeaveButton = mouseOverLeave;
                    hoverDebugAction = hoveredDebug;
                    render();
                }

                boolean hand = mouseOverSettings || mouseOverLeave || hoveredDebug >= 0;
                setCursor(hand ? Cursor.HAND : Cursor.DEFAULT);
            });

            setOnMouseExited(e -> {
                hoverSettingsButton = false;
                hoverLeaveButton = false;
                hoverDebugAction = -1;
                render();
            });

            setOnMousePressed(e -> {
                double x = e.getX();
                double y = e.getY();
            });

            setOnMouseDragged(e -> {
                if (dragging) {
                    stage.setX(e.getScreenX() - dragOffX);
                    stage.setY(e.getScreenY() - dragOffY);
                }
            });

            setOnMouseReleased(e -> dragging = false);

            setOnMouseClicked(e -> {
                double x = e.getX();
                double y = e.getY();

                if (isInsideSettingsButton(x, y)) {
                    settingsPanelOpen = !settingsPanelOpen;
                    render();
                    return;
                }

                if (isInsideLeaveButton(x, y)) {
                    settingsPanelOpen = false;
                    render();
                    onLeave.run();
                    return;
                }

                int debugAction = hitTestDebugButton(x, y);
                if (debugAction >= 0) {
                    onBackToGame.run();
                    return;
                }

                if (settingsPanelOpen && !inPanel(x, y)) {
                    settingsPanelOpen = false;
                    render();
                    return;
                }

            });

            setOnKeyPressed(e -> {
                if (e.getCode() == javafx.scene.input.KeyCode.ESCAPE && settingsPanelOpen) {
                    settingsPanelOpen = false;
                    render();
                }
            });

            setFocusTraversable(true);
        }

        // Render ----------------------------------------------------------------------------

        /**
         * Renders the current shop screen frame.
         */
        public void render() {
            double w = getWidth();
            double h = getHeight();
            if (w <= 0 || h <= 0) return;

            GraphicsContext gc = getGraphicsContext2D();
            gc.clearRect(0, 0, w, h);
            gc.setGlobalAlpha(1.0);

            drawBackground(gc, w, h);
            drawShop(gc, w, h);
            drawSettingsOverlay(gc);
        }

        private void drawBackground(GraphicsContext gc, double w, double h) {
            gc.setFill(BG);
            gc.fillRect(0, 0, w, h);

            if (backgroundPattern != null) {
                double totalW = BACKGROUND_CARD_W + BACKGROUND_CARD_GAP;
                double totalH = BACKGROUND_CARD_H + BACKGROUND_CARD_GAP;
                double startX = -(totalW) + (animTime * 60.0) % totalW;
                double startY = -(totalH) + (animTime * 24.0) % totalH;

                gc.setGlobalAlpha(BACKGROUND_CARD_OPACITY);
                for (double x = startX; x < w; x += totalW) {
                    for (double y = startY; y < h; y += totalH) {
                        gc.drawImage(backgroundPattern, x, y, BACKGROUND_CARD_W, BACKGROUND_CARD_H);
                    }
                }
                gc.setGlobalAlpha(1.0);
            }
        }


        private void drawShop(GraphicsContext gc, double w, double h) {
            double contentY = TOP_BAR_H + 42.0;
            double totalW = ITEM_W * 3 + ITEM_GAP * 2;
            double startX = (w - totalW) / 2.0;

            Font titleFont = Font.font(fontBold.getFamily(), FontWeight.BOLD, 34);
            Font sectionFont = Font.font(fontBold.getFamily(), FontWeight.BOLD, 16);
            Font smallFont = Font.font(fontBase.getFamily(), FontWeight.NORMAL, 11);


            for (int i = 0; i < 3; i++) {
                double x = startX + i * (ITEM_W + ITEM_GAP);
                double y = contentY + 48.0;
                drawItemCard(gc, x, y, ITEM_W, ITEM_H, i + 1, sectionFont, smallFont);
            }

            double footerY = contentY + 48.0 + ITEM_H + 34.0;
            drawFooterButton(gc, startX, footerY, 220.0, 62.0, "REROLL", Color.web("#163e72"), Color.web("#86b4ff"), sectionFont);
            drawFooterButton(gc, startX + totalW - 280.0, footerY, 280.0, 62.0, "NEXT ROUND", Color.web("#1f5c2c"), Color.web("#96efab"), sectionFont);

        }

        private void drawItemCard(GraphicsContext gc, double x, double y, double w, double h,
                                  int index, Font sectionFont, Font smallFont) {
            gc.setFill(PANEL_BG);
            gc.fillRoundRect(x, y, w, h, 18, 18);
            gc.setStroke(PANEL_BORDER);
            gc.setLineWidth(1.2);
            gc.strokeRoundRect(x, y, w, h, 18, 18);

            gc.setFont(smallFont);
            gc.setFill(TEXT_DIM);
            gc.fillText("ITEM SLOT " + index, x + 18, y + 28);

            gc.setFill(Color.web("#1f2b54"));
            gc.fillRoundRect(x + 18, y + 48, w - 36, 158, 16, 16);
            gc.setStroke(Color.web("#5f75bf"));
            gc.strokeRoundRect(x + 18, y + 48, w - 36, 158, 16, 16);

            gc.setFont(sectionFont);
            gc.setFill(Color.WHITE);
            gc.fillText("Placeholder card", x + 18, y + 238);

            gc.setFont(smallFont);
            gc.setFill(TEXT_DIM);
            gc.fillText("Price", x + 18, y + 270);

            gc.setFill(Color.web("#ffc800"));
            gc.fillRoundRect(x + 18, y + 282, 88, 36, 12, 12);
            gc.setFill(Color.web("#332700"));
            gc.setFont(sectionFont);
            gc.fillText("$5", x + 47, y + 307);

            gc.setFill(Color.web("#2a2f4f"));
            gc.fillRoundRect(x + 120, y + 282, w - 138, 36, 12, 12);
            gc.setStroke(Color.web("#7e8bc0"));
            gc.strokeRoundRect(x + 120, y + 282, w - 138, 36, 12, 12);
            gc.setFill(Color.WHITE);
            gc.setFont(smallFont);
            gc.fillText("BUY", x + 120 + (w - 138) / 2.0 - 10.0, y + 305);
        }

        private void drawFooterButton(GraphicsContext gc, double x, double y, double w, double h,
                                      String label, Color bg, Color border, Font font) {
            gc.setFill(bg);
            gc.fillRoundRect(x, y, w, h, 18, 18);
            gc.setStroke(border);
            gc.setLineWidth(1.2);
            gc.strokeRoundRect(x, y, w, h, 18, 18);

            gc.setFill(Color.WHITE);
            gc.setFont(font);
            double tw = measureText(label, font);
            gc.fillText(label, x + (w - tw) / 2.0, y + h / 2.0 + 6.0);
        }

        private void drawSettingsOverlay(GraphicsContext gc) {
            drawSettingsButton(gc);
            if (settingsPanelOpen) {
                drawSettingsPanel(gc);
            }
        }

        private void drawSettingsButton(GraphicsContext gc) {
            double x = SETTINGS_BUTTON_X;
            double y = SETTINGS_BUTTON_Y;
            double s = SETTINGS_BUTTON_SIZE;

            Color bgColor = settingsPanelOpen ? ACCENT
                : hoverSettingsButton ? Color.web("#2a2a55")
                : Color.web("#1a1a3a");
            gc.setFill(bgColor);
            gc.fillRoundRect(x, y, s, s, 6, 6);
            gc.setStroke(settingsPanelOpen || hoverSettingsButton ? ACCENT : PANEL_BORDER);
            gc.setLineWidth(1.5);
            gc.strokeRoundRect(x, y, s, s, 6, 6);

            Color lineColor = settingsPanelOpen ? BG : hoverSettingsButton ? ACCENT : TEXT_DIM;
            gc.setStroke(lineColor);
            gc.setLineWidth(2.2);
            double lx1 = x + 7;
            double lx2 = x + s - 7;
            gc.strokeLine(lx1, y + 8, lx2, y + 8);
            gc.strokeLine(lx1, y + s / 2.0, lx2, y + s / 2.0);
            gc.strokeLine(lx1, y + s - 8, lx2, y + s - 8);
        }

        private void drawSettingsPanel(GraphicsContext gc) {
            double px = PANEL_X;
            double py = PANEL_Y;
            double pw = PANEL_W;
            double ph = PANEL_H;

            gc.setFill(Color.rgb(0, 0, 0, 0.5));
            gc.fillRoundRect(px + 4, py + 4, pw, ph, 12, 12);

            gc.setFill(Color.web("#14142e"));
            gc.fillRoundRect(px, py, pw, ph, 12, 12);

            gc.setStroke(ACCENT);
            gc.setLineWidth(1.5);
            gc.strokeRoundRect(px, py, pw, ph, 12, 12);

            Font titleFont = Font.font(fontBold.getFamily(), FontWeight.BOLD, 11);
            gc.setFont(titleFont);
            gc.setFill(TEXT_DIM);
            gc.fillText("SETTINGS", px + LEAVE_PAD, py + 20);

            gc.setStroke(Color.web("#2a2a55"));
            gc.setLineWidth(1.0);
            gc.strokeLine(px + LEAVE_PAD, py + 28, px + pw - LEAVE_PAD, py + 28);

            if (debugMode) {
                drawDebugButtons(gc);
            }

            drawLeaveButton(gc);
        }

        private void drawDebugButtons(GraphicsContext gc) {
            Font bf = Font.font(fontBold.getFamily(), FontWeight.BOLD, 10);
            gc.setFont(bf);

            for (int i = 0; i < DEBUG_BUTTON_LABELS.length; i++) {
                double x = getDebugButtonX();
                double y = getDebugButtonY(i);
                double w = getDebugButtonWidth();
                double h = DEBUG_BUTTON_HEIGHT;
                boolean hover = hoverDebugAction == i;

                gc.setFill(hover ? Color.web("#2a2a55") : Color.web("#1b1b3f"));
                gc.fillRoundRect(x, y, w, h, 6, 6);
                gc.setStroke(hover ? ACCENT : PANEL_BORDER);
                gc.setLineWidth(1.2);
                gc.strokeRoundRect(x, y, w, h, 6, 6);

                gc.setFill(hover ? ACCENT : Color.WHITE);
                String label = DEBUG_BUTTON_LABELS[i];
                double tw = measureText(label, bf);
                gc.fillText(label, x + (w - tw) / 2.0, y + h / 2.0 + 3.5);
            }
        }

        private void drawLeaveButton(GraphicsContext gc) {
            double x = getLeaveButtonX();
            double y = getLeaveButtonY();
            double w = getLeaveButtonWidth();
            double h = LEAVE_BUTTON_HEIGHT;

            gc.setFill(hoverLeaveButton ? Color.web("#5a2130") : Color.web("#391522"));
            gc.fillRoundRect(x, y, w, h, 6, 6);
            gc.setStroke(hoverLeaveButton ? Color.web("#ff9db6") : Color.web("#8a4b61"));
            gc.setLineWidth(1.2);
            gc.strokeRoundRect(x, y, w, h, 6, 6);

            Font bf = Font.font(fontBold.getFamily(), FontWeight.BOLD, 10);
            gc.setFont(bf);
            gc.setFill(hoverLeaveButton ? Color.WHITE : Color.web("#ffaaaa"));
            String label = "<- LEAVE TO MENU";
            double tw = measureText(label, bf);
            gc.fillText(label, x + (w - tw) / 2.0, y + h / 2.0 + 4.0);
        }



        // Toolbox ----------------------------------------------------------------------------

        private double measureText(String s, Font f) {
            Text t = new Text(s);
            t.setFont(f);
            return t.getBoundsInLocal().getWidth();
        }
    }
}
