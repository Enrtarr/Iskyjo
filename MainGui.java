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
    private static Point  splashPosition;
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
            @Override public void mouseClicked(MouseEvent e)  {}
            @Override public void mouseReleased(MouseEvent e) {}
            @Override public void mouseEntered(MouseEvent e)  {}
            @Override public void mouseExited(MouseEvent e)   {}
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

    private static float clamp(float v) { return Math.max(0f, Math.min(1f, v)); }

    // Credits Splash screen ---------------------------------------------------------------------

    static class SplashPanel extends JPanel {
        private int     frame = 0;
        private boolean mainFrameShown = false;
        private final BufferedImage                logo;
        private final AnimBox.FadeInOutAnimation splashAnimation;

        SplashPanel() {
            BufferedImage img = null;
            try {
                img = ImageIO.read(new File("Assets/NP_logo.png"));
            } catch (Exception e) {
                System.out.println("Failed loading splash logo: " + e.getMessage());
            }
            logo = img;
            splashAnimation = new AnimBox.FadeInOutAnimation(0, 40, 100, 40);
        }

        public void nextFrame()     { frame++; }
        public float getAlpha()     { return splashAnimation.getAlpha(frame); }
        public boolean isFinished() { return splashAnimation.isFinished(frame); }

        public boolean isMainFrameRevealPhase() {
            return splashAnimation.isInFadeOutPhase(frame);
        }

        public boolean isMainFrameShown()               { return mainFrameShown; }
        public void    setMainFrameShown(boolean value) { this.mainFrameShown = value; }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);

            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);

            g2.setColor(Color.BLACK);
            g2.fillRect(0, 0, getWidth(), getHeight());

            float alpha = getAlpha();
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, clamp(alpha)));

            int centerX = getWidth()  / 2;
            int centerY = getHeight() / 2;

            int logoW = 0, logoH = 0;
            if (logo != null) {
                logoW = logo.getWidth();
                logoH = logo.getHeight();
                g2.drawImage(logo, centerX - logoW / 2, centerY - logoH / 2, null);
            }

            g2.setColor(Color.WHITE);

            Font mainFont = customFont.deriveFont(Font.BOLD, 42f);
            Font subFont  = customFont.deriveFont(Font.PLAIN, 18f);

            FontMetrics fmMain = g2.getFontMetrics(mainFont);
            FontMetrics fmSub  = g2.getFontMetrics(subFont);

            int spacing = 160;

            String leftMain      = "Enrtarr";
            String leftSub       = "Game";
            int    leftMainWidth = fmMain.stringWidth(leftMain);
            int    leftSubWidth  = fmSub.stringWidth(leftSub);
            int    leftX         = centerX - logoW / 2 - spacing - Math.max(leftMainWidth, leftSubWidth);
            int    leftYMain     = centerY - 5;
            int    leftYSub      = leftYMain + fmSub.getHeight() + 2;

            g2.setFont(mainFont);
            g2.drawString(leftMain, leftX, leftYMain);
            g2.setFont(subFont);
            g2.drawString(leftSub, leftX + (leftMainWidth - leftSubWidth) / 2, leftYSub);

            String rightMain      = "Maaple";
            String rightSub       = "GUI";
            int    rightMainWidth = fmMain.stringWidth(rightMain);
            int    rightSubWidth  = fmSub.stringWidth(rightSub);
            int    rightX         = centerX + logoW / 2 + spacing;
            int    rightYMain     = centerY - 5;
            int    rightYSub      = rightYMain + fmSub.getHeight() + 2;

            g2.setFont(mainFont);
            g2.drawString(rightMain, rightX, rightYMain);
            g2.setFont(subFont);
            g2.drawString(rightSub, rightX + (rightMainWidth - rightSubWidth) / 2, rightYSub);

            g2.dispose();
        }

        private float clamp(float v) { return Math.max(0f, Math.min(1f, v)); }
    }

    // MainPanel ---------------------------------------------------------------------

    static class MainPanel extends JPanel {
        private int   mainCounter = 0;
        private Timer mainTimer;

        private float revealProgress  = 0.0f;
        private float backgroundAlpha = 0.0f;
        private float titleAlpha      = 0.0f;
        private float buttonsAlpha    = 0.0f;

        private BufferedImage cardPattern;
        private BufferedImage buttonPlayImg;
        private BufferedImage buttonLeaderboardImg;

        private AnimBox.FadeTrack cardsFade;
        private AnimBox.FadeTrack titleFade;
        private AnimBox.FadeTrack buttonsFade;

        // Menu buttons
        private boolean hoverPlay        = false;
        private boolean hoverLeaderboard = false;
        private static final int MENU_BUTTON_W   = 240;
        private static final int MENU_BUTTON_H   = 65;
        private static final int MENU_BUTTON_GAP = 70;

        // Menu Background
        private static final int   CARD_WIDTH       = 36;
        private static final int   CARD_HEIGHT      = 48;
        private static final int   SPACING          = 20;
        private static final float ANIMATION_SPEED  = 1.5f;
        private static final float CARD_OPACITY_BASE = 0.05f;

        // Overlay buttons (minimize / close)
        private static final int BUTTON_FONT_SIZE    = 16;
        private static final int BUTTON_HIT_SIZE     = 28;
        private static final int BUTTON_TOP          = 8;
        private static final int BUTTON_RIGHT        = 10;
        private static final int BUTTON_GAP          = 6;
        private static final int DRAG_ZONE_HEIGHT = 50;
        private boolean hoverMin   = false;
        private boolean hoverClose = false;

        // Background color fade on button hover
        private static final Color BG_BASE        = new Color(0x1d2b53);
        private static final Color BG_PLAY        = new Color(0x1e532c);
        private static final Color BG_LEADERBOARD = new Color(0x534b1f);
        private static final int   BG_FADE_STEPS  = 6;
        private AnimBox.ColorFade bgPlayFade;
        private AnimBox.ColorFade bgLeaderboardFade;

        // Draggable window
        private final JFrame frame;
        private Point   dragStart;
        private boolean dragging        = false;
        private boolean mainAnimSkipped = false;

        public MainPanel(JFrame frame) {
            this.frame = frame;
            setLayout(null);
            setBackground(Color.BLACK);

            loadAssets();
            initAnimations();
            initInteraction();
        }

        // --- Initialisation -----------------------------------------------------------

        private void initAnimations() {
            cardsFade   = new AnimBox.FadeTrack(0,  40, 0, 0, true);
            titleFade   = new AnimBox.FadeTrack(16, 34, 0, 0, true);
            buttonsFade = new AnimBox.FadeTrack(40, 30, 0, 0, true);

            bgPlayFade        = new AnimBox.ColorFade(BG_BASE, BG_PLAY,        BG_FADE_STEPS);
            bgLeaderboardFade = new AnimBox.ColorFade(BG_BASE, BG_LEADERBOARD, BG_FADE_STEPS);
        }

        private void loadAssets() {
            try {
                cardPattern = ImageIO.read(new File("Assets/Card_bg.png"));
            } catch (Exception error) {
                System.out.println("Failed loading background cards: " + error.getMessage());
            }
            try {
                buttonPlayImg = ImageIO.read(new File("Assets/Buttons/button_play.png"));
            } catch (Exception error) {
                System.out.println("Failed loading play button: " + error.getMessage());
            }
            try {
                buttonLeaderboardImg = ImageIO.read(new File("Assets/Buttons/button_leaderboard.png"));
            } catch (Exception error) {
                System.out.println("Failed loading leaderboard button: " + error.getMessage());
            }
        }

        // --- Animation skip / start ---------------------------------------------------

        public void skipMainAnim() {
            if (mainAnimSkipped) return;
            mainAnimSkipped = true;
            backgroundAlpha = 1.0f;
            titleAlpha      = 1.0f;
            buttonsAlpha    = 1.0f;
            repaint();
        }

        public void startMain() {
            if (mainTimer != null && mainTimer.isRunning()) return;

            mainCounter = 0;

            mainTimer = new Timer(25, e -> {
                mainCounter++;

                if (!mainAnimSkipped) {
                    backgroundAlpha = cardsFade.getAlpha(mainCounter);
                    titleAlpha      = titleFade.getAlpha(mainCounter);
                    buttonsAlpha    = buttonsFade.getAlpha(mainCounter);
                }

                bgPlayFade.tick(hoverPlay);
                bgLeaderboardFade.tick(hoverLeaderboard);

                repaint();
            });

            mainTimer.start();
        }

        public void setRevealProgress(float revealProgress) {
            this.revealProgress = clamp(revealProgress);
            repaint();
        }

        // --- Interactions --------------------------------------------------------------

        private void initInteraction() {
            addMouseMotionListener(new MouseMotionAdapter() {
                @Override
                public void mouseMoved(MouseEvent e) {
                    boolean nm = minBounds().contains(e.getPoint());
                    boolean nc = closeBounds().contains(e.getPoint());
                    boolean np = playBounds().contains(e.getPoint());
                    boolean nl = leaderboardBounds().contains(e.getPoint());

                    if (nm != hoverMin || nc != hoverClose || np != hoverPlay || nl != hoverLeaderboard) {
                        hoverMin         = nm;
                        hoverClose       = nc;
                        hoverPlay        = np;
                        hoverLeaderboard = nl;
                    }
                    repaint();
                }

                @Override
                public void mouseDragged(MouseEvent e) {
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
                    if (!minBounds().contains(e.getPoint()) && !closeBounds().contains(e.getPoint())
                            && e.getY() <= DRAG_ZONE_HEIGHT) {
                        dragStart = e.getPoint();
                        dragging  = true;
                    }
                }

                @Override
                public void mouseReleased(MouseEvent e) {
                    dragging  = false;
                    dragStart = null;
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    if (hoverMin || hoverClose || hoverPlay || hoverLeaderboard) {
                        hoverMin         = false;
                        hoverClose       = false;
                        hoverPlay        = false;
                        hoverLeaderboard = false;
                    }
                    repaint();
                }

                @Override
                public void mouseClicked(MouseEvent e) {
                    // Animation skip
                    if (!mainAnimSkipped && buttonsAlpha < 1.0f) {
                        skipMainAnim();
                        return;
                    }

                    if (minBounds().contains(e.getPoint())) {
                        frame.setState(JFrame.ICONIFIED);
                    } else if (closeBounds().contains(e.getPoint())) {
                        System.exit(0);
                    } else if (playBounds().contains(e.getPoint())) {
                        System.out.println("Play clicked");
                        //
                    } else if (leaderboardBounds().contains(e.getPoint())) {
                        System.out.println("Leaderboard clicked");
                        //
                    }
                }
            });
        }

        // --- Hitboxs ----------------------------------------------------------------

        private Rectangle closeBounds() {
            return new Rectangle(
                    getWidth() - BUTTON_RIGHT - BUTTON_HIT_SIZE,
                    BUTTON_TOP, BUTTON_HIT_SIZE, BUTTON_HIT_SIZE);
        }

        private Rectangle minBounds() {
            return new Rectangle(
                    getWidth() - BUTTON_RIGHT - BUTTON_HIT_SIZE - BUTTON_GAP - BUTTON_HIT_SIZE,
                    BUTTON_TOP, BUTTON_HIT_SIZE, BUTTON_HIT_SIZE);
        }

        private Rectangle playBounds() {
            int totalW = MENU_BUTTON_W * 2 + MENU_BUTTON_GAP;
            int startX = (getWidth() - totalW) / 2;
            int y      = getHeight() / 2 + 80;
            return new Rectangle(startX, y, MENU_BUTTON_W, MENU_BUTTON_H);
        }

        private Rectangle leaderboardBounds() {
            int totalW = MENU_BUTTON_W * 2 + MENU_BUTTON_GAP;
            int startX = (getWidth() - totalW) / 2;
            int y      = getHeight() / 2 + 80;
            return new Rectangle(startX + MENU_BUTTON_W + MENU_BUTTON_GAP, y, MENU_BUTTON_W, MENU_BUTTON_H);
        }

        // --- Render --------------------------------------------------------------------

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);

            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,  RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);

            // Background
            g2.setColor(BG_BASE);
            g2.fillRect(0, 0, getWidth(), getHeight());

            // Background buttons shade
            drawColorOverlay(g2, bgPlayFade.getColor());
            drawColorOverlay(g2, bgLeaderboardFade.getColor());

            // Cards pattern
            float finalBackgroundAlpha = backgroundAlpha * revealProgress;
            if (cardPattern != null && finalBackgroundAlpha > 0.001f) {
                float totalW = CARD_WIDTH  + SPACING;
                float totalH = CARD_HEIGHT + SPACING;

                int startX = (int) (-(totalW) + (mainCounter * ANIMATION_SPEED)        % totalW);
                int startY = (int) (-(totalH) + (mainCounter * ANIMATION_SPEED * 0.4f) % totalH);

                g2.setComposite(AlphaComposite.getInstance(
                        AlphaComposite.SRC_OVER,
                        clamp(CARD_OPACITY_BASE * finalBackgroundAlpha)));

                for (int x = startX; x < getWidth(); x += (int) totalW)
                    for (int y = startY; y < getHeight(); y += (int) totalH)
                        g2.drawImage(cardPattern, x, y, CARD_WIDTH, CARD_HEIGHT, null);

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

            drawOverlayButton(g2, minBounds(),   hoverMin,   false);
            drawOverlayButton(g2, closeBounds(), hoverClose, true);

            // Buttons
            float finalButtonsAlpha = buttonsAlpha * revealProgress;
            if (finalButtonsAlpha > 0.001f) {
                drawMenuButton(g2, playBounds(),        buttonPlayImg,        hoverPlay,        finalButtonsAlpha);
                drawMenuButton(g2, leaderboardBounds(), buttonLeaderboardImg, hoverLeaderboard, finalButtonsAlpha);
            }
        }

        private void drawColorOverlay(Graphics2D g2, Color c) {
            int a = c.getAlpha();
            if (a < 2) return;
            Composite old = g2.getComposite();
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, a / 255f));
            g2.setColor(new Color(c.getRed(), c.getGreen(), c.getBlue()));
            g2.fillRect(0, 0, getWidth(), getHeight());
            g2.setComposite(old);
        }

        private void drawMenuButton(Graphics2D g2, Rectangle r, BufferedImage img, boolean hover, float alpha) {
            float finalAlpha = clamp(alpha);
            float scale      = hover ? 1.06f : 1.0f;

            Composite old = g2.getComposite();
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, finalAlpha));

            int drawW = (int) (r.width  * scale);
            int drawH = (int) (r.height * scale);
            int drawX = r.x + (r.width  - drawW) / 2;
            int drawY = r.y + (r.height - drawH) / 2;

            if (img != null) {
                g2.drawImage(img, drawX, drawY, drawW, drawH, null);
            } else {
                g2.setColor(new Color(255, 200, 0, (int) (finalAlpha * 200)));
                g2.fillRoundRect(drawX, drawY, drawW, drawH, 12, 12);
                g2.setColor(new Color(255, 200, 0, (int) (finalAlpha * 255)));
                g2.setStroke(new BasicStroke(2f));
                g2.drawRoundRect(drawX, drawY, drawW, drawH, 12, 12);
            }

            g2.setComposite(old);
        }

        private void drawOverlayButton(Graphics2D g2, Rectangle r, boolean hover, boolean isClose) {
            g2.setColor(hover
                    ? (isClose ? new Color(255, 90, 90) : Color.WHITE)
                    : new Color(255, 255, 255, 140));

            String symbol = isClose ? "X" : "_";
            g2.setFont(customFont.deriveFont(Font.BOLD, (float) BUTTON_FONT_SIZE));
            FontMetrics fm = g2.getFontMetrics();

            int tx = r.x + (r.width - fm.stringWidth(symbol)) / 2;
            int ty = r.y + (r.height + fm.getAscent() - fm.getDescent()) / 2;
            g2.drawString(symbol, tx, ty);
        }

        private float clamp(float v) { return Math.max(0f, Math.min(1f, v)); }
    }
}