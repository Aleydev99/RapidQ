package view;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;
import core.Database;
import util.AudioManager;
import util.Constants;

public class QuizView extends JPanel {
    
    private final ScreenManager screenManager;
    private final AudioManager audioManager;
    
    // UI Components
    private JLabel timerLabel;
    private JLabel scoreLabel;
    private JLabel progressLabel;
    private JLabel questionLabel;
    private JButton[] optionButtons;
    private JButton skipButton;
    private JButton fiftyFiftyButton;
    private JButton hintButton;
    private JPanel questionPanel;
    private JLabel hintLabel;
    
    // Quiz state
    private List<Database.Question> questions;
    private int currentQuestionIndex = 0;
    private int score = 0;
    private int correctAnswers = 0;
    private int incorrectAnswers = 0;
    private int timeRemaining = 60;
    private boolean quizActive = false;
    private Database.Question nextQuestion = null;
    
    // Lifelines
    private boolean fiftyFiftyUsed = false;
    private boolean hintUsed = false;
    
    // Threads
    private Thread timerThread;
    private Thread animationThread;
    private Thread preloadThread;
    
    // Animation state
    private boolean animatingFiftyFifty = false;
    private int[] fadingOptions = null;
    private float fadeAlpha = 1.0f;
    
    // Countdown state
    private boolean showingCountdown = false;
    private int countdownNumber = 3;
    private JPanel countdownOverlay;
    
    // Quiz session
    private int quizSessionId = -1;
    
    public QuizView(ScreenManager screenManager) {
        this.screenManager = screenManager;
        this.audioManager = AudioManager.getInstance();
        setLayout(null);
        setOpaque(false);
        setPreferredSize(Constants.WINDOW_SIZE);
        createComponents();
        createCountdownOverlay();
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
        timerLabel = new JLabel("⏱️ 60", SwingConstants.CENTER);
        timerLabel.setFont(new Font("Arial Black", Font.BOLD, 32));
        timerLabel.setForeground(Constants.NEO_GREEN);
        
        scoreLabel = new JLabel("Score: 0", SwingConstants.CENTER);
        scoreLabel.setFont(new Font("Arial Black", Font.BOLD, 24));
        scoreLabel.setForeground(Constants.NEO_YELLOW);
        
        progressLabel = new JLabel("", SwingConstants.CENTER);
        progressLabel.setFont(new Font("Arial", Font.BOLD, 16));
        progressLabel.setForeground(new Color(255, 255, 255, 200));
        
        questionPanel = createQuestionPanel();
        
        optionButtons = new JButton[4];
        String[] optionLabels = {"A", "B", "C", "D"};
        for (int i = 0; i < 4; i++) {
            final int index = i;
            optionButtons[i] = createOptionButton(optionLabels[i]);
            optionButtons[i].addActionListener(e -> handleAnswerSelection(index));
        }
        
        skipButton = createLifelineButton("SKIP", Constants.NEO_BLUE);
        skipButton.addActionListener(e -> handleSkip());
        
        fiftyFiftyButton = createLifelineButton("50:50", Constants.NEO_PURPLE);
        fiftyFiftyButton.addActionListener(e -> handleFiftyFifty());
        
        hintButton = createLifelineButton("HINT", Constants.NEO_ORANGE);
        hintButton.addActionListener(e -> handleHint());
    }
    
