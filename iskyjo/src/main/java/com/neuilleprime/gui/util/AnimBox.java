package com.neuilleprime.gui.util;

import javafx.scene.paint.Color;


/**
 * Utility class containing animation helpers such as easing functions,
 * alpha-based animations, and color interpolation utilities.
 */
public class AnimBox {

    /**
     * Collection of easing functions used to smooth animations.
     */
    public static class Easing {

        /**
         * Ease-in function (slow start, fast end).
         * @param t Progress value between 0 and 1.
         * @return Smoothed progress.
         */
        public static float easeIn(float t) {
            t = clamp(t);
            return t * t;
        }

        /**
         * Ease-out function (fast start, slow end).
         * @param t Progress value between 0 and 1.
         * @return Smoothed progress.
         */
        public static float easeOut(float t) {
            t = clamp(t);
            return 1f - (1f - t) * (1f - t);
        }

        /**
         * Ease-in-out function (slow start and end).
         * @param t Progress value between 0 and 1.
         * @return Smoothed progress.
         */
        public static float easeInOut(float t) {
            t = clamp(t);
            return t < 0.5f ? 2f * t * t : 1f - 2f * (1f - t) * (1f - t);
        }

        /**
         * Clamps a value between 0 and 1.
         * @param v Input value.
         * @return Clamped value.
         */
        public static float clamp(float v) {
            return Math.max(0f, Math.min(1f, v));
        }
    }

    /**
     * Base abstract class for alpha-based animations.
     * Handles delay and normalization of alpha values.
     */
    public static abstract class AlphaAnimation {

        /** Delay before animation starts (in frames). */
        protected final int delayFrames;

        /**
         * @param delayFrames Number of frames to wait before animation starts.
         */
        protected AlphaAnimation(int delayFrames) {
            this.delayFrames = Math.max(0, delayFrames);
        }

        /**
         * Computes the alpha value for a given frame.
         * @param frame Current global frame.
         * @return Alpha value between 0 and 1.
         */
        public final float getAlpha(int frame) {
            int localFrame = frame - delayFrames;
            if (localFrame < 0) return 0f;
            return Easing.clamp(computeAlpha(localFrame));
        }

        /**
         * Computes raw alpha (before clamping).
         * @param localFrame Frame relative to animation start.
         * @return Alpha value.
         */
        protected abstract float computeAlpha(int localFrame);

        /**
         * Indicates if animation is finished.
         * @param frame Current frame.
         * @return true if finished.
         */
        public abstract boolean isFinished(int frame);
    }

    /**
     * Fade-in animation (alpha goes from 0 → 1).
     */
    public static class FadeInAnimation extends AlphaAnimation {

        protected final int fadeInFrames;

        /**
         * @param delayFrames Delay before animation starts.
         * @param fadeInFrames Duration of fade-in.
         */
        public FadeInAnimation(int delayFrames, int fadeInFrames) {
            super(delayFrames);
            this.fadeInFrames = Math.max(1, fadeInFrames);
        }

        @Override
        protected float computeAlpha(int localFrame) {
            if (localFrame >= fadeInFrames) return 1f;
            return Easing.easeOut((float) localFrame / fadeInFrames);
        }

        @Override
        public boolean isFinished(int frame) {
            return false;
        }
    }

    /**
     * Fade-out animation (alpha goes from 1 → 0).
     */
    public static class FadeOutAnimation extends FadeInAnimation {

        /**
         * @param delayFrames Delay before animation starts.
         * @param fadeOutFrames Duration of fade-out.
         */
        public FadeOutAnimation(int delayFrames, int fadeOutFrames) {
            super(delayFrames, fadeOutFrames);
        }

        @Override
        protected float computeAlpha(int localFrame) {
            if (localFrame >= fadeInFrames) return 0f;
            return 1f - Easing.easeIn((float) localFrame / fadeInFrames);
        }

        @Override
        public boolean isFinished(int frame) {
            return frame - delayFrames >= fadeInFrames;
        }
    }

    /**
     * Fade-in → hold → fade-out animation.
     */
    public static class FadeInOutAnimation extends AlphaAnimation {

        private final int fadeInFrames;
        private final int holdFrames;
        private final int fadeOutFrames;

        /**
         * @param delayFrames Delay before animation starts.
         * @param fadeInFrames Fade-in duration.
         * @param holdFrames Time at full opacity.
         * @param fadeOutFrames Fade-out duration.
         */
        public FadeInOutAnimation(int delayFrames, int fadeInFrames, int holdFrames, int fadeOutFrames) {
            super(delayFrames);
            this.fadeInFrames  = Math.max(1, fadeInFrames);
            this.holdFrames    = Math.max(0, holdFrames);
            this.fadeOutFrames = Math.max(1, fadeOutFrames);
        }

