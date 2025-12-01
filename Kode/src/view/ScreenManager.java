package view;

import java.awt.CardLayout;
import javax.swing.JFrame;
import javax.swing.JPanel;

public class ScreenManager {
    public static final String MAIN_MENU = "MAIN_MENU";
    public static final String SETTINGS = "SETTINGS";
    public static final String LEADERBOARD = "LEADERBOARD";
    public static final String USERNAME_INPUT = "USERNAME_INPUT";
    public static final String CATEGORY_SELECTION = "CATEGORY_SELECTION";
    public static final String DIFFICULTY_SELECTION = "DIFFICULTY_SELECTION";
    public static final String QUIZ = "QUIZ";
    public static final String RESULT = "RESULT";

    private final JFrame frame;
    private final CardLayout cardLayout;
    private final JPanel rootPanel;
    private final FullscreenManager fullscreenManager;
    private final MainMenuView mainMenuView;
    private final SettingsView settingsView;
    private final LeaderboardView leaderboardView;
    private final UsernameInputView usernameInputView;
    private final CategorySelectionView categorySelectionView;
    private final DifficultySelectionView difficultySelectionView;
    private final QuizView quizView;
    private final ResultView resultView;
    private String playerName;
    private String selectedCategory;
    private String selectedDifficulty;

    public ScreenManager(JFrame frame) {
        this.frame = frame;
        this.cardLayout = new CardLayout();
        this.rootPanel = new JPanel(cardLayout);
        this.rootPanel.setOpaque(false);
        this.fullscreenManager = new FullscreenManager(frame);
        this.mainMenuView = new MainMenuView(this);
        this.settingsView = new SettingsView(this);
        this.leaderboardView = new LeaderboardView(this);
        this.usernameInputView = new UsernameInputView(this);
        this.categorySelectionView = new CategorySelectionView(this);
        this.difficultySelectionView = new DifficultySelectionView(this);
        this.quizView = new QuizView(this);
        this.resultView = new ResultView(this);
        rootPanel.add(mainMenuView, MAIN_MENU);
        rootPanel.add(settingsView, SETTINGS);
        rootPanel.add(leaderboardView, LEADERBOARD);
        rootPanel.add(usernameInputView, USERNAME_INPUT);
        rootPanel.add(categorySelectionView, CATEGORY_SELECTION);
        rootPanel.add(difficultySelectionView, DIFFICULTY_SELECTION);
        rootPanel.add(quizView, QUIZ);
        rootPanel.add(resultView, RESULT);
    }

    public JPanel getRootPanel() {
        return rootPanel;
    }

    public JFrame getFrame() {
        return frame;
    }

    public FullscreenManager getFullscreenManager() {
        return fullscreenManager;
    }

    public void showMainMenu() {
        cardLayout.show(rootPanel, MAIN_MENU);
        mainMenuView.requestFocusInWindow();
    }

    public void showSettings() {
        settingsView.syncFullscreenState();
        cardLayout.show(rootPanel, SETTINGS);
        settingsView.requestFocusInWindow();
    }

    public void showLeaderboard() {
        leaderboardView.refreshLeaderboard();
        cardLayout.show(rootPanel, LEADERBOARD);
        leaderboardView.requestFocusInWindow();
    }
    
    public void showUsernameInput() {
        usernameInputView.resetForm();
        cardLayout.show(rootPanel, USERNAME_INPUT);
        usernameInputView.requestFocusInWindow();
    }
    
    public void setPlayerName(String name) {
        this.playerName = name;
    }
    
    public String getPlayerName() {
        return playerName;
    }
    
    public void showCategorySelection() {
        cardLayout.show(rootPanel, CATEGORY_SELECTION);
        categorySelectionView.requestFocusInWindow();
    }
    
    public void setSelectedCategory(String category) {
        this.selectedCategory = category;
    }
    
    public String getSelectedCategory() {
        return selectedCategory;
    }
    
    public void showDifficultySelection() {
        difficultySelectionView.updateCategoryDisplay();
        cardLayout.show(rootPanel, DIFFICULTY_SELECTION);
        difficultySelectionView.requestFocusInWindow();
    }
    
    public void setSelectedDifficulty(String difficulty) {
        this.selectedDifficulty = difficulty;
    }
    
    public String getSelectedDifficulty() {
        return selectedDifficulty;
    }
    
    public void showQuiz() {
        quizView.startQuiz();
        cardLayout.show(rootPanel, QUIZ);
        quizView.requestFocusInWindow();
    }
    
    public void showResult(int score, int correctAnswers, int incorrectAnswers, int speedBonus) {
        resultView.updateResult(score, correctAnswers, incorrectAnswers, speedBonus);
        cardLayout.show(rootPanel, RESULT);
        resultView.requestFocusInWindow();
    }
}
