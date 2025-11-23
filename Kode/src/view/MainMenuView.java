package view;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import javax.swing.*;
import util.Constants;

public class MainMenuView extends JFrame {
    
    private JPanel mainPanel;
    private JButton startButton;
    private JButton leaderboardButton;
    private JButton exitButton;
    private JToggleButton darkModeToggle;
    private List<FloatingShape> floatingShapes;
    private Timer animationTimer;
    
    public MainMenuView() {
        initializeFrame();
        initializeFloatingShapes();
        createComponents();
        setupLayout();
        startAnimation();
        setVisible(true);
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
                shape.update(mainPanel.getWidth(), mainPanel.getHeight());
            }
            mainPanel.repaint();
        });
        animationTimer.start();
    }

    private void initializeFrame() {
        setTitle("RapidQ - Speed Quiz Game");
        setSize(Constants.WINDOW_SIZE);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);
    }
    
    private void createComponents() {
        mainPanel = new JPanel() {
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
        };
        mainPanel.setLayout(null);
        
        darkModeToggle = createDarkModeToggle();
        
        startButton = createCartoonButton("START QUIZ", Constants.NEO_GREEN);
        leaderboardButton = createCartoonButton("LEADERBOARD", Constants.NEO_PINK);
        exitButton = createCartoonButton("EXIT", Constants.NEO_RED);
    }
    
    private void setupLayout() {
        JLabel titleLabel = new JLabel("RapidQ") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                
                String text = getText();
                g2d.setFont(getFont());
                FontMetrics fm = g2d.getFontMetrics();
                int x = (getWidth() - fm.stringWidth(text)) / 2;
                int y = ((getHeight() - fm.getHeight()) / 2) + fm.getAscent();
                
                for (int i = Constants.TEXT_SHADOW_DEPTH; i > 0; i--) {
                    int alpha = 255 - (i * 25);
                    g2d.setColor(new Color(0, 0, 0, Math.max(50, alpha)));
                    g2d.drawString(text, x + i, y + i);
                }
                
                GradientPaint gradient = new GradientPaint(
                    0, 0, Constants.TEXT_ORANGE_START,
                    0, getHeight(), Constants.TEXT_ORANGE_END
                );
                g2d.setPaint(gradient);
                g2d.drawString(text, x, y);
                
                g2d.setColor(Color.WHITE);
                g2d.setStroke(new BasicStroke(4));
                for (int dx = -1; dx <= 1; dx++) {
                    for (int dy = -1; dy <= 1; dy++) {
                        if (dx != 0 || dy != 0) {
                            g2d.drawString(text, x + dx * 2, y + dy * 2);
                        }
                    }
                }
                
                g2d.setPaint(gradient);
                g2d.drawString(text, x, y);
                
                g2d.dispose();
            }
        };
        titleLabel.setFont(Constants.TITLE_FONT);
        titleLabel.setBounds(200, 100, 600, 100);
        
        JLabel subtitleLabel = new JLabel("SPEED QUIZ CHALLENGE") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                
                String text = getText();
                g2d.setFont(getFont());
                FontMetrics fm = g2d.getFontMetrics();
                int x = (getWidth() - fm.stringWidth(text)) / 2;
                int y = ((getHeight() - fm.getHeight()) / 2) + fm.getAscent();
                
                g2d.setColor(new Color(0, 0, 0, 100));
                g2d.drawString(text, x + 3, y + 3);
                
                GradientPaint gradient = new GradientPaint(
                    0, 0, Constants.TEXT_CYAN_START,
                    0, getHeight(), Constants.TEXT_CYAN_END
                );
                g2d.setPaint(gradient);
                g2d.drawString(text, x, y);
                
                g2d.setColor(Color.WHITE);
                g2d.setStroke(new BasicStroke(3));
                g2d.drawString(text, x, y);
                
                g2d.setPaint(gradient);
                g2d.drawString(text, x, y);
                
                g2d.dispose();
            }
        };
        subtitleLabel.setFont(Constants.SUBTITLE_FONT);
        subtitleLabel.setBounds(250, 210, 500, 50);
        
        startButton.setBounds(350, 320, Constants.BUTTON_WIDTH, Constants.BUTTON_HEIGHT);
        leaderboardButton.setBounds(350, 405, Constants.BUTTON_WIDTH, Constants.BUTTON_HEIGHT);
        exitButton.setBounds(350, 490, Constants.BUTTON_WIDTH, Constants.BUTTON_HEIGHT);
        
        darkModeToggle.setBounds(20, 20, 50, 50);
        
        JLabel versionLabel = new JLabel("v1.0 - 3D Cartoon Edition", SwingConstants.CENTER);
        versionLabel.setFont(new Font("Arial", Font.BOLD, 12));
        versionLabel.setForeground(new Color(255, 255, 255, 180));
        versionLabel.setBounds(400, 640, 200, 20);
        
        mainPanel.add(titleLabel);
        mainPanel.add(subtitleLabel);
        mainPanel.add(startButton);
        mainPanel.add(leaderboardButton);
        mainPanel.add(exitButton);
        mainPanel.add(darkModeToggle);
        mainPanel.add(versionLabel);
        
        add(mainPanel);
    }
    
    private JButton createCartoonButton(String text, Color bgColor) {
        JButton button = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                
                boolean pressed = getModel().isPressed();
                
                g2d.setColor(new Color(0, 0, 0, 100));
                g2d.fillRoundRect(Constants.SHADOW_OFFSET, Constants.SHADOW_OFFSET, 
                    getWidth() - Constants.SHADOW_OFFSET, getHeight() - Constants.SHADOW_OFFSET, 
                    Constants.BORDER_RADIUS, Constants.BORDER_RADIUS);
                
                Color lightColor = bgColor.brighter();
                Color darkColor = bgColor;
                
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
                button.repaint();
            }
            
            @Override
            public void mouseExited(MouseEvent e) {
                button.repaint();
            }
        });
        
        return button;
    }
    
    private JToggleButton createDarkModeToggle() {
        JToggleButton toggle = new JToggleButton("☀️") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                g2d.setColor(new Color(0, 0, 0, 80));
                g2d.fillOval(4, 4, getWidth() - 4, getHeight() - 4);
                
                Color color1 = isSelected() ? new Color(44, 62, 80) : Constants.NEO_YELLOW;
                Color color2 = isSelected() ? new Color(52, 73, 94) : Constants.NEO_ORANGE;
                GradientPaint gradient = new GradientPaint(
                    0, 0, color1,
                    0, getHeight(), color2
                );
                g2d.setPaint(gradient);
                g2d.fillOval(0, 0, getWidth() - 4, getHeight() - 4);
                
                g2d.setColor(Color.WHITE);
                g2d.setStroke(new BasicStroke(3));
                g2d.drawOval(0, 0, getWidth() - 4, getHeight() - 4);
                
                g2d.setColor(new Color(255, 255, 255, 60));
                g2d.fillOval(5, 5, getWidth() - 20, (getHeight() - 10) / 2);
                
                g2d.setFont(getFont());
                FontMetrics fm = g2d.getFontMetrics();
                int x = (getWidth() - 4 - fm.stringWidth(getText())) / 2;
                int y = ((getHeight() - 4 - fm.getHeight()) / 2) + fm.getAscent();
                g2d.drawString(getText(), x, y);
                
                g2d.dispose();
            }
        };
        
        toggle.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 24));
        toggle.setFocusPainted(false);
        toggle.setBorderPainted(false);
        toggle.setContentAreaFilled(false);
        toggle.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        toggle.addActionListener(e -> {
            Constants.toggleDarkMode();
            if (Constants.isDarkMode()) {
                toggle.setText("🌙");
            } else {
                toggle.setText("☀️");
            }
            repaint();
        });
        
        return toggle;
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
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {
                e.printStackTrace();
            }
            new MainMenuView();
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
