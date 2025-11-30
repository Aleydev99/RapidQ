package view;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.*;
import util.AudioManager;
import util.Constants;

public class DifficultySelectionView extends JPanel {
    
    private final ScreenManager screenManager;
    private final AudioManager audioManager;
    private JLabel titleLabel;
    private JLabel subtitleLabel;
    private JLabel categoryLabel;
    private JButton[] difficultyButtons;
    private JButton backButton;
    
    private static final String[] DIFFICULTIES = {
        "MUDAH",
        "SEDANG",
        "SULIT"
    };
    
    private static final String[] DIFFICULTY_DESCRIPTIONS = {
        "Untuk Kelas 1-2 SD",
        "Untuk Kelas 3-4 SD",
        "Untuk Kelas 5-6 SD"
    };
    
    private static final String[] DIFFICULTY_STARS = {
        "⭐",
        "⭐⭐",
        "⭐⭐⭐"
    };
    
    private static final Color[] DIFFICULTY_COLORS = {
        Constants.NEO_GREEN,    // Mudah
        Constants.NEO_YELLOW,   // Sedang
        Constants.NEO_RED       // Sulit
    };
    
    public DifficultySelectionView(ScreenManager screenManager) {
        this.screenManager = screenManager;
        this.audioManager = AudioManager.getInstance();
        setLayout(null);
        setOpaque(false);
        setPreferredSize(Constants.WINDOW_SIZE);
        createComponents();
        setupLayout();
        layoutComponents();
        
        addComponentListener(new java.awt.event.ComponentAdapter() {
            @Override
            public void componentResized(java.awt.event.ComponentEvent e) {
                layoutComponents();
            }
        });
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
        
        // Floating rays effect
        g2d.setColor(new Color(255, 255, 255, 30));
        g2d.setStroke(new BasicStroke(3));
        for (int i = 0; i < 16; i++) {
            double angle = (Math.PI * 2 / 16) * i;
            int x2 = centerX + (int)(Math.cos(angle) * radius);
            int y2 = centerY + (int)(Math.sin(angle) * radius);
            g2d.drawLine(centerX, centerY, x2, y2);
        }
    }
    
