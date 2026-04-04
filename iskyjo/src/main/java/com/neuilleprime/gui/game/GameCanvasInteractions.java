package com.neuilleprime.gui.game;

import com.neuilleprime.gui.game.GameCanvasLayout.GridMetrics;
import com.neuilleprime.gui.model.Card;
import com.neuilleprime.gui.util.AnimBox;

import javafx.scene.Cursor;

/**
 * Encapsulates user interaction for the in-game canvas.
 *
 * <p>The interaction helper owns hover detection, selection, drag and drop,
 * and UI-triggered debug actions. Gameplay side effects can be forwarded to the
 * {@link GameCanvasGameBridge} when needed.</p>
 */
public final class GameCanvasInteractions {
    final GameCanvas owner;

    GameCanvasInteractions(GameCanvas owner) {
        this.owner = owner;
    }

    boolean isDraggingDrawCard() {
        return owner.draggedCard != null && owner.draggingFromDrawPile;
    }

    boolean isDraggingDiscardCard() {
        return owner.draggedCard != null && owner.draggingFromDiscardPile;
    }

    boolean isDraggingAnyCard() {
        return owner.draggedCard != null;
    }

    void cancelDrag() {
        owner.draggedCard = null;
        owner.dragOriginCard = null;
        owner.draggingFromGrid = false;
        owner.draggingFromDrawPile = false;
        owner.draggingFromDiscardPile = false;
        owner.dragSourceCol = -1;
        owner.dragSourceRow = -1;
        owner.hoverDiscardDropZone = false;
        owner.hoverDropGridCol = -1;
        owner.hoverDropGridRow = -1;
    }

    void beginGridCardDrag(int col, int row, double mouseX, double mouseY) {
        Card card = owner.state.grid.get(col, row);
        if (card == null) return;

        owner.draggedCard = card;
        owner.dragOriginCard = card;
        owner.draggingFromGrid = true;
        owner.draggingFromDrawPile = false;
        owner.draggingFromDiscardPile = false;
        owner.dragSourceCol = col;
        owner.dragSourceRow = row;

        GridMetrics m = owner.layout.gridMetrics();
        double cardX = m.originX() + col * (m.cardW() + m.gap());
        double cardY = m.originY() + row * (m.cardH() + m.gap());

        owner.dragOffsetX = mouseX - cardX;
        owner.dragOffsetY = mouseY - cardY;
        owner.dragMouseX = mouseX;
        owner.dragMouseY = mouseY;
    }

    void beginDrawPileDrag(double mouseX, double mouseY) {
        if (owner.state.drawPile == null) return;

        owner.draggedCard = new Card(owner.state.drawPile.value, false);
        owner.dragOriginCard = owner.state.drawPile;
        owner.draggingFromGrid = false;
        owner.draggingFromDrawPile = true;
        owner.draggingFromDiscardPile = false;
        owner.dragSourceCol = -1;
        owner.dragSourceRow = -1;

        owner.dragOffsetX = mouseX - owner.layout.drawCardX();
        owner.dragOffsetY = mouseY - owner.layout.drawCardY();
        owner.dragMouseX = mouseX;
        owner.dragMouseY = mouseY;
    }

    void beginDiscardPileDrag(double mouseX, double mouseY) {
        if (owner.state.discardPile == null) return;

        owner.draggedCard = owner.state.discardPile;
        owner.dragOriginCard = owner.state.discardPile;
        owner.draggingFromGrid = false;
        owner.draggingFromDrawPile = false;
        owner.draggingFromDiscardPile = true;
        owner.dragSourceCol = -1;
        owner.dragSourceRow = -1;

        owner.dragOffsetX = mouseX - owner.layout.discardCardX();
        owner.dragOffsetY = mouseY - owner.layout.discardCardY();
        owner.dragMouseX = mouseX;
        owner.dragMouseY = mouseY;
    }

