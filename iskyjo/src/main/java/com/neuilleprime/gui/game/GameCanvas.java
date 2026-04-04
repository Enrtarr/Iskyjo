package com.neuilleprime.gui.game;

import java.util.HashMap;
import java.util.Map;

import com.neuilleprime.gui.model.Card;
import com.neuilleprime.gui.model.GameState;
import com.neuilleprime.gui.util.AnimBox;

import javafx.animation.AnimationTimer;
import javafx.scene.canvas.Canvas;
import javafx.scene.image.Image;
import javafx.scene.text.Font;
import javafx.stage.Stage;

/**
 * Thin orchestration canvas for the in-game screen.
 *
 * <p>This class delegates the heavy work to dedicated collaborators:
 * {@link GameCanvasLayout}, {@link GameCanvasRenderer},
 * {@link GameCanvasInteractions}, {@link GameCanvasAnimations}, and
 * {@link GameCanvasGameBridge}. The goal is to keep the canvas focused on
 * lifecycle, shared state, and integration wiring.</p>
 *
 * <h2>Highlight state</h2>
 * <p>Three shared fields track which cards are currently highlighted:</p>
 * <ul>
 *   <li>{@link #highlightedCells} – per-cell countdown timers for grid cards.</li>
 *   <li>{@link #highlightedJokerIndex} / {@link #highlightedJokerTicks} – for the bottom joker bar.</li>
 *   <li>{@link #highlightedConsumableIndex} / {@link #highlightedConsumableTicks} – for the consumable bar.</li>
 * </ul>
 * <p>All highlight entry points live in {@link GameCanvasAnimations}.</p>
 */
public class GameCanvas extends Canvas {
    static final double TOP_BAR_H = 64.0;
    static final double BOTTOM_BAR_H = 190.0;
    static final double PADDING = 22.0;
    static final double SECTION_GAP = 16.0;
    static final double SCORE_PANEL_W = 250.0;

    static final double CARD_W = 82.0;
    static final double CARD_H = 114.0;
    static final double CARD_GAP = 14.0;
    static final double CARD_RADIUS = 10.0;

    static final double SPECIAL_CARD_W = 70.0;
    static final double SPECIAL_CARD_H = 98.0;
    static final double DRAW_ZONE_W = 180.0;

    static final double SETTINGS_BUTTON_X = 30.0;
    static final double SETTINGS_BUTTON_SIZE = 28.0;
    static final double SETTINGS_BUTTON_Y = 30.0;

    static final double PANEL_X = SETTINGS_BUTTON_X + SETTINGS_BUTTON_SIZE + 10.0;
    static final double PANEL_Y = SETTINGS_BUTTON_Y - 2.0;
    static final double PANEL_W = 260.0;
    static final double PANEL_H = 370.0;   // increased to accommodate extra debug button
    static final double LEAVE_PAD = 10.0;
    static final double LEAVE_BUTTON_HEIGHT = 32.0;
    static final double DEBUG_BUTTON_HEIGHT = 26.0;
    static final double DEBUG_WINDOW_BUTTON_SPACING = 8.0;

    static final int BACKGROUND_CARD_W = 82;
    static final int BACKGROUND_CARD_H = 114;
    static final int BACKGROUND_CARD_GAP = 20;
    static final double BACKGROUND_CARD_OPACITY = 0.05;

    static final javafx.scene.paint.Color ACCENT = javafx.scene.paint.Color.web("#ffc800");
    static final javafx.scene.paint.Color BG = javafx.scene.paint.Color.web("#1d2b53");
    static final javafx.scene.paint.Color GRID_BG = javafx.scene.paint.Color.web("#1a1a34");
    static final javafx.scene.paint.Color ZONE_BORDER = javafx.scene.paint.Color.web("#394b86");
    static final javafx.scene.paint.Color TEXT_DIM = javafx.scene.paint.Color.web("#a0acd8");
    static final javafx.scene.paint.Color CARD_FRONT = javafx.scene.paint.Color.web("#f0ede8");
    static final javafx.scene.paint.Color CARD_BACK = javafx.scene.paint.Color.web("#1e3a7a");
    static final javafx.scene.paint.Color CARD_EMPTY = javafx.scene.paint.Color.web("#232338");

    static final javafx.scene.paint.Color JOKER_BG = javafx.scene.paint.Color.web("#3d1060");
    static final javafx.scene.paint.Color JOKER_BORD = javafx.scene.paint.Color.web("#8c3fc0");
    static final javafx.scene.paint.Color JOKER_LABEL = javafx.scene.paint.Color.web("#cc88ff");

    static final javafx.scene.paint.Color CONSU_BG = javafx.scene.paint.Color.web("#0d3d22");
    static final javafx.scene.paint.Color CONSU_BORD = javafx.scene.paint.Color.web("#2a9c55");
    static final javafx.scene.paint.Color CONSU_LABEL = javafx.scene.paint.Color.web("#66e899");

    // ── Debug action constants ─────────────────────────────────────────────────

    public static final int DEBUG_FLIP_SELECTED = 0;
    public static final int DEBUG_ADD_CARD      = 1;
    public static final int DEBUG_REMOVE_CARD   = 2;
    public static final int DEBUG_ADD_JOKER     = 3;
    public static final int DEBUG_REMOVE_JOKER  = 4;
    public static final int DEBUG_ADD_CONSU     = 5;
    public static final int DEBUG_REMOVE_CONSU  = 6;
    public static final int DEBUG_OPEN_SHOP     = 7;
    /**
     * Emulates a random Skyjo combo appearing in the score-panel feed
     * and highlights the corresponding grid cells.
     */
    public static final int DEBUG_SCORE         = 8;
    /**
     * Highlights a random group of grid cells (row / column / diagonal /
     * anti-diagonal) without adding a combo entry to the feed.
     * Purely a visual test for the highlight subsystem.
     */
    public static final int DEBUG_HIGHLIGHT     = 9;

