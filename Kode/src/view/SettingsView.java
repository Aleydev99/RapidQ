package view;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import util.AudioManager;
import util.Constants;

public class SettingsView extends JPanel {
    
    private final ScreenManager screenManager;
    private final FullscreenManager fullscreenManager;
    private final AudioManager audioManager;
    private JButton backButton;
    private JButton displayModeButton;
    private JSlider bgmVolumeSlider;
    private JSlider sfxVolumeSlider;
    private JLabel titleLabel;
    private JPanel settingsPanel;
    private boolean isFullscreen = false;
    
    public SettingsView(ScreenManager screenManager) {
        this.screenManager = screenManager;
        this.fullscreenManager = screenManager.getFullscreenManager();
        this.audioManager = AudioManager.getInstance();
        setLayout(null);
        setOpaque(false);
        setPreferredSize(Constants.WINDOW_SIZE);
        createComponents();
        setupLayout();
        layoutComponents();
        syncFullscreenState();
        syncAudioSettings();
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
        
        backButton = createCartoonButton("← KEMBALI", Constants.NEO_BLUE);
        backButton.addActionListener(e -> {
            audioManager.playSFX("assets/click.wav");
            screenManager.showMainMenu();
        });
        
        displayModeButton = createSmallCartoonButton("WINDOWED", Constants.NEO_PURPLE);
        displayModeButton.addActionListener(e -> {
            audioManager.playSFX("assets/click.wav");
            fullscreenManager.toggleFullscreen();
            syncFullscreenState();
        });
        
        bgmVolumeSlider = new JSlider(0, 100, audioManager.getBGMVolumeSliderValue());
        bgmVolumeSlider.setOpaque(false);
        bgmVolumeSlider.setMajorTickSpacing(25);
        bgmVolumeSlider.setMinorTickSpacing(5);
        bgmVolumeSlider.setPaintTicks(true);
        bgmVolumeSlider.setPaintLabels(true);
        bgmVolumeSlider.setForeground(new Color(13, 37, 103));
        bgmVolumeSlider.setFont(new Font("Arial", Font.BOLD, 12));
        
        bgmVolumeSlider.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                int value = bgmVolumeSlider.getValue();
                audioManager.setBGMVolumeFromSlider(value);
            }
        });
        
        sfxVolumeSlider = new JSlider(0, 100, audioManager.getSFXVolumeSliderValue());
        sfxVolumeSlider.setOpaque(false);
        sfxVolumeSlider.setMajorTickSpacing(25);
        sfxVolumeSlider.setMinorTickSpacing(5);
        sfxVolumeSlider.setPaintTicks(true);
        sfxVolumeSlider.setPaintLabels(true);
        sfxVolumeSlider.setForeground(new Color(13, 37, 103));
        sfxVolumeSlider.setFont(new Font("Arial", Font.BOLD, 12));
        
        sfxVolumeSlider.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                int value = sfxVolumeSlider.getValue();
                audioManager.setSFXVolumeFromSlider(value);
            }
        });
    }
    
    private void setupLayout() {
        titleLabel = new JLabel("PENGATURAN", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial Black", Font.BOLD, 48));
        titleLabel.setForeground(Color.WHITE);
        
        settingsPanel = createSettingsPanel();
        
        add(titleLabel);
        add(settingsPanel);
        add(backButton);
    }
    
    private JPanel createSettingsPanel() {
        JPanel panel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                g2d.setColor(new Color(0, 0, 0, 100));
                g2d.fillRoundRect(8, 8, getWidth() - 16, getHeight() - 16, 30, 30);
                
                GradientPaint gradient = new GradientPaint(
                    0, 0, new Color(255, 255, 255, 180),
                    0, getHeight(), new Color(255, 255, 255, 120)
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
        
        JLabel displayLabel = new JLabel("Mode Tampilan:");
        displayLabel.setFont(new Font("Arial Black", Font.BOLD, 20));
        displayLabel.setForeground(new Color(13, 37, 103));
        displayLabel.setBounds(50, 50, 250, 35);
        
        displayModeButton.setBounds(320, 50, 230, 45);
        
        JLabel bgmLabel = new JLabel("Volume Musik (BGM):");
        bgmLabel.setFont(new Font("Arial Black", Font.BOLD, 18));
        bgmLabel.setForeground(new Color(13, 37, 103));
        bgmLabel.setBounds(50, 140, 500, 30);
        
        bgmVolumeSlider.setBounds(70, 180, 460, 60);
        
        JLabel sfxLabel = new JLabel("Volume Sound Effects:");
        sfxLabel.setFont(new Font("Arial Black", Font.BOLD, 18));
        sfxLabel.setForeground(new Color(13, 37, 103));
        sfxLabel.setBounds(50, 260, 500, 30);
        
        sfxVolumeSlider.setBounds(70, 300, 460, 60);
        
        panel.add(displayLabel);
        panel.add(displayModeButton);
        panel.add(bgmLabel);
        panel.add(bgmVolumeSlider);
        panel.add(sfxLabel);
        panel.add(sfxVolumeSlider);
        
        return panel;
    }
    
    private void layoutComponents() {
        if (titleLabel == null || settingsPanel == null || backButton == null) {
            return;
        }
        int width = Math.max(getWidth(), Constants.WINDOW_SIZE.width);
        int height = Math.max(getHeight(), Constants.WINDOW_SIZE.height);
        int titleWidth = 500;
        int titleHeight = 60;
        int titleX = (width - titleWidth) / 2;
        int titleY = Math.max(40, height / 14);
        titleLabel.setBounds(titleX, titleY, titleWidth, titleHeight);
        
        int panelWidth = 600;
        int panelHeight = 400;
        int panelX = (width - panelWidth) / 2;
        int panelY = titleY + titleHeight + 30;
        settingsPanel.setBounds(panelX, panelY, panelWidth, panelHeight);
        
        int backY = Math.min(height - 100, panelY + panelHeight + 40);
        backButton.setBounds(Math.max(50, panelX), backY, 200, 60);
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
    
    private JButton createSmallCartoonButton(String text, Color bgColor) {
        JButton button = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                
                boolean pressed = getModel().isPressed();
                Boolean isHoveredObj = (Boolean) getClientProperty("isHovered");
                boolean isHovered = isHoveredObj != null && isHoveredObj;
                
                int shadowOffset = 4;
                int borderRadius = 20;
                
                g2d.setColor(new Color(0, 0, 0, 100));
                g2d.fillRoundRect(shadowOffset, shadowOffset, 
                    getWidth() - shadowOffset, getHeight() - shadowOffset, 
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
                    0, getHeight() - shadowOffset, darkColor
                );
                g2d.setPaint(gradient);
                g2d.fillRoundRect(0, 0, 
                    getWidth() - shadowOffset, getHeight() - shadowOffset,
                    borderRadius, borderRadius);
                
                int highlightAlpha = pressed ? 40 : 80;
                g2d.setColor(new Color(255, 255, 255, highlightAlpha));
                g2d.fillRoundRect(3, 3, 
                    getWidth() - shadowOffset - 6, (getHeight() - shadowOffset) / 2 - 3,
                    borderRadius - 5, borderRadius - 5);
                
                int outlineAlpha = pressed ? 150 : 255;
                g2d.setColor(new Color(255, 255, 255, outlineAlpha));
                g2d.setStroke(new BasicStroke(3));
                g2d.drawRoundRect(1, 1, 
                    getWidth() - shadowOffset - 2, getHeight() - shadowOffset - 2,
                    borderRadius, borderRadius);
                
                g2d.setFont(getFont());
                FontMetrics fm = g2d.getFontMetrics();
                int x = (getWidth() - shadowOffset - fm.stringWidth(getText())) / 2;
                int y = ((getHeight() - shadowOffset - fm.getHeight()) / 2) + fm.getAscent();
                
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
    
    public final void syncFullscreenState() {
        isFullscreen = fullscreenManager.isFullscreen();
        displayModeButton.setText(isFullscreen ? "FULLSCREEN" : "WINDOWED");
    }
    
    public final void syncAudioSettings() {
        bgmVolumeSlider.setValue(audioManager.getBGMVolumeSliderValue());
        sfxVolumeSlider.setValue(audioManager.getSFXVolumeSliderValue());
    }
    
    public JButton getBackButton() {
        return backButton;
    }
    
    public JButton getDisplayModeButton() {
        return displayModeButton;
    }
    
    public boolean isFullscreen() {
        return isFullscreen;
    }
    
    public JSlider getBgmVolumeSlider() {
        return bgmVolumeSlider;
    }
    
    public JSlider getSfxVolumeSlider() {
        return sfxVolumeSlider;
    }
    
}
