package view;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.*;
import util.AudioManager;
import util.Constants;

public class ResultView extends JPanel {
    
    private final ScreenManager screenManager;
    private final AudioManager audioManager;
    
    private JLabel titleLabel;
    private JLabel scoreLabel;
    private JLabel statsLabel;
    private JLabel accuracyLabel;
    private JLabel messageLabel;
    private JButton leaderboardButton;
    private JButton mainMenuButton;
    private JPanel resultPanel;
    
    private int finalScore;
    private int correctAnswers;
    private int incorrectAnswers;
    
    public ResultView(ScreenManager screenManager) {
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
        
        // Floating rays
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
        titleLabel = new JLabel("QUIZ SELESAI!", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial Black", Font.BOLD, 52));
        titleLabel.setForeground(Color.WHITE);
        
        // Result panel
        resultPanel = createResultPanel();
        
        // Buttons
        leaderboardButton = createCartoonButton("🏆 LIHAT LEADERBOARD", Constants.NEO_YELLOW);
        leaderboardButton.addActionListener(e -> {
            audioManager.playSFX("assets/click.wav");
            screenManager.showLeaderboard();
        });
        
        mainMenuButton = createCartoonButton("🏠 MENU UTAMA", Constants.NEO_BLUE);
        mainMenuButton.addActionListener(e -> {
            audioManager.playSFX("assets/click.wav");
            screenManager.showMainMenu();
        });
    }
    
    private JPanel createResultPanel() {
        JPanel panel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // Shadow
                g2d.setColor(new Color(0, 0, 0, 100));
                g2d.fillRoundRect(8, 8, getWidth() - 16, getHeight() - 16, 30, 30);
                
                // Background
                GradientPaint gradient = new GradientPaint(
                    0, 0, new Color(255, 255, 255, 230),
                    0, getHeight(), new Color(255, 255, 255, 190)
                );
                g2d.setPaint(gradient);
                g2d.fillRoundRect(0, 0, getWidth() - 16, getHeight() - 16, 30, 30);
                
                // Border
                g2d.setColor(Constants.NEO_WHITE);
                g2d.setStroke(new BasicStroke(4));
                g2d.drawRoundRect(2, 2, getWidth() - 20, getHeight() - 20, 26, 26);
            }
        };
        
        panel.setLayout(null);
        panel.setOpaque(false);
        
        // Trophy emoji
        JLabel trophyLabel = new JLabel("🎉", SwingConstants.CENTER);
        trophyLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 80));
        trophyLabel.setBounds(0, 30, 550, 100);
        
        // Score
        scoreLabel = new JLabel("Score: 0", SwingConstants.CENTER);
        scoreLabel.setFont(new Font("Arial Black", Font.BOLD, 42));
        scoreLabel.setForeground(Constants.NEO_GREEN);
        scoreLabel.setBounds(0, 140, 550, 50);
        
        // Stats
        statsLabel = new JLabel("<html><center>Benar: 0 | Salah: 0</center></html>", SwingConstants.CENTER);
        statsLabel.setFont(new Font("Arial", Font.BOLD, 20));
        statsLabel.setForeground(new Color(13, 37, 103));
        statsLabel.setBounds(50, 200, 450, 30);
        
        // Accuracy
        accuracyLabel = new JLabel("Akurasi: 0%", SwingConstants.CENTER);
        accuracyLabel.setFont(new Font("Arial Black", Font.BOLD, 28));
        accuracyLabel.setForeground(Constants.NEO_BLUE);
        accuracyLabel.setBounds(0, 240, 550, 35);
        
        // Message
        messageLabel = new JLabel("<html><center>Luar biasa!</center></html>", SwingConstants.CENTER);
        messageLabel.setFont(new Font("Arial", Font.BOLD | Font.ITALIC, 18));
        messageLabel.setForeground(new Color(13, 37, 103, 180));
        messageLabel.setBounds(50, 290, 450, 40);
        
        panel.add(trophyLabel);
        panel.add(scoreLabel);
        panel.add(statsLabel);
        panel.add(accuracyLabel);
        panel.add(messageLabel);
        
        return panel;
    }
    
    private void setupLayout() {
        add(titleLabel);
        add(resultPanel);
        add(leaderboardButton);
        add(mainMenuButton);
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
        
        // Result panel
        int panelWidth = 550;
        int panelHeight = 360;
        int panelX = (width - panelWidth) / 2;
        int panelY = titleY + titleHeight + 30;
        resultPanel.setBounds(panelX, panelY, panelWidth, panelHeight);
        
        // Buttons
        int buttonWidth = 300;
        int buttonHeight = 70;
        int buttonSpacing = 30;
        int totalButtonWidth = (buttonWidth * 2) + buttonSpacing;
        int buttonY = panelY + panelHeight + 40;
        
        int leaderboardX = (width - totalButtonWidth) / 2;
        leaderboardButton.setBounds(leaderboardX, buttonY, buttonWidth, buttonHeight);
        
        int mainMenuX = leaderboardX + buttonWidth + buttonSpacing;
        mainMenuButton.setBounds(mainMenuX, buttonY, buttonWidth, buttonHeight);
    }
    
    public void updateResult(int score, int correct, int incorrect) {
        this.finalScore = score;
        this.correctAnswers = correct;
        this.incorrectAnswers = incorrect;
        
        int totalQuestions = correct + incorrect;
        double accuracy = totalQuestions > 0 ? (correct * 100.0 / totalQuestions) : 0;
        
        // Update labels
        scoreLabel.setText("Score: " + score);
        statsLabel.setText("<html><center>✅ Benar: " + correct + " | ❌ Salah: " + incorrect + "</center></html>");
        accuracyLabel.setText("Akurasi: " + String.format("%.1f", accuracy) + "%");
        
        // Motivational message based on accuracy
        String message;
        if (accuracy >= 90) {
            message = "🌟 Sempurna! Kamu luar biasa!";
        } else if (accuracy >= 75) {
            message = "💪 Bagus sekali! Pertahankan!";
        } else if (accuracy >= 60) {
            message = "👍 Lumayan! Terus berlatih!";
        } else if (accuracy >= 40) {
            message = "💡 Coba lagi, pasti bisa lebih baik!";
        } else {
            message = "🎯 Jangan menyerah, terus belajar!";
        }
        
        messageLabel.setText("<html><center>" + message + "</center></html>");
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
                    lightColor = new Color(lightColor.getRed(), lightColor.getGreen(), lightColor.getBlue(), 150);
                    darkColor = new Color(darkColor.getRed(), darkColor.getGreen(), darkColor.getBlue(), 150);
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
        
        button.setFont(new Font("Arial Black", Font.BOLD, 18));
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
