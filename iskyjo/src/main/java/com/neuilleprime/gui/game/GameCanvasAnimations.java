package com.neuilleprime.gui.game;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

import com.neuilleprime.gui.model.Card;
import com.neuilleprime.gui.util.AnimBox;

import javafx.animation.AnimationTimer;
import javafx.scene.image.Image;

/**
 * Handles time-based behaviour for the in-game canvas.
 *
 * <p>This includes asset loading, the main animation timer, card flip
 * animations, the score-panel combo feed, and grid / card highlight effects.</p>
 *
 * <h2>Combo feed</h2>
 * <p>Call {@link #pushComboEntry(ComboEntry)} to add a scored combo to the
 * animated feed that appears in the score panel.  Each entry slides in from
 * the right while fading in, then is progressively pushed down and dimmed as
 * newer entries arrive.  The feed is rendered by
 * {@link GameCanvasRenderer#drawComboFeed(javafx.scene.canvas.GraphicsContext)}.</p>
 *
 * <h2>Card highlights</h2>
 * <p>Highlights make individual cells in the grid, or special cards in the
 * bottom bar, "pop" visually by scaling them up slightly.  Three entry points
 * are provided:</p>
 * <ul>
 *   <li>{@link #highlightGridRange(int, int, int, int)} – highlights a straight
 *       sequence of grid cells (row, column, diagonal, or anti-diagonal).</li>
 *   <li>{@link #highlightCard(Card)} – highlights a single {@link Card} wherever
 *       it lives (grid, jokers, or consumables).</li>
 *   <li>{@link #clearHighlights()} – removes all active highlights.</li>
 * </ul>
 */
public final class GameCanvasAnimations {

    // ── Constants ──────────────────────────────────────────────────────────────

    /** Duration of a card highlight pulse in animation ticks (~25 ms each). */
    private static final int HIGHLIGHT_DURATION_TICKS = 60;

    // ── State ──────────────────────────────────────────────────────────────────

    final GameCanvas owner;

    /**
     * Ordered list of active combo entries.  Index 0 is the newest entry.
     * Entries are removed once they are no longer visible.
     */
    final List<ComboEntry> comboFeed = new ArrayList<>();

    // ── Constructor ────────────────────────────────────────────────────────────

    GameCanvasAnimations(GameCanvas owner) {
        this.owner = owner;
    }

    // ── Asset loading ──────────────────────────────────────────────────────────

    /** Loads optional card and background images from the classpath. */
    void tryLoadImages() {
        try {
            var resource = GameCanvas.class.getResource("/Assets/Cards/card_back.png");
            if (resource != null) owner.cardBackImg = new Image(resource.toExternalForm());
        } catch (Exception ignored) {
        }

        try {
            var resource = GameCanvas.class.getResource("/Assets/Cards/card_overlay-2.png");
            if (resource != null) owner.cardOverlayImg = new Image(resource.toExternalForm());
        } catch (Exception ignored) {
        }

        try {
            var resource = GameCanvas.class.getResource("/Assets/Cards/card_bg.png");
            if (resource != null) owner.backgroundPattern = new Image(resource.toExternalForm());
        } catch (Exception ignored) {
        }
    }

    // ── Flip animations ────────────────────────────────────────────────────────

    /** Updates all active flip animations and applies their face switch when needed. */
    void updateFlipAnimations() {
        if (owner.activeFlipAnimations.isEmpty()) {
            return;
        }

        List<Card> finishedCards = new ArrayList<>();

        for (Map.Entry<Card, AnimBox.CardFlipAnimation> entry : owner.activeFlipAnimations.entrySet()) {
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
            owner.activeFlipAnimations.remove(card);
        }
    }

    /** Starts a flip animation for the supplied card. */
    void startFlip(Card card) {
        if (card == null) {
            return;
        }
        if (owner.activeFlipAnimations.containsKey(card)) {
            return;
        }
        owner.activeFlipAnimations.put(card, new AnimBox.CardFlipAnimation(18));
    }

    // ── Combo feed ─────────────────────────────────────────────────────────────

