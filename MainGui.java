import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionAdapter;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;

public class MainGui {
    private static Font customFont;

    static {
        try {
            customFont = Font.createFont(Font.TRUETYPE_FONT, new File("Assets/Fonts/VCR_OSD_MONO.ttf"))
                    .deriveFont(Font.BOLD, 14f);
        } catch (Exception e) {
            customFont = new Font("Comic Sans MS", Font.BOLD, 14);
            System.err.println("Failed loading font: " + e.getMessage());
        }
    }

    private static JFrame mainFrame;
    private static Point splashPosition;
    private static boolean splashSkipped = false;

    public static void main(String[] args) {
        System.setProperty("awt.useSystemAAFontSettings", "off");
        System.setProperty("swing.aatext", "false");

        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        mainFrame = new JFrame("Iskyjo");
        mainFrame.setUndecorated(true);
        mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        mainFrame.setSize(1400, 900);
        mainFrame.setLocationRelativeTo(null);
        mainFrame.setResizable(true);

        splashPosition = mainFrame.getLocation();

        MainPanel mainPanel = new MainPanel(mainFrame);
        mainFrame.getContentPane().add(mainPanel);
        mainFrame.setVisible(false);

        showSplashScreen(mainPanel);
    }

    // Splash screen ---------------------------------------------------------------------

    private static void showSplashScreen(MainPanel mainPanel) {
        JFrame splashFrame = new JFrame();
        splashFrame.setUndecorated(true);
        splashFrame.setSize(1400, 900);
        splashFrame.setLocation(splashPosition);

        SplashPanel splashPanel = new SplashPanel();
        splashPanel.setBackground(Color.BLACK);
        splashFrame.add(splashPanel);
        splashFrame.setVisible(true);

        Timer splashTimer = new Timer(30, null);

        splashPanel.addMouseListener(new MouseListener() {
            @Override public void mousePressed(MouseEvent e) { skipSplash(splashTimer, splashFrame, mainPanel, splashPanel); }
            @Override public void mouseClicked(MouseEvent e) {}
            @Override public void mouseReleased(MouseEvent e) {}
            @Override public void mouseEntered(MouseEvent e) {}
            @Override public void mouseExited(MouseEvent e) {}
        });

        splashTimer.addActionListener(e -> {
            splashPanel.nextFrame();

            if (splashPanel.isMainFrameRevealPhase()) {
                if (!splashPanel.isMainFrameShown()) {
                    mainFrame.setOpacity(0f);
                    mainFrame.setVisible(true);
                    splashPanel.setMainFrameShown(true);
                }

                float reveal = 1.0f - splashPanel.getAlpha();
                mainFrame.setOpacity(clamp(reveal));
                mainPanel.setRevealProgress(reveal);
            }

            if (splashPanel.isFinished()) {
                mainFrame.setOpacity(1.0f);
                mainPanel.setRevealProgress(1.0f);

                splashTimer.stop();
                splashFrame.setVisible(false);
                splashFrame.dispose();

                if (!splashSkipped) {
                    splashSkipped = true;
                    mainPanel.startMain();
                }
            }

            splashPanel.repaint();
        });

        splashTimer.start();
    }

    private static void skipSplash(Timer splashTimer, JFrame splashFrame, MainPanel mainPanel, SplashPanel splashPanel) {
        splashTimer.stop();

        mainFrame.setOpacity(1.0f);
        mainPanel.setRevealProgress(1.0f);
        mainFrame.setVisible(true);
        mainFrame.toFront();
        mainFrame.requestFocus();
        mainPanel.paintImmediately(0, 0, mainPanel.getWidth(), mainPanel.getHeight());

        splashFrame.setVisible(false);
        splashFrame.dispose();

        splashPanel.setMainFrameShown(true);
        splashSkipped = true;

        mainPanel.startMain();
    }

    // Force value between 0 to 1 (protection for alpha bugs)
    private static float clamp(float v) {
        return Math.max(0f, Math.min(1f, v));
    }

    // SplashPanel ---------------------------------------------------------------------

    static class SplashPanel extends JPanel {
        private int frame = 0;
        private boolean mainFrameShown = false;
        private final BufferedImage logo;
        private final AlphaAnimation splashAnimation;