    void dropDraggedGridCardIntoDiscard() {
        if (!isDraggingGridCard()) return;
        if (owner.dragSourceCol < 0 || owner.dragSourceRow < 0) return;

        owner.state.grid.set(owner.dragSourceCol, owner.dragSourceRow, null);
        owner.draggedCard.selected = false;
        owner.state.discardPile = owner.draggedCard;
        cancelDrag();
        owner.bridge.dispatchUiEvent("grid_card_discarded", java.util.Map.of(
            "col", owner.dragSourceCol,
            "row", owner.dragSourceRow
        ));
    }

    void dropDraggedDrawCardIntoGrid(int col, int row) {
        if (!isDraggingDrawCard()) return;

        Card oldCard = owner.state.grid.get(col, row);
        if (oldCard != null) {
            oldCard.selected = false;
            owner.state.discardPile = oldCard;
        }

        owner.draggedCard.faceUp = false;
        owner.draggedCard.selected = false;
        owner.state.grid.set(col, row, owner.draggedCard);

        cancelDrag();
        owner.bridge.dispatchUiEvent("draw_card_placed", java.util.Map.of(
            "col", col,
            "row", row
        ));
    }

    void dropDraggedDiscardCardIntoGrid(int col, int row) {
        if (!isDraggingDiscardCard()) return;
        if (owner.state.discardPile == null) return;

        Card oldCard = owner.state.grid.get(col, row);
        if (oldCard != null) {
            oldCard.selected = false;
            owner.state.discardPile = oldCard;
        } else {
            owner.state.discardPile = null;
        }

        owner.draggedCard.selected = false;
        owner.state.grid.set(col, row, owner.draggedCard);

        if (oldCard == null) {
            owner.state.discardPile = null;
        }

        cancelDrag();
        owner.bridge.dispatchUiEvent("discard_card_placed", java.util.Map.of(
            "col", col,
            "row", row
        ));
    }

    boolean isDraggingGridCard() {
        return owner.draggedCard != null && owner.draggingFromGrid;
    }


    Card getSelectedGridCard() {
        for (int c = 0; c < owner.state.grid.cols; c++) {
            for (int r = 0; r < owner.state.grid.rows; r++) {
                Card card = owner.state.grid.get(c, r);
                if (card != null && card.selected) {
                    return card;
                }
            }
        }
        return null;
    }

    int[] getSelectedGridCardPosition() {
        for (int c = 0; c < owner.state.grid.cols; c++) {
            for (int r = 0; r < owner.state.grid.rows; r++) {
                Card card = owner.state.grid.get(c, r);
                if (card != null && card.selected) {
                    return new int[] {c, r};
                }
            }
        }
        return null;
    }

    boolean canDiscardSelectedCard() {
        return getSelectedGridCard() != null || isDraggingGridCard();
    }

    boolean canPlaceDrawCard() {
        return (owner.state.drawPile != null && owner.state.drawPile.selected) || isDraggingDrawCard();
    }


    void discardSelectedGridCard() {
        int[] pos = getSelectedGridCardPosition();
        if (pos == null) return;

        Card card = owner.state.grid.get(pos[0], pos[1]);
        if (card == null) return;

        owner.state.grid.set(pos[0], pos[1], null);
        card.selected = false;
        owner.state.discardPile = card;
        owner.bridge.dispatchUiEvent("selected_card_discarded", java.util.Map.of(
            "col", pos[0],
            "row", pos[1]
        ));
    }

    void placeDrawCardIntoGrid(int col, int row) {
        if (owner.state.drawPile == null) return;

        Card drawnCard = owner.state.drawPile;
        drawnCard.faceUp = false;
        drawnCard.selected = false;

        Card oldCard = owner.state.grid.get(col, row);
        if (oldCard != null) {
            oldCard.selected = false;
            owner.state.discardPile = oldCard;
        }

        owner.state.grid.set(col, row, drawnCard);
        owner.state.drawPile = null;
        owner.bridge.dispatchUiEvent("draw_card_inserted", java.util.Map.of(
            "col", col,
            "row", row
        ));
    }


    // Interaction ----------------------------------------------------------------------------

