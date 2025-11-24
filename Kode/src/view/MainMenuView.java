package view;

import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import javax.imageio.ImageIO;
import javax.swing.*;
import util.Constants;

public class MainMenuView extends JPanel {
    
    private final ScreenManager screenManager;
    private final JFrame parentFrame;
    private JButton startButton;
    private JButton leaderboardButton;
    private JButton exitButton;
    private JButton settingsButton;
    private JLabel headerLabel;
    private JLabel versionLabel;
    private List<FloatingShape> floatingShapes;
    private Timer animationTimer;
    private BufferedImage headerImage;
    private BufferedImage settingsImage;
    private JPanel exitOverlay;
    private JPanel exitDialogPanel;
    private ComponentAdapter exitOverlayResizeListener;
    
    public MainMenuView(ScreenManager screenManager) {
        this.screenManager = screenManager;
        this.parentFrame = screenManager.getFrame();
        setLayout(null);
        setOpaque(false);
        setPreferredSize(Constants.WINDOW_SIZE);
        setDoubleBuffered(true);
        loadHeaderImage();
        initializeFloatingShapes();
        createComponents();
        setupLayout();
        startAnimation();
        addComponentListener(new java.awt.event.ComponentAdapter() {
            @Override
            public void componentResized(java.awt.event.ComponentEvent e) {
                layoutComponents();
            }
        });
    }
    
    private void loadHeaderImage() {
        try {
            headerImage = ImageIO.read(new File("assets/header.png"));
            settingsImage = ImageIO.read(new File("assets/pengaturan.png"));
        } catch (Exception e) {
            System.err.println("Failed to load header image: " + e.getMessage());
        }
    }
    
    private void initializeFloatingShapes() {
        floatingShapes = new ArrayList<>();
        Random rand = new Random();
        
        Color[] colors = {Constants.NEO_YELLOW, Constants.NEO_PINK, Constants.NEO_BLUE, 
                         Constants.NEO_GREEN, Constants.NEO_PURPLE, Constants.NEO_ORANGE};
        
        for (int i = 0; i < 8; i++) {
            int size = 40 + rand.nextInt(60);
            int x = rand.nextInt(1000);
            int y = rand.nextInt(700);
            float speedX = (rand.nextFloat() - 0.5f) * 2;
            float speedY = (rand.nextFloat() - 0.5f) * 2;
            Color color = colors[rand.nextInt(colors.length)];
            boolean isCircle = rand.nextBoolean();
            
            floatingShapes.add(new FloatingShape(x, y, size, speedX, speedY, color, isCircle));
        }
    }
    
    private void startAnimation() {
        animationTimer = new Timer(16, e -> {
            for (FloatingShape shape : floatingShapes) {
                shape.update(getWidth(), getHeight());
            }
            repaint();
        });
        animationTimer.start();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        int centerX = getWidth() / 2;
        int centerY = getHeight() / 2;
        float radius = Math.max(getWidth(), getHeight());
        
        float[] dist = {0.0f, 0.5f, 1.0f};
        Color[] colors = {Constants.CARTOON_RADIAL_CENTER, Constants.CARTOON_BG_START, Constants.CARTOON_BG_END};
        RadialGradientPaint gradient = new RadialGradientPaint(
            centerX, centerY, radius, dist, colors
        );
        g2d.setPaint(gradient);
        g2d.fillRect(0, 0, getWidth(), getHeight());
        
        g2d.setColor(new Color(255, 255, 255, 30));
        g2d.setStroke(new BasicStroke(3));
        for (int i = 0; i < 16; i++) {
            double angle = (Math.PI * 2 / 16) * i;
            int x2 = centerX + (int)(Math.cos(angle) * radius);
            int y2 = centerY + (int)(Math.sin(angle) * radius);
            g2d.drawLine(centerX, centerY, x2, y2);
        }
        
        for (FloatingShape shape : floatingShapes) {
            shape.draw(g2d);
        }
    }
    
    private void createComponents() {
        
        startButton = createCartoonButton("START QUIZ", Constants.NEO_GREEN);
        leaderboardButton = createCartoonButton("LEADERBOARD", Constants.NEO_PINK);
        exitButton = createCartoonButton("EXIT", Constants.NEO_RED);
        settingsButton = createIconButton(settingsImage, Constants.NEO_PURPLE);
        
        exitButton.addActionListener(e -> showExitConfirmation());
        settingsButton.addActionListener(e -> screenManager.showSettings());
    }
    