        SplashPanel() {
            BufferedImage img = null;
            try {
                img = ImageIO.read(new File("Assets/NP_logo.png"));
            } catch (Exception e) {
                System.out.println("Failed loading splash logo: " + e.getMessage());
            }
            logo = img;

            splashAnimation = new FadeInOutAnimation(
                    0,
                    40,
                    100,
                    40
            );
        }

        public void nextFrame() {
            frame++;
        }

        public float getAlpha() {
            return splashAnimation.getAlpha(frame);
        }

        public boolean isFinished() {
            return splashAnimation.isFinished(frame);
        }

        public boolean isMainFrameRevealPhase() {
            if (splashAnimation instanceof FadeInOutAnimation animation) {
                return animation.isInFadeOutPhase(frame);
            }
            return false;
        }

        public boolean isMainFrameShown() {
            return mainFrameShown;
        }

        public void setMainFrameShown(boolean mainFrameShown) {
            this.mainFrameShown = mainFrameShown;
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);

            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);

            g2.setColor(Color.BLACK);
            g2.fillRect(0, 0, getWidth(), getHeight());

            float alpha = getAlpha();

            g2.setComposite(AlphaComposite.getInstance(
                    AlphaComposite.SRC_OVER,
                    clamp(alpha)
            ));

            int centerX = getWidth() / 2;
            int centerY = getHeight() / 2;

            // --- LOGO ---
            int logoW = 0;
            int logoH = 0;

            if (logo != null) {
                logoW = logo.getWidth();
                logoH = logo.getHeight();

                int x = centerX - logoW / 2;
                int y = centerY - logoH / 2;

                g2.drawImage(logo, x, y, null);
            }

            g2.setColor(Color.WHITE);

            Font mainFont = customFont.deriveFont(Font.BOLD, 42f);
            Font subFont  = customFont.deriveFont(Font.PLAIN, 18f);

            FontMetrics fmMain = g2.getFontMetrics(mainFont);
            FontMetrics fmSub  = g2.getFontMetrics(subFont);

            int spacing = 160;

            String leftMain = "Enrtarr";
            String leftSub  = "Code";

            int leftMainWidth = fmMain.stringWidth(leftMain);
            int leftSubWidth  = fmSub.stringWidth(leftSub);

            int leftX = centerX - logoW / 2 - spacing - Math.max(leftMainWidth, leftSubWidth);
            int leftYMain = centerY - 5;
            int leftYSub  = leftYMain + fmSub.getHeight() + 2;

            g2.setFont(mainFont);
            g2.drawString(leftMain, leftX, leftYMain);

            g2.setFont(subFont);
            g2.drawString(leftSub, leftX + (leftMainWidth - leftSubWidth) / 2, leftYSub);

            String rightMain = "Maaple";
            String rightSub  = "GUI";

            int rightMainWidth = fmMain.stringWidth(rightMain);
            int rightSubWidth  = fmSub.stringWidth(rightSub);

            int rightX = centerX + logoW / 2 + spacing;
            int rightYMain = centerY - 5;
            int rightYSub  = rightYMain + fmSub.getHeight() + 2;

            g2.setFont(mainFont);
            g2.drawString(rightMain, rightX, rightYMain);

            g2.setFont(subFont);
            g2.drawString(rightSub, rightX + (rightMainWidth - rightSubWidth) / 2, rightYSub);