    /**
     * Registers all mouse and keyboard event handlers on this canvas.
     * Handles hover detection, window dragging, click dispatch to game actions,
     * settings panel toggle, and ESC-to-close for the settings panel.
     */
    void setupInteractions() {
        owner.setOnMouseMoved(e -> {
            double x = e.getX();
            double y = e.getY();

            boolean mouseOverDraw = owner.layout.isInsideDrawCard(x, y);
            boolean mouseOverDiscard = owner.layout.isInsideDiscardCard(x, y);
            boolean mouseOverSettings = owner.layout.isInsideSettingsButton(x, y);
            boolean mouseOverLeave = owner.layout.isInsideLeaveButton(x, y);
            int hoveredDebug = owner.layout.hitTestDebugButton(x, y);

            int[] hoveredGrid = owner.layout.getGridCellAt(x, y);
            int newHoverGridCol = hoveredGrid != null ? hoveredGrid[0] : -1;
            int newHoverGridRow = hoveredGrid != null ? hoveredGrid[1] : -1;
            int newHoverJokerIndex = owner.layout.getJokerIndexAt(x, y);
            int newHoverConsumableIndex = owner.layout.getConsumableIndexAt(x, y);

            boolean changed = mouseOverDraw != owner.hoverDraw
                || mouseOverDiscard != owner.hoverDiscard
                || mouseOverSettings != owner.hoverSettingsButton
                || mouseOverLeave != owner.hoverLeaveButton
                || hoveredDebug != owner.hoverDebugAction
                || newHoverGridCol != owner.hoverGridCol
                || newHoverGridRow != owner.hoverGridRow
                || newHoverJokerIndex != owner.hoverJokerIndex
                || newHoverConsumableIndex != owner.hoverConsumableIndex;

            if (changed) {
                owner.hoverDraw = mouseOverDraw;
                owner.hoverDiscard = mouseOverDiscard;
                owner.hoverSettingsButton = mouseOverSettings;
                owner.hoverLeaveButton = mouseOverLeave;
                owner.hoverDebugAction = hoveredDebug;
                owner.hoverGridCol = newHoverGridCol;
                owner.hoverGridRow = newHoverGridRow;
                owner.hoverJokerIndex = newHoverJokerIndex;
                owner.hoverConsumableIndex = newHoverConsumableIndex;
                owner.render();
            }

            boolean hand = mouseOverDraw
                || mouseOverDiscard
                || mouseOverSettings
                || mouseOverLeave
                || hoveredDebug >= 0
                || hoveredGrid != null
                || newHoverJokerIndex >= 0
                || newHoverConsumableIndex >= 0;

            owner.setCursor(hand ? Cursor.HAND : Cursor.DEFAULT);
        });

        owner.setOnMouseExited(e -> {
            owner.hoverDraw = false;
            owner.hoverDiscard = false;
            owner.hoverSettingsButton = false;
            owner.hoverLeaveButton = false;
            owner.hoverDebugAction = -1;
            owner.hoverGridCol = -1;
            owner.hoverGridRow = -1;
            owner.hoverJokerIndex = -1;
            owner.hoverConsumableIndex = -1;
            owner.render();
        });


        owner.setOnMouseClicked(e -> {
            double x = e.getX();
            double y = e.getY();

            if (isDraggingAnyCard()) {
                return;
            }

            if (owner.layout.isInsideSettingsButton(x, y)) {
                owner.settingsPanelOpen = !owner.settingsPanelOpen;
                owner.render();
                return;
            }

            if (owner.layout.isInsideLeaveButton(x, y)) {
                owner.settingsPanelOpen = false;
                owner.render();
                owner.onLeave.run();
                return;
            }

            int debugAction = owner.layout.hitTestDebugButton(x, y);
            if (debugAction >= 0) {
                performDebugAction(debugAction);
                return;
            }

            if (owner.settingsPanelOpen && !owner.layout.inPanel(x, y)) {
                owner.settingsPanelOpen = false;
                owner.render();
                return;
            }

            int jokerIndex = owner.layout.getJokerIndexAt(x, y);
            if (jokerIndex >= 0) {
                onJokerClicked(jokerIndex);
                return;
            }

            int consumableIndex = owner.layout.getConsumableIndexAt(x, y);
            if (consumableIndex >= 0) {
                onConsuClicked(consumableIndex);
            }
        });

        owner.setOnMouseReleased(e -> {
            if (isDraggingGridCard()) {
                if (owner.dragStarted && owner.layout.isInsideDiscardCard(e.getX(), e.getY())) {
                    dropDraggedGridCardIntoDiscard();
                } else {
                    cancelDrag();
                }
                owner.render();
                return;
            }

            if (isDraggingDrawCard()) {
                int[] cell = owner.layout.getGridCellAt(e.getX(), e.getY());
                if (owner.dragStarted && cell != null) {
                    dropDraggedDrawCardIntoGrid(cell[0], cell[1]);
                } else {
                    cancelDrag();
                }
                owner.render();
                return;
            }

            if (isDraggingDiscardCard()) {
                int[] cell = owner.layout.getGridCellAt(e.getX(), e.getY());
                if (owner.dragStarted && cell != null) {
                    dropDraggedDiscardCardIntoGrid(cell[0], cell[1]);
                } else {
                    cancelDrag();
                }
                owner.render();
            }
        });

        owner.setOnMouseDragged(e -> {
            owner.dragMouseX = e.getX();
            owner.dragMouseY = e.getY();

            double dx = owner.dragMouseX - owner.pressMouseX;
            double dy = owner.dragMouseY - owner.pressMouseY;
            double dist = Math.hypot(dx, dy);

            if (dist >= owner.DRAG_THRESHOLD) {
                owner.dragStarted = true;
            }

            if (isDraggingGridCard()) {
                owner.hoverDiscardDropZone = owner.layout.isInsideDiscardCard(owner.dragMouseX, owner.dragMouseY);
                owner.hoverDropGridCol = -1;
                owner.hoverDropGridRow = -1;
                owner.render();
                return;
            }

            if (isDraggingDrawCard() || isDraggingDiscardCard()) {
                int[] hoverCell = owner.layout.getGridCellAt(owner.dragMouseX, owner.dragMouseY);
                owner.hoverDiscardDropZone = false;
                owner.hoverDropGridCol = hoverCell != null ? hoverCell[0] : -1;
                owner.hoverDropGridRow = hoverCell != null ? hoverCell[1] : -1;
                owner.render();
            }
        });

        owner.setOnMousePressed(e -> {
            double x = e.getX();
            double y = e.getY();

            owner.dragMouseX = x;
            owner.dragMouseY = y;
            owner.pressMouseX = x;
            owner.pressMouseY = y;
            owner.dragStarted = false;

            if (owner.state.drawPile != null && owner.layout.isInsideDrawCard(x, y)) {
                beginDrawPileDrag(x, y);
                return;
            }

            if (owner.state.discardPile != null && owner.layout.isInsideDiscardCard(x, y)) {
                beginDiscardPileDrag(x, y);
                return;
            }

            int[] cell = owner.layout.getGridCellAt(x, y);
            if (cell != null) {
                Card card = owner.state.grid.get(cell[0], cell[1]);
                if (card != null) {
                    owner.debugTargetCard = card;
                    beginGridCardDrag(cell[0], cell[1], x, y);
                    owner.render();
                }
            }
        });

        owner.setOnKeyPressed(e -> {
            if (e.getCode() == javafx.scene.input.KeyCode.ESCAPE && owner.settingsPanelOpen) {
                owner.settingsPanelOpen = false;
                owner.render();
            }
        });

        owner.setFocusTraversable(true);
    }

