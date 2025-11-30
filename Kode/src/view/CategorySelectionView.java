package view;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.*;
import util.AudioManager;
import util.Constants;

public class CategorySelectionView extends JPanel {
    
    private final ScreenManager screenManager;
    private final AudioManager audioManager;
    private JLabel titleLabel;
    private JLabel subtitleLabel;
    private JButton[] categoryButtons;
    private JButton backButton;
    
    private static final String[] CATEGORIES = {
        "Matematika",
        "Bahasa Indonesia", 
        "IPA",
        "IPS",
        "Bahasa Inggris"
    };
    
    private static final String[] CATEGORY_EMOJIS = {
        "🔢",
        "📖",
        "🔬",
        "🌍",
        "🇬🇧"
    };
    
    private static final Color[] CATEGORY_COLORS = {
        Constants.NEO_BLUE,      // Matematika
        Constants.NEO_PINK,      // Bahasa Indonesia
        Constants.NEO_GREEN,     // IPA
        Constants.NEO_YELLOW,    // IPS
        Constants.NEO_PURPLE     // Bahasa Inggris
    };
    
    public CategorySelectionView(ScreenManager screenManager) {
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
        titleLabel = new JLabel("PILIH KATEGORI", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial Black", Font.BOLD, 48));
        titleLabel.setForeground(Color.WHITE);
        
        // Subtitle
        subtitleLabel = new JLabel("Pilih mata pelajaran yang ingin kamu mainkan", SwingConstants.CENTER);
        subtitleLabel.setFont(new Font("Arial", Font.BOLD, 16));
        subtitleLabel.setForeground(new Color(255, 255, 255, 200));
        
        // Category buttons
        categoryButtons = new JButton[CATEGORIES.length];
        for (int i = 0; i < CATEGORIES.length; i++) {
            final int index = i;
            categoryButtons[i] = createCategoryButton(
                CATEGORY_EMOJIS[i] + " " + CATEGORIES[i],
                CATEGORY_COLORS[i]
            );
            categoryButtons[i].addActionListener(e -> {
                audioManager.playSFX("assets/click.wav");
                handleCategorySelection(CATEGORIES[index]);
            });
        }
        
        // Back button
        backButton = createCartoonButton("← KEMBALI", Constants.NEO_RED);
        backButton.addActionListener(e -> {
            audioManager.playSFX("assets/click.wav");
            screenManager.showUsernameInput();
        });
    }
    
    private void setupLayout() {
        add(titleLabel);
        add(subtitleLabel);
        for (JButton button : categoryButtons) {
            add(button);
        }
        add(backButton);
    }
    
    private void layoutComponents() {
        int width = Math.max(getWidth(), Constants.WINDOW_SIZE.width);
        int height = Math.max(getHeight(), Constants.WINDOW_SIZE.height);
        
        // Title
        int titleWidth = 700;
        int titleHeight = 70;
        int titleX = (width - titleWidth) / 2;
        int titleY = Math.max(40, height / 14);
        titleLabel.setBounds(titleX, titleY, titleWidth, titleHeight);
        
        // Subtitle
        int subtitleWidth = 600;
        int subtitleHeight = 25;
        int subtitleX = (width - subtitleWidth) / 2;
        int subtitleY = titleY + titleHeight + 5;
        subtitleLabel.setBounds(subtitleX, subtitleY, subtitleWidth, subtitleHeight);
        
        // Category buttons - 2 columns layout
        int buttonWidth = 300;
        int buttonHeight = 90;
        int horizontalSpacing = 40;
        int verticalSpacing = 25;
        int totalButtonsWidth = (buttonWidth * 2) + horizontalSpacing;
        int startX = (width - totalButtonsWidth) / 2;
        int startY = subtitleY + subtitleHeight + 60;
        
        // First row - 2 buttons
        categoryButtons[0].setBounds(startX, startY, buttonWidth, buttonHeight);
        categoryButtons[1].setBounds(startX + buttonWidth + horizontalSpacing, startY, buttonWidth, buttonHeight);
        
        // Second row - 2 buttons
        int row2Y = startY + buttonHeight + verticalSpacing;
        categoryButtons[2].setBounds(startX, row2Y, buttonWidth, buttonHeight);
        categoryButtons[3].setBounds(startX + buttonWidth + horizontalSpacing, row2Y, buttonWidth, buttonHeight);
        
        // Third row - 1 button centered
        int row3Y = row2Y + buttonHeight + verticalSpacing;
        int centerButtonX = (width - buttonWidth) / 2;
        categoryButtons[4].setBounds(centerButtonX, row3Y, buttonWidth, buttonHeight);
        
        // Back button
        int backWidth = 200;
        int backHeight = 60;
        int backX = 50;
        int backY = height - 100;
        backButton.setBounds(backX, backY, backWidth, backHeight);
    }
    
    private void handleCategorySelection(String category) {
        screenManager.setSelectedCategory(category);
        screenManager.showDifficultySelection();
    }
    
    private JButton createCategoryButton(String text, Color bgColor) {
        JButton button = new JButton(text) {
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
                
                // Text
                g2d.setFont(getFont());
                FontMetrics fm = g2d.getFontMetrics();
                int x = (getWidth() - Constants.SHADOW_OFFSET - fm.stringWidth(getText())) / 2;
                int y = ((getHeight() - Constants.SHADOW_OFFSET - fm.getHeight()) / 2) + fm.getAscent();
                
                // Text shadow
                int textShadowAlpha = pressed ? 50 : 100;
                g2d.setColor(new Color(0, 0, 0, textShadowAlpha));
                g2d.drawString(getText(), x + 2, y + 2);
                
                // Text
                int textAlpha = pressed ? 180 : 255;
                g2d.setColor(new Color(255, 255, 255, textAlpha));
                g2d.drawString(getText(), x, y);
                
                g2d.dispose();
            }
        };
        
        button.setFont(new Font("Arial Black", Font.BOLD, 20));
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