    private void createComponents() {
        // Title
        titleLabel = new JLabel("PILIH TINGKAT KESULITAN", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial Black", Font.BOLD, 42));
        titleLabel.setForeground(Color.WHITE);
        
        // Subtitle
        subtitleLabel = new JLabel("Pilih tingkat yang sesuai dengan kemampuanmu", SwingConstants.CENTER);
        subtitleLabel.setFont(new Font("Arial", Font.BOLD, 16));
        subtitleLabel.setForeground(new Color(255, 255, 255, 200));
        
        // Category label - will be updated when shown
        categoryLabel = new JLabel("", SwingConstants.CENTER);
        categoryLabel.setFont(new Font("Arial Black", Font.BOLD, 24));
        categoryLabel.setForeground(Constants.NEO_YELLOW);
        
        // Difficulty buttons
        difficultyButtons = new JButton[DIFFICULTIES.length];
        for (int i = 0; i < DIFFICULTIES.length; i++) {
            final int index = i;
            difficultyButtons[i] = createDifficultyButton(
                DIFFICULTY_STARS[i] + " " + DIFFICULTIES[i],
                DIFFICULTY_DESCRIPTIONS[i],
                DIFFICULTY_COLORS[i]
            );
            difficultyButtons[i].addActionListener(e -> {
                audioManager.playSFX("assets/click.wav");
                handleDifficultySelection(DIFFICULTIES[index].toLowerCase());
            });
        }
        
        // Back button
        backButton = createCartoonButton("← KEMBALI", Constants.NEO_BLUE);
        backButton.addActionListener(e -> {
            audioManager.playSFX("assets/click.wav");
            screenManager.showCategorySelection();
        });
    }
    
    private void setupLayout() {
        add(titleLabel);
        add(subtitleLabel);
        add(categoryLabel);
        for (JButton button : difficultyButtons) {
            add(button);
        }
        add(backButton);
    }
    
    private void layoutComponents() {
        int width = Math.max(getWidth(), Constants.WINDOW_SIZE.width);
        int height = Math.max(getHeight(), Constants.WINDOW_SIZE.height);
        
        // Title
        int titleWidth = 800;
        int titleHeight = 60;
        int titleX = (width - titleWidth) / 2;
        int titleY = Math.max(40, height / 14);
        titleLabel.setBounds(titleX, titleY, titleWidth, titleHeight);
        
        // Subtitle
        int subtitleWidth = 600;
        int subtitleHeight = 25;
        int subtitleX = (width - subtitleWidth) / 2;
        int subtitleY = titleY + titleHeight + 5;
        subtitleLabel.setBounds(subtitleX, subtitleY, subtitleWidth, subtitleHeight);
        
        // Category label
        int categoryWidth = 500;
        int categoryHeight = 35;
        int categoryX = (width - categoryWidth) / 2;
        int categoryY = subtitleY + subtitleHeight + 20;
        categoryLabel.setBounds(categoryX, categoryY, categoryWidth, categoryHeight);
        
        // Difficulty buttons - vertical stack centered
        int buttonWidth = 400;
        int buttonHeight = 110;
        int verticalSpacing = 25;
        int startX = (width - buttonWidth) / 2;
        int startY = categoryY + categoryHeight + 50;
        
        for (int i = 0; i < difficultyButtons.length; i++) {
            int buttonY = startY + (i * (buttonHeight + verticalSpacing));
            difficultyButtons[i].setBounds(startX, buttonY, buttonWidth, buttonHeight);
        }
        
        // Back button
        int backWidth = 200;
        int backHeight = 60;
        int backX = 50;
        int backY = height - 100;
        backButton.setBounds(backX, backY, backWidth, backHeight);
    }
    
    private void handleDifficultySelection(String difficulty) {
        screenManager.setSelectedDifficulty(difficulty);
        screenManager.showQuiz();
    }
    
    public void updateCategoryDisplay() {
        String category = screenManager.getSelectedCategory();
        if (category != null) {
            categoryLabel.setText("📚 " + category);
        }
    }
    
    private JButton createDifficultyButton(String mainText, String description, Color bgColor) {
        JButton button = new JButton() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                
                boolean pressed = getModel().isPressed();
                Boolean isHoveredObj = (Boolean) getClientProperty("isHovered");
                boolean isHovered = isHoveredObj != null && isHoveredObj;
                
                // Shadow
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
                
                // Gradient background
                GradientPaint gradient = new GradientPaint(
                    0, 0, lightColor,
                    0, getHeight() - Constants.SHADOW_OFFSET, darkColor
                );
                g2d.setPaint(gradient);
                g2d.fillRoundRect(0, 0,
                    getWidth() - Constants.SHADOW_OFFSET, getHeight() - Constants.SHADOW_OFFSET,
                    Constants.BORDER_RADIUS, Constants.BORDER_RADIUS);
                
                // Highlight
                int highlightAlpha = pressed ? 40 : 80;
                g2d.setColor(new Color(255, 255, 255, highlightAlpha));
                g2d.fillRoundRect(5, 5,
                    getWidth() - Constants.SHADOW_OFFSET - 10, (getHeight() - Constants.SHADOW_OFFSET) / 2 - 5,
                    Constants.BORDER_RADIUS - 5, Constants.BORDER_RADIUS - 5);
                
                // Border
                int outlineAlpha = pressed ? 150 : 255;
                g2d.setColor(new Color(255, 255, 255, outlineAlpha));
                g2d.setStroke(new BasicStroke(Constants.BORDER_THICKNESS));
                g2d.drawRoundRect(0, 0,
                    getWidth() - Constants.SHADOW_OFFSET, getHeight() - Constants.SHADOW_OFFSET,
                    Constants.BORDER_RADIUS, Constants.BORDER_RADIUS);
                
                // Main text
                g2d.setFont(new Font("Arial Black", Font.BOLD, 28));
                FontMetrics mainFm = g2d.getFontMetrics();
                int mainX = (getWidth() - Constants.SHADOW_OFFSET - mainFm.stringWidth(mainText)) / 2;
                int mainY = ((getHeight() - Constants.SHADOW_OFFSET) / 2) - 10;
                
                // Main text shadow
                int textShadowAlpha = pressed ? 50 : 100;
                g2d.setColor(new Color(0, 0, 0, textShadowAlpha));
                g2d.drawString(mainText, mainX + 2, mainY + 2);
                
                // Main text
                int textAlpha = pressed ? 180 : 255;
                g2d.setColor(new Color(255, 255, 255, textAlpha));
                g2d.drawString(mainText, mainX, mainY);
                
                // Description text
                g2d.setFont(new Font("Arial", Font.BOLD, 16));
                FontMetrics descFm = g2d.getFontMetrics();
                int descX = (getWidth() - Constants.SHADOW_OFFSET - descFm.stringWidth(description)) / 2;
                int descY = mainY + 35;
                
                // Description shadow
                g2d.setColor(new Color(0, 0, 0, textShadowAlpha));
                g2d.drawString(description, descX + 1, descY + 1);
                
                // Description text (slightly transparent)
                int descAlpha = pressed ? 150 : 220;
                g2d.setColor(new Color(255, 255, 255, descAlpha));
                g2d.drawString(description, descX, descY);
                
                g2d.dispose();
            }
        };
        
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
}
