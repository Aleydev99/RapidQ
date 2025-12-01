package view;

import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.*;
import util.AudioManager;
import util.Constants;

public class UsernameInputView extends JPanel {
    
    private final ScreenManager screenManager;
    private final AudioManager audioManager;
    private JTextField nameTextField;
    private JButton startButton;
    private JButton backButton;
    private JLabel titleLabel;
    private JLabel instructionLabel;
    private JLabel errorLabel;
    private JPanel inputPanel;
    
    public UsernameInputView(ScreenManager screenManager) {
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
        titleLabel = new JLabel("MASUKKAN NAMAMU", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial Black", Font.BOLD, 42));
        titleLabel.setForeground(Color.WHITE);
        
        instructionLabel = new JLabel("Nama akan ditampilkan di Leaderboard", SwingConstants.CENTER);
        instructionLabel.setFont(new Font("Arial", Font.BOLD, 16));
        instructionLabel.setForeground(new Color(255, 255, 255, 200));
        
        inputPanel = createInputPanel();
        
        errorLabel = new JLabel("", SwingConstants.CENTER);
        errorLabel.setFont(new Font("Arial", Font.BOLD, 14));
        errorLabel.setForeground(Constants.NEO_RED);
        errorLabel.setVisible(false);
        
        startButton = createCartoonButton("MULAI!", Constants.NEO_GREEN);
        startButton.addActionListener(e -> handleStartButton());
        
        backButton = createCartoonButton("← KEMBALI", Constants.NEO_BLUE);
        backButton.addActionListener(e -> {
            audioManager.playSFX("assets/click.wav");
            screenManager.showMainMenu();
        });
    }
    
    private JPanel createInputPanel() {
        JPanel panel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setColor(new Color(0, 0, 0, 100));
                g2d.fillRoundRect(8, 8, getWidth() - 16, getHeight() - 16, 30, 30);
                
                GradientPaint gradient = new GradientPaint(
                    0, 0, new Color(255, 255, 255, 200),
                    0, getHeight(), new Color(255, 255, 255, 150)
                );
                g2d.setPaint(gradient);
                g2d.fillRoundRect(0, 0, getWidth() - 16, getHeight() - 16, 30, 30);
                
                g2d.setColor(Constants.NEO_WHITE);
                g2d.setStroke(new BasicStroke(4));
                g2d.drawRoundRect(2, 2, getWidth() - 20, getHeight() - 20, 26, 26);
            }
        };
        
        panel.setLayout(null);
        panel.setOpaque(false);
        
        JLabel iconLabel = new JLabel();
        iconLabel.setHorizontalAlignment(SwingConstants.CENTER);
        try {
            ImageIcon originalIcon = new ImageIcon("assets/profil.png");
            Image scaledImage = originalIcon.getImage().getScaledInstance(80, 80, Image.SCALE_SMOOTH);
            iconLabel.setIcon(new ImageIcon(scaledImage));
        } catch (Exception e) {
            iconLabel.setText("👤");
            iconLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 60));
        }
        iconLabel.setBounds(0, 30, 500, 80);
        
        JLabel nameLabel = new JLabel("Nama:", SwingConstants.CENTER);
        nameLabel.setFont(new Font("Arial Black", Font.BOLD, 20));
        nameLabel.setForeground(new Color(13, 37, 103));
        nameLabel.setBounds(50, 130, 400, 30);
        
        nameTextField = new JTextField();
        nameTextField.setFont(new Font("Arial", Font.BOLD, 24));
        nameTextField.setHorizontalAlignment(JTextField.CENTER);
        nameTextField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(13, 37, 103), 3, true),
            BorderFactory.createEmptyBorder(10, 15, 10, 15)
        ));
        nameTextField.setBounds(75, 170, 350, 55);
        
        JLabel limitLabel = new JLabel("Min. 3 karakter, Max. 20 karakter", SwingConstants.CENTER);
        limitLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        limitLabel.setForeground(new Color(13, 37, 103, 150));
        limitLabel.setBounds(50, 235, 400, 20);
        
        nameTextField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    handleStartButton();
                }
                if (errorLabel.isVisible()) {
                    errorLabel.setVisible(false);
                }
            }
        });
        
        panel.add(iconLabel);
        panel.add(nameLabel);
        panel.add(nameTextField);
        panel.add(limitLabel);
        
        return panel;
    }
    
    private void setupLayout() {
        add(titleLabel);
        add(instructionLabel);
        add(inputPanel);
        add(errorLabel);
        add(startButton);
        add(backButton);
    }
    
    private void layoutComponents() {
        int width = Math.max(getWidth(), Constants.WINDOW_SIZE.width);
        int height = Math.max(getHeight(), Constants.WINDOW_SIZE.height);
        
        int titleWidth = 600;
        int titleHeight = 60;
        int titleX = (width - titleWidth) / 2;
        int titleY = Math.max(50, height / 12);
        titleLabel.setBounds(titleX, titleY, titleWidth, titleHeight);
        
        int instrWidth = 500;
        int instrHeight = 25;
        int instrX = (width - instrWidth) / 2;
        int instrY = titleY + titleHeight + 10;
        instructionLabel.setBounds(instrX, instrY, instrWidth, instrHeight);
        
        int panelWidth = 500;
        int panelHeight = 280;
        int panelX = (width - panelWidth) / 2;
        int panelY = instrY + instrHeight + 40;
        inputPanel.setBounds(panelX, panelY, panelWidth, panelHeight);
        
        int errorWidth = 400;
        int errorHeight = 25;
        int errorX = (width - errorWidth) / 2;
        int errorY = panelY + panelHeight + 10;
        errorLabel.setBounds(errorX, errorY, errorWidth, errorHeight);
        
        int buttonWidth = 220;
        int buttonHeight = 70;
        int buttonSpacing = 30;
        int totalButtonWidth = (buttonWidth * 2) + buttonSpacing;
        int buttonY = errorY + errorHeight + 30;
        
        int backX = (width - totalButtonWidth) / 2;
        backButton.setBounds(backX, buttonY, buttonWidth, buttonHeight);
        
        int startX = backX + buttonWidth + buttonSpacing;
        startButton.setBounds(startX, buttonY, buttonWidth, buttonHeight);
    }
    
    private void handleStartButton() {
        String username = nameTextField.getText().trim();
        
        if (username.isEmpty()) {
            showError("Nama tidak boleh kosong!");
            audioManager.playSFX("assets/click.wav");
            return;
        }
        
        if (username.length() < 3) {
            showError("Nama minimal 3 karakter!");
            audioManager.playSFX("assets/click.wav");
            return;
        }
        
        if (username.length() > 20) {
            showError("Nama maksimal 20 karakter!");
            audioManager.playSFX("assets/click.wav");
            return;
        }
        
        audioManager.playSFX("assets/click.wav");
        screenManager.setPlayerName(username);
        screenManager.showCategorySelection();
    }
    
    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
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
    
    public void resetForm() {
        nameTextField.setText("");
        errorLabel.setVisible(false);
        nameTextField.requestFocus();
    }
}
