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
    game_mode VARCHAR(100) NOT NULL DEFAULT 'SPEED_60', -- menyimpan mode + level mis. "Speed Quiz 60s - MUDAH"
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
    user_answer VARCHAR(10), -- 'A', 'B', 'C', 'D', 'SKIP'
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

-- Insert data kategori default (sesuai dengan CategorySelectionView)
INSERT INTO categories (category_name, description) VALUES
('Matematika', 'Matematika untuk SD kelas 1-6'),
('Bahasa Indonesia', 'Bahasa Indonesia untuk SD kelas 1-6'),
('IPA', 'Ilmu Pengetahuan Alam untuk SD kelas 1-6'),
('IPS', 'Ilmu Pengetahuan Sosial untuk SD kelas 1-6'),
('Bahasa Inggris', 'Bahasa Inggris untuk SD kelas 1-6');

-- Insert contoh soal untuk MATEMATIKA (category_id = 1)
INSERT INTO questions (category_id, question_text, option_a, option_b, option_c, option_d, correct_answer, difficulty_level) VALUES
(1, 'Berapa hasil dari 5 + 3?', '6', '7', '8', '9', 'C', 'EASY'),
(1, 'Berapa hasil dari 10 - 4?', '5', '6', '7', '8', 'B', 'EASY'),
(1, 'Berapa hasil dari 7 x 2?', '12', '14', '16', '18', 'B', 'EASY'),
(1, 'Berapa hasil dari 12 ÷ 3?', '3', '4', '5', '6', 'B', 'EASY'),
(1, 'Berapa hasil dari 15 + 8?', '21', '22', '23', '24', 'C', 'EASY'),
(1, 'Berapa hasil dari 25 - 12?', '11', '12', '13', '14', 'C', 'MEDIUM'),
(1, 'Berapa hasil dari 9 x 8?', '64', '72', '81', '90', 'B', 'MEDIUM'),
(1, 'Berapa hasil dari 144 ÷ 12?', '10', '11', '12', '13', 'C', 'MEDIUM'),
(1, 'Berapa hasil dari 123 + 456?', '579', '589', '569', '599', 'A', 'HARD'),
(1, 'Berapa hasil dari 15 x 15?', '200', '215', '225', '235', 'C', 'HARD'),
(1, 'Berapa akar kuadrat dari 196?', '12', '13', '14', '15', 'C', 'HARD'),
(1, 'Berapa hasil dari 2^8?', '128', '256', '512', '1024', 'B', 'HARD');

-- Insert contoh soal untuk BAHASA INDONESIA (category_id = 2)
INSERT INTO questions (category_id, question_text, option_a, option_b, option_c, option_d, correct_answer, difficulty_level) VALUES
(2, 'Apa ibu kota Indonesia?', 'Bandung', 'Jakarta', 'Surabaya', 'Medan', 'B', 'EASY'),
(2, 'Kata benda yang benar adalah?', 'Lari', 'Meja', 'Indah', 'Cepat', 'B', 'EASY'),
(2, 'Sinonim dari kata "besar" adalah?', 'Kecil', 'Raksasa', 'Tinggi', 'Lebar', 'B', 'EASY'),
(2, 'Antonim dari kata "panas" adalah?', 'Hangat', 'Dingin', 'Sejuk', 'Kering', 'B', 'EASY'),
(2, 'Kalimat tanya yang benar adalah?', 'Apa namamu', 'Apa namamu?', 'namamu apa', 'Apa nama', 'B', 'EASY'),
(2, 'Kata kerja dari "tulisan" adalah?', 'Menulis', 'Tulis', 'Ditulis', 'Penulis', 'A', 'MEDIUM'),
(2, 'Awalan yang tepat untuk kata "datang" adalah?', 'Kedatangan', 'Mendatang', 'Didatangi', 'Pendatang', 'B', 'MEDIUM'),
(2, 'Jenis kalimat "Tolong tutup pintu!" adalah?', 'Tanya', 'Berita', 'Perintah', 'Seru', 'C', 'MEDIUM'),
(2, 'Kata baku yang benar adalah?', 'Apotek', 'Apotik', 'Apotex', 'Apotic', 'A', 'HARD'),
(2, 'Majas yang menyatakan perbandingan adalah?', 'Personifikasi', 'Metafora', 'Hiperbola', 'Ironi', 'B', 'HARD');