    private void createCountdownOverlay() {
        countdownOverlay = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                
                g2d.setColor(new Color(0, 0, 0, 180));
                g2d.fillRect(0, 0, getWidth(), getHeight());
                
                String text = String.valueOf(countdownNumber);
                Font font = new Font("Arial Black", Font.BOLD, 200);
                g2d.setFont(font);
                FontMetrics fm = g2d.getFontMetrics();
                int textWidth = fm.stringWidth(text);
                int textHeight = fm.getHeight();
                int x = (getWidth() - textWidth) / 2;
                int y = (getHeight() - textHeight) / 2 + fm.getAscent();
                
                g2d.setColor(new Color(0, 0, 0, 150));
                g2d.drawString(text, x + 8, y + 8);
                
                g2d.setColor(Constants.NEO_YELLOW);
                g2d.setStroke(new BasicStroke(8));
                for (int i = -3; i <= 3; i++) {
                    for (int j = -3; j <= 3; j++) {
                        if (i != 0 || j != 0) {
                            g2d.drawString(text, x + i, y + j);
                        }
                    }
                }
                
                g2d.setColor(Color.WHITE);
                g2d.drawString(text, x, y);
                
                String readyText = "Bersiap...";
                Font smallFont = new Font("Arial Black", Font.BOLD, 36);
                g2d.setFont(smallFont);
                FontMetrics smallFm = g2d.getFontMetrics();
                int readyWidth = smallFm.stringWidth(readyText);
                int readyX = (getWidth() - readyWidth) / 2;
                int readyY = y + 120;
                
                g2d.setColor(new Color(0, 0, 0, 150));
                g2d.drawString(readyText, readyX + 3, readyY + 3);
                
                g2d.setColor(Constants.NEO_PINK);
                g2d.drawString(readyText, readyX, readyY);
            }
        };
        
        countdownOverlay.setOpaque(false);
        countdownOverlay.setVisible(false);
        countdownOverlay.setBounds(0, 0, Constants.WINDOW_SIZE.width, Constants.WINDOW_SIZE.height);
        add(countdownOverlay);
        setComponentZOrder(countdownOverlay, 0);
    }
    
    private JPanel createQuestionPanel() {
        JPanel panel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                g2d.setColor(new Color(0, 0, 0, 100));
                g2d.fillRoundRect(8, 8, getWidth() - 16, getHeight() - 16, 30, 30);
                
                GradientPaint gradient = new GradientPaint(
                    0, 0, new Color(255, 255, 255, 220),
                    0, getHeight(), new Color(255, 255, 255, 180)
                );
                g2d.setPaint(gradient);
                g2d.fillRoundRect(0, 0, getWidth() - 16, getHeight() - 16, 30, 30);
                
                g2d.setColor(Constants.NEO_WHITE);
                g2d.setStroke(new BasicStroke(4));
                g2d.drawRoundRect(2, 2, getWidth() - 20, getHeight() - 20, 26, 26);
            }
        };
        
        panel.setLayout(new BorderLayout());
        panel.setOpaque(false);
        
        questionLabel = new JLabel("<html><center>Loading question...</center></html>", SwingConstants.CENTER);
        questionLabel.setFont(new Font("Arial", Font.BOLD, 20));
        questionLabel.setForeground(new Color(13, 37, 103));
        questionLabel.setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));
        
        hintLabel = new JLabel("", SwingConstants.CENTER);
        hintLabel.setFont(new Font("Arial", Font.BOLD | Font.ITALIC, 16));
        hintLabel.setForeground(Constants.NEO_ORANGE);
        hintLabel.setVisible(false);
        hintLabel.setBorder(BorderFactory.createEmptyBorder(5, 20, 10, 20));
        
        panel.add(questionLabel, BorderLayout.CENTER);
        panel.add(hintLabel, BorderLayout.SOUTH);
        
        return panel;
    }
    
    private void setupLayout() {
        add(timerLabel);
        add(scoreLabel);
        add(progressLabel);
        add(questionPanel);
        for (JButton button : optionButtons) {
            add(button);
        }
        add(skipButton);
        add(fiftyFiftyButton);
        add(hintButton);
    }
    
    private void layoutComponents() {
        int width = Math.max(getWidth(), Constants.WINDOW_SIZE.width);
        int height = Math.max(getHeight(), Constants.WINDOW_SIZE.height);
        
        if (countdownOverlay != null) {
            countdownOverlay.setBounds(0, 0, width, height);
        }
        
        timerLabel.setBounds(40, 30, 120, 50);
        scoreLabel.setBounds(width - 200, 30, 160, 40);
        progressLabel.setBounds((width - 200) / 2, 90, 200, 25);
        
        int panelWidth = 700;
        int panelHeight = 180;
        int panelX = (width - panelWidth) / 2;
        int panelY = 130;

        questionPanel.setBounds(panelX, panelY, panelWidth, panelHeight);
        
        int buttonWidth = 320;
        int buttonHeight = 70;
        int horizontalSpacing = 30;
        int verticalSpacing = 20;
        int totalButtonsWidth = (buttonWidth * 2) + horizontalSpacing;
        int startX = (width - totalButtonsWidth) / 2;
        int startY = panelY + panelHeight + 30;
        
        optionButtons[0].setBounds(startX, startY, buttonWidth, buttonHeight);
        optionButtons[1].setBounds(startX + buttonWidth + horizontalSpacing, startY, buttonWidth, buttonHeight);
        
        int row2Y = startY + buttonHeight + verticalSpacing;
        optionButtons[2].setBounds(startX, row2Y, buttonWidth, buttonHeight);
        optionButtons[3].setBounds(startX + buttonWidth + horizontalSpacing, row2Y, buttonWidth, buttonHeight);
        
        int lifelineWidth = 170;
        int lifelineHeight = 55;
        int lifelineY = row2Y + buttonHeight + 30;
        int lifelineSpacing = 20;
        int totalLifelineWidth = (lifelineWidth * 3) + (lifelineSpacing * 2);
        int lifelineStartX = (width - totalLifelineWidth) / 2;
        
        fiftyFiftyButton.setBounds(lifelineStartX, lifelineY, lifelineWidth, lifelineHeight);
        hintButton.setBounds(lifelineStartX + lifelineWidth + lifelineSpacing, lifelineY, lifelineWidth, lifelineHeight);
        skipButton.setBounds(lifelineStartX + (lifelineWidth + lifelineSpacing) * 2, lifelineY, lifelineWidth, lifelineHeight);
    }

    // LANJUTAN QuizView.java - BAGIAN 2
    
    public void startQuiz() {
        currentQuestionIndex = 0;
        score = 0;
        correctAnswers = 0;
        incorrectAnswers = 0;
        timeRemaining = 60;
        fiftyFiftyUsed = false;
        hintUsed = false;
        quizActive = true;
        
        String category = screenManager.getSelectedCategory();
        String difficulty = screenManager.getSelectedDifficulty();
        
        Database db = Database.getInstance();
        questions = db.getRandomQuestions(category, difficulty, 100);
        
        if (questions == null || questions.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                "Tidak ada soal tersedia untuk kategori dan tingkat ini!",
                "Error",
                JOptionPane.ERROR_MESSAGE);
            screenManager.showMainMenu();
            return;
        }
        
        System.out.println("Loaded " + questions.size() + " questions for quiz session");
        
        String playerName = screenManager.getPlayerName();
        quizSessionId = db.createSession(playerName, category, difficulty);
        if (quizSessionId == -1) {
            quizActive = false;
            JOptionPane.showMessageDialog(this,
                "Gagal membuat sesi permainan. Periksa koneksi database kamu ya!",
                "Database Error",
                JOptionPane.ERROR_MESSAGE);
            screenManager.showMainMenu();
            return;
        }
        
        startCountdown();
    }
    
    private void startCountdown() {
        showingCountdown = true;
        countdownNumber = 3;
        countdownOverlay.setVisible(true);
        
        for (JButton btn : optionButtons) {
            btn.setEnabled(false);
        }
        skipButton.setEnabled(false);
        fiftyFiftyButton.setEnabled(false);
        hintButton.setEnabled(false);
        
        Thread countdownThread = new Thread(() -> {
            try {
                for (int i = 3; i >= 1; i--) {
                    final int count = i;
                    SwingUtilities.invokeLater(() -> {
                        countdownNumber = count;
                        countdownOverlay.repaint();
                        audioManager.playSFX("assets/click.wav");
                    });
                    Thread.sleep(1000);
                }
                
                SwingUtilities.invokeLater(() -> {
                    showingCountdown = false;
                    countdownOverlay.setVisible(false);
                    
                    for (JButton btn : optionButtons) {
                        btn.setEnabled(true);
                    }
                    skipButton.setEnabled(true);
                    fiftyFiftyButton.setEnabled(!fiftyFiftyUsed);
                    hintButton.setEnabled(!hintUsed);
                    
                    startTimerThread();
                    startPreloadThread();
                    displayQuestion();
                });
            } catch (InterruptedException e) {
            }
        });
        countdownThread.start();
    }
    
    private void startTimerThread() {
        if (timerThread != null && timerThread.isAlive()) {
            timerThread.interrupt();
        }
        
        timerThread = new Thread(() -> {
            try {
                while (quizActive && timeRemaining > 0) {
                    Thread.sleep(1000);
                    timeRemaining--;
                    
                    SwingUtilities.invokeLater(() -> {
                        updateTimerDisplay();
                    });
                }
                
                if (quizActive) {
                    SwingUtilities.invokeLater(() -> {
                        endQuiz();
                    });
                }
            } catch (InterruptedException e) {
            }
        });
        timerThread.start();
    }
    
    private void startPreloadThread() {
        if (preloadThread != null && preloadThread.isAlive()) {
            preloadThread.interrupt();
        }
        
        preloadThread = new Thread(() -> {
            try {
                while (quizActive && currentQuestionIndex < questions.size() - 1) {
                    int nextIndex = currentQuestionIndex + 1;
                    if (nextIndex < questions.size()) {
                        nextQuestion = questions.get(nextIndex);
                    }
                    Thread.sleep(500);
                }
            } catch (InterruptedException e) {
            }
        });
        preloadThread.start();
    }
    
    private void updateTimerDisplay() {
        timerLabel.setText("⏱️ " + timeRemaining);
        
        if (timeRemaining <= 10) {
            timerLabel.setForeground(Constants.NEO_RED);
        } else if (timeRemaining <= 30) {
            timerLabel.setForeground(Constants.NEO_YELLOW);
        } else {
            timerLabel.setForeground(Constants.NEO_GREEN);
        }
    }
    
    private void displayQuestion() {
        if (currentQuestionIndex >= questions.size()) {
            endQuiz();
            return;
        }
        
        Database.Question question = questions.get(currentQuestionIndex);
        
        progressLabel.setText("Soal " + (currentQuestionIndex + 1));
        questionLabel.setText("<html><center>" + question.questionText + "</center></html>");
        
        optionButtons[0].setText("A. " + question.optionA);
        optionButtons[1].setText("B. " + question.optionB);
        optionButtons[2].setText("C. " + question.optionC);
        optionButtons[3].setText("D. " + question.optionD);
        
        for (JButton button : optionButtons) {
            button.setEnabled(true);
            button.putClientProperty("fadeAlpha", 1.0f);
            button.putClientProperty("isHighlighted", false);
        }
        
        hintLabel.setVisible(false);
        hintLabel.setText("");
        
        skipButton.setEnabled(true);
        fiftyFiftyButton.setEnabled(!fiftyFiftyUsed);
        hintButton.setEnabled(!hintUsed);
        
        repaint();
    }
    
    private void handleAnswerSelection(int optionIndex) {
        if (!quizActive || animatingFiftyFifty) return;
        
        audioManager.playSFX("assets/click.wav");
        
        Database.Question question = questions.get(currentQuestionIndex);
        String selectedAnswer = String.valueOf((char)('A' + optionIndex));
        boolean isCorrect = selectedAnswer.equals(question.correctAnswer);
        
        if (isCorrect) {
            correctAnswers++;
            int points = 10;
            String diff = question.difficulty.toUpperCase();
            if (diff.equals("EASY") || diff.equals("MUDAH")) {
                points = 10;
            } else if (diff.equals("MEDIUM") || diff.equals("SEDANG")) {
                points = 15;
            } else if (diff.equals("HARD") || diff.equals("SULIT")) {
                points = 25;
            }
            score += points;
            scoreLabel.setText("Score: " + score);
        } else {
            incorrectAnswers++;
        }
        
        saveAnswerAsync(question.id, selectedAnswer, isCorrect);
        
        currentQuestionIndex++;
        
        if (currentQuestionIndex < questions.size()) {
            displayQuestion();
        } else {
            endQuiz();
        }
    }
    
    private void handleSkip() {
        if (!quizActive || animatingFiftyFifty) return;
        
        audioManager.playSFX("assets/click.wav");
        
        Database.Question question = questions.get(currentQuestionIndex);
        saveAnswerAsync(question.id, "SKIP", false);
        
        incorrectAnswers++;
        currentQuestionIndex++;
        
        if (currentQuestionIndex < questions.size()) {
            displayQuestion();
        } else {
            endQuiz();
        }
    }
    
    private void handleFiftyFifty() {
        if (!quizActive || fiftyFiftyUsed || animatingFiftyFifty) return;
        
        audioManager.playSFX("assets/click.wav");
        fiftyFiftyUsed = true;
        fiftyFiftyButton.setEnabled(false);
        
        Database.Question question = questions.get(currentQuestionIndex);
        int correctIndex = question.correctAnswer.charAt(0) - 'A';
        
        List<Integer> incorrectIndices = new ArrayList<>();
        for (int i = 0; i < 4; i++) {
            if (i != correctIndex) {
                incorrectIndices.add(i);
            }
        }
        
        fadingOptions = new int[2];
        fadingOptions[0] = incorrectIndices.get(0);
        fadingOptions[1] = incorrectIndices.get(1);
        
        startFiftyFiftyAnimation();
    }
    
    private void handleHint() {
        if (!quizActive || hintUsed || animatingFiftyFifty) return;
        
        audioManager.playSFX("assets/click.wav");
        hintUsed = true;
        hintButton.setEnabled(false);
        
        Database.Question question = questions.get(currentQuestionIndex);
        char correctAnswer = question.correctAnswer.charAt(0);
        
        int correctIndex = correctAnswer - 'A';
        optionButtons[correctIndex].putClientProperty("isHighlighted", true);
        optionButtons[correctIndex].repaint();
    }
    
    private void startFiftyFiftyAnimation() {
        animatingFiftyFifty = true;
        fadeAlpha = 1.0f;
        
        if (animationThread != null && animationThread.isAlive()) {
            animationThread.interrupt();
        }
        
        animationThread = new Thread(() -> {
            try {
                while (fadeAlpha > 0) {
                    Thread.sleep(50);
                    fadeAlpha -= 0.05f;
                    
                    SwingUtilities.invokeLater(() -> {
                        for (int index : fadingOptions) {
                            optionButtons[index].putClientProperty("fadeAlpha", Math.max(0, fadeAlpha));
                            optionButtons[index].repaint();
                        }
                    });
                }
                
                SwingUtilities.invokeLater(() -> {
                    for (int index : fadingOptions) {
                        optionButtons[index].setEnabled(false);
                    }
                    animatingFiftyFifty = false;
                });
                
            } catch (InterruptedException e) {
            }
        });
        animationThread.start();
    }
    
    private void saveAnswerAsync(int questionId, String userAnswer, boolean isCorrect) {
        SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                Database db = Database.getInstance();
                db.saveQuestionAnswer(quizSessionId, questionId, userAnswer, isCorrect);
                return null;
            }
        };
        worker.execute();
    }
    
    private void endQuiz() {
        quizActive = false;
        
        if (timerThread != null) timerThread.interrupt();
        if (preloadThread != null) preloadThread.interrupt();
        if (animationThread != null) animationThread.interrupt();
        
        Database db = Database.getInstance();
        db.updateSession(quizSessionId, score, correctAnswers, incorrectAnswers);
        
        String playerName = screenManager.getPlayerName();
        int totalQuestions = correctAnswers + incorrectAnswers;
        double accuracy = totalQuestions > 0 ? (correctAnswers * 100.0 / totalQuestions) : 0;
        db.addLeaderboard(playerName, quizSessionId, score, totalQuestions, accuracy);
        
        screenManager.showResult(score, correctAnswers, incorrectAnswers);
    }
    
    private JButton createOptionButton(String label) {
        JButton button = new JButton() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                
                boolean pressed = getModel().isPressed();
                boolean enabled = isEnabled();
                Boolean isHoveredObj = (Boolean) getClientProperty("isHovered");
                boolean isHovered = isHoveredObj != null && isHoveredObj;
                Float fadeAlphaObj = (Float) getClientProperty("fadeAlpha");
                float alpha = fadeAlphaObj != null ? fadeAlphaObj : 1.0f;
                Boolean isHighlighted = (Boolean) getClientProperty("isHighlighted");
                boolean highlighted = isHighlighted != null && isHighlighted;
                
                if (!enabled || alpha < 0.1f) {
                    alpha = 0.3f;
                }
                
                g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha * 0.5f));
                g2d.setColor(new Color(0, 0, 0, 100));
                g2d.fillRoundRect(Constants.SHADOW_OFFSET, Constants.SHADOW_OFFSET,
                    getWidth() - Constants.SHADOW_OFFSET, getHeight() - Constants.SHADOW_OFFSET,
                    Constants.BORDER_RADIUS, Constants.BORDER_RADIUS);
                
                g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
                
                Color bgColor = highlighted ? Constants.NEO_ORANGE : Constants.NEO_PINK;
                Color lightColor = bgColor.brighter();
                Color darkColor = bgColor;
                
                if (isHovered && !pressed && enabled) {
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
                if (highlighted) {
                    g2d.setColor(new Color(255, 215, 0, outlineAlpha));
                    g2d.setStroke(new BasicStroke(Constants.BORDER_THICKNESS + 2));
                } else {
                    g2d.setColor(new Color(255, 255, 255, outlineAlpha));
                    g2d.setStroke(new BasicStroke(Constants.BORDER_THICKNESS));
                }
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
        
        button.setFont(new Font("Arial", Font.BOLD, 18));
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setContentAreaFilled(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                if (button.isEnabled()) {
                    button.putClientProperty("isHovered", true);
                    button.repaint();
                }
            }
            
            @Override
            public void mouseExited(MouseEvent e) {
                button.putClientProperty("isHovered", false);
                button.repaint();
            }
        });
        
        return button;
    }
    
    private JButton createLifelineButton(String text, Color bgColor) {
        JButton button = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                
                boolean pressed = getModel().isPressed();
                boolean enabled = isEnabled();
                Boolean isHoveredObj = (Boolean) getClientProperty("isHovered");
                boolean isHovered = isHoveredObj != null && isHoveredObj;
                
                float alpha = enabled ? 1.0f : 0.4f;
                g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
                
                g2d.setColor(new Color(0, 0, 0, 100));
                g2d.fillRoundRect(Constants.SHADOW_OFFSET, Constants.SHADOW_OFFSET,
                    getWidth() - Constants.SHADOW_OFFSET, getHeight() - Constants.SHADOW_OFFSET,
                    Constants.BORDER_RADIUS, Constants.BORDER_RADIUS);
                
                Color lightColor = bgColor.brighter();
                Color darkColor = bgColor;
                
                if (isHovered && !pressed && enabled) {
                    lightColor = new Color(
                        Math.min(255, lightColor.getRed() + 30),
                        Math.min(255, lightColor.getGreen() + 30),
                        Math.min(255, lightColor.getBlue() + 30)
                    );
                    darkColor = bgColor.brighter();
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
                
                int textAlpha = enabled ? (pressed ? 180 : 255) : 180;
                g2d.setColor(new Color(255, 255, 255, textAlpha));
                g2d.drawString(getText(), x, y);
                
                g2d.dispose();
            }
        };
        
        button.setFont(new Font("Arial Black", Font.BOLD, 15));
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setContentAreaFilled(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                if (button.isEnabled()) {
                    button.putClientProperty("isHovered", true);
                    button.repaint();
                }
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