    // Actions ----------------------------------------------------------------------------

    private void startFlip(Card card) {
        if (card == null) {
            return;
        }

        if (owner.activeFlipAnimations.containsKey(card)) {
            return;
        }

        owner.activeFlipAnimations.put(card, new AnimBox.CardFlipAnimation(18));
    }


    /**
     * Clears the {@code selected} flag on every card in the grid,
     * every joker, and every consumable.
     */
    void deselectAll() {
        for (int c = 0; c < owner.state.grid.cols; c++) {
            for (int r = 0; r < owner.state.grid.rows; r++) {
                if (owner.state.grid.get(c, r) != null) {
                    owner.state.grid.get(c, r).selected = false;
                }
            }
        }
        owner.state.jokers.forEach(card -> card.selected = false);
        owner.state.consumables.forEach(card -> card.selected = false);
    }

    public void onDrawClicked() {
        if (owner.state.drawPile == null) return;

        boolean wasSelected = owner.state.drawPile.selected;
        deselectAll();
        owner.state.drawPile.selected = !wasSelected;
        owner.render();
    }

    public void onGridCellClicked(int col, int row) {
        if (isDraggingDrawCard()) {
            dropDraggedDrawCardIntoGrid(col, row);
            owner.render();
            return;
        }

        Card card = owner.state.grid.get(col, row);
        if (card == null) return;

        boolean wasSelected = card.selected;
        deselectAll();
        card.selected = !wasSelected;
        owner.render();
    }

