package com.neuilleprime.gui.game;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.neuilleprime.gui.model.Card;
import com.neuilleprime.gui.util.AnimBox;

import javafx.animation.AnimationTimer;
import javafx.scene.image.Image;

/**
 * Handles time-based behaviour for the in-game canvas.
 *
 * <p>This includes asset loading, the main animation timer, and card flip
 * animations.</p>
 */
public final class GameCanvasAnimations {
    final GameCanvas owner;

    GameCanvasAnimations(GameCanvas owner) {
        this.owner = owner;
    }

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
                owner.render();
            }
        };
        owner.animationTimer.start();
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
}