    static final double DRAG_THRESHOLD = 8.0;

    static final String[] DEBUG_BUTTON_LABELS = {
        "FLIP SELECTED",
        "+ CARD",
        "- CARD",
        "+ JOKER",
        "- JOKER",
        "+ CONSU",
        "- CONSU",
        "OPEN SHOP",
        "EMULATE SCORE",
        "HIGHLIGHT GROUP"
    };

    // ── Mutable interaction state ──────────────────────────────────────────────

    Card debugTargetCard = null;
    boolean dragStarted = false;
    double pressMouseX = 0.0;
    double pressMouseY = 0.0;

    final Stage stage;
    final GameState state;
    final Font fontBase;
    final Font fontBold;
    final Runnable onLeave;
    final Runnable onOpenShop;
    final boolean debugMode;

    boolean hoverDraw = false;
    boolean hoverDiscard = false;
    boolean hoverSettingsButton = false;
    boolean hoverLeaveButton = false;
    int hoverDebugAction = -1;
    int hoverGridCol = -1;
    int hoverGridRow = -1;
    int hoverJokerIndex = -1;
    int hoverConsumableIndex = -1;

    Card draggedCard = null;
    Card dragOriginCard = null;

    boolean draggingFromGrid = false;
    boolean draggingFromDrawPile = false;
    boolean draggingFromDiscardPile = false;

    int dragSourceCol = -1;
    int dragSourceRow = -1;

    double dragMouseX = 0.0;
    double dragMouseY = 0.0;
    double dragOffsetX = 0.0;
    double dragOffsetY = 0.0;

    boolean hoverDiscardDropZone = false;
    int hoverDropGridCol = -1;
    int hoverDropGridRow = -1;

    boolean settingsPanelOpen = false;

    Image cardBackImg = null;
    Image cardOverlayImg = null;
    Image backgroundPattern = null;

    final Map<String, Image> tintedOverlayCache = new HashMap<>();
    final Map<Card, AnimBox.CardFlipAnimation> activeFlipAnimations = new HashMap<>();

    AnimationTimer animationTimer;
    long lastFrameTime = -1L;
    double animTime = 0.0;

    // ── Highlight state ────────────────────────────────────────────────────────

    /**
     * Per-cell countdown timers (in animation ticks) for grid-card highlights.
     * Dimensions are [cols][rows]; a positive value means the cell is highlighted.
     * Allocated lazily in the constructor to match the current grid size.
     */
    int[][] highlightedCells;

    /**
     * Zero-based index of the joker currently highlighted in the bottom bar,
     * or {@code -1} when no joker is highlighted.
     */
    int highlightedJokerIndex = -1;

    /** Remaining highlight ticks for the highlighted joker. */
    int highlightedJokerTicks = 0;

    /**
     * Zero-based index of the consumable currently highlighted in the bottom bar,
     * or {@code -1} when none is highlighted.
     */
    int highlightedConsumableIndex = -1;

    /** Remaining highlight ticks for the highlighted consumable. */
    int highlightedConsumableTicks = 0;

    // ── Collaborators ──────────────────────────────────────────────────────────

    final GameCanvasLayout layout;
    final GameCanvasRenderer renderer;
    final GameCanvasInteractions interactions;
    final GameCanvasAnimations animations;
    final GameCanvasGameBridge bridge;

    // ── Constructors ───────────────────────────────────────────────────────────

    public GameCanvas(Stage stage, GameState state,
                      Font fontBase, Font fontBold,
                      Runnable onLeave, boolean debugMode) {
        this(stage, state, fontBase, fontBold, onLeave, () -> {}, debugMode);
    }

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

        // Allocate highlight grid matching the current grid dimensions
        this.highlightedCells = new int[state.grid.cols][state.grid.rows];

        this.layout = new GameCanvasLayout(this);
        this.renderer = new GameCanvasRenderer(this);
        this.interactions = new GameCanvasInteractions(this);
        this.animations = new GameCanvasAnimations(this);
        this.bridge = new GameCanvasGameBridge(this);

        this.animations.tryLoadImages();
        this.interactions.setupInteractions();
        this.animations.startAnimation();

        widthProperty().addListener((o, a, b) -> render());
        heightProperty().addListener((o, a, b) -> render());
    }

    // ── Public API ─────────────────────────────────────────────────────────────

    /** Returns the mutable visual state currently displayed by the canvas. */
    public GameState getState() {
        return state;
    }

    /** Returns the game bridge used to connect the canvas to gameplay or networking code. */
    public GameCanvasGameBridge getBridge() {
        return bridge;
    }

    /** Binds a gameplay adapter to the canvas bridge. */
    public void bindGameAdapter(GameCanvasGameAdapter adapter) {
        bridge.bindAdapter(adapter);
    }

    /** Binds a network client to the canvas bridge for multiplayer requests. */
    public void bindNetworkClient(GameCanvasNetworkClient networkClient) {
        bridge.bindNetworkClient(networkClient);
    }

    /** Re-renders the whole in-game scene. */
    public void render() {
        renderer.render();
    }
}