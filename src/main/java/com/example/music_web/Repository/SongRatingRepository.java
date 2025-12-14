package com.example.music_web.Repository;

import com.example.music_web.Entity.SongRating;
import com.example.music_web.Entity.Song;
import com.example.music_web.Entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface SongRatingRepository extends JpaRepository<SongRating, Long> {
    List<SongRating> findBySong(Song song);

    Optional<SongRating> findByUserAndSong(User user, Song song);

    // Tính điểm trung bình rating của 1 bài hát
    @Query("SELECT AVG(r.rating) FROM SongRating r WHERE r.song.songId = :songId")
    Double getAverageRating(@Param("songId") Long songId);

    // Đếm tổng số lượt đánh giá
    Long countBySongSongId(Long songId);
}