    /**
     * Pushes a new {@link ComboEntry} to the top of the score-panel combo feed.
     *
     * <p>Existing entries are bumped down by one slot.  Entries that fall
     * beyond {@link ComboEntry#MAX_VISIBLE_ENTRIES} slots are pruned immediately
     * to keep the list compact.</p>
     *
     * @param entry the combo to add; must not be {@code null}
     */
    public void pushComboEntry(ComboEntry entry) {
        // Bump all existing entries down one slot
        for (ComboEntry existing : comboFeed) {
            existing.slotIndex++;
        }

        // Insert new entry at position 0 (top / newest)
        comboFeed.add(0, entry);

        // Prune entries that are too far down to be visible
        comboFeed.removeIf(e -> e.slotIndex >= ComboEntry.MAX_VISIBLE_ENTRIES + 1);
    }

    /**
     * Advances the slide-in animation of all active combo entries by one tick.
     * Called once per frame by the animation timer.
     */
    private void updateComboAnimations() {
        for (ComboEntry entry : comboFeed) {
            entry.tick();
        }
    }

    // ── Highlight ──────────────────────────────────────────────────────────────

    /**
     * Highlights a contiguous sequence of grid cells defined by two corner
     * positions.  The method infers the combo orientation automatically:
     *
     * <ul>
     *   <li>Same row → <b>row</b> highlight</li>
     *   <li>Same column → <b>column</b> highlight</li>
     *   <li>Same {@code col - row} delta → <b>diagonal</b> (top-left to bottom-right)</li>
     *   <li>Same {@code col + row} delta → <b>anti-diagonal</b> (top-right to bottom-left)</li>
     * </ul>
     *
     * <p>Only cells that actually contain a {@link Card} are highlighted.
     * Invalid or out-of-range positions are silently ignored.</p>
     *
     * @param startCol grid column of the first cell (inclusive)
     * @param startRow grid row of the first cell (inclusive)
     * @param endCol   grid column of the last cell (inclusive)
     * @param endRow   grid row of the last cell (inclusive)
     */
    public void highlightGridRange(int startCol, int startRow, int endCol, int endRow) {
        // Collect all cells in the range
        List<int[]> cells = new ArrayList<>();

        if (startRow == endRow) {
            // Row
            int minC = Math.min(startCol, endCol);
            int maxC = Math.max(startCol, endCol);
            for (int c = minC; c <= maxC; c++) {
                cells.add(new int[]{c, startRow});
            }
        } else if (startCol == endCol) {
            // Column
            int minR = Math.min(startRow, endRow);
            int maxR = Math.max(startRow, endRow);
            for (int r = minR; r <= maxR; r++) {
                cells.add(new int[]{startCol, r});
            }
        } else {
            // Diagonal or anti-diagonal – walk step by step
            int dc = Integer.signum(endCol - startCol);
            int dr = Integer.signum(endRow - startRow);
            int c = startCol;
            int r = startRow;
            while (true) {
                cells.add(new int[]{c, r});
                if (c == endCol && r == endRow) break;
                c += dc;
                r += dr;
            }
        }

        // Apply highlight to matching cells
        for (int[] cell : cells) {
            int c = cell[0];
            int r = cell[1];
            if (c >= 0 && c < owner.state.grid.cols
                    && r >= 0 && r < owner.state.grid.rows) {
                owner.highlightedCells[c][r] = HIGHLIGHT_DURATION_TICKS;
            }
        }
    }

    /**
     * Highlights a single {@link Card} object anywhere on the canvas.
     *
     * <p>The method searches the grid first, then the joker list, then the
     * consumable list.  The first match is highlighted; if the card is not
     * found, the call is silently ignored.</p>
     *
     * @param card the card to highlight; must not be {@code null}
     */
    public void highlightCard(Card card) {
        if (card == null) return;

        // Search grid
        for (int c = 0; c < owner.state.grid.cols; c++) {
            for (int r = 0; r < owner.state.grid.rows; r++) {
                if (owner.state.grid.get(c, r) == card) {
                    owner.highlightedCells[c][r] = HIGHLIGHT_DURATION_TICKS;
                    return;
                }
            }
        }

        // Search jokers
        for (int i = 0; i < owner.state.jokers.size(); i++) {
            if (owner.state.jokers.get(i) == card) {
                owner.highlightedJokerIndex = i;
                owner.highlightedJokerTicks = HIGHLIGHT_DURATION_TICKS;
                return;
            }
        }

        // Search consumables
        for (int i = 0; i < owner.state.consumables.size(); i++) {
            if (owner.state.consumables.get(i) == card) {
                owner.highlightedConsumableIndex = i;
                owner.highlightedConsumableTicks = HIGHLIGHT_DURATION_TICKS;
                return;
            }
        }
    }

