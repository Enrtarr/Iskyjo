import java.awt.Color;

public class AnimBox {

    // tah After Effect
    public static class Easing {

        public static float easeIn(float t) {
            t = clamp(t);
            return t * t;
        }

        public static float easeOut(float t) {
            t = clamp(t);
            return 1f - (1f - t) * (1f - t);
        }

        public static float easeInOut(float t) {
            t = clamp(t);
            return t < 0.5f ? 2f * t * t : 1f - 2f * (1f - t) * (1f - t);
        }

        public static float clamp(float v) {
            return Math.max(0f, Math.min(1f, v));
        }
    }

    public static abstract class AlphaAnimation {
        protected final int delayFrames;

        protected AlphaAnimation(int delayFrames) {
            this.delayFrames = Math.max(0, delayFrames);
        }

        public final float getAlpha(int frame) {
            int localFrame = frame - delayFrames;
            if (localFrame < 0) return 0f;
            return Easing.clamp(computeAlpha(localFrame));
        }

        protected abstract float computeAlpha(int localFrame);
        public abstract boolean isFinished(int frame);
    }

    public static class FadeInAnimation extends AlphaAnimation {
        protected final int fadeInFrames;

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

    public static class FadeOutAnimation extends FadeInAnimation {
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

    public static class FadeInOutAnimation extends AlphaAnimation {
        private final int fadeInFrames;
        private final int holdFrames;
        private final int fadeOutFrames;

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

        public boolean isInFadeOutPhase(int frame) {
            int localFrame = frame - delayFrames;
            if (localFrame < 0) return false;
            return localFrame >= fadeInFrames + holdFrames
                && localFrame <  fadeInFrames + holdFrames + fadeOutFrames;
        }
    }

    // Helps making fading stuff easier
    public static class FadeTrack {
        private final int     delayFrames;
        private final int     fadeInFrames;
        private final int     holdFrames;
        private final int     fadeOutFrames;
        private final boolean stayVisibleAfterFadeIn;

        public FadeTrack(int delayFrames, int fadeInFrames, int holdFrames,
                         int fadeOutFrames, boolean stayVisibleAfterFadeIn) {
            this.delayFrames           = delayFrames;
            this.fadeInFrames          = fadeInFrames;
            this.holdFrames            = holdFrames;
            this.fadeOutFrames         = fadeOutFrames;
            this.stayVisibleAfterFadeIn = stayVisibleAfterFadeIn;
        }

        public float getAlpha(int frame) {
            int t = frame - delayFrames;
            if (t < 0) return 0f;

            if (fadeInFrames > 0 && t < fadeInFrames)
                return Easing.easeOut(Easing.clamp((float) t / fadeInFrames));

            if (fadeInFrames > 0) t -= fadeInFrames;

            if (stayVisibleAfterFadeIn) return 1f;

            if (holdFrames > 0 && t < holdFrames) return 1f;
            if (holdFrames > 0) t -= holdFrames;

            if (fadeOutFrames > 0 && t < fadeOutFrames)
                return 1f - Easing.easeIn(Easing.clamp((float) t / fadeOutFrames));

            if (fadeInFrames == 0 && holdFrames == 0 && fadeOutFrames == 0)
                return stayVisibleAfterFadeIn ? 1f : 0f;

            return 0f;
        }
    }

    public static class ColorFade {
        private final Color colorFrom;
        private final Color colorTo;
        private final int   steps;
        private float       progress; // 0.0 (colorFrom) → 1.0 (colorTo)

        public ColorFade(Color colorFrom, Color colorTo, int steps) {
            this.colorFrom = colorFrom;
            this.colorTo   = colorTo;
            this.steps     = Math.max(1, steps);
            this.progress  = 0f;
        }


        public void tick(boolean forward) {
            float delta = 1f / steps;
            if (forward) progress = Math.min(1f, progress + delta);
            else         progress = Math.max(0f, progress - delta);
        }

        public Color getColor() {
            float t = Easing.easeInOut(progress);
            int r = lerp(colorFrom.getRed(),   colorTo.getRed(),   t);
            int g = lerp(colorFrom.getGreen(), colorTo.getGreen(), t);
            int b = lerp(colorFrom.getBlue(),  colorTo.getBlue(),  t);
            int a = (int) (t * 255);
            return new Color(r, g, b, a);
        }

        public float getProgress() { return progress; }

        public void reset() { progress = 0f; }

        public void skipToEnd() { progress = 1f; }

        // aka linear interpolisation
        private int lerp(int a, int b, float t) {
            return Math.round(a + (b - a) * t);
        }
    }
}