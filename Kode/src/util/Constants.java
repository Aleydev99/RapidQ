package util;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;

public class Constants {
    
    public static final Color CARTOON_BG_START = new Color(41, 98, 255);
    public static final Color CARTOON_BG_END = new Color(13, 37, 103);
    public static final Color CARTOON_RADIAL_CENTER = new Color(100, 149, 237);
    public static final Color TEXT_ORANGE_START = new Color(255, 184, 77);
    public static final Color TEXT_ORANGE_END = new Color(255, 140, 0);
    public static final Color TEXT_CYAN_START = new Color(102, 255, 255);
    public static final Color TEXT_CYAN_END = new Color(0, 191, 255);
    public static final Color NEO_BLACK = new Color(0, 0, 0);
    public static final Color NEO_WHITE = new Color(255, 255, 255);
    public static final Color NEO_YELLOW = new Color(255, 215, 0);
    public static final Color NEO_PINK = new Color(255, 105, 180);
    public static final Color NEO_BLUE = new Color(30, 144, 255);
    public static final Color NEO_GREEN = new Color(50, 205, 50);
    public static final Color NEO_PURPLE = new Color(147, 112, 219);
    public static final Color NEO_ORANGE = new Color(255, 140, 0);
    public static final Color NEO_RED = new Color(255, 69, 0);
    public static final Color LIGHT_BACKGROUND = CARTOON_BG_START;
    public static final Color LIGHT_CARD_BG = NEO_WHITE;
    public static final Color LIGHT_TEXT = NEO_WHITE;
    public static final Color LIGHT_TEXT_SECONDARY = new Color(220, 220, 220);
    
    public static final Color DARK_BACKGROUND = new Color(18, 18, 18);
    public static final Color DARK_CARD_BG = new Color(30, 30, 30);
    public static final Color DARK_TEXT = Color.WHITE;
    public static final Color DARK_TEXT_SECONDARY = new Color(189, 189, 189);
    
    public static final Color PRIMARY_COLOR = NEO_BLUE;
    public static final Color PRIMARY_HOVER = new Color(0, 150, 200);
    public static final Color SECONDARY_COLOR = NEO_PINK;
    public static final Color SECONDARY_HOVER = new Color(230, 50, 160);
    
    public static final Color SUCCESS_COLOR = NEO_GREEN;
    public static final Color ERROR_COLOR = NEO_RED;
    public static final Color WARNING_COLOR = NEO_ORANGE;
    public static final Color INFO_COLOR = NEO_BLUE;
    
    public static final Color TIMER_NORMAL = NEO_YELLOW;
    public static final Color TIMER_CRITICAL = NEO_RED;
    public static final Color ACCENT_COLOR = NEO_PURPLE;
    
    public static final Font TITLE_FONT = new Font("Arial Black", Font.BOLD, 72);
    public static final Font SUBTITLE_FONT = new Font("Arial Black", Font.BOLD, 24);
    public static final Font HEADER_FONT = new Font("Arial Black", Font.BOLD, 36);
    public static final Font BUTTON_FONT = new Font("Arial Black", Font.BOLD, 20);
    public static final Font TEXT_FONT = new Font("Arial", Font.BOLD, 16);
    public static final Font QUESTION_FONT = new Font("Arial Black", Font.BOLD, 24);
    public static final Font TIMER_FONT = new Font("Arial Black", Font.BOLD, 48);
    public static final Font SCORE_FONT = new Font("Arial Black", Font.BOLD, 32);
    
    public static final Dimension WINDOW_SIZE = new Dimension(1000, 700);
    public static final int BUTTON_HEIGHT = 65;
    public static final int BUTTON_WIDTH = 300;
    public static final int BORDER_RADIUS = 35;
    public static final int SHADOW_OFFSET = 6;
    public static final int BORDER_THICKNESS = 5;
    public static final int TEXT_SHADOW_DEPTH = 8;
    public static final int PADDING_LARGE = 40;
    public static final int PADDING_MEDIUM = 20;
    public static final int PADDING_SMALL = 10;
    
    public static final int QUIZ_TIME_SECONDS = 60;
    public static final int QUESTIONS_PER_QUIZ = 20;
    public static final int CORRECT_ANSWER_POINTS = 10;
    public static final int TIME_BONUS_MULTIPLIER = 2;
    
    public static final int ANIMATION_DURATION_MS = 300;
    public static final int TRANSITION_DELAY_MS = 200;
    public static final int PULSE_INTERVAL_MS = 1000;
    
    public static final String DB_NAME = "rapidq.db";
    public static final String DB_PATH = "data/" + DB_NAME;
    
    public static final int MAX_SKIP_LIFELINE = 3;
    public static final int MAX_FIFTY_FIFTY_LIFELINE = 2;
    
    private static boolean isDarkMode = false;
    
    public static void toggleDarkMode() {
        isDarkMode = !isDarkMode;
    }
    
    public static boolean isDarkMode() {
        return isDarkMode;
    }
    
    public static Color getBackgroundColor() {
        return isDarkMode ? DARK_BACKGROUND : LIGHT_BACKGROUND;
    }
    
    public static Color getCardBackground() {
        return isDarkMode ? DARK_CARD_BG : LIGHT_CARD_BG;
    }
    
    public static Color getTextColor() {
        return isDarkMode ? DARK_TEXT : LIGHT_TEXT;
    }
    
    public static Color getSecondaryTextColor() {
        return isDarkMode ? DARK_TEXT_SECONDARY : LIGHT_TEXT_SECONDARY;
    }
}
