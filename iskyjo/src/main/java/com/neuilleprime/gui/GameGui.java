package com.neuilleprime.gui;

import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.image.PixelReader;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GameGui extends Application {

    // Models ----------------------------------------------------------------------------

    public static class Card {
        public final int value;
        public boolean selected;
        public boolean faceUp;

        /**
         * Creates a new Card with the given value and face orientation.
         *
         * @param value  the numeric value of the card (can be negative)
         * @param faceUp {@code true} if the card should be visible face-up; {@code false} for face-down
         */
        public Card(int value, boolean faceUp) {
            this.value = value;
            this.faceUp = faceUp;
            this.selected = false;
        }
    }

    public static class CardGrid {
        public final int cols;
        public final int rows;
        private final Card[][] slots;

        /**
         * Creates a new CardGrid with the specified dimensions.
         * All slots are initialised to {@code null} (empty).
         *
         * @param cols number of columns in the grid
         * @param rows number of rows in the grid
         */
        public CardGrid(int cols, int rows) {
            this.cols = cols;
            this.rows = rows;
            this.slots = new Card[cols][rows];
        }

        /**
         * Returns the card at the given grid position, or {@code null} if the slot is empty.
         *
         * @param col zero-based column index
         * @param row zero-based row index
         * @return the {@link Card} at {@code (col, row)}, or {@code null}
         */
        public Card get(int col, int row) {
            return slots[col][row];
        }

        /**
         * Places a card at the given grid position.
         * Pass {@code null} to clear the slot.
         *
         * @param col  zero-based column index
         * @param row  zero-based row index
         * @param card the {@link Card} to place, or {@code null} to empty the slot
         */
        public void set(int col, int row, Card card) {
            slots[col][row] = card;
        }

        /**
         * Searches the entire grid and returns the first card that is currently selected.
         *
         * @return the first selected {@link Card}, or {@code null} if none is selected
         */
        public Card getSelected() {
            for (int c = 0; c < cols; c++) {
                for (int r = 0; r < rows; r++) {
                    if (slots[c][r] != null && slots[c][r].selected) {
                        return slots[c][r];
                    }
                }
            }
            return null;
        }
    }

    public static class GameState {
        public final CardGrid grid;
        public final List<Card> jokers;
        public final List<Card> consumables;
        public Card drawPile;
        public Card discardPile;
        public int scoreToBeat;
        public int playerScore;

        /**
         * Creates a new GameState with an empty grid of the given dimensions.
         * The joker and consumable lists start empty, and {@code scoreToBeat} defaults to 0.
         *
         * @param gridCols number of columns in the card grid
         * @param gridRows number of rows in the card grid
         */
        public GameState(int gridCols, int gridRows) {
            this.grid = new CardGrid(gridCols, gridRows);
            this.jokers = new ArrayList<>();
            this.consumables = new ArrayList<>();
            this.scoreToBeat = 0;
            this.playerScore = 0;
        }
    }

    // Fonts ----------------------------------------------------------------------------

    private static Font mainFont;
    private static Font mainFontBold;
    private static Font balatroFont;

    static {
        try {
            var stream1 = GameGui.class.getResourceAsStream("/Assets/Fonts/VCR_OSD_MONO.ttf");
            if (stream1 == null) throw new RuntimeException("Font [VCR OSD MONO] not found");
            mainFont = Font.loadFont(stream1, 14);
            if (mainFont == null) throw new RuntimeException("Font load failed");
            mainFontBold = Font.font(mainFont.getFamily(), FontWeight.BOLD, 14);

            var stream2 = GameGui.class.getResourceAsStream("/Assets/Fonts/balatro.otf");
            if (stream2 == null) throw new RuntimeException("Font [Balatro] not found");
            balatroFont = Font.loadFont(stream2, 14);

        } catch (Exception e) {
            mainFont = Font.font("Courier New", FontWeight.NORMAL, 14);
            mainFontBold = Font.font("Courier New", FontWeight.BOLD, 14);
            balatroFont = Font.font("Courier New", FontWeight.NORMAL, 14);
            System.err.println("[GameGui] Font fallback: " + e.getMessage());
        }
    }

    // Standalone window ----------------------------------------------------------------------------

    /**
     * JavaFX entry point. Configures and displays the primary game window,
     * binding the {@link GameCanvas} size to the stage dimensions and
     * populating it with a demo game state.
     *
     * @param primaryStage the main window provided by the JavaFX runtime
     */
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

    // Demo state ----------------------------------------------------------------------------

    /**
     * Builds and returns a pre-populated {@link GameState} suitable for UI testing.
     * The grid is 4×3 and contains a handful of face-up and face-down cards,
     * two jokers, one consumable, plus draw and discard piles.
     *
     * @return a ready-to-use demo {@link GameState}
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

    /**
     * Application entry point. Delegates to {@link Application#launch} to
     * start the JavaFX runtime and open the game window.
     *
     * @param args command-line arguments (passed through to JavaFX)
     */
    public static void main(String[] args) {
        launch(args);
    }

    // Game canvas ----------------------------------------------------------------------------

    public static class GameCanvas extends Canvas {
        private static final double TOP_BAR_H = 64.0;
        private static final double BOTTOM_BAR_H = 190.0;
        private static final double PADDING = 22.0;
        private static final double SECTION_GAP = 16.0;
        private static final double SCORE_PANEL_W = 250.0;

        private static final double CARD_W = 82.0;
        private static final double CARD_H = 114.0;
        private static final double CARD_GAP = 14.0;
        private static final double CARD_RADIUS = 10.0;

        private static final double SPECIAL_CARD_W = 70.0;
        private static final double SPECIAL_CARD_H = 98.0;

        private static final double DRAW_ZONE_W = 180.0;

        private static final double SETTINGS_BUTTON_X = 30.0;
        private static final double SETTINGS_BUTTON_SIZE = 28.0;
        private static final double SETTINGS_BUTTON_Y = 30.0;

        private static final double PANEL_X = SETTINGS_BUTTON_X + SETTINGS_BUTTON_SIZE + 10.0;
        private static final double PANEL_Y = SETTINGS_BUTTON_Y - 2.0;
        private static final double PANEL_W = 260.0;
        private static final double PANEL_H = 330.0;
        private static final double LEAVE_PAD = 10.0;
        private static final double LEAVE_BUTTON_HEIGHT = 32.0;
        private static final double DEBUG_BUTTON_HEIGHT = 26.0;
        private static final double DEBUG_WINDOW_BUTTON_SPACING = 8.0;

        private static final int BACKGROUND_CARD_W = 82;
        private static final int BACKGROUND_CARD_H = 114;
        private static final int BACKGROUND_CARD_GAP = 20;
        private static final double BACKGROUND_CARD_OPACITY = 0.05;

        private static final Color ACCENT = Color.web("#ffc800");
        private static final Color BG = Color.web("#1d2b53");
        private static final Color GRID_BG = Color.web("#1a1a34");
        private static final Color ZONE_BORDER = Color.web("#394b86");
        private static final Color TEXT_DIM = Color.web("#a0acd8");
        private static final Color CARD_FRONT = Color.web("#f0ede8");
        private static final Color CARD_BACK = Color.web("#1e3a7a");
        private static final Color CARD_EMPTY = Color.web("#232338");

        private static final Color JOKER_BG = Color.web("#3d1060");
        private static final Color JOKER_BORD = Color.web("#8c3fc0");
        private static final Color JOKER_LABEL = Color.web("#cc88ff");

        private static final Color CONSU_BG = Color.web("#0d3d22");
        private static final Color CONSU_BORD = Color.web("#2a9c55");
        private static final Color CONSU_LABEL = Color.web("#66e899");

        private static final int DEBUG_FLIP_SELECTED = 0;
        private static final int DEBUG_ADD_CARD = 1;
        private static final int DEBUG_REMOVE_CARD = 2;
        private static final int DEBUG_ADD_JOKER = 3;
        private static final int DEBUG_REMOVE_JOKER = 4;
        private static final int DEBUG_ADD_CONSU = 5;
        private static final int DEBUG_REMOVE_CONSU = 6;
        private static final int DEBUG_OPEN_SHOP = 7;
        private static final int DEBUG_SCORE = 8;

        private static final String[] DEBUG_BUTTON_LABELS = {
            "FLIP SELECTED",
            "+ CARD",
            "- CARD",
            "+ JOKER",
            "- JOKER",
            "+ CONSU",
            "- CONSU",
            "OPEN SHOP",
            "EMULATE SCORE"
            
        };

        private Card debugTargetCard = null;
        private boolean dragStarted = false;
        private double pressMouseX = 0.0;
        private double pressMouseY = 0.0;
        private static final double DRAG_THRESHOLD = 8.0;

        private final Stage stage;
        private final GameState state;
        private final Font fontBase;
        private final Font fontBold;
        private final Runnable onLeave;
        private final Runnable onOpenShop;
        private final boolean debugMode;

        private boolean hoverDraw = false;
        private boolean hoverDiscard = false;
        private boolean hoverSettingsButton = false;
        private boolean hoverLeaveButton = false;
        private int hoverDebugAction = -1;
        private int hoverGridCol = -1;
        private int hoverGridRow = -1;
        private int hoverJokerIndex = -1;
        private int hoverConsumableIndex = -1;

        private Card draggedCard = null;
        private Card dragOriginCard = null;

        private boolean draggingFromGrid = false;
        private boolean draggingFromDrawPile = false;
        private boolean draggingFromDiscardPile = false;

        private int dragSourceCol = -1;
        private int dragSourceRow = -1;

        private double dragMouseX = 0.0;
        private double dragMouseY = 0.0;
        private double dragOffsetX = 0.0;
        private double dragOffsetY = 0.0;

        private boolean hoverDiscardDropZone = false;
        private int hoverDropGridCol = -1;
        private int hoverDropGridRow = -1;

        private boolean settingsPanelOpen = false;

        private Image cardBackImg = null;
        private Image cardOverlayImg = null;
        private Image backgroundPattern = null;

        private final Map<String, Image> tintedOverlayCache = new HashMap<>();
        private final Map<Card, AnimBox.CardFlipAnimation> activeFlipAnimations = new HashMap<>();

        private AnimationTimer animationTimer;
        private long lastFrameTime = -1L;
        private double animTime = 0.0;

        /**
         * Convenience constructor that creates a {@code GameCanvas} without a custom shop callback.
         * Equivalent to calling the full constructor with an empty {@code onOpenShop} runnable.
         *
         * @param stage     the owning JavaFX {@link Stage} (used for drag-to-move and minimise)
         * @param state     the {@link GameState} to render
         * @param fontBase  regular-weight font used for labels and small text
         * @param fontBold  bold-weight font used for titles and card values
         * @param onLeave   callback invoked when the player confirms leaving the game
         * @param debugMode {@code true} to display the debug action panel in settings
         */
        public GameCanvas(Stage stage, GameState state,
                          Font fontBase, Font fontBold,
                          Runnable onLeave, boolean debugMode) {
            this(stage, state, fontBase, fontBold, onLeave, () -> {}, debugMode);
        }

        /**
         * Full constructor. Initialises the canvas, loads image assets, wires up all
         * mouse and keyboard interaction handlers, and starts the animation timer.
         *
         * @param stage       the owning JavaFX {@link Stage}
         * @param state       the {@link GameState} to render
         * @param fontBase    regular-weight font used throughout the UI
         * @param fontBold    bold-weight font used for emphasis and card values
         * @param onLeave     callback invoked when the player leaves the game
         * @param onOpenShop  callback invoked when the debug "OPEN SHOP" button is pressed
         * @param debugMode   {@code true} to show debug controls inside the settings panel
         */
        public GameCanvas(Stage stage, GameState state,
                          Font fontBase, Font fontBold,
                          Runnable onLeave, Runnable onOpenShop, boolean debugMode) {
            this.stage = stage;
            this.state = state;
            this.fontBase = fontBase;
            this.fontBold = fontBold;
            this.onLeave = onLeave;
            this.onOpenShop = onOpenShop;
            this.debugMode = debugMode;

            tryLoadImages();
            setupInteractions();
            startAnimation();

            widthProperty().addListener((o, a, b) -> render());
            heightProperty().addListener((o, a, b) -> render());
        }

        // Assets ----------------------------------------------------------------------------

        /**
         * Attempts to load the card-back, card-overlay, and background-pattern images
         * from the classpath. Failures are silently swallowed; missing assets cause
         * the renderer to fall back to solid-colour drawing.
         */
        private void tryLoadImages() {
            try {
                var resource = GameCanvas.class.getResource("/Assets/Cards/card_back.png");
                if (resource != null) cardBackImg = new Image(resource.toExternalForm());
            } catch (Exception ignored) {
            }

            try {
                var resource = GameCanvas.class.getResource("/Assets/Cards/card_overlay-2.png");
                if (resource != null) cardOverlayImg = new Image(resource.toExternalForm());
            } catch (Exception ignored) {
            }

            try {
                var resource = GameCanvas.class.getResource("/Assets/Cards/card_bg.png");
                if (resource != null) backgroundPattern = new Image(resource.toExternalForm());
            } catch (Exception ignored) {
            }
        }


        private void updateFlipAnimations() {
            if (activeFlipAnimations.isEmpty()) {
                return;
            }

            List<Card> finishedCards = new ArrayList<>();

            for (Map.Entry<Card, AnimBox.CardFlipAnimation> entry : activeFlipAnimations.entrySet()) {
                Card card = entry.getKey();
                AnimBox.CardFlipAnimation animation = entry.getValue();

                animation.tick();

                if (animation.shouldSwitchFace()) {
                    card.faceUp = !card.faceUp;
                }

                if (animation.isFinished()) {
                    finishedCards.add(card);
                }
            }

            for (Card card : finishedCards) {
                activeFlipAnimations.remove(card);
            }
        }

        /**
         * Starts the JavaFX {@link AnimationTimer} that drives continuous rendering.
         * Frames are throttled to a minimum interval of 25 ms (~40 fps) and
         * {@code animTime} is advanced each tick to power time-based animations
         * such as the card-shake and scrolling background.
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
                    updateFlipAnimations();
                    render();
                }
            };
            animationTimer.start();
        }

        // Layout ----------------------------------------------------------------------------

        private record GridMetrics(
            double cardW,
            double cardH,
            double gap,
            double totalW,
            double totalH,
            double originX,
            double originY
        ) {}

        private GridMetrics gridMetrics() {
            double pad = 16.0;

            double availableW = Math.max(40.0, gridZoneW() - pad * 2.0);
            double availableH = Math.max(40.0, playAreaH() - pad * 2.0);

            double baseTotalW = state.grid.cols * CARD_W + (state.grid.cols - 1) * CARD_GAP;
            double baseTotalH = state.grid.rows * CARD_H + (state.grid.rows - 1) * CARD_GAP;

            double scaleX = availableW / baseTotalW;
            double scaleY = availableH / baseTotalH;
            double maxFitScale = Math.min(scaleX, scaleY);

            double windowScaleX = getWidth() / 1400.0;
            double windowScaleY = getHeight() / 900.0;
            double windowScale = Math.min(windowScaleX, windowScaleY);

            double minScale = 1.2 * windowScale;

            double scale = Math.max(minScale, 1.0);
            scale = Math.min(scale, maxFitScale);

            double cardW = CARD_W * scale;
            double cardH = CARD_H * scale;
            double gap = CARD_GAP * scale;

            double totalW = state.grid.cols * cardW + (state.grid.cols - 1) * gap;
            double totalH = state.grid.rows * cardH + (state.grid.rows - 1) * gap;

            double originX = gridZoneX() + (gridZoneW() - totalW) / 2.0;
            double originY = playAreaY() + (playAreaH() - totalH) / 2.0;

            return new GridMetrics(cardW, cardH, gap, totalW, totalH, originX, originY);
        }


        /**
         * Returns the display colour for a card's numeric value.
         * <ul>
         *   <li>Negative values → red</li>
         *   <li>0 → black</li>
         *   <li>1–4 → green</li>
         *   <li>5–8 → pink/magenta</li>
         *   <li>9+ → dark blue</li>
         * </ul>
         *
         * @param value the card value to colourise
         * @return the corresponding {@link Color}
         */
        private static Color cardValueColor(int value) {
            if (value < 0) return Color.web("#b11919");
            if (value == 0) return Color.web("#000000");
            if (value <= 4) return Color.web("#27d383");
            if (value <= 8) return Color.web("#f040bb");
            return Color.web("#4020ce");
        }

        /**
         * Returns the tint colour used for the card overlay image, derived from the card's value.
         *
         * @param value the card value to derive the tint from
         * @return the tint {@link Color} matching the card's value range
         */
        private Color cardOverlayTint(int value) {
            return cardValueColor(value);
        }

        /**
         * Returns the X coordinate of the left edge of the main play area.
         *
         * @return play area left X, equal to {@code PADDING}
         */
        private double playAreaX() {
            return PADDING;
        }

        /**
         * Returns the Y coordinate of the top edge of the main play area,
         * just below the top bar.
         *
         * @return play area top Y
         */
        private double playAreaY() {
            return TOP_BAR_H + 16.0;
        }

        /**
         * Returns the total width of the main play area (canvas width minus left and right padding).
         *
         * @return play area width in pixels
         */
        private double playAreaW() {
            return getWidth() - PADDING * 2.0;
        }

        /**
         * Returns the height of the main play area, excluding the top bar and bottom bar.
         *
         * @return play area height in pixels
         */
        private double playAreaH() {
            return getHeight() - TOP_BAR_H - BOTTOM_BAR_H - 32.0;
        }

        /**
         * Returns the X coordinate of the draw/discard panel's left edge.
         *
         * @return draw panel left X
         */
        private double drawPanelX() {
            return playAreaX();
        }

        /**
         * Returns the Y coordinate of the draw/discard panel's top edge.
         *
         * @return draw panel top Y
         */
        private double drawPanelY() {
            return playAreaY();
        }

        /**
         * Returns the fixed width of the draw/discard panel.
         *
         * @return draw panel width in pixels
         */
        private double drawPanelW() {
            return DRAW_ZONE_W;
        }

        /**
         * Returns the height of the draw/discard panel, matching the play area height.
         *
         * @return draw panel height in pixels
         */
        private double drawPanelH() {
            return playAreaH();
        }

        /**
         * Returns the X coordinate of the card grid zone's left edge,
         * positioned immediately to the right of the draw/discard panel.
         *
         * @return grid zone left X
         */
        private double gridZoneX() {
            return drawPanelX() + drawPanelW() + SECTION_GAP;
        }

        /**
         * Returns the width allocated to the card grid zone,
         * computed from the remaining space between the draw panel and the score panel.
         *
         * @return grid zone width in pixels
         */
        private double gridZoneW() {
            return playAreaW() - drawPanelW() - SCORE_PANEL_W - SECTION_GAP * 2.0;
        }

        /**
         * Returns the X coordinate of the score panel's left edge,
         * anchored to the right side of the grid zone.
         *
         * @return score panel left X
         */
        private double scorePanelX() {
            return gridZoneX() + gridZoneW() + SECTION_GAP;
        }


        private double[] gridOrigin() {
            GridMetrics m = gridMetrics();
            return new double[] {m.originX(), m.originY()};
        }

        /**
         * Returns the Y coordinate of the top edge of the bottom zone (jokers + consumables bar).
         *
         * @return bottom zone top Y
         */
        private double bottomZoneY() {
            return getHeight() - BOTTOM_BAR_H + 10.0;
        }

        /**
         * Returns the height of the bottom zone panel.
         *
         * @return bottom zone height in pixels
         */
        private double bottomZoneH() {
            return BOTTOM_BAR_H - 20.0;
        }

        /**
         * Returns the total available width within the bottom zone,
         * accounting for left and right padding.
         *
         * @return available width in pixels
         */
        private double bottomAvailableW() {
            return getWidth() - PADDING * 2.0;
        }

        /**
         * Returns the width of the joker zone, which takes 60% of the available bottom width.
         *
         * @return joker zone width in pixels
         */
        private double jokerZoneW() {
            return (bottomAvailableW() - SECTION_GAP) * 0.60;
        }

        /**
         * Returns the width of the consumable zone, which occupies the remainder of the
         * bottom bar after the joker zone and section gap.
         *
         * @return consumable zone width in pixels
         */
        private double consumableZoneW() {
            return bottomAvailableW() - jokerZoneW() - SECTION_GAP;
        }

        /**
         * Returns the X coordinate of the joker zone's left edge.
         *
         * @return joker zone left X
         */
        private double jokerZoneX() {
            return PADDING;
        }

        /**
         * Returns the X coordinate of the consumable zone's left edge,
         * positioned right after the joker zone.
         *
         * @return consumable zone left X
         */
        private double consumableZoneX() {
            return jokerZoneX() + jokerZoneW() + SECTION_GAP;
        }

        /**
         * Returns the X coordinate of the draw pile card, centred horizontally in the draw panel.
         *
         * @return draw card left X
         */
        private double drawCardX() {
            return drawPanelX() + (drawPanelW() - SPECIAL_CARD_W) / 2.0;
        }

        /**
         * Returns the X coordinate of the discard pile card.
         * Shares the same horizontal centre as the draw card.
         *
         * @return discard card left X
         */
        private double discardCardX() {
            return drawCardX();
        }

        /**
         * Returns the Y coordinate of the draw pile card,
         * placed in the upper half of the draw panel.
         *
         * @return draw card top Y
         */
        private double drawCardY() {
            return drawPanelY() + drawPanelH() / 2.0 - SPECIAL_CARD_H - 12.0;
        }

        /**
         * Returns the Y coordinate of the discard pile card,
         * placed in the lower half of the draw panel.
         *
         * @return discard card top Y
         */
        private double discardCardY() {
            return drawPanelY() + drawPanelH() / 2.0 + 12.0;
        }



        /**
         * Returns the X coordinate of the "Leave Game" button inside the settings panel.
         *
         * @return leave button left X
         */
        private double getLeaveButtonX() {
            return PANEL_X + LEAVE_PAD;
        }

        /**
         * Returns the Y coordinate of the "Leave Game" button,
         * pinned to the bottom of the settings panel.
         *
         * @return leave button top Y
         */
        private double getLeaveButtonY() {
            return PANEL_Y + PANEL_H - LEAVE_BUTTON_HEIGHT - LEAVE_PAD;
        }

        /**
         * Returns the width of the "Leave Game" button,
         * spanning the full panel width minus padding on both sides.
         *
         * @return leave button width in pixels
         */
        private double getLeaveButtonWidth() {
            return PANEL_W - LEAVE_PAD * 2.0;
        }

        /**
         * Returns the X coordinate shared by all debug action buttons in the settings panel.
         *
         * @return debug buttons left X
         */
        private double getDebugButtonX() {
            return PANEL_X + LEAVE_PAD;
        }

        /**
         * Returns the width shared by all debug action buttons in the settings panel.
         *
         * @return debug button width in pixels
         */
        private double getDebugButtonWidth() {
            return PANEL_W - LEAVE_PAD * 2.0;
        }

        /**
         * Returns the Y coordinate of the first debug button, just below the settings panel header.
         *
         * @return first debug button top Y
         */
        private double debugStartY() {
            return PANEL_Y + 42.0;
        }

        /**
         * Returns the Y coordinate of the debug button at the given index,
         * stacking buttons vertically with a fixed spacing.
         *
         * @param index zero-based index of the debug button
         * @return top Y of the button at {@code index}
         */
        private double getDebugButtonY(int index) {
            return debugStartY() + index * (DEBUG_BUTTON_HEIGHT + DEBUG_WINDOW_BUTTON_SPACING);
        }

        // Hitboxes ----------------------------------------------------------------------------

        private boolean isInsideDrawCard(double x, double y) {
            double panelX = drawPanelX();
            double panelY = drawPanelY();
            double panelW = drawPanelW();
            double panelH = drawPanelH();

            double innerPad = 12.0;
            double zoneGap = 12.0;
            double zoneW = panelW - innerPad * 2.0;
            double zoneH = (panelH - innerPad * 2.0 - zoneGap - 36.0) / 2.0;

            double drawZoneX = panelX + innerPad;
            double drawZoneY = panelY + 48.0;

            return x >= drawZoneX && x <= drawZoneX + zoneW
                && y >= drawZoneY && y <= drawZoneY + zoneH;
        }

        private boolean isInsideDiscardCard(double x, double y) {
            double panelX = drawPanelX();
            double panelY = drawPanelY();
            double panelW = drawPanelW();
            double panelH = drawPanelH();

            double innerPad = 12.0;
            double zoneGap = 12.0;
            double zoneW = panelW - innerPad * 2.0;
            double zoneH = (panelH - innerPad * 2.0 - zoneGap - 36.0) / 2.0;

            double drawZoneY = panelY + 48.0;
            double discardZoneY = drawZoneY + zoneH + zoneGap;
            double zoneX = panelX + innerPad;

            return x >= zoneX && x <= zoneX + zoneW
                && y >= discardZoneY && y <= discardZoneY + zoneH;
        }

        /**
         * Returns {@code true} if the given point lies within the settings (hamburger) button's hit area.
         *
         * @param x cursor X coordinate
         * @param y cursor Y coordinate
         * @return {@code true} if the point is over the settings button
         */
        private boolean isInsideSettingsButton(double x, double y) {
            return x >= SETTINGS_BUTTON_X && x <= SETTINGS_BUTTON_X + SETTINGS_BUTTON_SIZE
                && y >= SETTINGS_BUTTON_Y && y <= SETTINGS_BUTTON_Y + SETTINGS_BUTTON_SIZE;
        }

        /**
         * Returns {@code true} if the settings panel is open and the given point is inside it.
         *
         * @param x cursor X coordinate
         * @param y cursor Y coordinate
         * @return {@code true} if the point is inside the open settings panel
         */
        private boolean inPanel(double x, double y) {
            return settingsPanelOpen
                && x >= PANEL_X && x <= PANEL_X + PANEL_W
                && y >= PANEL_Y && y <= PANEL_Y + PANEL_H;
        }

        /**
         * Returns {@code true} if the settings panel is open and the given point is
         * within the "Leave Game" button's hit area.
         *
         * @param x cursor X coordinate
         * @param y cursor Y coordinate
         * @return {@code true} if the point is over the leave button
         */
        private boolean isInsideLeaveButton(double x, double y) {
            return settingsPanelOpen
                && x >= getLeaveButtonX() && x <= getLeaveButtonX() + getLeaveButtonWidth()
                && y >= getLeaveButtonY() && y <= getLeaveButtonY() + LEAVE_BUTTON_HEIGHT;
        }

        /**
         * Returns the index of the debug button under the given point,
         * or {@code -1} if the point does not hit any button or the panel/debug mode is inactive.
         *
         * @param x cursor X coordinate
         * @param y cursor Y coordinate
         * @return zero-based index of the hovered debug button, or {@code -1} if none
         */
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

        private int[] getGridCellAt(double x, double y) {
            GridMetrics m = gridMetrics();
            for (int c = 0; c < state.grid.cols; c++) {
                for (int r = 0; r < state.grid.rows; r++) {
                    double cx = m.originX() + c * (m.cardW() + m.gap());
                    double cy = m.originY() + r * (m.cardH() + m.gap());
                    if (x >= cx && x <= cx + m.cardW() && y >= cy && y <= cy + m.cardH()) {
                        return new int[] {c, r};
                    }
                }
            }
            return null;
        }

        /**
         * Returns the index of the joker card under the given canvas coordinates,
         * or {@code -1} if none is hit or the joker list is empty.
         *
         * @param x canvas X coordinate
         * @param y canvas Y coordinate
         * @return zero-based joker index, or {@code -1}
         */
        private int getJokerIndexAt(double x, double y) {
            if (state.jokers.isEmpty()) return -1;

            double totalW = state.jokers.size() * (SPECIAL_CARD_W + CARD_GAP) - CARD_GAP;
            double startX = jokerZoneX() + (jokerZoneW() - totalW) / 2.0;
            double cardY = bottomZoneY() + 40.0;

            for (int i = 0; i < state.jokers.size(); i++) {
                double cx = startX + i * (SPECIAL_CARD_W + CARD_GAP);
                if (x >= cx && x <= cx + SPECIAL_CARD_W && y >= cardY && y <= cardY + SPECIAL_CARD_H) {
                    return i;
                }
            }
            return -1;
        }

        /**
         * Returns the index of the consumable card under the given canvas coordinates,
         * or {@code -1} if none is hit or the consumable list is empty.
         *
         * @param x canvas X coordinate
         * @param y canvas Y coordinate
         * @return zero-based consumable index, or {@code -1}
         */
        private int getConsumableIndexAt(double x, double y) {
            if (state.consumables.isEmpty()) return -1;

            double totalW = state.consumables.size() * (SPECIAL_CARD_W + CARD_GAP) - CARD_GAP;
            double startX = consumableZoneX() + (consumableZoneW() - totalW) / 2.0;
            double cardY = bottomZoneY() + 40.0;

            for (int i = 0; i < state.consumables.size(); i++) {
                double cx = startX + i * (SPECIAL_CARD_W + CARD_GAP);
                if (x >= cx && x <= cx + SPECIAL_CARD_W && y >= cardY && y <= cardY + SPECIAL_CARD_H) {
                    return i;
                }
            }
            return -1;
        }

        private boolean isDraggingDrawCard() {
            return draggedCard != null && draggingFromDrawPile;
        }

        private boolean isDraggingDiscardCard() {
            return draggedCard != null && draggingFromDiscardPile;
        }

        private boolean isDraggingAnyCard() {
            return draggedCard != null;
        }

        private void cancelDrag() {
            draggedCard = null;
            dragOriginCard = null;
            draggingFromGrid = false;
            draggingFromDrawPile = false;
            draggingFromDiscardPile = false;
            dragSourceCol = -1;
            dragSourceRow = -1;
            hoverDiscardDropZone = false;
            hoverDropGridCol = -1;
            hoverDropGridRow = -1;
        }

        private void beginGridCardDrag(int col, int row, double mouseX, double mouseY) {
            Card card = state.grid.get(col, row);
            if (card == null) return;

            draggedCard = card;
            dragOriginCard = card;
            draggingFromGrid = true;
            draggingFromDrawPile = false;
            draggingFromDiscardPile = false;
            dragSourceCol = col;
            dragSourceRow = row;

            GridMetrics m = gridMetrics();
            double cardX = m.originX() + col * (m.cardW() + m.gap());
            double cardY = m.originY() + row * (m.cardH() + m.gap());

            dragOffsetX = mouseX - cardX;
            dragOffsetY = mouseY - cardY;
            dragMouseX = mouseX;
            dragMouseY = mouseY;
        }

        private void beginDrawPileDrag(double mouseX, double mouseY) {
            if (state.drawPile == null) return;

            draggedCard = new Card(state.drawPile.value, false);
            dragOriginCard = state.drawPile;
            draggingFromGrid = false;
            draggingFromDrawPile = true;
            draggingFromDiscardPile = false;
            dragSourceCol = -1;
            dragSourceRow = -1;

            dragOffsetX = mouseX - drawCardX();
            dragOffsetY = mouseY - drawCardY();
            dragMouseX = mouseX;
            dragMouseY = mouseY;
        }

        private void beginDiscardPileDrag(double mouseX, double mouseY) {
            if (state.discardPile == null) return;

            draggedCard = state.discardPile;
            dragOriginCard = state.discardPile;
            draggingFromGrid = false;
            draggingFromDrawPile = false;
            draggingFromDiscardPile = true;
            dragSourceCol = -1;
            dragSourceRow = -1;

            dragOffsetX = mouseX - discardCardX();
            dragOffsetY = mouseY - discardCardY();
            dragMouseX = mouseX;
            dragMouseY = mouseY;
        }

        private void dropDraggedGridCardIntoDiscard() {
            if (!isDraggingGridCard()) return;
            if (dragSourceCol < 0 || dragSourceRow < 0) return;

            state.grid.set(dragSourceCol, dragSourceRow, null);
            draggedCard.selected = false;
            state.discardPile = draggedCard;
            cancelDrag();
        }

        private void dropDraggedDrawCardIntoGrid(int col, int row) {
            if (!isDraggingDrawCard()) return;

            Card oldCard = state.grid.get(col, row);
            if (oldCard != null) {
                oldCard.selected = false;
                state.discardPile = oldCard;
            }

            draggedCard.faceUp = false;
            draggedCard.selected = false;
            state.grid.set(col, row, draggedCard);

            cancelDrag();
        }

        private void dropDraggedDiscardCardIntoGrid(int col, int row) {
            if (!isDraggingDiscardCard()) return;
            if (state.discardPile == null) return;

            Card oldCard = state.grid.get(col, row);
            if (oldCard != null) {
                oldCard.selected = false;
                state.discardPile = oldCard;
            } else {
                state.discardPile = null;
            }

            draggedCard.selected = false;
            state.grid.set(col, row, draggedCard);

            if (oldCard == null) {
                state.discardPile = null;
            }

            cancelDrag();
        }

        private boolean isDraggingGridCard() {
            return draggedCard != null && draggingFromGrid;
        }


        private Card getSelectedGridCard() {
            for (int c = 0; c < state.grid.cols; c++) {
                for (int r = 0; r < state.grid.rows; r++) {
                    Card card = state.grid.get(c, r);
                    if (card != null && card.selected) {
                        return card;
                    }
                }
            }
            return null;
        }

        private int[] getSelectedGridCardPosition() {
            for (int c = 0; c < state.grid.cols; c++) {
                for (int r = 0; r < state.grid.rows; r++) {
                    Card card = state.grid.get(c, r);
                    if (card != null && card.selected) {
                        return new int[] {c, r};
                    }
                }
            }
            return null;
        }

        private boolean canDiscardSelectedCard() {
            return getSelectedGridCard() != null || isDraggingGridCard();
        }

        private boolean canPlaceDrawCard() {
            return (state.drawPile != null && state.drawPile.selected) || isDraggingDrawCard();
        }


        private void discardSelectedGridCard() {
            int[] pos = getSelectedGridCardPosition();
            if (pos == null) return;

            Card card = state.grid.get(pos[0], pos[1]);
            if (card == null) return;

            state.grid.set(pos[0], pos[1], null);
            card.selected = false;
            state.discardPile = card;
        }

        private void placeDrawCardIntoGrid(int col, int row) {
            if (state.drawPile == null) return;

            Card drawnCard = state.drawPile;
            drawnCard.faceUp = false;
            drawnCard.selected = false;

            Card oldCard = state.grid.get(col, row);
            if (oldCard != null) {
                oldCard.selected = false;
                state.discardPile = oldCard;
            }

            state.grid.set(col, row, drawnCard);
            state.drawPile = null;
        }


        // Interaction ----------------------------------------------------------------------------

        /**
         * Registers all mouse and keyboard event handlers on this canvas.
         * Handles hover detection, window dragging, click dispatch to game actions,
         * settings panel toggle, and ESC-to-close for the settings panel.
         */
        private void setupInteractions() {
            setOnMouseMoved(e -> {
                double x = e.getX();
                double y = e.getY();

                boolean mouseOverDraw = isInsideDrawCard(x, y);
                boolean mouseOverDiscard = isInsideDiscardCard(x, y);
                boolean mouseOverSettings = isInsideSettingsButton(x, y);
                boolean mouseOverLeave = isInsideLeaveButton(x, y);
                int hoveredDebug = hitTestDebugButton(x, y);

                int[] hoveredGrid = getGridCellAt(x, y);
                int newHoverGridCol = hoveredGrid != null ? hoveredGrid[0] : -1;
                int newHoverGridRow = hoveredGrid != null ? hoveredGrid[1] : -1;
                int newHoverJokerIndex = getJokerIndexAt(x, y);
                int newHoverConsumableIndex = getConsumableIndexAt(x, y);

                boolean changed = mouseOverDraw != hoverDraw
                    || mouseOverDiscard != hoverDiscard
                    || mouseOverSettings != hoverSettingsButton
                    || mouseOverLeave != hoverLeaveButton
                    || hoveredDebug != hoverDebugAction
                    || newHoverGridCol != hoverGridCol
                    || newHoverGridRow != hoverGridRow
                    || newHoverJokerIndex != hoverJokerIndex
                    || newHoverConsumableIndex != hoverConsumableIndex;

                if (changed) {
                    hoverDraw = mouseOverDraw;
                    hoverDiscard = mouseOverDiscard;
                    hoverSettingsButton = mouseOverSettings;
                    hoverLeaveButton = mouseOverLeave;
                    hoverDebugAction = hoveredDebug;
                    hoverGridCol = newHoverGridCol;
                    hoverGridRow = newHoverGridRow;
                    hoverJokerIndex = newHoverJokerIndex;
                    hoverConsumableIndex = newHoverConsumableIndex;
                    render();
                }

                boolean hand = mouseOverDraw
                    || mouseOverDiscard
                    || mouseOverSettings
                    || mouseOverLeave
                    || hoveredDebug >= 0
                    || hoveredGrid != null
                    || newHoverJokerIndex >= 0
                    || newHoverConsumableIndex >= 0;

                setCursor(hand ? Cursor.HAND : Cursor.DEFAULT);
            });

            setOnMouseExited(e -> {
                hoverDraw = false;
                hoverDiscard = false;
                hoverSettingsButton = false;
                hoverLeaveButton = false;
                hoverDebugAction = -1;
                hoverGridCol = -1;
                hoverGridRow = -1;
                hoverJokerIndex = -1;
                hoverConsumableIndex = -1;
                render();
            });


            setOnMouseClicked(e -> {
                double x = e.getX();
                double y = e.getY();

                if (isDraggingAnyCard()) {
                    return;
                }

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
                    performDebugAction(debugAction);
                    return;
                }

                if (settingsPanelOpen && !inPanel(x, y)) {
                    settingsPanelOpen = false;
                    render();
                    return;
                }

                int jokerIndex = getJokerIndexAt(x, y);
                if (jokerIndex >= 0) {
                    onJokerClicked(jokerIndex);
                    return;
                }

                int consumableIndex = getConsumableIndexAt(x, y);
                if (consumableIndex >= 0) {
                    onConsuClicked(consumableIndex);
                }
            });

            setOnMouseReleased(e -> {
                if (isDraggingGridCard()) {
                    if (dragStarted && isInsideDiscardCard(e.getX(), e.getY())) {
                        dropDraggedGridCardIntoDiscard();
                    } else {
                        cancelDrag();
                    }
                    render();
                    return;
                }

                if (isDraggingDrawCard()) {
                    int[] cell = getGridCellAt(e.getX(), e.getY());
                    if (dragStarted && cell != null) {
                        dropDraggedDrawCardIntoGrid(cell[0], cell[1]);
                    } else {
                        cancelDrag();
                    }
                    render();
                    return;
                }

                if (isDraggingDiscardCard()) {
                    int[] cell = getGridCellAt(e.getX(), e.getY());
                    if (dragStarted && cell != null) {
                        dropDraggedDiscardCardIntoGrid(cell[0], cell[1]);
                    } else {
                        cancelDrag();
                    }
                    render();
                }
            });

            setOnMouseDragged(e -> {
                dragMouseX = e.getX();
                dragMouseY = e.getY();

                double dx = dragMouseX - pressMouseX;
                double dy = dragMouseY - pressMouseY;
                double dist = Math.hypot(dx, dy);

                if (dist >= DRAG_THRESHOLD) {
                    dragStarted = true;
                }

                if (isDraggingGridCard()) {
                    hoverDiscardDropZone = isInsideDiscardCard(dragMouseX, dragMouseY);
                    hoverDropGridCol = -1;
                    hoverDropGridRow = -1;
                    render();
                    return;
                }

                if (isDraggingDrawCard() || isDraggingDiscardCard()) {
                    int[] hoverCell = getGridCellAt(dragMouseX, dragMouseY);
                    hoverDiscardDropZone = false;
                    hoverDropGridCol = hoverCell != null ? hoverCell[0] : -1;
                    hoverDropGridRow = hoverCell != null ? hoverCell[1] : -1;
                    render();
                }
            });

            setOnMousePressed(e -> {
                double x = e.getX();
                double y = e.getY();

                dragMouseX = x;
                dragMouseY = y;
                pressMouseX = x;
                pressMouseY = y;
                dragStarted = false;

                if (state.drawPile != null && isInsideDrawCard(x, y)) {
                    beginDrawPileDrag(x, y);
                    return;
                }

                if (state.discardPile != null && isInsideDiscardCard(x, y)) {
                    beginDiscardPileDrag(x, y);
                    return;
                }

                int[] cell = getGridCellAt(x, y);
                if (cell != null) {
                    Card card = state.grid.get(cell[0], cell[1]);
                    if (card != null) {
                        debugTargetCard = card;
                        beginGridCardDrag(cell[0], cell[1], x, y);
                        render();
                    }
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

        // Actions ----------------------------------------------------------------------------

        private void startFlip(Card card) {
            if (card == null) {
                return;
            }

            if (activeFlipAnimations.containsKey(card)) {
                return;
            }

            activeFlipAnimations.put(card, new AnimBox.CardFlipAnimation(18));
        }


        /**
         * Clears the {@code selected} flag on every card in the grid,
         * every joker, and every consumable.
         */
        private void deselectAll() {
            for (int c = 0; c < state.grid.cols; c++) {
                for (int r = 0; r < state.grid.rows; r++) {
                    if (state.grid.get(c, r) != null) {
                        state.grid.get(c, r).selected = false;
                    }
                }
            }
            state.jokers.forEach(card -> card.selected = false);
            state.consumables.forEach(card -> card.selected = false);
        }

        public void onDrawClicked() {
            if (state.drawPile == null) return;

            boolean wasSelected = state.drawPile.selected;
            deselectAll();
            state.drawPile.selected = !wasSelected;
            render();
        }

        public void onGridCellClicked(int col, int row) {
            if (isDraggingDrawCard()) {
                dropDraggedDrawCardIntoGrid(col, row);
                render();
                return;
            }

            Card card = state.grid.get(col, row);
            if (card == null) return;

            boolean wasSelected = card.selected;
            deselectAll();
            card.selected = !wasSelected;
            render();
        }

        /**
         * Called when the player clicks the discard pile card.
         * Override or extend to implement discard logic; currently logs and re-renders.
         */
        public void onDiscardClicked() {
            System.out.println("[GameGui] Discard clicked");
            render();
        }


        /**
         * Called when the player clicks a joker card in the bottom bar.
         * Toggles its selection state and deselects all other cards.
         *
         * @param i zero-based index of the clicked joker
         */
        public void onJokerClicked(int i) {
            Card card = state.jokers.get(i);
            boolean wasSelected = card.selected;
            deselectAll();
            card.selected = !wasSelected;
            render();
        }

        /**
         * Called when the player clicks a consumable card in the bottom bar.
         * Toggles its selection state and deselects all other cards.
         *
         * @param i zero-based index of the clicked consumable
         */
        public void onConsuClicked(int i) {
            Card card = state.consumables.get(i);
            boolean wasSelected = card.selected;
            deselectAll();
            card.selected = !wasSelected;
            render();
        }

        /**
         * Dispatches a debug action identified by its constant index to the appropriate handler method.
         * Triggers a re-render after the action completes.
         *
         * @param action one of the {@code DEBUG_*} constants (e.g. {@link #DEBUG_ADD_CARD})
         */
        private void performDebugAction(int action) {
            switch (action) {
                case DEBUG_FLIP_SELECTED ->flipDebugTargetCard();
                case DEBUG_ADD_CARD -> addDebugCard();
                case DEBUG_REMOVE_CARD -> removeDebugCard();
                case DEBUG_ADD_JOKER -> addDebugJoker();
                case DEBUG_REMOVE_JOKER -> removeDebugJoker();
                case DEBUG_ADD_CONSU -> addDebugConsu();
                case DEBUG_REMOVE_CONSU -> removeDebugConsu();
                case DEBUG_OPEN_SHOP -> onOpenShop.run();
                case DEBUG_SCORE -> debugScore();
                default -> {
                }
            }
            render();
        }

        private void flipDebugTargetCard() {
            if (debugTargetCard != null) {
                startFlip(debugTargetCard);
            }
        }

        /**
         * Debug action: places a new face-up card into the first empty slot found in the grid.
         * The card value is cycled from a small predefined pool. Does nothing if the grid is full.
         */
        private void addDebugCard() {
            for (int c = 0; c < state.grid.cols; c++) {
                for (int r = 0; r < state.grid.rows; r++) {
                    if (state.grid.get(c, r) == null) {
                        state.grid.set(c, r, new Card(nextDebugCardValue(), true));
                        deselectAll();
                        return;
                    }
                }
            }
        }

        /**
         * Debug action: removes a card from the grid.
         * If a card is currently selected, that card is removed first.
         * Otherwise, the last non-null card found (bottom-right first) is removed.
         */
        private void removeDebugCard() {
            for (int c = 0; c < state.grid.cols; c++) {
                for (int r = 0; r < state.grid.rows; r++) {
                    Card card = state.grid.get(c, r);
                    if (card != null && card.selected) {
                        state.grid.set(c, r, null);
                        return;
                    }
                }
            }
            for (int c = state.grid.cols - 1; c >= 0; c--) {
                for (int r = state.grid.rows - 1; r >= 0; r--) {
                    if (state.grid.get(c, r) != null) {
                        state.grid.set(c, r, null);
                        return;
                    }
                }
            }
        }

        /**
         * Debug action: appends a new face-up joker card (value 0) to the joker list
         * and clears all current selections.
         */
        private void addDebugJoker() {
            deselectAll();
            state.jokers.add(new Card(0, true));
        }

        /**
         * Debug action: removes a joker from the list.
         * If a joker is currently selected, it is removed; otherwise the last joker is removed.
         * Does nothing if the joker list is empty.
         */
        private void removeDebugJoker() {
            for (int i = 0; i < state.jokers.size(); i++) {
                if (state.jokers.get(i).selected) {
                    state.jokers.remove(i);
                    return;
                }
            }
            if (!state.jokers.isEmpty()) {
                state.jokers.remove(state.jokers.size() - 1);
            }
        }

        /**
         * Debug action: appends a new face-up consumable card (value 0) to the consumable list
         * and clears all current selections.
         */
        private void addDebugConsu() {
            deselectAll();
            state.consumables.add(new Card(0, true));
        }

        /**
         * Debug action: removes a consumable from the list.
         * If a consumable is currently selected, it is removed; otherwise the last one is removed.
         * Does nothing if the consumable list is empty.
         */
        private void removeDebugConsu() {
            for (int i = 0; i < state.consumables.size(); i++) {
                if (state.consumables.get(i).selected) {
                    state.consumables.remove(i);
                    return;
                }
            }
            if (!state.consumables.isEmpty()) {
                state.consumables.remove(state.consumables.size() - 1);
            }
        }

        /**
         * Returns the next card value to use when adding a debug card,
         * cycling through a fixed pool {@code {-2, 0, 3, 7, 12}} based on
         * how many cards are currently on the grid.
         *
         * @return the next debug card value
         */
        private int nextDebugCardValue() {
            int[] pool = {-2, 0, 3, 7, 12};
            int count = 0;
            for (int c = 0; c < state.grid.cols; c++) {
                for (int r = 0; r < state.grid.rows; r++) {
                    if (state.grid.get(c, r) != null) {
                        count++;
                    }
                }
            }
            return pool[count % pool.length];
        }

        private void debugScore() {
            
        }

        // Render ----------------------------------------------------------------------------

        /**
         * Main render entry point. Clears the canvas and redraws all UI layers in order:
         * background, draw/discard panel, card grid, score panel, bottom zones,
         * window chrome, and the settings overlay.
         * Does nothing if the canvas has no size yet.
         */
        public void render() {
            double w = getWidth();
            double h = getHeight();
            if (w <= 0 || h <= 0) return;

            GraphicsContext gc = getGraphicsContext2D();
            gc.clearRect(0, 0, w, h);
            gc.setGlobalAlpha(1.0);

            drawBackground(gc, w, h);
            drawDrawDiscardPanel(gc);
            drawGrid(gc);
            drawScorePanel(gc);
            drawBottomZones(gc);
            drawDraggedCard(gc);
            drawSettingsOverlay(gc);
        }

        /**
         * Draws the background layer: fills the canvas with the base colour {@code BG},
         * then tiles the scrolling background pattern image (if loaded) at low opacity
         * to create an animated card-pattern wallpaper effect.
         *
         * @param gc the {@link GraphicsContext} to draw onto
         * @param w  current canvas width
         * @param h  current canvas height
         */
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


        private void drawGrid(GraphicsContext gc) {
            GridMetrics m = gridMetrics();
            double ox = m.originX();
            double oy = m.originY();
            double pad = 16.0;
            double panelW = m.totalW() + pad * 2.0;
            double panelH = m.totalH() + pad * 2.0;

            boolean dropIntoGridMode = isDraggingDrawCard() || isDraggingDiscardCard();

            gc.setFill(GRID_BG);
            gc.fillRoundRect(ox - pad, oy - pad, panelW, panelH, 14, 14);

            if (dropIntoGridMode) {
                gc.setFill(Color.color(ACCENT.getRed(), ACCENT.getGreen(), ACCENT.getBlue(), 0.10));
                gc.fillRoundRect(ox - pad - 4, oy - pad - 4, panelW + 8, panelH + 8, 16, 16);
            }

            gc.setStroke(dropIntoGridMode ? ACCENT : ZONE_BORDER);
            gc.setLineWidth(dropIntoGridMode ? 2.0 : 1.2);
            gc.strokeRoundRect(ox - pad, oy - pad, panelW, panelH, 14, 14);

            for (int c = 0; c < state.grid.cols; c++) {
                for (int r = 0; r < state.grid.rows; r++) {
                    boolean hovered = (c == hoverGridCol && r == hoverGridRow)
                        || (c == hoverDropGridCol && r == hoverDropGridRow);
                    drawCardSlot(
                        gc,
                        ox + c * (m.cardW() + m.gap()),
                        oy + r * (m.cardH() + m.gap()),
                        m.cardW(),
                        m.cardH(),
                        state.grid.get(c, r),
                        hovered
                    );
                }
            }

            if (dropIntoGridMode) {
                Font hintFont = Font.font(fontBold.getFamily(), FontWeight.BOLD, 26);
                gc.setFont(hintFont);
                gc.setFill(Color.color(1, 1, 1, 0.14));
                drawCenteredText(gc, "PLACE CARD", ox - pad, oy + panelH / 2.0 + 10.0, panelW, hintFont);
            }
        }

        private void drawDrawDiscardPanel(GraphicsContext gc) {
            double x = drawPanelX();
            double y = drawPanelY();
            double w = drawPanelW();
            double h = drawPanelH();

            double innerPad = 12.0;
            double zoneGap = 12.0;
            double zoneW = w - innerPad * 2.0;
            double zoneH = (h - innerPad * 2.0 - zoneGap - 36.0) / 2.0;

            double drawZoneX = x + innerPad;
            double drawZoneY = y + 48.0;
            double discardZoneX = drawZoneX;
            double discardZoneY = drawZoneY + zoneH + zoneGap;

            gc.setFill(Color.rgb(13, 20, 43, 0.88));
            gc.fillRoundRect(x, y, w, h, 18, 18);
            gc.setStroke(Color.web("#4f62a7"));
            gc.setLineWidth(1.2);
            gc.strokeRoundRect(x, y, w, h, 18, 18);

            Font titleFont = Font.font(fontBold.getFamily(), FontWeight.BOLD, 16);
            Font labelFont = Font.font(fontBase.getFamily(), FontWeight.NORMAL, 10);
            Font bigHintFont = Font.font(fontBold.getFamily(), FontWeight.BOLD, 24);

            gc.setFont(titleFont);
            gc.setFill(Color.WHITE);
            drawCenteredText(gc, "DRAW / DISCARD", x, y + 28, w, titleFont);

            gc.setStroke(Color.web("#31457f"));
            gc.setLineWidth(1.0);
            gc.strokeLine(x + 16, y + 40, x + w - 16, y + 40);

            boolean drawActive = isDraggingDrawCard() || isDraggingDiscardCard() || hoverDropGridCol >= 0;
            boolean discardActive = isDraggingGridCard() || hoverDiscardDropZone;

            gc.setFill(Color.rgb(20, 30, 60, drawActive ? 0.95 : 0.72));
            gc.fillRoundRect(drawZoneX, drawZoneY, zoneW, zoneH, 14, 14);
            gc.setStroke(drawActive ? ACCENT : Color.web("#38518b"));
            gc.setLineWidth(drawActive ? 2.0 : 1.0);
            gc.strokeRoundRect(drawZoneX, drawZoneY, zoneW, zoneH, 14, 14);

            gc.setFill(Color.rgb(20, 30, 60, discardActive ? 0.95 : 0.72));
            gc.fillRoundRect(discardZoneX, discardZoneY, zoneW, zoneH, 14, 14);
            gc.setStroke(discardActive ? ACCENT : Color.web("#38518b"));
            gc.setLineWidth(discardActive ? 2.0 : 1.0);
            gc.strokeRoundRect(discardZoneX, discardZoneY, zoneW, zoneH, 14, 14);

            double drawCardAreaX = drawZoneX + (zoneW - SPECIAL_CARD_W) / 2.0;
            double drawCardAreaY = drawZoneY + (zoneH - SPECIAL_CARD_H) / 2.0 + 6.0;
            double discardCardAreaX = discardZoneX + (zoneW - SPECIAL_CARD_W) / 2.0;
            double discardCardAreaY = discardZoneY + (zoneH - SPECIAL_CARD_H) / 2.0 + 6.0;

            if (discardActive) {
                gc.setFont(bigHintFont);
                gc.setFill(Color.color(1, 1, 1, hoverDiscardDropZone ? 0.55 : 0.20));
                drawCenteredText(gc, "DISCARD", discardZoneX, discardZoneY + 28, zoneW, bigHintFont);
            }

            if (drawActive) {
                gc.setFont(bigHintFont);
                gc.setFill(Color.color(1, 1, 1, 0.16));
                drawCenteredText(gc, "PLACE", drawZoneX, drawZoneY + 28, zoneW, bigHintFont);
            }

            drawSideCard(gc, drawCardAreaX, drawCardAreaY, SPECIAL_CARD_W, SPECIAL_CARD_H, state.drawPile, hoverDraw || isDraggingDrawCard(), "DRAW");
            drawSideCard(gc, discardCardAreaX, discardCardAreaY, SPECIAL_CARD_W, SPECIAL_CARD_H, state.discardPile, hoverDiscard || hoverDiscardDropZone || isDraggingDiscardCard(), "DISCARD");

            gc.setFont(labelFont);
            gc.setFill(TEXT_DIM);
            drawCenteredText(gc, "DRAW", drawZoneX, drawZoneY + zoneH - 8, zoneW, labelFont);
            drawCenteredText(gc, "DISCARD", discardZoneX, discardZoneY + zoneH - 8, zoneW, labelFont);
        }

        /**
         * Draws the score panel on the right side of the play area.
         * Displays the score to beat, the current chip and multiplier values,
         * the hand name, and the hand level label.
         *
         * @param gc the {@link GraphicsContext} to draw onto
         */
        private void drawScorePanel(GraphicsContext gc) {
            double x = scorePanelX();
            double y = playAreaY();
            double yPadding = y + 20;
            double w = SCORE_PANEL_W;
            double h = playAreaH();

            gc.setFill(Color.web("#0d142b", 0.88));
            gc.fillRoundRect(x, y, w, h, 18, 18);
            gc.setStroke(Color.web("#4f62a7"));
            gc.setLineWidth(1.2);
            gc.strokeRoundRect(x, y, w, h, 18, 18);

            Font titleFont = Font.font(fontBold.getFamily(), FontWeight.BOLD, 18);
            Font handFont = Font.font(fontBold.getFamily(), FontWeight.BOLD, 28);
            Font labelFont = Font.font(fontBase.getFamily(), FontWeight.NORMAL, 12);
            Font valueFont = Font.font(fontBold.getFamily(), FontWeight.BOLD, 32);

            gc.setFill(Color.web("#222b47", 0.90));
            gc.fillRoundRect(x + 18, yPadding, 215, 95, 18, 18);
            gc.setStroke(Color.web("#4f62a7"));
            gc.setLineWidth(1);
            gc.strokeRoundRect(x + 18, yPadding, 215, 95, 18, 18);

            gc.setFont(titleFont);
            gc.setFill(TEXT_DIM);
            gc.fillText("SCORE TO BEAT", x + 28, yPadding + 28);

            gc.setFont(valueFont);
            gc.setFill(ACCENT);
            drawCenteredText(gc, String.valueOf(state.scoreToBeat), x, yPadding + 77, SCORE_PANEL_W, valueFont);

            gc.setFill(Color.web("#434855", 0.3));
            gc.fillRoundRect(x + 18, (yPadding + 92) + 20, 215, 50, 18, 18);
            gc.setStroke(Color.web("#4f62a7"));
            gc.setLineWidth(1);
            gc.strokeRoundRect(x + 18, yPadding + 112, 215, 50, 18, 18);

            gc.setFont(valueFont);
            gc.setFill(Color.web("#222b47", 0.8));
            drawCenteredText(gc, "S C O R E", x, yPadding + 112 + 38, SCORE_PANEL_W, Font.font(fontBold.getFamily(), FontWeight.BOLD, 36));

            gc.setFont(valueFont);
            gc.setFill(Color.WHITE);
            drawCenteredText(gc, String.valueOf(state.playerScore), x, yPadding + 112 + 38, SCORE_PANEL_W, valueFont);

            gc.setStroke(Color.web("#31457f"));
            gc.setLineWidth(2.0);
            gc.strokeLine(x + 18, ((yPadding + 92) + 40) + 50, x + w - 18, ((yPadding + 92) + 40) + 50);

            

        }

        private void drawBottomZones(GraphicsContext gc) {
            double zoneY = bottomZoneY();
            double zoneH = bottomZoneH();
            double bottomPadding = 18.0;
            double topPadding = 18.0;
            double cardY = zoneY + topPadding + 22.0;

            gc.setStroke(ZONE_BORDER);
            gc.setLineWidth(1.0);

            drawBottomZoneShell(gc, jokerZoneX(), zoneY, jokerZoneW(), zoneH, Color.web("#0d0d1c"), Color.web("#4a1070"), "JOKERS", JOKER_LABEL);
            drawBottomZoneShell(gc, consumableZoneX(), zoneY, consumableZoneW(), zoneH, Color.web("#0a1a12"), Color.web("#1a7044"), "CONSUMABLES", CONSU_LABEL);

            if (state.jokers.isEmpty()) {
                drawZoneHint(gc, jokerZoneX(), zoneY, jokerZoneW(), zoneH - bottomPadding, "No jokers", JOKER_LABEL);
            } else {
                double totalW = state.jokers.size() * (SPECIAL_CARD_W + CARD_GAP) - CARD_GAP;
                double startX = jokerZoneX() + (jokerZoneW() - totalW) / 2.0;
                for (int i = 0; i < state.jokers.size(); i++) {
                    boolean hovered = (i == hoverJokerIndex);
                    drawSpecialCard(gc, startX + i * (SPECIAL_CARD_W + CARD_GAP), cardY,
                        SPECIAL_CARD_W, SPECIAL_CARD_H, state.jokers.get(i),
                        JOKER_BG, JOKER_BORD, JOKER_LABEL, "J", hovered);
                }
            }

            if (state.consumables.isEmpty()) {
                drawZoneHint(gc, consumableZoneX(), zoneY, consumableZoneW(), zoneH - bottomPadding, "No consumables", CONSU_LABEL);
            } else {
                double totalW = state.consumables.size() * (SPECIAL_CARD_W + CARD_GAP) - CARD_GAP;
                double startX = consumableZoneX() + (consumableZoneW() - totalW) / 2.0;
                for (int i = 0; i < state.consumables.size(); i++) {
                    boolean hovered = (i == hoverConsumableIndex);
                    drawSpecialCard(gc, startX + i * (SPECIAL_CARD_W + CARD_GAP), cardY,
                        SPECIAL_CARD_W, SPECIAL_CARD_H, state.consumables.get(i),
                        CONSU_BG, CONSU_BORD, CONSU_LABEL, "C", hovered);
                }
            }
        }

        /**
         * Draws the background shell (rounded rectangle + border + title label)
         * for a bottom zone panel such as the joker or consumable area.
         *
         * @param gc         the {@link GraphicsContext} to draw onto
         * @param x          left X of the shell
         * @param y          top Y of the shell
         * @param w          width of the shell
         * @param h          height of the shell
         * @param bg         background fill colour
         * @param border     border stroke colour
         * @param title      label text drawn in the top-left corner of the shell
         * @param titleColor colour used for the title label
         */
        private void drawBottomZoneShell(GraphicsContext gc, double x, double y, double w, double h,
                                         Color bg, Color border, String title, Color titleColor) {
            gc.setFill(bg);
            gc.fillRoundRect(x, y, w, h, 12, 12);
            gc.setStroke(border);
            gc.setLineWidth(1.0);
            gc.strokeRoundRect(x, y, w, h, 12, 12);

            Font titleFont = Font.font(fontBase.getFamily(), FontWeight.NORMAL, 10);
            gc.setFont(titleFont);
            gc.setFill(titleColor);
            gc.fillText(title, x + 10, y + 16);
        }

        private void drawDraggedCard(GraphicsContext gc) {
            if (draggedCard == null) {
                return;
            }

            double cardW = isDraggingDrawCard() ? SPECIAL_CARD_W : gridMetrics().cardW();
            double cardH = isDraggingDrawCard() ? SPECIAL_CARD_H : gridMetrics().cardH();

            double x = dragMouseX - dragOffsetX;
            double y = dragMouseY - dragOffsetY;

            gc.save();
            gc.setGlobalAlpha(0.95);
            drawCardShape(gc, x, y, cardW, cardH, draggedCard, true);
            gc.restore();
        }

        /**
         * Draws the settings overlay layer: always renders the settings (hamburger) button,
         * and additionally renders the settings panel drop-down when it is open.
         *
         * @param gc the {@link GraphicsContext} to draw onto
         */
        private void drawSettingsOverlay(GraphicsContext gc) {
            drawSettingsButton(gc);
            if (settingsPanelOpen) {
                drawSettingsPanel(gc);
            }
        }

        /**
         * Draws the hamburger-style settings toggle button in the top-left corner.
         * The button background and line colours respond to hover and open/closed state.
         *
         * @param gc the {@link GraphicsContext} to draw onto
         */
        private void drawSettingsButton(GraphicsContext gc) {
            double x = SETTINGS_BUTTON_X;
            double y = SETTINGS_BUTTON_Y;
            double s = SETTINGS_BUTTON_SIZE;

            Color bgColor = settingsPanelOpen ? ACCENT
                : hoverSettingsButton ? Color.web("#2a2a55")
                : Color.web("#1a1a3a");
            gc.setFill(bgColor);
            gc.fillRoundRect(x, y, s, s, 6, 6);
            gc.setStroke(settingsPanelOpen || hoverSettingsButton ? ACCENT : ZONE_BORDER);
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

        /**
         * Draws the settings panel drop-down, including a drop shadow, dark background,
         * accent border, title, separator, and either the debug buttons or a
         * "Debug mode disabled" message, followed by the "Leave Game" button.
         *
         * @param gc the {@link GraphicsContext} to draw onto
         */
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
            } else {
                Font infoFont = Font.font(fontBase.getFamily(), FontWeight.NORMAL, 10);
                gc.setFont(infoFont);
                gc.setFill(TEXT_DIM);
                gc.fillText("Debug mode disabled", px + LEAVE_PAD, py + 48);
            }

            drawLeaveButton(gc);
        }

        /**
         * Draws all debug action buttons inside the settings panel.
         * Each button is highlighted when hovered and labelled from {@link #DEBUG_BUTTON_LABELS}.
         *
         * @param gc the {@link GraphicsContext} to draw onto
         */
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
                gc.setStroke(hover ? ACCENT : ZONE_BORDER);
                gc.setLineWidth(1.2);
                gc.strokeRoundRect(x, y, w, h, 6, 6);

                gc.setFill(hover ? ACCENT : Color.WHITE);
                String label = DEBUG_BUTTON_LABELS[i];
                double tw = measureText(label, bf);
                gc.fillText(label, x + (w - tw) / 2.0, y + h / 2.0 + 3.5);
            }
        }

        /**
         * Draws the "← LEAVE GAME" button at the bottom of the settings panel.
         * The button background and text colour respond to hover state.
         *
         * @param gc the {@link GraphicsContext} to draw onto
         */
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
            String label = "<- LEAVE GAME";
            double tw = measureText(label, bf);
            gc.fillText(label, x + (w - tw) / 2.0, y + h / 2.0 + 4.0);
        }


        // Card drawing ----------------------------------------------------------------------------

        /**
         * Returns a tinted copy of the card overlay image coloured with the given tint,
         * using a cache keyed on the RGBA components to avoid redundant pixel processing.
         * Transparent pixels in the source image are preserved as transparent in the output.
         *
         * @param tint the {@link Color} to apply as the overlay tint
         * @return the tinted {@link Image}, or {@code null} if the overlay image is unavailable
         */
        private Image getTintedOverlay(Color tint) {
            if (cardOverlayImg == null) return null;

            String key = String.format("%.4f-%.4f-%.4f-%.4f",
                tint.getRed(), tint.getGreen(), tint.getBlue(), tint.getOpacity());

            Image cached = tintedOverlayCache.get(key);
            if (cached != null) return cached;

            int width = (int) Math.round(cardOverlayImg.getWidth());
            int height = (int) Math.round(cardOverlayImg.getHeight());
            if (width <= 0 || height <= 0) return cardOverlayImg;

            WritableImage tinted = new WritableImage(width, height);
            PixelReader pr = cardOverlayImg.getPixelReader();
            PixelWriter pw = tinted.getPixelWriter();
            if (pr == null) return cardOverlayImg;

            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    Color src = pr.getColor(x, y);
                    double alpha = src.getOpacity();
                    if (alpha <= 0.001) {
                        pw.setColor(x, y, Color.TRANSPARENT);
                        continue;
                    }
                    Color out = new Color(tint.getRed(), tint.getGreen(), tint.getBlue(), alpha);
                    pw.setColor(x, y, out);
                }
            }

            tintedOverlayCache.put(key, tinted);
            return tinted;
        }

        /**
         * Draws the tinted overlay image on top of a face-up card, clipped to the card's
         * rounded-rectangle bounds. Does nothing if the overlay image is missing,
         * the card is {@code null}, or the card is face-down.
         *
         * @param gc   the {@link GraphicsContext} to draw onto
         * @param x    left X of the card
         * @param y    top Y of the card
         * @param w    card width
         * @param h    card height
         * @param card the card whose value drives the overlay tint colour
         */
        private void drawCardOverlay(GraphicsContext gc, double x, double y, double w, double h, Card card) {
            if (cardOverlayImg == null || card == null || !card.faceUp) return;

            Image tintedOverlay = getTintedOverlay(cardOverlayTint(card.value));
            if (tintedOverlay == null) return;

            gc.save();
            clipRoundedRect(gc, x, y, w, h, CARD_RADIUS);
            gc.drawImage(tintedOverlay, x, y, w, h);
            gc.restore();
        }

        /**
         * Draws the visual shape of a single card at the given position and size.
         * Face-down cards show the card-back image (or a fallback pattern) with a coloured border.
         * Face-up cards show a light front, the tinted overlay, the centred value in the
         * appropriate colour, and small corner labels.
         *
         * @param gc          the {@link GraphicsContext} to draw onto
         * @param x           left X of the card
         * @param y           top Y of the card
         * @param w           card width
         * @param h           card height
         * @param card        the {@link Card} to render
         * @param highlighted {@code true} to draw the accent-coloured border (hover or selected)
         */
        private void drawCardShape(GraphicsContext gc, double x, double y, double w, double h,
                                   Card card, boolean highlighted) {
            if (!card.faceUp) {
                if (cardBackImg != null) {
                    gc.save();
                    clipRoundedRect(gc, x, y, w, h, CARD_RADIUS);
                    gc.drawImage(cardBackImg, x, y, w, h);
                    gc.restore();
                } else {
                    gc.setFill(CARD_BACK);
                    gc.fillRoundRect(x, y, w, h, CARD_RADIUS, CARD_RADIUS);
                    gc.setFill(Color.web("#152c5e"));
                    gc.fillRoundRect(x + 5, y + 5, w - 10, h - 10, 5, 5);
                }

                gc.setStroke(highlighted ? ACCENT : Color.web("#3355aa"));
                gc.setLineWidth(highlighted ? 2.5 : 1.5);
                gc.strokeRoundRect(x, y, w, h, CARD_RADIUS, CARD_RADIUS);
                return;
            }

            gc.setFill(CARD_FRONT);
            gc.fillRoundRect(x, y, w, h, CARD_RADIUS, CARD_RADIUS);
            drawCardOverlay(gc, x, y, w, h, card);

            gc.setStroke(highlighted ? ACCENT : Color.web("#cccccc"));
            gc.setLineWidth(highlighted ? 2.5 : 1.2);
            gc.strokeRoundRect(x, y, w, h, CARD_RADIUS, CARD_RADIUS);

            String label = String.valueOf(card.value);
            Color valueColor = cardValueColor(card.value);
            Font valueFont = Font.font(fontBold.getFamily(), FontWeight.BOLD, 32);
            gc.setFont(valueFont);

            AnimBox.CardFlipAnimation flipAnimation = activeFlipAnimations.get(card);
            double textScaleX = 1.0;
            double textAlpha = 1.0;

            if (flipAnimation != null) {
                textScaleX = Math.max(0.05, flipAnimation.getScaleX());
                textAlpha = Math.max(0.0, Math.min(1.0, flipAnimation.getScaleX() * 1.4));
            }

            double tw = measureText(label, valueFont);
            double scaledTextW = tw * textScaleX;
            double textX = x + (w - scaledTextW) / 2.0;
            double textY = y + h / 2.0 + 11.0;

            gc.save();
            gc.setGlobalAlpha(textAlpha);
            gc.translate(x + w / 2.0, 0);
            gc.scale(textScaleX, 1.0);
            gc.translate(-(x + w / 2.0), 0);
            gc.setFill(valueColor);
            gc.fillText(label, textX, textY);
            gc.restore();
        }

        private void drawCardSlot(GraphicsContext gc, double x, double y,
                                  double w, double h, Card card, boolean hovered) {
            if (card == null) {
                double scale = hovered ? 1.06 : 1.0;
                double nw = w * scale;
                double nh = h * scale;
                double nx = x + (w - nw) / 2.0;
                double ny = y + (h - nh) / 2.0;

                gc.setFill(CARD_EMPTY);
                gc.fillRoundRect(nx, ny, nw, nh, CARD_RADIUS, CARD_RADIUS);
                gc.setStroke(hovered ? ACCENT : Color.web("#2a2a48"));
                gc.setLineWidth(hovered ? 1.8 : 1.0);
                gc.strokeRoundRect(nx, ny, nw, nh, CARD_RADIUS, CARD_RADIUS);
                return;
            }

            boolean effectiveHighlight = card.selected || hovered;
            double scale = card.selected ? (hovered ? 1.12 : 1.08) : (hovered ? 1.06 : 1.0);
            double nw = w * scale;
            double nh = h * scale;
            double shakeX = hovered && !card.selected ? Math.sin(animTime * 6.0) * 1.5 : 0.0;
            double shakeY = hovered && !card.selected ? Math.cos(animTime * 5.0) * 1.2 : 0.0;
            double nx = x + (w - nw) / 2.0 + shakeX;
            double ny = y + (h - nh) / 2.0 + shakeY;

            if (isDraggingGridCard() && card == draggedCard) {
                gc.setFill(CARD_EMPTY);
                gc.fillRoundRect(x, y, w, h, CARD_RADIUS, CARD_RADIUS);
                gc.setStroke(Color.web("#2a2a48"));
                gc.setLineWidth(1.0);
                gc.strokeRoundRect(x, y, w, h, CARD_RADIUS, CARD_RADIUS);
                return;
            }

            AnimBox.CardFlipAnimation flipAnimation = activeFlipAnimations.get(card);
            if (flipAnimation != null) {
                double flipScaleX = Math.max(0.06, flipAnimation.getScaleX());
                double flippedW = nw * flipScaleX;
                double flippedX = nx + (nw - flippedW) / 2.0;
                drawCardShape(gc, flippedX, ny, flippedW, nh, card, effectiveHighlight);
            } else {
                drawCardShape(gc, nx, ny, nw, nh, card, effectiveHighlight);
            }
        }

        /**
         * Draws a side card (draw pile or discard pile) within a given bounding box.
         * Applies a hover scale effect and reduces opacity slightly when not hovered.
         * If no card is provided, renders an empty placeholder with a centred tag label.
         *
         * @param gc      the {@link GraphicsContext} to draw onto
         * @param x       left X of the bounding box
         * @param y       top Y of the bounding box
         * @param w       bounding box width
         * @param h       bounding box height
         * @param topCard the top {@link Card} to display, or {@code null} for an empty pile
         * @param hover   {@code true} if the cursor is over this card
         * @param tag     fallback label shown when the pile is empty (e.g. {@code "DRAW"})
         */
        private void drawSideCard(GraphicsContext gc, double x, double y, double w, double h,
                                  Card topCard, boolean hover, String tag) {
            double scale = hover ? 1.06 : 1.0;
            double dw = w * scale;
            double dh = h * scale;
            double dx = x + (w - dw) / 2.0;
            double dy = y + (h - dh) / 2.0;

            gc.setGlobalAlpha(hover ? 1.0 : 0.90);
            if (topCard != null) {
                drawCardShape(gc, dx, dy, dw, dh, topCard, hover);
            } else {
                gc.setFill(CARD_EMPTY);
                gc.fillRoundRect(dx, dy, dw, dh, CARD_RADIUS, CARD_RADIUS);
                gc.setStroke(hover ? ACCENT : ZONE_BORDER);
                gc.setLineWidth(1.5);
                gc.strokeRoundRect(dx, dy, dw, dh, CARD_RADIUS, CARD_RADIUS);

                Font tf = Font.font(fontBold.getFamily(), FontWeight.BOLD, 9);
                gc.setFont(tf);
                gc.setFill(TEXT_DIM);
                drawCenteredText(gc, tag, dx, dy + dh / 2.0 + 5, dw, tf);
            }
            gc.setGlobalAlpha(1.0);
        }

        /**
         * Draws a special card (joker or consumable) with custom colours and a symbol label.
         * Applies hover scaling and an accent glow halo when hovered or selected.
         *
         * @param gc         the {@link GraphicsContext} to draw onto
         * @param x          left X of the card bounding box
         * @param y          top Y of the card bounding box
         * @param w          bounding box width
         * @param h          bounding box height
         * @param card       the {@link Card} to render (used for selected state)
         * @param bg         background fill colour of the card face
         * @param border     border stroke colour
         * @param labelColor colour of the centred symbol when not hovered/selected
         * @param symbol     the text symbol displayed on the card face (e.g. {@code "J"} or {@code "C"})
         * @param hovered    {@code true} if the cursor is currently over this card
         */
        private void drawSpecialCard(GraphicsContext gc, double x, double y, double w, double h,
                                     Card card, Color bg, Color border, Color labelColor,
                                     String symbol, boolean hovered) {
            boolean selected = card.selected;
            double scale = hovered ? 1.06 : 1.0;
            double nw = w * scale;
            double nh = h * scale;
            double nx = x + (w - nw) / 2.0;
            double ny = y + (h - nh) / 2.0;

            if (selected || hovered) {
                gc.setFill(Color.color(ACCENT.getRed(), ACCENT.getGreen(), ACCENT.getBlue(), hovered ? 0.16 : 0.20));
                gc.fillRoundRect(nx - 5, ny - 5, nw + 10, nh + 10, CARD_RADIUS + 3, CARD_RADIUS + 3);
            }

            gc.setFill(bg);
            gc.fillRoundRect(nx, ny, nw, nh, CARD_RADIUS, CARD_RADIUS);
            gc.setStroke((selected || hovered) ? ACCENT : border);
            gc.setLineWidth((selected || hovered) ? 2.5 : 1.5);
            gc.strokeRoundRect(nx, ny, nw, nh, CARD_RADIUS, CARD_RADIUS);

            Font sf = Font.font(fontBold.getFamily(), FontWeight.BOLD, 28);
            gc.setFont(sf);
            gc.setFill((selected || hovered) ? ACCENT : labelColor);
            double tw = measureText(symbol, sf);
            gc.fillText(symbol, nx + (nw - tw) / 2.0, ny + nh / 2.0 + 10.0);
        }

        /**
         * Applies a rounded-rectangle clipping path to the given {@link GraphicsContext}.
         * Subsequent draw calls will be clipped to this region until {@link GraphicsContext#restore()}
         * is called. Uses quadratic Bézier curves to form the rounded corners.
         *
         * @param gc     the {@link GraphicsContext} to clip
         * @param x      left X of the clip rectangle
         * @param y      top Y of the clip rectangle
         * @param w      width of the clip rectangle
         * @param h      height of the clip rectangle
         * @param radius corner radius in pixels
         */
        private void clipRoundedRect(GraphicsContext gc, double x, double y, double w, double h, double radius) {
            gc.beginPath();
            gc.moveTo(x + radius, y);
            gc.lineTo(x + w - radius, y);
            gc.quadraticCurveTo(x + w, y, x + w, y + radius);
            gc.lineTo(x + w, y + h - radius);
            gc.quadraticCurveTo(x + w, y + h, x + w - radius, y + h);
            gc.lineTo(x + radius, y + h);
            gc.quadraticCurveTo(x, y + h, x, y + h - radius);
            gc.lineTo(x, y + radius);
            gc.quadraticCurveTo(x, y, x + radius, y);
            gc.closePath();
            gc.clip();
        }

        // Toolbox ----------------------------------------------------------------------------

        /**
         * Draws a faint centred hint message inside a zone, typically used when
         * the zone is empty (e.g. "No jokers"). The text is rendered at low opacity
         * using the provided colour.
         *
         * @param gc    the {@link GraphicsContext} to draw onto
         * @param x     left X of the zone bounding box
         * @param y     top Y of the zone bounding box
         * @param w     width of the zone bounding box
         * @param h     height of the zone bounding box
         * @param msg   the hint message to display
         * @param color the base colour of the hint text (drawn at 35% opacity)
         */
        private void drawZoneHint(GraphicsContext gc, double x, double y, double w, double h,
                                  String msg, Color color) {
            Font f = Font.font(fontBase.getFamily(), FontWeight.NORMAL, 10);
            gc.setFont(f);
            gc.setFill(Color.color(color.getRed(), color.getGreen(), color.getBlue(), 0.35));
            double tw = measureText(msg, f);
            gc.fillText(msg, x + (w - tw) / 2.0, y + h / 2.0 + 5.0);
        }

        /**
         * Draws a string centred horizontally within the given width at the specified Y position.
         * The font must be set on the {@link GraphicsContext} fill colour before calling this method,
         * as it only sets the font and position.
         *
         * @param gc    the {@link GraphicsContext} to draw onto
         * @param text  the string to render
         * @param x     left X of the container used for centering
         * @param y     baseline Y of the text
         * @param width width of the container used for centering
         * @param font  the {@link Font} to apply before drawing
         */
        private void drawCenteredText(GraphicsContext gc, String text, double x, double y,
                                      double width, Font font) {
            double tw = measureText(text, font);
            gc.setFont(font);
            gc.fillText(text, x + (width - tw) / 2.0, y);
        }

        /**
         * Measures and returns the rendered pixel width of a string in the given font.
         * Uses a temporary off-screen {@link Text} node to obtain accurate layout bounds.
         *
         * @param s the string to measure
         * @param f the {@link Font} to use for measurement
         * @return the width of the string in pixels
         */
        private double measureText(String s, Font f) {
            Text t = new Text(s);
            t.setFont(f);
            return t.getBoundsInLocal().getWidth();
        }

    }
}