    /**
     * Removes all active highlights from grid cells, jokers, and consumables.
     */
    public void clearHighlights() {
        for (int c = 0; c < owner.highlightedCells.length; c++) {
            for (int r = 0; r < owner.highlightedCells[c].length; r++) {
                owner.highlightedCells[c][r] = 0;
            }
        }
        owner.highlightedJokerIndex = -1;
        owner.highlightedJokerTicks = 0;
        owner.highlightedConsumableIndex = -1;
        owner.highlightedConsumableTicks = 0;
    }

    /**
     * Decrements all non-zero highlight timers by one tick.
     * Called once per frame by the animation timer.
     */
    private void updateHighlightTimers() {
        for (int c = 0; c < owner.highlightedCells.length; c++) {
            for (int r = 0; r < owner.highlightedCells[c].length; r++) {
                if (owner.highlightedCells[c][r] > 0) {
                    owner.highlightedCells[c][r]--;
                }
            }
        }
        if (owner.highlightedJokerTicks > 0) {
            owner.highlightedJokerTicks--;
            if (owner.highlightedJokerTicks == 0) owner.highlightedJokerIndex = -1;
        }
        if (owner.highlightedConsumableTicks > 0) {
            owner.highlightedConsumableTicks--;
            if (owner.highlightedConsumableTicks == 0) owner.highlightedConsumableIndex = -1;
        }
    }

    // ── Debug helpers ──────────────────────────────────────────────────────────

    /**
     * Debug action: pushes a randomly generated combo entry into the score-panel
     * feed and triggers a matching grid highlight.
     *
     * <p>The orientation (row / column / diagonal / anti-diagonal) is chosen at
     * random, along with a random starting position that fits within the current
     * grid dimensions.  The streak length is randomised between 2 and the
     * maximum that fits in the chosen direction.</p>
     *
     * <p>This method is wired to the {@code DEBUG_SCORE} button and is the sole
     * entry point for feed testing.  Replace or supplement it with real game-logic
     * calls once combo detection is implemented.</p>
     */
    public void debugEmulateRandomCombo() {
        Random rng = new Random();
        int cols = owner.state.grid.cols;
        int rows = owner.state.grid.rows;

        // Pick a random orientation
        String[] types = {"Row", "Column", "Diagonal", "Anti-diagonal"};
        String type = types[rng.nextInt(types.length)];

        int startCol, startRow, endCol, endRow, count;

        switch (type) {
            case "Row" -> {
                startRow = rng.nextInt(rows);
                endRow   = startRow;
                int maxLen = cols;
                count    = 2 + rng.nextInt(Math.max(1, maxLen - 1));
                startCol = rng.nextInt(Math.max(1, cols - count + 1));
                endCol   = startCol + count - 1;
            }
            case "Column" -> {
                startCol = rng.nextInt(cols);
                endCol   = startCol;
                int maxLen = rows;
                count    = 2 + rng.nextInt(Math.max(1, maxLen - 1));
                startRow = rng.nextInt(Math.max(1, rows - count + 1));
                endRow   = startRow + count - 1;
            }
            case "Diagonal" -> {
                // top-left to bottom-right
                int maxLen = Math.min(cols, rows);
                count    = 2 + rng.nextInt(Math.max(1, maxLen - 1));
                startCol = rng.nextInt(Math.max(1, cols - count + 1));
                startRow = rng.nextInt(Math.max(1, rows - count + 1));
                endCol   = startCol + count - 1;
                endRow   = startRow + count - 1;
            }
            default -> { // Anti-diagonal (top-right to bottom-left)
                int maxLen = Math.min(cols, rows);
                count    = 2 + rng.nextInt(Math.max(1, maxLen - 1));
                endCol   = rng.nextInt(Math.max(1, cols - count + 1));
                startRow = rng.nextInt(Math.max(1, rows - count + 1));
                startCol = endCol + count - 1;
                endRow   = startRow + count - 1;
            }
        }

        // Clamp to grid bounds
        startCol = Math.max(0, Math.min(cols - 1, startCol));
        startRow = Math.max(0, Math.min(rows - 1, startRow));
        endCol   = Math.max(0, Math.min(cols - 1, endCol));
        endRow   = Math.max(0, Math.min(rows - 1, endRow));

        ComboEntry entry = new ComboEntry(type, count, startCol, startRow, endCol, endRow);
        pushComboEntry(entry);
        highlightGridRange(startCol, startRow, endCol, endRow);
    }