-- Insert contoh soal untuk IPA (category_id = 3)
INSERT INTO questions (category_id, question_text, option_a, option_b, option_c, option_d, correct_answer, difficulty_level) VALUES
(3, 'Hewan yang berkembang biak dengan bertelur adalah?', 'Kucing', 'Ayam', 'Sapi', 'Kambing', 'B', 'EASY'),
(3, 'Tumbuhan membuat makanan melalui proses?', 'Respirasi', 'Fotosintesis', 'Transpirasi', 'Evaporasi', 'B', 'EASY'),
(3, 'Planet yang paling dekat dengan matahari adalah?', 'Venus', 'Merkurius', 'Bumi', 'Mars', 'B', 'EASY'),
(3, 'Bagian tumbuhan yang menyerap air adalah?', 'Daun', 'Batang', 'Akar', 'Bunga', 'C', 'EASY'),
(3, 'Sumber energi terbesar di bumi adalah?', 'Angin', 'Air', 'Matahari', 'Batubara', 'C', 'EASY'),
(3, 'Simbol kimia untuk air adalah?', 'H2O', 'O2', 'CO2', 'H2', 'A', 'MEDIUM'),
(3, 'Planet terbesar di tata surya adalah?', 'Saturnus', 'Jupiter', 'Uranus', 'Neptunus', 'B', 'MEDIUM'),
(3, 'Proses perubahan air menjadi uap disebut?', 'Kondensasi', 'Evaporasi', 'Presipitasi', 'Sublimasi', 'B', 'MEDIUM'),
(3, 'Organ pernapasan pada ikan adalah?', 'Paru-paru', 'Insang', 'Kulit', 'Trakea', 'B', 'HARD'),
(3, 'Kecepatan cahaya adalah?', '300.000 km/s', '150.000 km/s', '450.000 km/s', '200.000 km/s', 'A', 'HARD');

-- Insert contoh soal untuk IPS (category_id = 4)
INSERT INTO questions (category_id, question_text, option_a, option_b, option_c, option_d, correct_answer, difficulty_level) VALUES
(4, 'Siapa presiden pertama Indonesia?', 'Soekarno', 'Soeharto', 'Habibie', 'Megawati', 'A', 'EASY'),
(4, 'Pulau terbesar di Indonesia adalah?', 'Jawa', 'Sumatera', 'Kalimantan', 'Papua', 'D', 'EASY'),
(4, 'Lagu kebangsaan Indonesia adalah?', 'Garuda Pancasila', 'Indonesia Raya', 'Tanah Airku', 'Ibu Pertiwi', 'B', 'EASY'),
(4, 'Pancasila memiliki berapa sila?', '3', '4', '5', '6', 'C', 'EASY'),
(4, 'Hari kemerdekaan Indonesia adalah?', '17 Agustus', '1 Juni', '20 Mei', '10 November', 'A', 'EASY'),
(4, 'Berapa jumlah provinsi di Indonesia?', '34', '35', '37', '38', 'D', 'MEDIUM'),
(4, 'Pahlawan yang dijuluki Bapak Pendidikan adalah?', 'Diponegoro', 'Ki Hajar Dewantara', 'Kartini', 'Hatta', 'B', 'MEDIUM'),
(4, 'Benua terbesar di dunia adalah?', 'Afrika', 'Amerika', 'Asia', 'Eropa', 'C', 'MEDIUM'),
(4, 'Organisasi negara-negara Asia Tenggara adalah?', 'ASEAN', 'NATO', 'UNESCO', 'WHO', 'A', 'HARD'),
(4, 'Candi Borobudur terletak di provinsi?', 'Jawa Barat', 'Jawa Tengah', 'Jawa Timur', 'Yogyakarta', 'B', 'HARD');

-- Insert contoh soal untuk BAHASA INGGRIS (category_id = 5)
INSERT INTO questions (category_id, question_text, option_a, option_b, option_c, option_d, correct_answer, difficulty_level) VALUES
(5, 'How do you say "Selamat pagi" in English?', 'Good night', 'Good morning', 'Good afternoon', 'Good evening', 'B', 'EASY'),
(5, 'What is the color of the sky?', 'Red', 'Blue', 'Green', 'Yellow', 'B', 'EASY'),
(5, 'How many days in a week?', '5', '6', '7', '8', 'C', 'EASY'),
(5, 'What is "Kucing" in English?', 'Dog', 'Cat', 'Bird', 'Fish', 'B', 'EASY'),
(5, 'The opposite of "big" is?', 'Large', 'Small', 'Tall', 'Wide', 'B', 'EASY'),
(5, 'I ... a student.', 'is', 'am', 'are', 'be', 'B', 'MEDIUM'),
(5, 'She ... to school every day.', 'go', 'goes', 'going', 'gone', 'B', 'MEDIUM'),
(5, 'What is the past tense of "eat"?', 'Eated', 'Ate', 'Eaten', 'Eating', 'B', 'MEDIUM'),
(5, 'How do you spell "Empat Puluh"?', 'Fourteen', 'Forty', 'Four ten', 'For ten', 'B', 'HARD'),
(5, 'The correct sentence is?', 'He don\'t like apples', 'He doesn\'t likes apples', 'He doesn\'t like apples', 'He not like apples', 'C', 'HARD');
