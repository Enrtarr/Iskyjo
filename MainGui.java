import javax.swing.*;
import java.awt.*;
import java.io.File;

public class MainGui {
    private static Font customFont;
    
    static {
        try {
            customFont = Font.createFont(Font.TRUETYPE_FONT, new File("C:/Users/Jules/Downloads/VCR_OSD_MONO.ttf")).deriveFont(Font.BOLD, 14f);
        } catch (Exception e) {
            customFont = new Font("Arial", Font.BOLD, 14);
        }
    }
    private static JFrame mainFrame;
    private static Point splashPosition;

    public static void main(String[] args) {
        System.setProperty("awt.useSystemAAFontSettings", "off");
        System.setProperty("swing.aatext", "false");
        
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Main Frame
        mainFrame = new JFrame("Iskyjo");
        mainFrame.setUndecorated(true);
        mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        mainFrame.setSize(1400, 900);
        mainFrame.setLocationRelativeTo(null);
        mainFrame.setResizable(true);

        // Store position for splash screen
        splashPosition = mainFrame.getLocation();

        MainPanel mainPanel = new MainPanel();
        mainFrame.getContentPane().add(mainPanel);
        mainFrame.setVisible(false);
        
        // Show splash screen with fade in of main frame
        showSplashScreen(mainPanel);
        
        mainPanel.startMain();
    }

    private static void showSplashScreen(MainPanel mainPanel) {
        JFrame splashFrame = new JFrame();
        splashFrame.setUndecorated(true);
        splashFrame.setSize(1400, 900);
        splashFrame.setLocation(splashPosition);
        
        SplashPanel splashPanel = new SplashPanel();
        splashPanel.setBackground(Color.BLACK);
        splashFrame.add(splashPanel);
        splashFrame.setVisible(true);
        
        Timer splashTimer = new Timer(30, e -> {
            if (splashPanel.phase == 0) {
                if (splashPanel.alpha < 255) {
                    splashPanel.alpha += 5;
                } else {
                    splashPanel.alpha = 255;
                    splashPanel.phase = 1;
                    splashPanel.holdCounter = 0;
                }
            }
            else if (splashPanel.phase == 1) {
                splashPanel.holdCounter++;
                if (splashPanel.holdCounter > 100) {
                    splashPanel.phase = 2;
                }
            }
            else if (splashPanel.phase == 2) {
                // Show main frame on first frame of fade out phase
                if (!splashPanel.mainFrameShown) {
                    mainFrame.setVisible(true);
                    splashPanel.mainFrameShown = true;
                }
                
                // Fade out splash and fade in main frame simultaneously
                if (splashPanel.alpha > 0) {
                    splashPanel.alpha -= 5;
                    mainFrame.setOpacity(1.0f - (splashPanel.alpha / 255.0f));
                    mainPanel.backgroundAlpha = 1.0f - (splashPanel.alpha / 255.0f);
                } else {
                    splashPanel.alpha = 0;
                    mainFrame.setOpacity(1.0f);
                    mainPanel.backgroundAlpha = 1.0f;
                    ((Timer)e.getSource()).stop();
                    splashFrame.setVisible(false);
                    splashFrame.dispose();
                }
            }
            
            splashPanel.repaint();
        });
        
        splashTimer.start();
        
        try {
            Thread.sleep(3500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    static class SplashPanel extends JPanel {
        int alpha = 0;
        int phase = 0; // 0: fade in, 1: hold, 2: fade out
        int holdCounter = 0;
        boolean mainFrameShown = false;
        
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;

            g2.setColor(new Color(0, 0, 0, 255));
            g2.fillRect(0, 0, getWidth(), getHeight());
            
            g2.setColor(new Color(255, 255, 255, alpha));
            g2.setFont(customFont.deriveFont(Font.BOLD, 48f));
            String text = "NEUILLE PRIME :3";
            FontMetrics fm = g2.getFontMetrics();
            int x = (getWidth() - fm.stringWidth(text)) / 2;
            int y = (getHeight() - fm.getHeight()) / 2 + fm.getAscent();
            g2.drawString(text, x, y);
        }
    }

    static class MainPanel extends JPanel {
        private int mainCounter = 0;
        private Timer mainTimer;
        public float backgroundAlpha = 0.0f;
        public float titleAlpha = 0.0f;

        public MainPanel() {
            setLayout(null);
            setBackground(Color.BLACK);
        }

        public void startMain() {
            mainTimer = new Timer(25, e -> {
                mainCounter++;
                // Fade in title after background finished faded in
                if (backgroundAlpha >= 0.99f && titleAlpha < 1.0f) {
                    titleAlpha += 0.04f;
                }
                repaint();
            });
            mainTimer.start();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            // Start with black background
            g2.setColor(Color.BLACK);
            g2.fillRect(0, 0, getWidth(), getHeight());

            // Animated background
            float hue = (mainCounter % 360) / 360f;
            Color bg1 = Color.getHSBColor(hue, 1f, 0.8f);
            Color bg2 = Color.getHSBColor((hue + 0.5f) % 1f, 1f, 0.8f);
            
            // Apply alpha blending for fade in effect
            int alpha = Math.min(255, (int)(backgroundAlpha * 255));
            Color fadedBg1 = new Color(bg1.getRed(), bg1.getGreen(), bg1.getBlue(), alpha);
            Color fadedBg2 = new Color(bg2.getRed(), bg2.getGreen(), bg2.getBlue(), alpha);
            
            java.awt.GradientPaint gradient = new java.awt.GradientPaint(0, 0, fadedBg1, getWidth(), getHeight(), fadedBg2);
            g2.setPaint(gradient);
            g2.fillRect(0, 0, getWidth(), getHeight());

            // Title fade in after background fade in
            if (backgroundAlpha >= 0.99f) {
                java.awt.geom.AffineTransform original = g2.getTransform();
                g2.translate(getWidth() / 2, getHeight() / 2);
                double rotation = Math.sin(mainCounter * 0.03) * 0.025;
                g2.rotate(rotation);
                g2.setFont(customFont.deriveFont(Font.BOLD, 120f));
                int titleAlphaInt = Math.min(255, (int)(titleAlpha * 255));
                g2.setColor(new Color(255, 200, 0, titleAlphaInt));
                g2.drawString("Iskyjo", -250, 0);
                g2.setTransform(original);
            }
        }
    }

}
