package com.example.music_web.Repository;

import com.example.music_web.Entity.Favorite;
import com.example.music_web.Entity.Song;
import com.example.music_web.Entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface FavoriteRepository extends JpaRepository<Favorite, Long> {
    List<Favorite> findByUser(User user);

    // 1. Đếm tổng số lượt thích của một bài hát (Dùng cho trang chi tiết)
    // Spring sẽ tự động hiểu: SELECT COUNT(*) FROM favorites WHERE song_id = ?
    Long countBySong(Song song);

    // 2. Kiểm tra xem User đã like bài này chưa (Để tô đỏ nút tim)
    boolean existsByUserAndSong(User user, Song song);

    // 3. Tìm Favorite cụ thể để xóa (Khi user bấm Unlike)
    Optional<Favorite> findByUserAndSong(User user, Song song);

    // (Tùy chọn) Xóa trực tiếp
    void deleteByUserAndSong(User user, Song song);
}
