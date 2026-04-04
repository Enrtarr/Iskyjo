package com.neuilleprime.gui.game;

/**
 * Represents a single scored combo entry displayed in the score panel feed.
 *
 * A combo entry is created each time a row, column, diagonal, or
 * anti-diagonal sequence of matching-value cards is detected in the grid.
 * It drives both the text shown in the feed and the slide-in / fade animation.
 *
 * Animation lifecycle:
 *   Slide-in phase – {@code slideProgress} goes from {@code 0.0} to {@code 1.0}.
 *       The entry slides from the right edge of the score panel to its resting position
 *       while fading in.
 *   Hold phase – the entry is fully visible at position 0 (newest) and is
 *       progressively pushed down as newer entries arrive.
 *   Fade-out phase– once the entry has been pushed past
 *       {@link #MAX_VISIBLE_ENTRIES} slots it fades and shrinks towards the bottom
 *       of the panel until it disappears.
 */
public final class ComboEntry {

    /** Maximum number of combo entries rendered simultaneously in the feed. */
    public static final int MAX_VISIBLE_ENTRIES = 5;

    /**
     * Duration of the slide-in animation in animation ticks
     * (each tick is approximately 25 ms when the timer fires at ~40 fps).
     */
    public static final int SLIDE_IN_TICKS = 18;

    // Combo description ------------------------------------------------------------------------

    /** Human-readable combo type label, e.g. {@code "Row"}, {@code "Column"},
     *  {@code "Diagonal"}, or {@code "Anti-diagonal"}. */
    public final String comboType;

    /** Number of cards involved in this combo (the streak length). */
    public final int count;

    /** Grid column of the first card in the combo sequence. */
    public final int startCol;

    /** Grid row of the first card in the combo sequence. */
    public final int startRow;

    /** Grid column of the last card in the combo sequence. */
    public final int endCol;

    /** Grid row of the last card in the combo sequence. */
    public final int endRow;

    // Animation state ------------------------------------------------------------------------

    /**
     * Slide-in progress in {@code [0.0, 1.0]}.  When {@code < 1.0} the entry
     * is still animating into view; at {@code 1.0} the entry is fully on screen.
     */
    public double slideProgress;

    /** Remaining slide-in ticks before the animation is considered complete. */
    public int slideTicksLeft;

    /**
     * Logical slot index occupied by this entry in the feed (0 = newest / top).
     * Updated every time a newer entry is pushed onto the feed.
     */
    public int slotIndex;

    // Constructor ------------------------------------------------------------------------

    /**
     * Creates a new combo entry that begins its slide-in animation immediately.
     *
     * @param comboType human-readable type label ({@code "Row"}, {@code "Column"},
     *                  {@code "Diagonal"}, {@code "Anti-diagonal"})
     * @param count     number of cards in the combo
     * @param startCol  grid column of the first combo card
     * @param startRow  grid row of the first combo card
     * @param endCol    grid column of the last combo card
     * @param endRow    grid row of the last combo card
     */
    public ComboEntry(String comboType, int count,
                      int startCol, int startRow,
                      int endCol, int endRow) {
        this.comboType    = comboType;
        this.count        = count;
        this.startCol     = startCol;
        this.startRow     = startRow;
        this.endCol       = endCol;
        this.endRow       = endRow;

        this.slideProgress   = 0.0;
        this.slideTicksLeft  = SLIDE_IN_TICKS;
        this.slotIndex       = 0;
    }

    // Derived display helpers ------------------------------------------------------------------------

    /**
     * Returns the display label for this combo as it appears in the score feed.
     * Format: {@code "Streak of <count> – <comboType>"}
     *
     * @return formatted combo label
     */
    public String displayLabel() {
        return "Streak of " + count + " \u2013 " + comboType;
    }

    /**
     * Returns {@code true} once the slide-in animation is complete.
     *
     * @return {@code true} if {@code slideProgress >= 1.0}
     */
    public boolean isFullyVisible() {
        return slideProgress >= 1.0;
    }

    /**
     * Returns the visual opacity this entry should be rendered at, based on
     * its current slot index and slide-in progress.
     *
     *   Slot 0 (newest) → full opacity, fades in during slide-in.
     *   Slots 1–{@value MAX_VISIBLE_ENTRIES} → linearly dimmed.
     *   Slots beyond {@value MAX_VISIBLE_ENTRIES} → fully transparent.
     *
     * @return opacity in {@code [0.0, 1.0]}
     */
    public double computeOpacity() {
        double baseOpacity;
        if (slotIndex == 0) {
            // Newest entry fades in
            baseOpacity = Math.min(1.0, slideProgress * 2.0);
        } else if (slotIndex < MAX_VISIBLE_ENTRIES) {
            // Older entries dim progressively
            baseOpacity = 1.0 - (double) slotIndex / MAX_VISIBLE_ENTRIES;
        } else {
            return 0.0;
        }
        return baseOpacity;
    }

    /**
     * Returns the font size multiplier for this entry based on its slot index.
     * The newest entry (slot 0) is rendered at full size; older ones shrink.
     *
     * @param baseSize the base font size in pixels
     * @return adjusted font size in pixels
     */
    public double computeFontSize(double baseSize) {
        if (slotIndex == 0) return baseSize;
        double shrink = 1.0 - (double) slotIndex / (MAX_VISIBLE_ENTRIES + 1.0) * 0.35;
        return Math.max(8.0, baseSize * shrink);
    }

    /**
     * Advances the slide-in animation by one tick.
     * Once {@code slideTicksLeft} reaches zero the progress is clamped to {@code 1.0}.
     */
    public void tick() {
        if (slideTicksLeft > 0) {
            slideTicksLeft--;
            slideProgress = 1.0 - (double) slideTicksLeft / SLIDE_IN_TICKS;
            if (slideTicksLeft == 0) slideProgress = 1.0;
        }
    }
}