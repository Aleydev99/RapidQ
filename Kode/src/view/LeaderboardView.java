package view;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.List;
import javax.imageio.ImageIO;
import javax.swing.*;
import core.Database;
import util.Constants;

public class LeaderboardView extends JPanel {
    
    private final ScreenManager screenManager;
    private JButton backButton;
    private JLabel titleLabel;
    private JPanel leaderboardPanel;
    private JScrollPane scrollPane;
    private Database database;
    private BufferedImage emptyLeaderboardImage;
    private static final int TOP_LIMIT = 10;
    private static final double EMPTY_IMAGE_VERTICAL_RATIO = 0.08; // Semakin kecil semakin mendekati atas
    
    public LeaderboardView(ScreenManager screenManager) {
        this.screenManager = screenManager;
        this.database = Database.getInstance();
        loadEmptyLeaderboardImage();
        setLayout(null);
        setOpaque(false);
        setPreferredSize(Constants.WINDOW_SIZE);
        createComponents();
        setupLayout();
        layoutComponents();
        loadLeaderboard();
        
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
        
        g2d.setColor(new Color(255, 255, 255, 30));
        g2d.setStroke(new BasicStroke(3));
        for (int i = 0; i < 16; i++) {
            double angle = (Math.PI * 2 / 16) * i;
            int x2 = centerX + (int)(Math.cos(angle) * radius);
            int y2 = centerY + (int)(Math.sin(angle) * radius);
            g2d.drawLine(centerX, centerY, x2, y2);
        }
    }
    
    private void loadEmptyLeaderboardImage() {
        try {
            emptyLeaderboardImage = ImageIO.read(new File("assets/belumleaderboard.png"));
        } catch (Exception e) {
            System.err.println("Failed to load empty leaderboard image: " + e.getMessage());
        }
    }
    
    private void createComponents() {
        backButton = createCartoonButton("← KEMBALI", Constants.NEO_BLUE);
        backButton.addActionListener(e -> screenManager.showMainMenu());
        
        titleLabel = new JLabel("LEADERBOARD", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial Black", Font.BOLD, 48));
        titleLabel.setForeground(Color.WHITE);
        
        leaderboardPanel = new JPanel();
        leaderboardPanel.setLayout(new BoxLayout(leaderboardPanel, BoxLayout.Y_AXIS));
        leaderboardPanel.setOpaque(false);
        
        scrollPane = new JScrollPane(leaderboardPanel);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
    }
    
    private void setupLayout() {
        add(titleLabel);
        add(scrollPane);
        add(backButton);
    }
    
    private void layoutComponents() {
        if (titleLabel == null || scrollPane == null || backButton == null) {
            return;
        }
        
        int width = Math.max(getWidth(), Constants.WINDOW_SIZE.width);
        int height = Math.max(getHeight(), Constants.WINDOW_SIZE.height);
        
        int titleWidth = 600;
        int titleHeight = 60;
        int titleX = (width - titleWidth) / 2;
        int titleY = Math.max(30, height / 16);
        titleLabel.setBounds(titleX, titleY, titleWidth, titleHeight);
        
        int scrollWidth = Math.min(850, width - 100);
        int scrollHeight = height - titleY - titleHeight - 150;
        int scrollX = (width - scrollWidth) / 2;
        int scrollY = titleY + titleHeight + 20;
        scrollPane.setBounds(scrollX, scrollY, scrollWidth, scrollHeight);
        
        int backY = Math.min(height - 90, scrollY + scrollHeight + 20);
        backButton.setBounds(Math.max(50, scrollX), backY, 200, 60);
    }
    
    private void loadLeaderboard() {
        leaderboardPanel.removeAll();
        
        try {
            List<Database.LeaderboardEntry> entries = database.getTopLeaderboard(TOP_LIMIT);
            
            if (entries.isEmpty()) {
                JPanel placeholderContainer = new JPanel();
                placeholderContainer.setOpaque(false);
                placeholderContainer.setLayout(new BoxLayout(placeholderContainer, BoxLayout.Y_AXIS));
                placeholderContainer.setAlignmentX(Component.CENTER_ALIGNMENT);
                placeholderContainer.setAlignmentY(Component.CENTER_ALIGNMENT);
                
                for (int i = 1; i <= TOP_LIMIT; i++) {
                    JPanel entryPanel = createEmptyLeaderboardEntry(i);
                    placeholderContainer.add(entryPanel);
                    if (i < TOP_LIMIT) {
                        placeholderContainer.add(Box.createRigidArea(new Dimension(0, 15)));
                    }
                }
                
                JPanel overlayPanel = createEmptyStateOverlay();
                overlayPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
                overlayPanel.setAlignmentY(Component.CENTER_ALIGNMENT);
                
                JPanel layeredContainer = new JPanel();
                layeredContainer.setOpaque(false);
                layeredContainer.setLayout(new OverlayLayout(layeredContainer));
                layeredContainer.setAlignmentX(Component.CENTER_ALIGNMENT);
                layeredContainer.setAlignmentY(Component.CENTER_ALIGNMENT);
                layeredContainer.add(placeholderContainer);
                layeredContainer.add(overlayPanel);
                layeredContainer.setComponentZOrder(overlayPanel, 0);
                layeredContainer.setComponentZOrder(placeholderContainer, 1);
                
                Dimension placeholdersSize = placeholderContainer.getPreferredSize();
                layeredContainer.setPreferredSize(placeholdersSize);
                overlayPanel.setPreferredSize(placeholdersSize);
                overlayPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, placeholdersSize.height));
                placeholderContainer.setMaximumSize(new Dimension(Integer.MAX_VALUE, placeholdersSize.height));
                
                leaderboardPanel.add(layeredContainer);
                leaderboardPanel.add(Box.createRigidArea(new Dimension(0, 15)));
            } else {
                for (Database.LeaderboardEntry entry : entries) {
                    JPanel entryPanel = createLeaderboardEntry(entry);
                    leaderboardPanel.add(entryPanel);
                    leaderboardPanel.add(Box.createRigidArea(new Dimension(0, 15)));
                }
            }
            
        } catch (Exception e) {
            System.err.println("Error loading leaderboard: " + e.getMessage());
            e.printStackTrace();
            
            JPanel errorPanel = createErrorPanel();
            leaderboardPanel.add(errorPanel);
        }
        
