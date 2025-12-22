package com.example.music_web.Repository;

import com.example.music_web.Entity.Genre;
import com.example.music_web.Entity.Song;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

@Repository
public interface SongRepository extends JpaRepository<Song, Long> {

    // 1. Core Search: Tìm kiếm đa năng (Title, Artist, Album) + Lọc ẩn hiện + Lọc ngày
    @Query("SELECT DISTINCT s FROM Song s " +
            "LEFT JOIN s.artist a " +
            "LEFT JOIN s.album al " +
            "WHERE s.isHidden = false " +
            "AND (:keyword IS NULL OR :keyword = '' OR " +
            "LOWER(s.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(a.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(al.title) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
            "AND (:date IS NULL OR CAST(s.uploadDate AS LocalDate) = :date)")
    List<Song> searchVisibleSongs(@Param("keyword") String keyword, @Param("date") LocalDate date);

    // 2. Tìm bài hát cùng thể loại (Cho tính năng Gợi ý / Related)
    @Query("SELECT DISTINCT s FROM Song s JOIN s.genres g " +
            "WHERE g IN :genres " +
            "AND s.songId <> :currentSongId " +
            "AND s.isHidden = false")
    List<Song> findRelatedSongs(@Param("genres") java.util.Collection<Genre> genres,
                                @Param("currentSongId") Long currentSongId,
                                Pageable pageable);

    // 3. Tìm bài hát theo List Genres (Cho AI Recommendation)
    @Query("SELECT DISTINCT s FROM Song s JOIN s.genres g WHERE g IN :genres AND s.isHidden = false")
    List<Song> findByGenresInAndIsHiddenFalse(@Param("genres") Set<Genre> genres);

    // 4. Lấy tất cả bài hát đang hiện (Cho Dashboard)
    List<Song> findByIsHiddenFalse();

    // 5. Query phục vụ AI lọc nâng cao (Các tiêu chí BPM, Energy...)
    @Query("SELECT s FROM Song s WHERE " +
            "s.isHidden = false AND " +
            "(:minBpm IS NULL OR s.bpm >= :minBpm) AND " +
            "(:maxBpm IS NULL OR s.bpm <= :maxBpm) AND " +
            "(:minEnergy IS NULL OR s.energyLevel >= :minEnergy) AND " +
            "(:minDance IS NULL OR s.danceability >= :minDance) AND " +
            "(:language IS NULL OR s.language = :language) AND " +
            "(:explicit IS NULL OR s.explicit = :explicit)")
    List<Song> findSongsByActivityParams(
            @Param("minBpm") Integer minBpm,
            @Param("maxBpm") Integer maxBpm,
            @Param("minEnergy") Integer minEnergy,
            @Param("minDance") Integer minDance,
            @Param("language") String language,
            @Param("explicit") Boolean explicit,
            Pageable pageable);
}