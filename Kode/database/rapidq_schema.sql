-- RapidQ Database Schema
-- Database untuk game quiz dengan fitur Speed Mode, Kategori, Lifeline, dan Leaderboard

CREATE DATABASE IF NOT EXISTS rapidq_db;
USE rapidq_db;

-- Tabel Kategori Soal
CREATE TABLE categories (
    category_id INT PRIMARY KEY AUTO_INCREMENT,
    category_name VARCHAR(50) NOT NULL UNIQUE,
    description VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Tabel Soal/Pertanyaan
CREATE TABLE questions (
    question_id INT PRIMARY KEY AUTO_INCREMENT,
    category_id INT NOT NULL,
    question_text TEXT NOT NULL,
    option_a VARCHAR(255) NOT NULL,
    option_b VARCHAR(255) NOT NULL,
    option_c VARCHAR(255) NOT NULL,
    option_d VARCHAR(255) NOT NULL,
    correct_answer CHAR(1) NOT NULL CHECK (correct_answer IN ('A', 'B', 'C', 'D')),
    difficulty_level ENUM('EASY', 'MEDIUM', 'HARD') DEFAULT 'MEDIUM',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (category_id) REFERENCES categories(category_id) ON DELETE CASCADE
);

-- Tabel Player/Pengguna
CREATE TABLE players (
    player_id INT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(50) NOT NULL UNIQUE,
    total_games_played INT DEFAULT 0,
    highest_score INT DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    last_played TIMESTAMP NULL
);

-- Tabel Quiz Session (untuk tracking setiap permainan)
CREATE TABLE quiz_sessions (
    session_id INT PRIMARY KEY AUTO_INCREMENT,
    player_id INT NOT NULL,
    category_id INT NOT NULL,
    game_mode ENUM('SPEED_60', 'NORMAL') DEFAULT 'SPEED_60',
    total_score INT DEFAULT 0,
    total_questions_answered INT DEFAULT 0,
    correct_answers INT DEFAULT 0,
    wrong_answers INT DEFAULT 0,
    time_taken_seconds INT DEFAULT 0,
    lifeline_skip_used INT DEFAULT 0,
    lifeline_fifty_fifty_used INT DEFAULT 0,
    session_start TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    session_end TIMESTAMP NULL,
    FOREIGN KEY (player_id) REFERENCES players(player_id) ON DELETE CASCADE,
    FOREIGN KEY (category_id) REFERENCES categories(category_id) ON DELETE CASCADE
);

-- Tabel Detail Jawaban per Soal (untuk tracking jawaban setiap pertanyaan)
CREATE TABLE question_answers (
    answer_id INT PRIMARY KEY AUTO_INCREMENT,
    session_id INT NOT NULL,
    question_id INT NOT NULL,
    player_answer CHAR(1) CHECK (player_answer IN ('A', 'B', 'C', 'D', 'S')), -- S = Skipped
    is_correct BOOLEAN DEFAULT FALSE,
    time_taken_seconds INT DEFAULT 0,
    lifeline_used VARCHAR(20), -- 'SKIP', 'FIFTY_FIFTY', 'NONE'
    answered_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (session_id) REFERENCES quiz_sessions(session_id) ON DELETE CASCADE,
    FOREIGN KEY (question_id) REFERENCES questions(question_id) ON DELETE CASCADE
);

-- Tabel Leaderboard View (untuk speed mode)
CREATE TABLE leaderboard (
    leaderboard_id INT PRIMARY KEY AUTO_INCREMENT,
    player_id INT NOT NULL,
    session_id INT NOT NULL,
    score INT NOT NULL,
    questions_answered INT NOT NULL,
    accuracy_percentage DECIMAL(5,2) DEFAULT 0.00,
    achieved_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (player_id) REFERENCES players(player_id) ON DELETE CASCADE,
    FOREIGN KEY (session_id) REFERENCES quiz_sessions(session_id) ON DELETE CASCADE,
    INDEX idx_score (score DESC),
    INDEX idx_achieved_at (achieved_at DESC)
);

-- Insert data kategori default
INSERT INTO categories (category_name, description) VALUES
('General', 'Pengetahuan umum dan wawasan sehari-hari'),
('Math', 'Matematika dan logika'),
('Science', 'Sains, fisika, kimia, dan biologi'),
('History', 'Sejarah dunia dan Indonesia'),
('Technology', 'Teknologi, komputer, dan informatika'),
('Sports', 'Olahraga dan atlet'),
('Geography', 'Geografi dan pengetahuan negara');

-- Insert contoh soal untuk kategori General
INSERT INTO questions (category_id, question_text, option_a, option_b, option_c, option_d, correct_answer, difficulty_level) VALUES
(1, 'Apa ibu kota Indonesia?', 'Bandung', 'Jakarta', 'Surabaya', 'Medan', 'B', 'EASY'),
(1, 'Siapa presiden pertama Indonesia?', 'Soekarno', 'Soeharto', 'Habibie', 'Megawati', 'A', 'EASY'),
(1, 'Berapa jumlah provinsi di Indonesia?', '34', '35', '38', '40', 'C', 'MEDIUM');

-- Insert contoh soal untuk kategori Math
INSERT INTO questions (category_id, question_text, option_a, option_b, option_c, option_d, correct_answer, difficulty_level) VALUES
(2, 'Berapa hasil dari 15 x 12?', '150', '180', '200', '175', 'B', 'EASY'),
(2, 'Akar kuadrat dari 144 adalah?', '10', '11', '12', '13', 'C', 'EASY'),
(2, 'Berapa hasil dari 2^10?', '512', '1024', '2048', '256', 'B', 'MEDIUM');

-- Insert contoh soal untuk kategori Science
INSERT INTO questions (category_id, question_text, option_a, option_b, option_c, option_d, correct_answer, difficulty_level) VALUES
(3, 'Simbol kimia untuk air adalah?', 'H2O', 'O2', 'CO2', 'H2', 'A', 'EASY'),
(3, 'Planet terbesar di tata surya adalah?', 'Saturnus', 'Jupiter', 'Uranus', 'Neptunus', 'B', 'EASY'),
(3, 'Berapa kecepatan cahaya?', '300.000 km/s', '150.000 km/s', '450.000 km/s', '200.000 km/s', 'A', 'MEDIUM');
