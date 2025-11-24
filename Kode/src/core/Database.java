package core;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class Database {
    private static Database instance;
    private Connection connection;
    
    private static final String DB_URL = "jdbc:mysql://localhost:3306/rapidq_db";
    private static final String DB_USER = "anon";
    private static final String DB_PASSWORD = "anon";
    
    private Database() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            this.connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
            System.out.println("Database connected successfully!");
        } catch (ClassNotFoundException e) {
            System.err.println("MySQL JDBC Driver not found! Pastikan library sudah ditambahkan");
            e.printStackTrace();
        } catch (SQLException e) {
            System.err.println("Failed to connect to database!");
            e.printStackTrace();
        }
    }
    
    public static Database getInstance() {
        if (instance == null) {
            synchronized (Database.class) {
                if (instance == null) {
                    instance = new Database();
                }
            }
        }
        return instance;
    }
    
    public Connection getConnection() {
        try {
            if (connection == null || connection.isClosed()) {
                connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return connection;
    }
        
    public List<Category> getAllCategories() {
        List<Category> categories = new ArrayList<>();
        String query = "SELECT * FROM categories ORDER BY category_name";
        
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            
            while (rs.next()) {
                categories.add(new Category(
                    rs.getInt("category_id"),
                    rs.getString("category_name"),
                    rs.getString("description")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return categories;
    }
    
    public Category getCategoryById(int categoryId) {
        String query = "SELECT * FROM categories WHERE category_id = ?";
        
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, categoryId);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                return new Category(
                    rs.getInt("category_id"),
                    rs.getString("category_name"),
                    rs.getString("description")
                );
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return null;
    }
        
    public List<Question> getRandomQuestions(int categoryId, int limit) {
        List<Question> questions = new ArrayList<>();
        String query = "SELECT * FROM questions WHERE category_id = ? ORDER BY RAND() LIMIT ?";
        
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, categoryId);
            stmt.setInt(2, limit);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                questions.add(new Question(
                    rs.getInt("question_id"),
                    rs.getInt("category_id"),
                    rs.getString("question_text"),
                    rs.getString("option_a"),
                    rs.getString("option_b"),
                    rs.getString("option_c"),
                    rs.getString("option_d"),
                    rs.getString("correct_answer").charAt(0)
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return questions;
    }
    
    public int countQuestions(int categoryId) {
        String query = "SELECT COUNT(*) as total FROM questions WHERE category_id = ?";
        
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, categoryId);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                return rs.getInt("total");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return 0;
    }
        
    public int createSession(int playerId, int categoryId, String gameMode) {
        String query = "INSERT INTO quiz_sessions (player_id, category_id, game_mode) VALUES (?, ?, ?)";
        
        try (PreparedStatement stmt = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, playerId);
            stmt.setInt(2, categoryId);
            stmt.setString(3, gameMode);
            
            int affectedRows = stmt.executeUpdate();
            if (affectedRows > 0) {
                ResultSet rs = stmt.getGeneratedKeys();
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return -1;
    }
    
    public boolean updateSession(int sessionId, int totalScore, int questionsAnswered, 
                                 int correctAnswers, int wrongAnswers, int timeTaken,
                                 int skipUsed, int fiftyFiftyUsed) {
        String query = "UPDATE quiz_sessions SET " +
                      "total_score = ?, total_questions_answered = ?, correct_answers = ?, " +
                      "wrong_answers = ?, time_taken_seconds = ?, lifeline_skip_used = ?, " +
                      "lifeline_fifty_fifty_used = ?, session_end = NOW() " +
                      "WHERE session_id = ?";
        
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, totalScore);
            stmt.setInt(2, questionsAnswered);
            stmt.setInt(3, correctAnswers);
            stmt.setInt(4, wrongAnswers);
            stmt.setInt(5, timeTaken);
            stmt.setInt(6, skipUsed);
            stmt.setInt(7, fiftyFiftyUsed);
            stmt.setInt(8, sessionId);
            
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return false;
    }
        
    public boolean addLeaderboard(int playerId, int sessionId, int score, 
                                  int questionsAnswered, double accuracy) {
        String query = "INSERT INTO leaderboard (player_id, session_id, score, questions_answered, accuracy_percentage) " +
                      "VALUES (?, ?, ?, ?, ?)";
        
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, playerId);
            stmt.setInt(2, sessionId);
            stmt.setInt(3, score);
            stmt.setInt(4, questionsAnswered);
            stmt.setDouble(5, accuracy);
            
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return false;
    }
    
    public List<LeaderboardEntry> getTopLeaderboard(int limit) {
        List<LeaderboardEntry> leaderboard = new ArrayList<>();
        String query = "SELECT l.*, p.username FROM leaderboard l " +
                      "JOIN players p ON l.player_id = p.player_id " +
                      "ORDER BY l.score DESC, l.accuracy_percentage DESC, l.achieved_at ASC " +
                      "LIMIT ?";
        
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, limit);
            ResultSet rs = stmt.executeQuery();
            
            int rank = 1;
            while (rs.next()) {
                leaderboard.add(new LeaderboardEntry(
                    rank++,
                    rs.getString("username"),
                    rs.getInt("score"),
                    rs.getInt("questions_answered"),
                    rs.getDouble("accuracy_percentage"),
                    rs.getTimestamp("achieved_at")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return leaderboard;
    }
    
    public int createPlayer(String username) {
        String query = "INSERT INTO players (username) VALUES (?)";
        
        try (PreparedStatement stmt = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, username);
            
            int affectedRows = stmt.executeUpdate();
            if (affectedRows > 0) {
                ResultSet rs = stmt.getGeneratedKeys();
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return -1;
    }
    
    public void closeConnection() {
        if (connection != null) {
            try {
                connection.close();
                System.out.println("Database connection closed.");
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
        
    public static class Category {
        private int categoryId;
        private String categoryName;
        private String description;
        
        public Category(int categoryId, String categoryName, String description) {
            this.categoryId = categoryId;
            this.categoryName = categoryName;
            this.description = description;
        }
        
        public int getCategoryId() { return categoryId; }
        public String getCategoryName() { return categoryName; }
        public String getDescription() { return description; }
        
        @Override
        public String toString() { return categoryName; }
    }
    
    public static class Question {
        private int questionId;
        private int categoryId;
        private String questionText;
        private String optionA;
        private String optionB;
        private String optionC;
        private String optionD;
        private char correctAnswer;
        
        public Question(int questionId, int categoryId, String questionText, 
                       String optionA, String optionB, String optionC, String optionD, 
                       char correctAnswer) {
            this.questionId = questionId;
            this.categoryId = categoryId;
            this.questionText = questionText;
            this.optionA = optionA;
            this.optionB = optionB;
            this.optionC = optionC;
            this.optionD = optionD;
            this.correctAnswer = correctAnswer;
        }
        
        public int getQuestionId() { return questionId; }
        public int getCategoryId() { return categoryId; }
        public String getQuestionText() { return questionText; }
        public String getOptionA() { return optionA; }
        public String getOptionB() { return optionB; }
        public String getOptionC() { return optionC; }
        public String getOptionD() { return optionD; }
        public char getCorrectAnswer() { return correctAnswer; }
        
        public boolean isCorrect(char answer) {
            return this.correctAnswer == Character.toUpperCase(answer);
        }
        
        public String getOption(char option) {
            switch (Character.toUpperCase(option)) {
                case 'A': return optionA;
                case 'B': return optionB;
                case 'C': return optionC;
                case 'D': return optionD;
                default: return null;
            }
        }
    }
    
    public static class QuizSession {
        private int sessionId;
        private int playerId;
        private int categoryId;
        private String gameMode;
        private int totalScore;
        private int correctAnswers;
        private int wrongAnswers;
        private int skipUsed;
        private int fiftyFiftyUsed;
        
        public QuizSession(int playerId, int categoryId, String gameMode) {
            this.playerId = playerId;
            this.categoryId = categoryId;
            this.gameMode = gameMode;
            this.totalScore = 0;
            this.correctAnswers = 0;
            this.wrongAnswers = 0;
            this.skipUsed = 0;
            this.fiftyFiftyUsed = 0;
        }
        
        public void setSessionId(int sessionId) { this.sessionId = sessionId; }
        public int getSessionId() { return sessionId; }
        public int getPlayerId() { return playerId; }
        public int getCategoryId() { return categoryId; }
        public String getGameMode() { return gameMode; }
        public int getTotalScore() { return totalScore; }
        public int getCorrectAnswers() { return correctAnswers; }
        public int getWrongAnswers() { return wrongAnswers; }
        public int getSkipUsed() { return skipUsed; }
        public int getFiftyFiftyUsed() { return fiftyFiftyUsed; }
        
        public void addScore(int points) { this.totalScore += points; }
        public void addCorrectAnswer() { this.correctAnswers++; }
        public void addWrongAnswer() { this.wrongAnswers++; }
        public void useSkip() { this.skipUsed++; }
        public void useFiftyFifty() { this.fiftyFiftyUsed++; }
        
        public int getTotalAnswered() { return correctAnswers + wrongAnswers; }
        public double getAccuracy() {
            int total = getTotalAnswered();
            return total == 0 ? 0.0 : (correctAnswers * 100.0) / total;
        }
    }
    
    public static class LeaderboardEntry {
        private int rank;
        private String username;
        private int score;
        private int questionsAnswered;
        private double accuracyPercentage;
        private Timestamp achievedAt;
        
        public LeaderboardEntry(int rank, String username, int score, 
                               int questionsAnswered, double accuracyPercentage, 
                               Timestamp achievedAt) {
            this.rank = rank;
            this.username = username;
            this.score = score;
            this.questionsAnswered = questionsAnswered;
            this.accuracyPercentage = accuracyPercentage;
            this.achievedAt = achievedAt;
        }
        
        public int getRank() { return rank; }
        public String getUsername() { return username; }
        public int getScore() { return score; }
        public int getQuestionsAnswered() { return questionsAnswered; }
        public double getAccuracyPercentage() { return accuracyPercentage; }
        public Timestamp getAchievedAt() { return achievedAt; }
    }
}