    /**
     * Called when the player clicks the discard pile card.
     * Override or extend to implement discard logic; currently logs and re-renders.
     */
    public void onDiscardClicked() {
        System.out.println("[GameGui] Discard clicked");
        owner.render();
    }


    /**
     * Called when the player clicks a joker card in the bottom bar.
     * Toggles its selection owner.state and deselects all other cards.
     *
     * @param i zero-based index of the clicked joker
     */
    public void onJokerClicked(int i) {
        Card card = owner.state.jokers.get(i);
        boolean wasSelected = card.selected;
        deselectAll();
        card.selected = !wasSelected;
        owner.render();
    }

    /**
     * Called when the player clicks a consumable card in the bottom bar.
     * Toggles its selection owner.state and deselects all other cards.
     *
     * @param i zero-based index of the clicked consumable
     */
    public void onConsuClicked(int i) {
        Card card = owner.state.consumables.get(i);
        boolean wasSelected = card.selected;
        deselectAll();
        card.selected = !wasSelected;
        owner.render();
    }

    /**
     * Dispatches a debug action identified by its constant index to the appropriate handler method.
     * Triggers a re-owner.render after the action completes.
     *
     * @param action one of the {@code DEBUG_*} constants (e.g. {@link #owner.DEBUG_ADD_CARD})
     */
    /**
     * Dispatches a debug action identified by its constant index to the
     * appropriate handler method.  Triggers a re-render after the action completes.
     *
     * @param action one of the {@code DEBUG_*} constants defined in {@link GameCanvas}
     */
    void performDebugAction(int action) {
        switch (action) {
            case GameCanvas.DEBUG_FLIP_SELECTED -> flipDebugTargetCard();
            case GameCanvas.DEBUG_ADD_CARD      -> addDebugCard();
            case GameCanvas.DEBUG_REMOVE_CARD   -> removeDebugCard();
            case GameCanvas.DEBUG_ADD_JOKER     -> addDebugJoker();
            case GameCanvas.DEBUG_REMOVE_JOKER  -> removeDebugJoker();
            case GameCanvas.DEBUG_ADD_CONSU     -> addDebugConsu();
            case GameCanvas.DEBUG_REMOVE_CONSU  -> removeDebugConsu();
            case GameCanvas.DEBUG_OPEN_SHOP     -> owner.onOpenShop.run();
            case GameCanvas.DEBUG_SCORE         -> debugScore();
            case GameCanvas.DEBUG_HIGHLIGHT     -> debugHighlight();
            default -> { /* unknown action – ignore */ }
        }
        owner.render();
    }

    void flipDebugTargetCard() {
        if (owner.debugTargetCard != null) {
            owner.animations.startFlip(owner.debugTargetCard);
        }
    }