            g2.dispose();
        }

        private float clamp(float v) {
            return Math.max(0f, Math.min(1f, v));
        }
    }

    // MainPanel ---------------------------------------------------------------------

    static class MainPanel extends JPanel {
        private int mainCounter = 0;
        private Timer mainTimer;

        private float revealProgress = 0.0f;
        private float backgroundAlpha = 0.0f;
        private float titleAlpha = 0.0f;

        private BufferedImage cardPattern;

        private FadeTrack cardsFade;
        private FadeTrack titleFade;

        // Menu Background
        private static final int CARD_WIDTH = 36;
        private static final int CARD_HEIGHT = 48;
        private static final int SPACING = 20;
        private static final float ANIMATION_SPEED = 1.5f;

        // Overlay button
        private static final int BTN_FONT_SIZE = 16;
        private static final int BTN_HIT_SIZE = 28;
        private static final int BTN_TOP = 8;
        private static final int BTN_RIGHT = 10;
        private static final int BTN_GAP = 6;

        // Hovering overlay button
        private boolean hoverMin = false;
        private boolean hoverClose = false;

        // Mouse position for spotlight effect
        private int mouseX = -1;
        private int mouseY = -1;
        private static final int SPOTLIGHT_RADIUS = 160;
        private static final float CARD_OPACITY_MAX = 0.45f;
        private static final float CARD_OPACITY_BASE = 0.05f;

        // Draggable window
        private final JFrame frame;
        private Point dragStart;
        private boolean dragging = false;

        public MainPanel(JFrame frame) {
            this.frame = frame;
            setLayout(null);
            setBackground(Color.BLACK);

            loadCardPattern();
            initInteraction();
            initAnimations();
        }

        private void initAnimations() {
            cardsFade = new FadeTrack(
                    0,
                    40,
                    0,
                    0,
                    true
            );

            titleFade = new FadeTrack(
                    16,
                    34,
                    0,
                    0,
                    true
            );
        }

        public void setRevealProgress(float revealProgress) {
            this.revealProgress = clamp(revealProgress);
            repaint();
        }

        private void initInteraction() {
            addMouseMotionListener(new MouseMotionAdapter() {
                @Override
                public void mouseMoved(MouseEvent e) {
                    mouseX = e.getX();
                    mouseY = e.getY();

                    boolean nm = minBounds().contains(e.getPoint());
                    boolean nc = closeBounds().contains(e.getPoint());

                    if (nm != hoverMin || nc != hoverClose) {
                        hoverMin = nm;
                        hoverClose = nc;
                    }

                    repaint();
                }

                @Override
                public void mouseDragged(MouseEvent e) {
                    mouseX = e.getX();
                    mouseY = e.getY();

                    if (dragging && dragStart != null) {
                        Point loc = frame.getLocation();
                        frame.setLocation(
                                loc.x + e.getX() - dragStart.x,
                                loc.y + e.getY() - dragStart.y
                        );
                    }
                }
            });

            addMouseListener(new MouseAdapter() {
                @Override
                public void mousePressed(MouseEvent e) {
                    if (!minBounds().contains(e.getPoint()) && !closeBounds().contains(e.getPoint())) {
                        dragStart = e.getPoint();
                        dragging = true;
                    }
                }

                @Override
                public void mouseReleased(MouseEvent e) {
                    dragging = false;
                    dragStart = null;
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    mouseX = -1;
                    mouseY = -1;

                    if (hoverMin || hoverClose) {
                        hoverMin = false;
                        hoverClose = false;
                    }

                    repaint();
                }

                @Override
                public void mouseClicked(MouseEvent e) {
                    if (minBounds().contains(e.getPoint())) {
                        frame.setState(JFrame.ICONIFIED);
                    } else if (closeBounds().contains(e.getPoint())) {
                        System.exit(0);
                    }
                }
            });
        }

        private Rectangle closeBounds() {
            return new Rectangle(
                    getWidth() - BTN_RIGHT - BTN_HIT_SIZE,
                    BTN_TOP,
                    BTN_HIT_SIZE,
                    BTN_HIT_SIZE
            );
        }

        private Rectangle minBounds() {
            return new Rectangle(
                    getWidth() - BTN_RIGHT - BTN_HIT_SIZE - BTN_GAP - BTN_HIT_SIZE,
                    BTN_TOP,
                    BTN_HIT_SIZE,
                    BTN_HIT_SIZE
            );
        }

        private void loadCardPattern() {
            try {
                cardPattern = ImageIO.read(new File("Assets/Card_bg.png"));
            } catch (Exception e) {
                cardPattern = null;
                System.out.println("Failed loading background cards: " + e.getMessage());
            }
        }

        public void startMain() {
            if (mainTimer != null && mainTimer.isRunning()) {
                return;
            }

            mainCounter = 0;

            mainTimer = new Timer(25, e -> {
                mainCounter++;

                backgroundAlpha = cardsFade.getAlpha(mainCounter);
                titleAlpha = titleFade.getAlpha(mainCounter);

                repaint();
            });

            mainTimer.start();
        }

        private boolean isCardsFadeInFinished() {
            return backgroundAlpha >= 0.999f;
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);

            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);

            // Background
            g2.setColor(new Color(0x1d2b53));
            g2.fillRect(0, 0, getWidth(), getHeight());

            // Cards pattern
            float finalBackgroundAlpha = backgroundAlpha * revealProgress;

            if (cardPattern != null && finalBackgroundAlpha > 0.001f) {
                float totalW = CARD_WIDTH + SPACING;
                float totalH = CARD_HEIGHT + SPACING;

                int startX = (int) (-(totalW) + (mainCounter * ANIMATION_SPEED) % totalW);
                int startY = (int) (-(totalH) + (mainCounter * ANIMATION_SPEED * 0.4f) % totalH);

                for (int x = startX; x < getWidth(); x += (int) totalW) {
                    for (int y = startY; y < getHeight(); y += (int) totalH) {
                        float cardCX = x + CARD_WIDTH / 2f;
                        float cardCY = y + CARD_HEIGHT / 2f;

                        float opacity;

                        if (isCardsFadeInFinished() && mouseX >= 0 && mouseY >= 0) {
                            float dx = cardCX - mouseX;
                            float dy = cardCY - mouseY;
                            float dist = (float) Math.sqrt(dx * dx + dy * dy);

                            float t = Math.max(0f, 1f - dist / SPOTLIGHT_RADIUS);
                            opacity = CARD_OPACITY_BASE + t * t * (CARD_OPACITY_MAX - CARD_OPACITY_BASE);
                        } else {
                            opacity = CARD_OPACITY_BASE;
                        }

                        float drawAlpha = opacity * finalBackgroundAlpha;

                        if (drawAlpha > 0.01f) {
                            g2.setComposite(AlphaComposite.getInstance(
                                    AlphaComposite.SRC_OVER,
                                    clamp(drawAlpha)
                            ));
                            g2.drawImage(cardPattern, x, y, CARD_WIDTH, CARD_HEIGHT, null);
                        }
                    }
                }

                g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
            }

            // Title
            float finalTitleAlpha = titleAlpha * revealProgress;

            if (finalTitleAlpha > 0.001f) {
                java.awt.geom.AffineTransform original = g2.getTransform();

                float scale = 0.92f + 0.08f * finalTitleAlpha;

                g2.translate(getWidth() / 2, getHeight() / 2);
                g2.rotate(Math.sin(mainCounter * 0.03) * 0.025);
                g2.scale(scale, scale);

                g2.setFont(customFont.deriveFont(Font.BOLD, 120f));
                g2.setColor(new Color(255, 200, 0, Math.min(255, (int) (finalTitleAlpha * 255))));
                g2.drawString("Iskyjo", -250, 0);

                g2.setTransform(original);
            }

            drawOverlayButton(g2, minBounds(), hoverMin, false);
            drawOverlayButton(g2, closeBounds(), hoverClose, true);
        }

        private void drawOverlayButton(Graphics2D g2, Rectangle r, boolean hover, boolean isClose) {
            g2.setColor(
                    hover
                            ? (isClose ? new Color(255, 90, 90) : Color.WHITE)
                            : new Color(255, 255, 255, 140)
            );

            String symbol = isClose ? "X" : "_";
            g2.setFont(customFont.deriveFont(Font.BOLD, (float) BTN_FONT_SIZE));
            FontMetrics fm = g2.getFontMetrics();

            int tx = r.x + (r.width - fm.stringWidth(symbol)) / 2;
            int ty = r.y + (r.height + fm.getAscent() - fm.getDescent()) / 2;

            g2.drawString(symbol, tx, ty);
        }

        private float clamp(float v) {
            return Math.max(0f, Math.min(1f, v));
        }
    }

    // Generic alpha animation ---------------------------------------------------------

    static abstract class AlphaAnimation {
        protected final int delayFrames;

        protected AlphaAnimation(int delayFrames) {
            this.delayFrames = Math.max(0, delayFrames);
        }

        public final float getAlpha(int frame) {
            int localFrame = frame - delayFrames;
            if (localFrame < 0) {
                return 0f;
            }
            return clamp(computeAlpha(localFrame));
        }

        protected abstract float computeAlpha(int localFrame);

        public abstract boolean isFinished(int frame);

        protected float clamp(float v) {
            return Math.max(0f, Math.min(1f, v));
        }

        protected float easeIn(float t) {
            t = clamp(t);
            return t * t;
        }

        protected float easeOut(float t) {
            t = clamp(t);
            return 1f - (1f - t) * (1f - t);
        }
    }

    // Fade in ------------------------------------------------------------------------

    static class FadeInAnimation extends AlphaAnimation {
        protected final int fadeInFrames;

        public FadeInAnimation(int delayFrames, int fadeInFrames) {
            super(delayFrames);
            this.fadeInFrames = Math.max(1, fadeInFrames);
        }

        @Override
        protected float computeAlpha(int localFrame) {
            if (localFrame >= fadeInFrames) {
                return 1f;
            }
            return easeOut((float) localFrame / fadeInFrames);
        }

        @Override
        public boolean isFinished(int frame) {
            return false;
        }
    }

    // Fade out -----------------------------------------------------------------------

    static class FadeOutAnimation extends FadeInAnimation {
        public FadeOutAnimation(int delayFrames, int fadeOutFrames) {
            super(delayFrames, fadeOutFrames);
        }

        @Override
        protected float computeAlpha(int localFrame) {
            if (localFrame >= fadeInFrames) {
                return 0f;
            }
            return 1f - easeIn((float) localFrame / fadeInFrames);
        }

        @Override
        public boolean isFinished(int frame) {
            return frame - delayFrames >= fadeInFrames;
        }
    }

    // Fade in + hold + fade out ------------------------------------------------------

    static class FadeInOutAnimation extends AlphaAnimation {
        private final int fadeInFrames;
        private final int holdFrames;
        private final int fadeOutFrames;

        public FadeInOutAnimation(int delayFrames, int fadeInFrames, int holdFrames, int fadeOutFrames) {
            super(delayFrames);
            this.fadeInFrames = Math.max(1, fadeInFrames);
            this.holdFrames = Math.max(0, holdFrames);
            this.fadeOutFrames = Math.max(1, fadeOutFrames);
        }

        @Override
        protected float computeAlpha(int localFrame) {
            if (localFrame < fadeInFrames) {
                return easeOut((float) localFrame / fadeInFrames);
            }

            localFrame -= fadeInFrames;

            if (localFrame < holdFrames) {
                return 1f;
            }

            localFrame -= holdFrames;

            if (localFrame < fadeOutFrames) {
                return 1f - easeIn((float) localFrame / fadeOutFrames);
            }

            return 0f;
        }

        @Override
        public boolean isFinished(int frame) {
            int total = delayFrames + fadeInFrames + holdFrames + fadeOutFrames;
            return frame >= total;
        }

        public boolean isInFadeOutPhase(int frame) {
            int localFrame = frame - delayFrames;
            if (localFrame < 0) {
                return false;
            }

            return localFrame >= fadeInFrames + holdFrames
                    && localFrame < fadeInFrames + holdFrames + fadeOutFrames;
        }
    }

    // Main menu fade helper ----------------------------------------------------------

    static class FadeTrack {
        private final int delayFrames;
        private final int fadeInFrames;
        private final int holdFrames;
        private final int fadeOutFrames;
        private final boolean stayVisibleAfterFadeIn;

        public FadeTrack(int delayFrames, int fadeInFrames, int holdFrames, int fadeOutFrames, boolean stayVisibleAfterFadeIn) {
            this.delayFrames = delayFrames;
            this.fadeInFrames = fadeInFrames;
            this.holdFrames = holdFrames;
            this.fadeOutFrames = fadeOutFrames;
            this.stayVisibleAfterFadeIn = stayVisibleAfterFadeIn;
        }

        public float getAlpha(int frame) {
            int t = frame - delayFrames;

            if (t < 0) {
                return 0f;
            }

            if (fadeInFrames > 0 && t < fadeInFrames) {
                return easeOut(clamp((float) t / fadeInFrames));
            }

            if (fadeInFrames > 0) {
                t -= fadeInFrames;
            }

            if (stayVisibleAfterFadeIn) {
                return 1f;
            }

            if (holdFrames > 0 && t < holdFrames) {
                return 1f;
            }

            if (holdFrames > 0) {
                t -= holdFrames;
            }

            if (fadeOutFrames > 0 && t < fadeOutFrames) {
                return 1f - easeIn(clamp((float) t / fadeOutFrames));
            }

            if (fadeInFrames == 0 && holdFrames == 0 && fadeOutFrames == 0) {
                return stayVisibleAfterFadeIn ? 1f : 0f;
            }

            return 0f;
        }

        private float clamp(float v) {
            return Math.max(0f, Math.min(1f, v));
        }

        private float easeOut(float t) {
            return 1f - (1f - t) * (1f - t);
        }

        private float easeIn(float t) {
            return t * t;
        }
    }
}