        leaderboardPanel.revalidate();
        leaderboardPanel.repaint();
    }
    
    private JPanel createLeaderboardEntry(Database.LeaderboardEntry entry) {
        JPanel panel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                int shadowOffset = 5;
                g2d.setColor(new Color(0, 0, 0, 100));
                g2d.fillRoundRect(shadowOffset, shadowOffset, getWidth() - shadowOffset, getHeight() - shadowOffset, 25, 25);
                
                Color topColor;
                if (entry.getRank() == 1) {
                    topColor = new Color(255, 215, 0, 220);  // Gold
                } else if (entry.getRank() == 2) {
                    topColor = new Color(192, 192, 192, 220); // Silver
                } else if (entry.getRank() == 3) {
                    topColor = new Color(205, 127, 50, 220);  // Bronze
                } else {
                    topColor = new Color(255, 255, 255, 200);
                }
                
                GradientPaint gradient = new GradientPaint(
                    0, 0, topColor,
                    0, getHeight(), new Color(255, 255, 255, 150)
                );
                g2d.setPaint(gradient);
                g2d.fillRoundRect(0, 0, getWidth() - shadowOffset, getHeight() - shadowOffset, 25, 25);
                
                g2d.setColor(new Color(255, 255, 255, 100));
                g2d.setStroke(new BasicStroke(3));
                g2d.drawRoundRect(2, 2, getWidth() - shadowOffset - 4, getHeight() - shadowOffset - 4, 22, 22);
            }
        };
        
        panel.setLayout(null);
        panel.setOpaque(false);
        panel.setPreferredSize(new Dimension(800, 80));
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 80));
        
        // Rank label with medal emoji for top 3
        JLabel rankLabel = new JLabel();
        if (entry.getRank() == 1) {
            rankLabel.setText("🥇");
            rankLabel.setFont(new Font("Arial", Font.PLAIN, 40));
        } else if (entry.getRank() == 2) {
            rankLabel.setText("🥈");
            rankLabel.setFont(new Font("Arial", Font.PLAIN, 40));
        } else if (entry.getRank() == 3) {
            rankLabel.setText("🥉");
            rankLabel.setFont(new Font("Arial", Font.PLAIN, 40));
        } else {
            rankLabel.setText("#" + entry.getRank());
            rankLabel.setFont(new Font("Arial Black", Font.BOLD, 28));
        }
        rankLabel.setForeground(new Color(13, 37, 103));
        rankLabel.setHorizontalAlignment(SwingConstants.CENTER);
        rankLabel.setBounds(15, 15, 60, 50);
        
        // Username
        JLabel usernameLabel = new JLabel(entry.getUsername());
        usernameLabel.setFont(new Font("Arial Black", Font.BOLD, 22));
        usernameLabel.setForeground(new Color(13, 37, 103));
        usernameLabel.setBounds(90, 12, 250, 30);
        
        // Score
        JLabel scoreLabel = new JLabel(entry.getScore() + " pts");
        scoreLabel.setFont(new Font("Arial Black", Font.BOLD, 26));
        scoreLabel.setForeground(Constants.NEO_GREEN);
        scoreLabel.setBounds(370, 10, 150, 35);
        
        // Questions answered
        JLabel questionsLabel = new JLabel(entry.getQuestionsAnswered() + " soal");
        questionsLabel.setFont(new Font("Arial", Font.BOLD, 16));
        questionsLabel.setForeground(new Color(13, 37, 103, 180));
        questionsLabel.setBounds(90, 42, 120, 25);
        
        // Accuracy
        JLabel accuracyLabel = new JLabel(String.format("%.1f%%", entry.getAccuracyPercentage()));
        accuracyLabel.setFont(new Font("Arial Black", Font.BOLD, 20));
        accuracyLabel.setForeground(Constants.NEO_BLUE);
        accuracyLabel.setBounds(540, 15, 100, 30);
        
        // Date
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
        JLabel dateLabel = new JLabel(dateFormat.format(entry.getAchievedAt()));
        dateLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        dateLabel.setForeground(new Color(13, 37, 103, 150));
        dateLabel.setBounds(540, 45, 120, 20);
        
        panel.add(rankLabel);
        panel.add(usernameLabel);
        panel.add(scoreLabel);
        panel.add(questionsLabel);
        panel.add(accuracyLabel);
        panel.add(dateLabel);
        
        return panel;
    }
    
    private JPanel createEmptyLeaderboardEntry(int rank) {
        JPanel panel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                int shadowOffset = 5;
                g2d.setColor(new Color(0, 0, 0, 100));
                g2d.fillRoundRect(shadowOffset, shadowOffset, getWidth() - shadowOffset, getHeight() - shadowOffset, 25, 25);
                
                // Warna gelap untuk empty state
                Color topColor = new Color(100, 100, 100, 150);
                
                GradientPaint gradient = new GradientPaint(
                    0, 0, topColor,
                    0, getHeight(), new Color(80, 80, 80, 120)
                );
                g2d.setPaint(gradient);
                g2d.fillRoundRect(0, 0, getWidth() - shadowOffset, getHeight() - shadowOffset, 25, 25);
                
                g2d.setColor(new Color(255, 255, 255, 50));
                g2d.setStroke(new BasicStroke(3));
                g2d.drawRoundRect(2, 2, getWidth() - shadowOffset - 4, getHeight() - shadowOffset - 4, 22, 22);
            }
        };
        
        panel.setLayout(null);
        panel.setOpaque(false);
        panel.setPreferredSize(new Dimension(800, 80));
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 80));
        
        // Rank label
        JLabel rankLabel = new JLabel("#" + rank);
        rankLabel.setFont(new Font("Arial Black", Font.BOLD, 28));
        rankLabel.setForeground(new Color(150, 150, 150, 120));
        rankLabel.setHorizontalAlignment(SwingConstants.CENTER);
        rankLabel.setBounds(15, 15, 60, 50);
        
        // Username placeholder
        JLabel usernameLabel = new JLabel("--------");
        usernameLabel.setFont(new Font("Arial Black", Font.BOLD, 22));
        usernameLabel.setForeground(new Color(150, 150, 150, 120));
        usernameLabel.setBounds(90, 12, 250, 30);
        
        // Score placeholder
        JLabel scoreLabel = new JLabel("--- pts");
        scoreLabel.setFont(new Font("Arial Black", Font.BOLD, 26));
        scoreLabel.setForeground(new Color(150, 150, 150, 120));
        scoreLabel.setBounds(370, 10, 150, 35);
        
        // Questions placeholder
        JLabel questionsLabel = new JLabel("-- soal");
        questionsLabel.setFont(new Font("Arial", Font.BOLD, 16));
        questionsLabel.setForeground(new Color(150, 150, 150, 100));
        questionsLabel.setBounds(90, 42, 120, 25);
        
        // Accuracy placeholder
        JLabel accuracyLabel = new JLabel("--.-%");
        accuracyLabel.setFont(new Font("Arial Black", Font.BOLD, 20));
        accuracyLabel.setForeground(new Color(150, 150, 150, 120));
        accuracyLabel.setBounds(540, 15, 100, 30);
        
        // Date placeholder
        JLabel dateLabel = new JLabel("--/--/----");
        dateLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        dateLabel.setForeground(new Color(150, 150, 150, 100));
        dateLabel.setBounds(540, 45, 120, 20);
        
        panel.add(rankLabel);
        panel.add(usernameLabel);
        panel.add(scoreLabel);
        panel.add(questionsLabel);
        panel.add(accuracyLabel);
        panel.add(dateLabel);
        
        return panel;
    }
    
    private JPanel createEmptyStateOverlay() {
        JPanel panel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
                
                // Semi-transparent overlay untuk efek gelap
                g2d.setColor(new Color(0, 0, 0, 60));
                g2d.fillRect(0, 0, getWidth(), getHeight());
                
                if (emptyLeaderboardImage != null) {
                    int imgWidth = emptyLeaderboardImage.getWidth();
                    int imgHeight = emptyLeaderboardImage.getHeight();
                    int panelWidth = getWidth();
                    int panelHeight = getHeight();
                    
                    double scaleX = (double) panelWidth / imgWidth;
                    double scaleY = (double) panelHeight / imgHeight;
                    double scale = Math.min(scaleX, scaleY) * 0.6;
                    
                    int scaledWidth = (int) (imgWidth * scale);
                    int scaledHeight = (int) (imgHeight * scale);
                    int x = (panelWidth - scaledWidth) / 2;
                    int y = Math.max(10, (int) (panelHeight * EMPTY_IMAGE_VERTICAL_RATIO));
                    y = Math.min(y, panelHeight - scaledHeight - 10);
                    
                    g2d.drawImage(emptyLeaderboardImage, x, y, scaledWidth, scaledHeight, null);
                }
            }
        };
        
        panel.setLayout(null);
        panel.setOpaque(false);
        panel.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.setAlignmentY(Component.CENTER_ALIGNMENT);
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));
        
        return panel;
    }
    
    private JPanel createErrorPanel() {
        JPanel panel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                g2d.setColor(new Color(0, 0, 0, 80));
                g2d.fillRoundRect(5, 5, getWidth() - 10, getHeight() - 10, 30, 30);
                
                GradientPaint gradient = new GradientPaint(
                    0, 0, new Color(255, 100, 100, 180),
                    0, getHeight(), new Color(255, 150, 150, 120)
                );
                g2d.setPaint(gradient);
                g2d.fillRoundRect(0, 0, getWidth() - 10, getHeight() - 10, 30, 30);
                
                g2d.setColor(new Color(255, 255, 255, 150));
                g2d.setStroke(new BasicStroke(3));
                g2d.drawRoundRect(2, 2, getWidth() - 14, getHeight() - 14, 26, 26);
            }
        };
        
        panel.setLayout(new BorderLayout());
        panel.setOpaque(false);
        panel.setPreferredSize(new Dimension(800, 200));
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 200));
        
        JLabel errorLabel = new JLabel("Gagal memuat leaderboard", SwingConstants.CENTER);
        errorLabel.setFont(new Font("Arial Black", Font.BOLD, 24));
        errorLabel.setForeground(Color.WHITE);
        
        JLabel subLabel = new JLabel("Pastikan database terhubung dengan benar", SwingConstants.CENTER);
        subLabel.setFont(new Font("Arial", Font.PLAIN, 16));
        subLabel.setForeground(new Color(255, 255, 255, 200));
        
        JPanel textPanel = new JPanel();
        textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));
        textPanel.setOpaque(false);
        textPanel.add(Box.createVerticalGlue());
        textPanel.add(errorLabel);
        textPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        textPanel.add(subLabel);
        textPanel.add(Box.createVerticalGlue());
        
        panel.add(textPanel, BorderLayout.CENTER);
        
        return panel;
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
    
    public void refreshLeaderboard() {
        loadLeaderboard();
    }
    
    public JButton getBackButton() {
        return backButton;
    }
}
