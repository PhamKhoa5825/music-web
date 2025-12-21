-- #############################################################################
-- #                         PHẦN 1: TẠO CẤU TRÚC BẢNG (CREATE TABLE)
-- #############################################################################
DROP DATABASE IF EXISTS MusicWeb;
-- Kiểm tra và tạo mới CSDL nếu chưa tồn tại
CREATE DATABASE IF NOT EXISTS MusicWeb;
USE MusicWeb;


-- Tắt kiểm tra khóa ngoại để thực hiện INSERT dễ dàng hơn
SET FOREIGN_KEY_CHECKS = 0;

-- 1. Bảng User
CREATE TABLE users (
    user_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(20) NOT NULL DEFAULT 'USER',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 2. Bảng Artist
CREATE TABLE artists (
    artist_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    description TEXT
);

-- 3. Bảng Genre
CREATE TABLE genres (
    genre_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE
);

-- 4. Bảng Album
CREATE TABLE albums (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    cover_url VARCHAR(255),
    release_year INT,
    artist_id BIGINT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (artist_id) REFERENCES artists(artist_id)
);

-- 5. Bảng Song
CREATE TABLE songs (
    song_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    artist_id BIGINT,
    album_id BIGINT,
    file_path VARCHAR(255),
    cover_image VARCHAR(255),
    views INT DEFAULT 0,
    upload_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    average_rating DOUBLE DEFAULT 0.0,
    total_ratings INT DEFAULT 0,
    audio_features TEXT, -- JSON/TEXT
    FOREIGN KEY (artist_id) REFERENCES artists(artist_id),
    FOREIGN KEY (album_id) REFERENCES albums(id)
);

-- Bảng trung gian cho quan hệ Many-to-Many Song - Genre
CREATE TABLE song_genres (
    song_id BIGINT NOT NULL,
    genre_id BIGINT NOT NULL,
    PRIMARY KEY (song_id, genre_id),
    FOREIGN KEY (song_id) REFERENCES songs(song_id),
    FOREIGN KEY (genre_id) REFERENCES genres(genre_id)
);

-- 6. Bảng UserPreference (Lưu ý: Đã lược bỏ M-M cho genres/artists để đơn giản script)
CREATE TABLE user_preferences (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL UNIQUE,
    listening_pattern JSON, -- JSON/TEXT
    FOREIGN KEY (user_id) REFERENCES users(user_id)
);

-- 6.1. Bảng trung gian UserPreference - Genre (Do Hibernate cần)
CREATE TABLE preferred_genres (
    user_preference_id BIGINT NOT NULL,
    genre_id BIGINT NOT NULL,
    PRIMARY KEY (user_preference_id, genre_id),
    FOREIGN KEY (user_preference_id) REFERENCES user_preferences(id),
    FOREIGN KEY (genre_id) REFERENCES genres(genre_id)
);

-- 6.2. Bảng trung gian UserPreference - Artist (Do Hibernate cần)
CREATE TABLE preferred_artists (
    user_preference_id BIGINT NOT NULL,
    artist_id BIGINT NOT NULL,
    PRIMARY KEY (user_preference_id, artist_id),
    FOREIGN KEY (user_preference_id) REFERENCES user_preferences(id),
    FOREIGN KEY (artist_id) REFERENCES artists(artist_id)
);

-- 7. Bảng SystemLog
CREATE TABLE system_logs (
    log_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    action VARCHAR(50) NOT NULL,
    description TEXT,
    time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    user_id BIGINT,
    FOREIGN KEY (user_id) REFERENCES users(user_id)
);

-- 8. Bảng Favorite
CREATE TABLE favorites (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    song_id BIGINT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_user_song (user_id, song_id),
    FOREIGN KEY (user_id) REFERENCES users(user_id),
    FOREIGN KEY (song_id) REFERENCES songs(song_id)
);

-- 9. Bảng SongRating
CREATE TABLE song_ratings (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    song_id BIGINT NOT NULL,
    rating INT NOT NULL CHECK (rating BETWEEN 1 AND 5),
    review TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_user_song_rating (user_id, song_id),
    FOREIGN KEY (user_id) REFERENCES users(user_id),
    FOREIGN KEY (song_id) REFERENCES songs(song_id)
);

-- 10. Bảng ListeningHistory
CREATE TABLE listening_history (
    history_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    song_id BIGINT NOT NULL,
    listened_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(user_id),
    FOREIGN KEY (song_id) REFERENCES songs(song_id)
);

-- 11. Bảng Playlist
CREATE TABLE playlists (
    playlist_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    name VARCHAR(255) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(user_id)
);

-- 12. Bảng trung gian PlaylistSong
CREATE TABLE playlist_songs (
    playlist_id BIGINT NOT NULL,
    song_id BIGINT NOT NULL,
    track_order INT,
    added_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (playlist_id, song_id),
    FOREIGN KEY (playlist_id) REFERENCES playlists(playlist_id),
    FOREIGN KEY (song_id) REFERENCES songs(song_id)
);

-- 13. Bảng SongRanking
CREATE TABLE song_rankings (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    song_id BIGINT NOT NULL,
    ranking_date DATE NOT NULL,
    ranks INT NOT NULL,
    total_views INT,
    UNIQUE KEY uk_date_song (ranking_date, song_id),
    FOREIGN KEY (song_id) REFERENCES songs(song_id)
);

-- 14. Bảng Recommendation
CREATE TABLE recommendations (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    recommended_song_id BIGINT NOT NULL,
    confidence_score DOUBLE,
    reason VARCHAR(255),
    recommended_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    clicked BOOLEAN DEFAULT FALSE,
    liked BOOLEAN DEFAULT FALSE,
    FOREIGN KEY (user_id) REFERENCES users(user_id),
    FOREIGN KEY (recommended_song_id) REFERENCES songs(song_id)
);


-- #############################################################################
-- #                         PHẦN 2: THÊM DỮ LIỆU MẪU (INSERT INTO)
-- #############################################################################

-- Bật kiểm tra khóa ngoại sau khi hoàn tất INSERT
SET FOREIGN_KEY_CHECKS = 1;

-- 1. Thêm 10 records vào users
INSERT INTO users (username, password, role) VALUES
('admin_hoang', 'hash_admin1', 'ADMIN'),
('user_an', 'hash_user2', 'USER'),
('user_binh', 'hash_user3', 'USER'),
('user_cuong', 'hash_user4', 'USER'),
('user_dung', 'hash_user5', 'USER'),
('user_e', 'hash_user6', 'USER'),
('user_f', 'hash_user7', 'USER'),
('user_g', 'hash_user8', 'USER'),
('user_h', 'hash_user9', 'USER'),
('user_i', 'hash_user10', 'USER');

-- 2. Thêm 10 records vào artists
INSERT INTO artists (name, description) VALUES
('Sơn Tùng M-TP', 'Nghệ sĩ pop hàng đầu Việt Nam.'),
('Đen Vâu', 'Rapper có ảnh hưởng lớn với ca từ ý nghĩa.'),
('Mỹ Tâm', 'Họa mi tóc nâu, diva nhạc Pop Việt Nam.'),
('Taylor Swift', 'Nữ ca sĩ, nhạc sĩ người Mỹ.'),
('BTS', 'Nhóm nhạc nam K-Pop toàn cầu.'),
('Hoàng Thùy Linh', 'Nghệ sĩ theo đuổi âm nhạc dân gian điện tử.'),
('Bích Phương', 'Ca sĩ nhạc Pop/Ballad nhẹ nhàng.'),
('Charlie Puth', 'Ca sĩ, nhạc sĩ, nhà sản xuất người Mỹ.'),
('The Weeknd', 'Ca sĩ, nhạc sĩ R&B/Pop người Canada.'),
('Hà Anh Tuấn', 'Ca sĩ nhạc Pop lãng mạn.');

-- 3. Thêm 10 records vào genres
INSERT INTO genres (name) VALUES
('Pop'), ('Ballad'), ('Hip Hop'), ('R&B'), ('Rock'),
('K-Pop'), ('V-Pop'), ('Acoustic'), ('EDM'), ('Indie');

-- 4. Thêm 10 records vào albums (Dựa trên Artist IDs 1-10)
INSERT INTO albums (title, artist_id, release_year) VALUES
('M-TP Decade', 1, 2023),
('Tháng 5 của Mây', 2, 2021),
('Tâm 9', 3, 2017),
('1989 (Taylor’s Version)', 4, 2023),
('Map of the Soul: 7', 5, 2020),
('Hoàng', 6, 2019),
('Bao Giờ Lấy Chồng EP', 7, 2017),
('Voicenotes', 8, 2018),
('After Hours', 9, 2020),
('Truyện Ngắn', 10, 2018);

-- 5. Thêm 10 records vào songs (Dựa trên Artist IDs 1-10 và Album IDs 1-10)
INSERT INTO songs (title, artist_id, album_id, file_path, cover_image, views) VALUES
('Chúng Ta Của Tương Lai', 1, 1, '/uploads/st_ctct.mp3', '/covers/st_ctct.jpg', 12000000),
('Mang Tiền Về Cho Mẹ', 2, 2, '/uploads/den_mtvcm.mp3', '/covers/den_mtvcm.jpg', 8500000),
('Đừng Hỏi Em', 3, 3, '/uploads/mt_dhe.mp3', '/covers/mt_dhe.jpg', 5000000),
('Cruel Summer', 4, 4, '/uploads/ts_cs.mp3', '/covers/ts_cs.jpg', 20000000),
('Dynamite', 5, 5, '/uploads/bts_dy.mp3', '/covers/bts_dy.jpg', 35000000),
('Duyên Âm', 6, 6, '/uploads/htl_da.mp3', '/covers/htl_da.jpg', 7800000),
('Đi Đu Đưa Đi', 7, 7, '/uploads/bp_dddd.mp3', '/covers/bp_dddd.jpg', 10500000),
('Attention', 8, 8, '/uploads/cp_at.mp3', '/covers/cp_at.jpg', 15000000),
('Blinding Lights', 9, 9, '/uploads/tw_bl.mp3', '/covers/tw_bl.jpg', 40000000),
('Người Tình Mùa Đông', 10, 10, '/uploads/hat_ntmd.mp3', '/covers/hat_ntmd.jpg', 6200000);

-- Thêm dữ liệu vào song_genres (Liên kết Song 1-10 với Genre 1-10)
INSERT INTO song_genres (song_id, genre_id) VALUES
(1, 7), (1, 1), -- V-Pop, Pop
(2, 3), (2, 8), -- Hip Hop, Acoustic
(3, 7), (3, 2), -- V-Pop, Ballad
(4, 1), (4, 4), -- Pop, R&B
(5, 6), (5, 1), -- K-Pop, Pop
(6, 7), (6, 9), -- V-Pop, EDM
(7, 7), (7, 1), -- V-Pop, Pop
(8, 1), (8, 4), -- Pop, R&B
(9, 4), (9, 9), -- R&B, EDM
(10, 7), (10, 2); -- V-Pop, Ballad

-- 6. Thêm 10 records vào user_preferences (Dựa trên User IDs 1-10)
INSERT INTO user_preferences (user_id, listening_pattern) VALUES
(1, '{"day_listen_min": 120, "top_genre": "EDM"}'),
(2, '{"day_listen_min": 60, "top_genre": "Ballad"}'),
(3, '{"day_listen_min": 90, "top_genre": "Hip Hop"}'),
(4, '{"day_listen_min": 45, "top_genre": "Pop"}'),
(5, '{"day_listen_min": 180, "top_genre": "Rock"}'),
(6, '{"day_listen_min": 30, "top_genre": "K-Pop"}'),
(7, '{"day_listen_min": 75, "top_genre": "Indie"}'),
(8, '{"day_listen_min": 100, "top_genre": "R&B"}'),
(9, '{"day_listen_min": 50, "top_genre": "Pop"}'),
(10, '{"day_listen_min": 150, "top_genre": "V-Pop"}');

-- 7. Thêm 10 records vào system_logs (Dựa trên User IDs 1-3)
INSERT INTO system_logs (action, description, user_id) VALUES
('LOGIN', 'User 2 logged in successfully.', 2),
('UPLOAD_SONG', 'Admin uploaded Chúng Ta Của Tương Lai.', 1),
('CREATE_PLAYLIST', 'User 3 created playlist Mùa Hè.', 3),
('DELETE_SONG', 'Admin deleted an old song.', 1),
('LOGIN_FAIL', 'User 5 failed to log in.', NULL),
('UPDATE_PROFILE', 'User 4 changed password.', 4),
('RATE_SONG', 'User 6 rated a song.', 6),
('UPLOAD_SONG', 'Admin uploaded Duyên Âm.', 1),
('LOGIN', 'User 7 logged in successfully.', 7),
('CREATE_PLAYLIST', 'User 8 created playlist Workout.', 8);

-- 8. Thêm 10 records vào favorites (Dựa trên User IDs 2-6 và Song IDs 1-10)
INSERT INTO favorites (user_id, song_id) VALUES
(2, 3), (2, 10), (3, 2), (4, 4), (4, 8),
(5, 9), (5, 5), (6, 6), (7, 1), (8, 7);

-- 9. Thêm 10 records vào song_ratings (Dựa trên User IDs 2-7 và Song IDs 1-10)
INSERT INTO song_ratings (user_id, song_id, rating, review) VALUES
(2, 3, 5, 'Rất hay, chạm đến cảm xúc!'),
(3, 2, 4, 'Cần thêm beat mạnh hơn.'),
(4, 4, 5, 'Nhạc phẩm kinh điển.'),
(5, 9, 5, 'Best R&B track.'),
(6, 6, 4, 'Nhạc bắt tai, đậm chất Việt.'),
(7, 1, 4, 'MV đẹp, giai điệu dễ nhớ.'),
(8, 7, 5, 'Tuyệt vời để đi chơi.'),
(9, 5, 3, 'Không phải gu của tôi.'),
(10, 10, 5, 'Quá lãng mạn.'),
(2, 1, 5, 'Nhạc Sếp luôn đỉnh!');

-- 10. Thêm 10 records vào listening_history (Dựa trên User IDs 2-5 và Song IDs 1-10)
INSERT INTO listening_history (user_id, song_id) VALUES
(2, 3), (2, 3), (2, 1), (3, 2), (4, 4),
(4, 4), (5, 9), (5, 9), (5, 5), (6, 6);

-- 11. Thêm 10 records vào playlists (Dựa trên User IDs 2-5)
INSERT INTO playlists (user_id, name) VALUES
(2, 'Nhạc Chill 2024'), (3, 'Hip Hop Workout'),
(4, 'Pop US/UK'), (5, 'Rock & EDM'),
(2, 'Ballad Việt'), (3, 'Những Bài Tự Kỷ'),
(4, 'Taylor Swift Hits'), (5, 'Nhạc Buổi Sáng'),
(6, 'V-Pop Hits'), (7, 'Top Trending');

-- 12. Thêm 10 records vào playlist_songs (Kết hợp Playlist 1-10 và Song 1-10)
INSERT INTO playlist_songs (playlist_id, song_id, track_order) VALUES
(1, 3, 1), (1, 10, 2), (2, 2, 1), (2, 5, 2),
(3, 4, 1), (3, 8, 2), (4, 9, 1), (4, 5, 2),
(5, 3, 1), (6, 2, 1);

-- 13. Thêm 10 records vào song_rankings (Dựa trên Song IDs 1-10 cho ngày 2025-12-04)
INSERT INTO song_rankings (song_id, ranking_date, ranks, total_views) VALUES
(9, '2025-12-04', 1, 100000), (4, '2025-12-04', 2, 95000),
(5, '2025-12-04', 3, 90000), (1, '2025-12-04', 4, 85000),
(7, '2025-12-04', 5, 80000), (8, '2025-12-04', 6, 75000),
(6, '2025-12-04', 7, 70000), (2, '2025-12-04', 8, 65000),
(10, '2025-12-04', 9, 60000), (3, '2025-12-04', 10, 55000);

-- 14. Thêm 10 records vào recommendations (Dựa trên User IDs 2-5 và Song IDs 1-10)
INSERT INTO recommendations (user_id, recommended_song_id, confidence_score, reason, clicked, liked) VALUES
(2, 1, 0.95, 'Similar to Song 3', TRUE, TRUE),
(3, 3, 0.88, 'Based on Hip Hop preference', FALSE, FALSE),
(4, 9, 0.92, 'Because you like Pop US/UK', TRUE, FALSE),
(5, 6, 0.75, 'New V-Pop release', TRUE, TRUE),
(6, 2, 0.81, 'Similar to Song 6', TRUE, FALSE),
(7, 4, 0.99, 'Top global hit', TRUE, TRUE),
(8, 7, 0.85, 'Related to Dance genre', FALSE, FALSE),
(9, 10, 0.70, 'You listened to Song 5', FALSE, FALSE),
(10, 8, 0.94, 'Based on R&B/Pop preference', TRUE, TRUE),
(2, 7, 0.80, 'For your workout playlist', FALSE, FALSE);

-- Lệnh SELECT để kiểm tra dữ liệu (tùy chọn)
-- SELECT * FROM users;
-- SELECT * FROM songs;
-- SELECT * FROM playlists;
-- SELECT * FROM playlist_songs;
