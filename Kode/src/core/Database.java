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
        return getTopLeaderboard(limit, null, null);
    }
    
    public List<LeaderboardEntry> getTopLeaderboard(int limit, String categoryName, String difficulty) {
        List<LeaderboardEntry> leaderboard = new ArrayList<>();
        StringBuilder queryBuilder = new StringBuilder();
        queryBuilder.append("SELECT l.*, p.username FROM leaderboard l ");
        queryBuilder.append("JOIN players p ON l.player_id = p.player_id ");
        queryBuilder.append("JOIN quiz_sessions qs ON l.session_id = qs.session_id ");
        
        // Add filters if provided
        List<String> conditions = new ArrayList<>();
        if (categoryName != null && !categoryName.equals("Semua Kategori")) {
            queryBuilder.append("JOIN categories c ON qs.category_id = c.category_id ");
            conditions.add("c.category_name = ?");
        }
        if (difficulty != null && !difficulty.equals("Semua Level")) {
            conditions.add("qs.game_mode LIKE ?");
        }
        
        if (!conditions.isEmpty()) {
            queryBuilder.append("WHERE ");
            queryBuilder.append(String.join(" AND ", conditions));
            queryBuilder.append(" ");
        }
        
        queryBuilder.append("ORDER BY l.score DESC, l.accuracy_percentage DESC, l.achieved_at ASC ");
        queryBuilder.append("LIMIT ?");
        
        try (PreparedStatement stmt = connection.prepareStatement(queryBuilder.toString())) {
            int paramIndex = 1;
            
            if (categoryName != null && !categoryName.equals("Semua Kategori")) {
                stmt.setString(paramIndex++, categoryName);
            }
            if (difficulty != null && !difficulty.equals("Semua Level")) {
                stmt.setString(paramIndex++, "%" + difficulty + "%");
            }
            stmt.setInt(paramIndex, limit);
            
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
    
    // New methods for QuizView
    public List<Question> getRandomQuestions(String categoryName, String difficulty, int limit) {
        List<Question> questions = new ArrayList<>();
        // Convert difficulty: MUDAH->EASY, SEDANG->MEDIUM, SULIT->HARD
        String dbDifficulty = convertDifficultyToDb(difficulty);
        
        String query = "SELECT q.* FROM questions q " +
                      "JOIN categories c ON q.category_id = c.category_id " +
                      "WHERE c.category_name = ? AND q.difficulty_level = ? " +
                      "ORDER BY RAND() LIMIT ?";
        
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, categoryName);
            stmt.setString(2, dbDifficulty);
            stmt.setInt(3, limit);
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
                    rs.getString("correct_answer"),
                    rs.getString("difficulty_level") // Gunakan dari database
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return questions;
    }
    
    private String convertDifficultyToDb(String difficulty) {
        if (difficulty == null) return "MEDIUM";
        switch (difficulty.toUpperCase()) {
            case "MUDAH": return "EASY";
            case "SEDANG": return "MEDIUM";
            case "SULIT": return "HARD";
            default: return difficulty.toUpperCase();
        }
    }
    
    public int createSession(String playerName, String categoryName, String difficulty) {
        // First, get or create player
        int playerId = getOrCreatePlayer(playerName);
        if (playerId == -1) return -1;
        
        // Get category ID
        int categoryId = getCategoryIdByName(categoryName);
        if (categoryId == -1) return -1;
        
        String query = "INSERT INTO quiz_sessions (player_id, category_id, game_mode) VALUES (?, ?, ?)";
        
        try (PreparedStatement stmt = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, playerId);
            stmt.setInt(2, categoryId);
            stmt.setString(3, "Speed Quiz 60s - " + difficulty);
            
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
    
    private int getOrCreatePlayer(String username) {
        // Check if player exists
        String checkQuery = "SELECT player_id FROM players WHERE username = ?";
        try (PreparedStatement stmt = connection.prepareStatement(checkQuery)) {
            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("player_id");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        // Create new player
        return createPlayer(username);
    }
    
    private int getCategoryIdByName(String categoryName) {
        String query = "SELECT category_id FROM categories WHERE category_name = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, categoryName);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("category_id");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }
    
    public boolean updateSession(int sessionId, int score, int correctAnswers, int incorrectAnswers) {
        String query = "UPDATE quiz_sessions SET " +
                      "total_score = ?, correct_answers = ?, wrong_answers = ?, " +
                      "total_questions_answered = ?, session_end = NOW() " +
                      "WHERE session_id = ?";
        
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, score);
            stmt.setInt(2, correctAnswers);
            stmt.setInt(3, incorrectAnswers);
            stmt.setInt(4, correctAnswers + incorrectAnswers);
            stmt.setInt(5, sessionId);
            
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return false;
    }
    
    public void saveQuestionAnswer(int sessionId, int questionId, String userAnswer, boolean isCorrect) {
        String query = "INSERT INTO question_answers (session_id, question_id, user_answer, is_correct) " +
                      "VALUES (?, ?, ?, ?)";
        
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, sessionId);
            stmt.setInt(2, questionId);
            stmt.setString(3, userAnswer);
            stmt.setBoolean(4, isCorrect);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    public boolean addLeaderboard(String playerName, int sessionId, int score, int questionsAnswered, double accuracy) {
        int playerId = getOrCreatePlayer(playerName);
        if (playerId == -1) return false;
        if (sessionId <= 0) return false;
        return addLeaderboard(playerId, sessionId, score, questionsAnswered, accuracy);
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
        public int id;
        public int categoryId;
        public String questionText;
        public String optionA;
        public String optionB;
        public String optionC;
        public String optionD;
        public String correctAnswer;
        public String difficulty;
        
        public Question(int questionId, int categoryId, String questionText, 
                       String optionA, String optionB, String optionC, String optionD, 
                       char correctAnswer) {
            this.id = questionId;
            this.categoryId = categoryId;
            this.questionText = questionText;
            this.optionA = optionA;
            this.optionB = optionB;
            this.optionC = optionC;
            this.optionD = optionD;
            this.correctAnswer = String.valueOf(correctAnswer);
            this.difficulty = "medium"; // default
        }
        
        public Question(int questionId, int categoryId, String questionText, 
                       String optionA, String optionB, String optionC, String optionD, 
                       String correctAnswer, String difficulty) {
            this.id = questionId;
            this.categoryId = categoryId;
            this.questionText = questionText;
            this.optionA = optionA;
            this.optionB = optionB;
            this.optionC = optionC;
            this.optionD = optionD;
            this.correctAnswer = correctAnswer;
            this.difficulty = difficulty != null ? difficulty : "medium";
        }
        
        public int getQuestionId() { return id; }
        public int getCategoryId() { return categoryId; }
        public String getQuestionText() { return questionText; }
        public String getOptionA() { return optionA; }
        public String getOptionB() { return optionB; }
        public String getOptionC() { return optionC; }
        public String getOptionD() { return optionD; }
        public char getCorrectAnswer() { return correctAnswer.charAt(0); }
        
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