        @Override
        protected float computeAlpha(int localFrame) {
            if (localFrame < fadeInFrames)
                return Easing.easeOut((float) localFrame / fadeInFrames);

            localFrame -= fadeInFrames;
            if (localFrame < holdFrames) return 1f;

            localFrame -= holdFrames;
            if (localFrame < fadeOutFrames)
                return 1f - Easing.easeIn((float) localFrame / fadeOutFrames);

            return 0f;
        }

        @Override
        public boolean isFinished(int frame) {
            return frame >= delayFrames + fadeInFrames + holdFrames + fadeOutFrames;
        }

        /**
         * Checks if animation is currently in fade-out phase.
         */
        public boolean isInFadeOutPhase(int frame) {
            int localFrame = frame - delayFrames;
            if (localFrame < 0) return false;
            return localFrame >= fadeInFrames + holdFrames
                && localFrame < fadeInFrames + holdFrames + fadeOutFrames;
        }
    }

    /**
     * Utility class to interpolate between two colors over time.
     */
    public static class ColorFade {

        private final Color colorFrom;
        private final Color colorTo;
        private final int steps;
        private float progress;

        /**
         * @param colorFrom Starting color.
         * @param colorTo Ending color.
         * @param steps Number of steps to reach target.
         */
        public ColorFade(Color colorFrom, Color colorTo, int steps) {
            this.colorFrom = colorFrom;
            this.colorTo   = colorTo;
            this.steps     = Math.max(1, steps);
            this.progress  = 0f;
        }

        /**
         * Advances or reverses the animation.
         * @param forward true to move forward, false to reverse.
         */
        public void tick(boolean forward) {
            float delta = 1f / steps;
            if (forward) progress = Math.min(1f, progress + delta);
            else         progress = Math.max(0f, progress - delta);
        }

        /**
         * Computes the current interpolated color.
         * @return Interpolated color.
         */
        public Color getColor() {
            float t = Easing.easeInOut(progress);
            double r = lerp(colorFrom.getRed(),   colorTo.getRed(),   t);
            double g = lerp(colorFrom.getGreen(), colorTo.getGreen(), t);
            double b = lerp(colorFrom.getBlue(),  colorTo.getBlue(),  t);
            return new Color(r, g, b, t);
        }

        /** @return Current progress (0 → 1). */
        public float getProgress() { return progress; }

        /** Resets animation. */
        public void reset() { progress = 0f; }

        /** Jumps to final state. */
        public void skipToEnd() { progress = 1f; }

        /**
         * Linear interpolation helper.
         */
        private double lerp(double a, double b, float t) {
            return a + (b - a) * t;
        }
    }

        /**
     * Horizontal card-flip animation.
     * <p>
     * The card visually shrinks on the X axis, switches face at mid-animation,
     * then expands back to full size.
     */
    public static class CardFlipAnimation {
        private final int totalFrames;
        private int frame;
        private boolean faceSwitched;

        /**
         * Creates a new card flip animation.
         *
         * @param totalFrames total duration of the flip animation in frames
         */
        public CardFlipAnimation(int totalFrames) {
            this.totalFrames = Math.max(2, totalFrames);
            this.frame = 0;
            this.faceSwitched = false;
        }

        /**
         * Advances the animation by one frame.
         */
        public void tick() {
            if (!isFinished()) {
                frame++;
            }
        }

        /**
         * Returns the current horizontal scale of the card.
         * Value is between 0 and 1.
         *
         * @return current X scale
         */
        public double getScaleX() {
            double progress = Math.min(1.0, (double) frame / totalFrames);

            if (progress < 0.5) {
                double local = progress / 0.5;
                return 1.0 - Easing.easeInOut((float) local);
            } else {
                double local = (progress - 0.5) / 0.5;
                return Easing.easeInOut((float) local);
            }
        }

        /**
         * Returns whether the card face should switch on this frame.
         * This only returns {@code true} once, when crossing the halfway point.
         *
         * @return {@code true} if the face should now be toggled
         */
        public boolean shouldSwitchFace() {
            if (!faceSwitched && frame >= totalFrames / 2) {
                faceSwitched = true;
                return true;
            }
            return false;
        }

        /**
         * Returns whether the animation is finished.
         *
         * @return {@code true} once the animation reached its final frame
         */
        public boolean isFinished() {
            return frame >= totalFrames;
        }
    }
}