    private void setupLayout() {
        headerLabel = new JLabel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (headerImage != null) {
                    Graphics2D g2d = (Graphics2D) g;
                    g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
                    
                    int imgWidth = headerImage.getWidth();
                    int imgHeight = headerImage.getHeight();
                    int labelWidth = getWidth();
                    int labelHeight = getHeight();
                    
                    double scaleX = (double) labelWidth / imgWidth;
                    double scaleY = (double) labelHeight / imgHeight;
                    double scale = Math.min(scaleX, scaleY);
                    
                    int scaledWidth = (int) (imgWidth * scale);
                    int scaledHeight = (int) (imgHeight * scale);
                    int x = (labelWidth - scaledWidth) / 2;
                    int y = (labelHeight - scaledHeight) / 2;
                    
                    g2d.drawImage(headerImage, x, y, scaledWidth, scaledHeight, null);
                }
            }
        };
        versionLabel = new JLabel("v1.0 - 3D Cartoon Edition", SwingConstants.CENTER);
        versionLabel.setFont(new Font("Arial", Font.BOLD, 12));
        versionLabel.setForeground(new Color(255, 255, 255, 180));
        
        add(headerLabel);
        add(startButton);
        add(leaderboardButton);
        add(exitButton);
        add(settingsButton);
        add(versionLabel);
        layoutComponents();
    }

    private void layoutComponents() {
        if (headerLabel == null || versionLabel == null) {
            return;
        }
        int width = Math.max(getWidth(), Constants.WINDOW_SIZE.width);
        int height = Math.max(getHeight(), Constants.WINDOW_SIZE.height);
        int headerWidth = Math.min(800, width - 80);
        int headerHeight = 320;
        int headerX = (width - headerWidth) / 2;
        int headerY = Math.max(40, height / 12);
        headerLabel.setBounds(headerX, headerY, headerWidth, headerHeight);
        
        int buttonWidth = Constants.BUTTON_WIDTH;
        int buttonHeight = Constants.BUTTON_HEIGHT;
        int buttonX = (width - buttonWidth) / 2;
        int verticalShift = Math.max(0, (height - Constants.WINDOW_SIZE.height) / 3);
        int firstButtonY = headerY + headerHeight - 30 + verticalShift;
        startButton.setBounds(buttonX, firstButtonY, buttonWidth, buttonHeight);
        leaderboardButton.setBounds(buttonX, firstButtonY + buttonHeight + 20, buttonWidth, buttonHeight);
        exitButton.setBounds(buttonX, firstButtonY + (buttonHeight + 20) * 2, buttonWidth, buttonHeight);
        
        settingsButton.setBounds(Math.max(20, width - 90), Math.max(20, headerY - 30), 60, 60);
        versionLabel.setBounds((width - 250) / 2, height - 50, 250, 20);
    }
    
    private JButton createCartoonButton(String text, Color bgColor) {
        JButton button = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                
                boolean pressed = getModel().isPressed();
                Boolean isHoveredObj = (Boolean) getClientProperty("isHovered");
                boolean isHovered = isHoveredObj != null && isHoveredObj;
                
                g2d.setColor(new Color(0, 0, 0, 100));
                g2d.fillRoundRect(Constants.SHADOW_OFFSET, Constants.SHADOW_OFFSET, 
                    getWidth() - Constants.SHADOW_OFFSET, getHeight() - Constants.SHADOW_OFFSET, 
                    Constants.BORDER_RADIUS, Constants.BORDER_RADIUS);
                
                Color lightColor = bgColor.brighter();
                Color darkColor = bgColor;
                
                if (isHovered && !pressed) {
                    lightColor = new Color(
                        Math.min(255, lightColor.getRed() + 30),
                        Math.min(255, lightColor.getGreen() + 30),
                        Math.min(255, lightColor.getBlue() + 30)
                    );
                    darkColor = bgColor.brighter();
                }
                
                if (pressed) {
                    lightColor = new Color(
                        lightColor.getRed(), 
                        lightColor.getGreen(), 
                        lightColor.getBlue(), 
                        150
                    );
                    darkColor = new Color(
                        darkColor.getRed(), 
                        darkColor.getGreen(), 
                        darkColor.getBlue(), 
                        150
                    );
                }
                
                GradientPaint gradient = new GradientPaint(
                    0, 0, lightColor,
                    0, getHeight() - Constants.SHADOW_OFFSET, darkColor
                );
                g2d.setPaint(gradient);
                g2d.fillRoundRect(0, 0, 
                    getWidth() - Constants.SHADOW_OFFSET, getHeight() - Constants.SHADOW_OFFSET,
                    Constants.BORDER_RADIUS, Constants.BORDER_RADIUS);
                
                int highlightAlpha = pressed ? 40 : 80;
                g2d.setColor(new Color(255, 255, 255, highlightAlpha));
                g2d.fillRoundRect(5, 5, 
                    getWidth() - Constants.SHADOW_OFFSET - 10, (getHeight() - Constants.SHADOW_OFFSET) / 2 - 5,
                    Constants.BORDER_RADIUS - 5, Constants.BORDER_RADIUS - 5);
                
                int outlineAlpha = pressed ? 150 : 255;
                g2d.setColor(new Color(255, 255, 255, outlineAlpha));
                g2d.setStroke(new BasicStroke(Constants.BORDER_THICKNESS));
                g2d.drawRoundRect(0, 0, 
                    getWidth() - Constants.SHADOW_OFFSET, getHeight() - Constants.SHADOW_OFFSET,
                    Constants.BORDER_RADIUS, Constants.BORDER_RADIUS);
                
                g2d.setFont(getFont());
                FontMetrics fm = g2d.getFontMetrics();
                int x = (getWidth() - Constants.SHADOW_OFFSET - fm.stringWidth(getText())) / 2;
                int y = ((getHeight() - Constants.SHADOW_OFFSET - fm.getHeight()) / 2) + fm.getAscent();
                
                int textShadowAlpha = pressed ? 50 : 100;
                g2d.setColor(new Color(0, 0, 0, textShadowAlpha));
                g2d.drawString(getText(), x + 2, y + 2);
                
                int textAlpha = pressed ? 180 : 255;
                g2d.setColor(new Color(255, 255, 255, textAlpha));
                g2d.drawString(getText(), x, y);
                
                g2d.dispose();
            }
        };
        
        button.setFont(Constants.BUTTON_FONT);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setContentAreaFilled(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                ((JButton)e.getSource()).putClientProperty("isHovered", true);
                button.repaint();
            }
            
            @Override
            public void mouseExited(MouseEvent e) {
                ((JButton)e.getSource()).putClientProperty("isHovered", false);
                button.repaint();
            }
        });
        
        return button;
    }
    
    private JButton createIconButton(BufferedImage icon, Color bgColor) {
        JButton button = new JButton() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
                
                boolean pressed = getModel().isPressed();
                Boolean isHoveredObj = (Boolean) getClientProperty("isHovered");
                boolean isHovered = isHoveredObj != null && isHoveredObj;
                int shadowOffset = 4;
                
                g2d.setColor(new Color(0, 0, 0, 100));
                g2d.fillOval(shadowOffset, shadowOffset, getWidth() - shadowOffset * 2, getHeight() - shadowOffset * 2);
                
                Color lightColor = bgColor.brighter();
                Color darkColor = bgColor;
                
                if (isHovered && !pressed) {
                    lightColor = new Color(
                        Math.min(255, lightColor.getRed() + 30),
                        Math.min(255, lightColor.getGreen() + 30),
                        Math.min(255, lightColor.getBlue() + 30)
                    );
                    darkColor = bgColor.brighter();
                }
                
                if (pressed) {
                    lightColor = new Color(lightColor.getRed(), lightColor.getGreen(), lightColor.getBlue(), 150);
                    darkColor = new Color(darkColor.getRed(), darkColor.getGreen(), darkColor.getBlue(), 150);
                }
                
                GradientPaint gradient = new GradientPaint(
                    0, 0, lightColor,
                    0, getHeight() - shadowOffset * 2, darkColor
                );
                g2d.setPaint(gradient);
                g2d.fillOval(0, 0, getWidth() - shadowOffset * 2, getHeight() - shadowOffset * 2);
                
                int highlightAlpha = pressed ? 40 : 80;
                g2d.setColor(new Color(255, 255, 255, highlightAlpha));
                g2d.fillOval(8, 8, getWidth() - shadowOffset * 2 - 16, (getHeight() - shadowOffset * 2) / 2 - 8);
                
                int outlineAlpha = pressed ? 150 : 255;
                g2d.setColor(new Color(255, 255, 255, outlineAlpha));
                g2d.setStroke(new BasicStroke(3));
                g2d.drawOval(1, 1, getWidth() - shadowOffset * 2 - 2, getHeight() - shadowOffset * 2 - 2);
                
                if (icon != null) {
                    int iconSize = (int)((getWidth() - shadowOffset * 2) * 0.5);
                    int iconX = (getWidth() - shadowOffset * 2 - iconSize) / 2;
                    int iconY = (getHeight() - shadowOffset * 2 - iconSize) / 2;
                    g2d.drawImage(icon, iconX, iconY, iconSize, iconSize, null);
                }
                
                g2d.dispose();
            }
        };
        
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setContentAreaFilled(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                button.putClientProperty("isHovered", true);
                button.repaint();
            }
            
            @Override
            public void mouseExited(MouseEvent e) {
                button.putClientProperty("isHovered", false);
                button.repaint();
            }
        });
        
        return button;
    }
    
    private JButton createDialogButton(String text, Color bgColor) {
        JButton button = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                
                boolean pressed = getModel().isPressed();
                Boolean isHoveredObj = (Boolean) getClientProperty("isHovered");
                boolean isHovered = isHoveredObj != null && isHoveredObj;
                int borderRadius = 20;
                int shadowOffset = 4;
                
                g2d.setColor(new Color(0, 0, 0, 100));
                g2d.fillRoundRect(shadowOffset, shadowOffset, 
                    getWidth() - shadowOffset * 2, getHeight() - shadowOffset * 2, 
                    borderRadius, borderRadius);
                
                Color lightColor = bgColor.brighter();
                Color darkColor = bgColor;
                
                if (isHovered && !pressed) {
                    lightColor = new Color(
                        Math.min(255, lightColor.getRed() + 30),
                        Math.min(255, lightColor.getGreen() + 30),
                        Math.min(255, lightColor.getBlue() + 30)
                    );
                    darkColor = bgColor.brighter();
                }
                
                if (pressed) {
                    lightColor = new Color(
                        lightColor.getRed(), 
                        lightColor.getGreen(), 
                        lightColor.getBlue(), 
                        150
                    );
                    darkColor = new Color(
                        darkColor.getRed(), 
                        darkColor.getGreen(), 
                        darkColor.getBlue(), 
                        150
                    );
                }
                
                GradientPaint gradient = new GradientPaint(
                    0, 0, lightColor,
                    0, getHeight() - shadowOffset * 2, darkColor
                );
                g2d.setPaint(gradient);
                g2d.fillRoundRect(0, 0, 
                    getWidth() - shadowOffset * 2, getHeight() - shadowOffset * 2,
                    borderRadius, borderRadius);
                
                int highlightAlpha = pressed ? 40 : 80;
                g2d.setColor(new Color(255, 255, 255, highlightAlpha));
                g2d.fillRoundRect(5, 5, 
                    getWidth() - shadowOffset * 2 - 10, (getHeight() - shadowOffset * 2) / 2 - 5,
                    borderRadius - 5, borderRadius - 5);
                
                int outlineAlpha = pressed ? 150 : 255;
                g2d.setColor(new Color(255, 255, 255, outlineAlpha));
                g2d.setStroke(new BasicStroke(3));
                g2d.drawRoundRect(0, 0, 
                    getWidth() - shadowOffset * 2, getHeight() - shadowOffset * 2,
                    borderRadius, borderRadius);
                
                g2d.setFont(getFont());
                FontMetrics fm = g2d.getFontMetrics();
                int x = (getWidth() - shadowOffset * 2 - fm.stringWidth(getText())) / 2;
                int y = ((getHeight() - shadowOffset * 2 - fm.getHeight()) / 2) + fm.getAscent();
                
                int textShadowAlpha = pressed ? 50 : 100;
                g2d.setColor(new Color(0, 0, 0, textShadowAlpha));
                g2d.drawString(getText(), x + 2, y + 2);
                
                int textAlpha = pressed ? 180 : 255;
                g2d.setColor(new Color(255, 255, 255, textAlpha));
                g2d.drawString(getText(), x, y);
                
                g2d.dispose();
            }
        };
        
        button.setFont(new Font("Arial Black", Font.BOLD, 16));
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setContentAreaFilled(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                button.putClientProperty("isHovered", true);
                button.repaint();
            }
            
            @Override
            public void mouseExited(MouseEvent e) {
                button.putClientProperty("isHovered", false);
                button.repaint();
            }
        });
        
        return button;
    }
    
    public JButton getStartButton() {
        return startButton;
    }
    
    public JButton getLeaderboardButton() {
        return leaderboardButton;
    }
    
    public JButton getExitButton() {
        return exitButton;
    }
    
    public JButton getSettingsButton() {
        return settingsButton;
    }
    
    private void showExitConfirmation() {
        if (exitOverlay != null) {
            return;
        }
        exitOverlay = createExitOverlay();
        exitDialogPanel = createExitDialogPanel();
        exitOverlay.add(exitDialogPanel);
        JLayeredPane layeredPane = parentFrame.getLayeredPane();
        layeredPane.add(exitOverlay, JLayeredPane.MODAL_LAYER);
        exitOverlayResizeListener = new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                updateExitOverlayLayout();
            }
        };
        parentFrame.addComponentListener(exitOverlayResizeListener);
        updateExitOverlayLayout();
        layeredPane.revalidate();
        layeredPane.repaint();
        exitOverlay.requestFocusInWindow();
    }

    private JPanel createExitDialogPanel() {
        JPanel dialogPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setColor(new Color(0, 0, 0, 150));
                g2d.fillRoundRect(8, 8, getWidth() - 16, getHeight() - 16, 30, 30);
                GradientPaint gradient = new GradientPaint(
                    0, 0, Constants.CARTOON_BG_START.brighter(),
                    0, getHeight() - 16, Constants.CARTOON_BG_END
                );
                g2d.setPaint(gradient);
                g2d.fillRoundRect(0, 0, getWidth() - 16, getHeight() - 16, 30, 30);
                g2d.setColor(Constants.NEO_WHITE);
                g2d.setStroke(new BasicStroke(4));
                g2d.drawRoundRect(2, 2, getWidth() - 20, getHeight() - 20, 26, 26);
            }
        };
        dialogPanel.setLayout(null);
        dialogPanel.setOpaque(false);
        JLabel messageLabel = new JLabel("Apakah Anda yakin ingin keluar?", SwingConstants.CENTER);
        messageLabel.setFont(new Font("Arial Black", Font.BOLD, 18));
        messageLabel.setForeground(Color.WHITE);
        messageLabel.setBounds(50, 60, 350, 40);
        JButton yesButton = createDialogButton("YA", Constants.NEO_GREEN);
        yesButton.setBounds(75, 140, 130, 55);
        yesButton.addActionListener(e -> System.exit(0));
        JButton noButton = createDialogButton("TIDAK", Constants.NEO_RED);
        noButton.setBounds(245, 140, 130, 55);
        noButton.addActionListener(e -> hideExitConfirmation());
        dialogPanel.add(messageLabel);
        dialogPanel.add(yesButton);
        dialogPanel.add(noButton);
        return dialogPanel;
    }

    private JPanel createExitOverlay() {
        JPanel overlay = new JPanel(null) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setColor(new Color(0, 0, 0, 180));
                g2d.fillRect(0, 0, getWidth(), getHeight());
                g2d.dispose();
            }
        };
        overlay.setOpaque(false);
        overlay.addMouseListener(new MouseAdapter() {});
        overlay.addMouseMotionListener(new MouseAdapter() {});
        return overlay;
    }

    private void updateExitOverlayLayout() {
        if (exitOverlay == null) {
            return;
        }
        exitOverlay.setBounds(0, 0, parentFrame.getWidth(), parentFrame.getHeight());
        if (exitDialogPanel != null) {
            int panelWidth = 450;
            int panelHeight = 250;
            int x = Math.max(20, (exitOverlay.getWidth() - panelWidth) / 2);
            int y = Math.max(20, (exitOverlay.getHeight() - panelHeight) / 2);
            exitDialogPanel.setBounds(x, y, panelWidth, panelHeight);
        }
        exitOverlay.revalidate();
        exitOverlay.repaint();
    }

    private void hideExitConfirmation() {
        if (exitOverlay == null) {
            return;
        }
        JLayeredPane layeredPane = parentFrame.getLayeredPane();
        layeredPane.remove(exitOverlay);
        if (exitOverlayResizeListener != null) {
            parentFrame.removeComponentListener(exitOverlayResizeListener);
            exitOverlayResizeListener = null;
        }
        exitOverlay = null;
        exitDialogPanel = null;
        layeredPane.revalidate();
        layeredPane.repaint();
    }
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {
                e.printStackTrace();
            }
            JFrame frame = new JFrame("RapidQ - Speed Quiz Game");
            frame.setSize(Constants.WINDOW_SIZE);
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setLocationRelativeTo(null);
            frame.setResizable(false);

            ScreenManager screenManager = new ScreenManager(frame);
            frame.setContentPane(screenManager.getRootPanel());
            frame.setVisible(true);
            screenManager.showMainMenu();
        });
    }
    
    private static class FloatingShape {
        private float x, y;
        private final int size;
        private float speedX, speedY;
        private final Color color;
        private final boolean isCircle;
        private float rotation = 0;
        
        public FloatingShape(int x, int y, int size, float speedX, float speedY, Color color, boolean isCircle) {
            this.x = x;
            this.y = y;
            this.size = size;
            this.speedX = speedX;
            this.speedY = speedY;
            this.color = color;
            this.isCircle = isCircle;
        }
        
        public void update(int maxWidth, int maxHeight) {
            x += speedX;
            y += speedY;
            rotation += 0.02f;
            
            if (x < -size || x > maxWidth) {
                speedX = -speedX;
            }
            if (y < -size || y > maxHeight) {
                speedY = -speedY;
            }
        }
        
        public void draw(Graphics2D g2d) {
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            
            g2d.setColor(new Color(0, 0, 0, 40));
            if (isCircle) {
                g2d.fillOval((int)x + 4, (int)y + 4, size, size);
            } else {
                Graphics2D g2dRotated = (Graphics2D) g2d.create();
                g2dRotated.rotate(rotation, x + size/2 + 4, y + size/2 + 4);
                g2dRotated.fillRect((int)x + 4, (int)y + 4, size, size);
                g2dRotated.dispose();
            }
            
            Color lightColor = color.brighter();
            GradientPaint gradient = new GradientPaint(
                x, y, lightColor,
                x, y + size, color
            );
            g2d.setPaint(gradient);
            
            if (isCircle) {
                g2d.fillOval((int)x, (int)y, size, size);
            } else {
                Graphics2D g2dRotated = (Graphics2D) g2d.create();
                g2dRotated.rotate(rotation, x + size/2, y + size/2);
                g2dRotated.setPaint(gradient);
                g2dRotated.fillRect((int)x, (int)y, size, size);
                g2dRotated.dispose();
            }
            
            g2d.setColor(new Color(255, 255, 255, 150));
            g2d.setStroke(new BasicStroke(3));
            if (isCircle) {
                g2d.drawOval((int)x, (int)y, size, size);
            } else {
                Graphics2D g2dRotated = (Graphics2D) g2d.create();
                g2dRotated.rotate(rotation, x + size/2, y + size/2);
                g2dRotated.setColor(new Color(255, 255, 255, 150));
                g2dRotated.setStroke(new BasicStroke(3));
                g2dRotated.drawRect((int)x, (int)y, size, size);
                g2dRotated.dispose();
            }
            
            g2d.setColor(new Color(255, 255, 255, 60));
            if (isCircle) {
                g2d.fillOval((int)x + size/4, (int)y + size/6, size/3, size/4);
            } else {
                Graphics2D g2dRotated = (Graphics2D) g2d.create();
                g2dRotated.rotate(rotation, x + size/2, y + size/2);
                g2dRotated.setColor(new Color(255, 255, 255, 60));
                g2dRotated.fillRect((int)x + size/4, (int)y + size/6, size/3, size/4);
                g2dRotated.dispose();
            }
        }
    }
}
