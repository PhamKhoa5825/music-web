package com.example.music_web.Repository;

import com.example.music_web.Entity.ListeningHistory;
import com.example.music_web.Entity.User;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ListeningHistoryRepository extends JpaRepository<ListeningHistory, Long> {
    List<ListeningHistory> findByUserOrderByListenedAtDesc(User user);
    List<ListeningHistory> findTop50ByUser_UserIdOrderByListenedAtDesc(Long userId);

    // 1. Thống kê theo KHUNG GIỜ (0h - 23h)
    // Nếu genreId null thì lấy tất cả, nếu có thì lọc theo genre
    @Query("SELECT HOUR(h.listenedAt), COUNT(h) FROM ListeningHistory h " +
            "LEFT JOIN h.song s LEFT JOIN s.genres g " +
            "WHERE (:genreId IS NULL OR g.genreId = :genreId) " +
            "AND YEAR(h.listenedAt) = :year " +
            "GROUP BY HOUR(h.listenedAt) ORDER BY HOUR(h.listenedAt)")
    List<Object[]> getStatsByHourOfDay(@Param("genreId") Long genreId, @Param("year") int year);

    // 2. Thống kê theo NGÀY (Trong 1 tháng cụ thể)
    @Query("SELECT DAY(h.listenedAt), COUNT(h) FROM ListeningHistory h " +
            "LEFT JOIN h.song s LEFT JOIN s.genres g " +
            "WHERE (:genreId IS NULL OR g.genreId = :genreId) " +
            "AND YEAR(h.listenedAt) = :year AND MONTH(h.listenedAt) = :month " +
            "GROUP BY DAY(h.listenedAt) ORDER BY DAY(h.listenedAt)")
    List<Object[]> getStatsByDayOfMonth(@Param("genreId") Long genreId, @Param("year") int year, @Param("month") int month);

    // 3. Thống kê theo THÁNG (Trong 1 năm cụ thể)
    @Query("SELECT MONTH(h.listenedAt), COUNT(h) FROM ListeningHistory h " +
            "LEFT JOIN h.song s LEFT JOIN s.genres g " +
            "WHERE (:genreId IS NULL OR g.genreId = :genreId) " +
            "AND YEAR(h.listenedAt) = :year " +
            "GROUP BY MONTH(h.listenedAt) ORDER BY MONTH(h.listenedAt)")
    List<Object[]> getStatsByMonthOfYear(@Param("genreId") Long genreId, @Param("year") int year);


    // 1. Theo NGÀY
    @Query("SELECT COUNT(h) FROM ListeningHistory h WHERE DATE(h.listenedAt) = :date")
    long countByDate(@Param("date") LocalDate date);

    @Query("SELECT s, COUNT(h) as total FROM ListeningHistory h JOIN h.song s " +
            "WHERE DATE(h.listenedAt) = :date GROUP BY s ORDER BY total DESC")
    List<Object[]> findTopSongsByDate(@Param("date") LocalDate date);

    // 2. Theo THÁNG
    @Query("SELECT COUNT(h) FROM ListeningHistory h WHERE YEAR(h.listenedAt) = :year AND MONTH(h.listenedAt) = :month")
    long countByMonth(@Param("year") int year, @Param("month") int month);

    @Query("SELECT s, COUNT(h) as total FROM ListeningHistory h JOIN h.song s " +
            "WHERE YEAR(h.listenedAt) = :year AND MONTH(h.listenedAt) = :month " +
            "GROUP BY s ORDER BY total DESC")
    List<Object[]> findTopSongsByMonth(@Param("year") int year, @Param("month") int month);

    // 3. Theo NĂM
    @Query("SELECT COUNT(h) FROM ListeningHistory h WHERE YEAR(h.listenedAt) = :year")
    long countByYear(@Param("year") int year);

    @Query("SELECT s, COUNT(h) as total FROM ListeningHistory h JOIN h.song s " +
            "WHERE YEAR(h.listenedAt) = :year GROUP BY s ORDER BY total DESC")
    List<Object[]> findTopSongsByYear(@Param("year") int year);

    //*****************************************************
    // KIỂM TRA XEM USER ĐÃ NGHE BÀI HÁT NÀY GẦN ĐÂY CHƯA (Tránh spam view)
    @Query("SELECT COUNT(h) > 0 FROM ListeningHistory h " +
            "WHERE h.user.userId = :userId AND h.song.songId = :songId " +
            "AND h.listenedAt > :timeThreshold")
    boolean existsByUserAndSongAndListenedAtAfter(@Param("userId") Long userId,
                                                  @Param("songId") Long songId,
                                                  @Param("timeThreshold") LocalDateTime timeThreshold);

    // --- QUERY TẠO BẢNG XẾP HẠNG ĐỘNG (CORE) ---
    // Đếm số lượt nghe trong khoảng thời gian, có thể lọc theo thể loại
    @Query("SELECT h.song, COUNT(h) as playCount FROM ListeningHistory h " +
            "JOIN h.song s " +
            "LEFT JOIN s.genres g " +
            "WHERE h.listenedAt BETWEEN :startTime AND :endTime " +
            "AND (:genreId IS NULL OR g.genreId = :genreId) " +
            "GROUP BY h.song " +
            "ORDER BY playCount DESC")
    List<Object[]> findTopSongsByTimeAndGenre(@Param("startTime") LocalDateTime startTime,
                                              @Param("endTime") LocalDateTime endTime,
                                              @Param("genreId") Long genreId,
                                              Pageable pageable);
}