    /**
     * Debug action: places a new face-up card into the first empty slot found in the grid.
     * The card value is cycled from a small predefined pool. Does nothing if the grid is full.
     */
    void addDebugCard() {
        for (int c = 0; c < owner.state.grid.cols; c++) {
            for (int r = 0; r < owner.state.grid.rows; r++) {
                if (owner.state.grid.get(c, r) == null) {
                    owner.state.grid.set(c, r, new Card(nextDebugCardValue(), true));
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
    void removeDebugCard() {
        for (int c = 0; c < owner.state.grid.cols; c++) {
            for (int r = 0; r < owner.state.grid.rows; r++) {
                Card card = owner.state.grid.get(c, r);
                if (card != null && card.selected) {
                    owner.state.grid.set(c, r, null);
                    return;
                }
            }
        }
        for (int c = owner.state.grid.cols - 1; c >= 0; c--) {
            for (int r = owner.state.grid.rows - 1; r >= 0; r--) {
                if (owner.state.grid.get(c, r) != null) {
                    owner.state.grid.set(c, r, null);
                    return;
                }
            }
        }
    }

    /**
     * Debug action: appends a new face-up joker card (value 0) to the joker list
     * and clears all current selections.
     */
    void addDebugJoker() {
        deselectAll();
        owner.state.jokers.add(new Card(0, true));
    }

    /**
     * Debug action: removes a joker from the list.
     * If a joker is currently selected, it is removed; otherwise the last joker is removed.
     * Does nothing if the joker list is empty.
     */
    void removeDebugJoker() {
        for (int i = 0; i < owner.state.jokers.size(); i++) {
            if (owner.state.jokers.get(i).selected) {
                owner.state.jokers.remove(i);
                return;
            }
        }
        if (!owner.state.jokers.isEmpty()) {
            owner.state.jokers.remove(owner.state.jokers.size() - 1);
        }
    }

    /**
     * Debug action: appends a new face-up consumable card (value 0) to the consumable list
     * and clears all current selections.
     */
    void addDebugConsu() {
        deselectAll();
        owner.state.consumables.add(new Card(0, true));
    }

    /**
     * Debug action: removes a consumable from the list.
     * If a consumable is currently selected, it is removed; otherwise the last one is removed.
     * Does nothing if the consumable list is empty.
     */
    void removeDebugConsu() {
        for (int i = 0; i < owner.state.consumables.size(); i++) {
            if (owner.state.consumables.get(i).selected) {
                owner.state.consumables.remove(i);
                return;
            }
        }
        if (!owner.state.consumables.isEmpty()) {
            owner.state.consumables.remove(owner.state.consumables.size() - 1);
        }
    }

    /**
     * Returns the next card value to use when adding a debug card,
     * cycling through a fixed pool {@code {-2, 0, 3, 7, 12}} based on
     * how many cards are currently on the grid.
     *
     * @return the next debug card value
     */
    int nextDebugCardValue() {
        int[] pool = {-2, 0, 3, 7, 12};
        int count = 0;
        for (int c = 0; c < owner.state.grid.cols; c++) {
            for (int r = 0; r < owner.state.grid.rows; r++) {
                if (owner.state.grid.get(c, r) != null) {
                    count++;
                }
            }
        }
        return pool[count % pool.length];
    }

    /**
     * Debug action: pushes a randomly generated Skyjo combo entry into the score-panel
     * combo feed and highlights the corresponding grid cells.
     *
     * <p>Delegates entirely to {@link GameCanvasAnimations#debugEmulateRandomCombo()}.
     * Once the game layer exposes real combo-detection results, those results should
     * call {@link GameCanvasAnimations#pushComboEntry(ComboEntry)} and
     * {@link GameCanvasAnimations#highlightGridRange(int, int, int, int)} directly.</p>
     */
    void debugScore() {
        owner.animations.debugEmulateRandomCombo();
    }

    /**
     * Debug action: highlights a random contiguous group of grid cells
     * (row / column / diagonal / anti-diagonal) without adding a combo entry to the
     * score feed.  This is a pure visual test for the highlight subsystem and is
     * completely independent of game-scoring functions.
     */
    void debugHighlight() {
        owner.animations.debugHighlightRandomGroup();
    }
}