    /**
     * Debug action: highlights a random contiguous group of grid cells
     * in a randomly chosen orientation (row, column, diagonal, or
     * anti-diagonal), regardless of card values.
     *
     * <p>This is purely a visual test for the highlight system and is
     * independent of any game-logic scoring function.  Wired to the
     * {@code DEBUG_HIGHLIGHT} button.</p>
     */
    public void debugHighlightRandomGroup() {
        Random rng = new Random();
        int cols = owner.state.grid.cols;
        int rows = owner.state.grid.rows;

        String[] types = {"Row", "Column", "Diagonal", "Anti-diagonal"};
        String type = types[rng.nextInt(types.length)];

        int startCol, startRow, endCol, endRow, count;

        switch (type) {
            case "Row" -> {
                startRow = rng.nextInt(rows);
                endRow   = startRow;
                count    = 2 + rng.nextInt(Math.max(1, cols - 1));
                startCol = rng.nextInt(Math.max(1, cols - count + 1));
                endCol   = startCol + count - 1;
            }
            case "Column" -> {
                startCol = rng.nextInt(cols);
                endCol   = startCol;
                count    = 2 + rng.nextInt(Math.max(1, rows - 1));
                startRow = rng.nextInt(Math.max(1, rows - count + 1));
                endRow   = startRow + count - 1;
            }
            case "Diagonal" -> {
                int maxLen = Math.min(cols, rows);
                count    = 2 + rng.nextInt(Math.max(1, maxLen - 1));
                startCol = rng.nextInt(Math.max(1, cols - count + 1));
                startRow = rng.nextInt(Math.max(1, rows - count + 1));
                endCol   = startCol + count - 1;
                endRow   = startRow + count - 1;
            }
            default -> {
                int maxLen = Math.min(cols, rows);
                count    = 2 + rng.nextInt(Math.max(1, maxLen - 1));
                endCol   = rng.nextInt(Math.max(1, cols - count + 1));
                startRow = rng.nextInt(Math.max(1, rows - count + 1));
                startCol = endCol + count - 1;
                endRow   = startRow + count - 1;
            }
        }

        startCol = Math.max(0, Math.min(cols - 1, startCol));
        startRow = Math.max(0, Math.min(rows - 1, startRow));
        endCol   = Math.max(0, Math.min(cols - 1, endCol));
        endRow   = Math.max(0, Math.min(rows - 1, endRow));

        clearHighlights();
        highlightGridRange(startCol, startRow, endCol, endRow);
    }

    // ── Animation timer ────────────────────────────────────────────────────────

    /** Starts the canvas animation timer. */
    void startAnimation() {
        owner.animationTimer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                if (owner.lastFrameTime < 0) {
                    owner.lastFrameTime = now;
                    return;
                }

                long elapsed = now - owner.lastFrameTime;
                if (elapsed < 25_000_000L) {
                    return;
                }

                owner.lastFrameTime = now;
                owner.animTime += elapsed / 1_000_000_000.0;
                updateFlipAnimations();
                updateComboAnimations();
                updateHighlightTimers();
                owner.render();
            }
        };
        owner.animationTimer.start();
    }
}