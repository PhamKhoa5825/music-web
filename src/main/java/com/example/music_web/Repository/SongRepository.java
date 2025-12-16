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
    // Tìm bài hát thuộc danh sách các thể loại (cho gợi ý)
    @Query("SELECT DISTINCT s FROM Song s JOIN s.genres g WHERE g IN :genres")
    List<Song> findByGenresIn(@Param("genres") Set<Genre> genres);

    // Tìm 5 bài hát cùng thể loại (trừ bài hiện tại)
    @Query("SELECT DISTINCT s FROM Song s JOIN s.genres g WHERE g IN :genres AND s.songId <> :currentSongId")
    List<Song> findRelatedSongs(@Param("genres") List<Genre> genres, @Param("currentSongId") Long currentSongId, Pageable pageable);

    // --- 1. LẤY TẤT CẢ BÀI HÁT ĐANG HIỆN (Cho Dashboard) ---
    List<Song> findByIsHiddenFalse();

    // --- 2. GỢI Ý BÀI HÁT (Chỉ lấy bài chưa ẩn) ---
    @Query("SELECT DISTINCT s FROM Song s JOIN s.genres g WHERE g IN :genres AND s.isHidden = false")
    List<Song> findByGenresInAndIsHiddenFalse(@Param("genres") Set<Genre> genres);

    @Query("SELECT s FROM Song s " +
            "WHERE " +
            // 1. Tìm theo từ khóa (Tên bài HOẶC Tên ca sĩ)
            "(:keyword IS NULL OR :keyword = '' OR " +
            "LOWER(s.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(s.artist.name) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
            "AND " +
            // 2. Tìm theo ngày đăng (So sánh phần ngày của uploadDate)
            "(:date IS NULL OR CAST(s.uploadDate AS LocalDate) = :date)")
    List<Song> searchSongs(@Param("keyword") String keyword, @Param("date") LocalDate date);

    // --- 3. TÌM KIẾM CHO USER (Chỉ tìm bài chưa ẩn) ---
    // Copy logic của searchSongs nhưng thêm điều kiện: AND s.isHidden = false
    @Query("SELECT s FROM Song s " +
            "WHERE " +
            "s.isHidden = false " + // <--- QUAN TRỌNG: Chỉ lấy bài hiện
            "AND " +
            "(:keyword IS NULL OR :keyword = '' OR " +
            "LOWER(s.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(s.artist.name) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
            "AND " +
            "(:date IS NULL OR CAST(s.uploadDate AS LocalDate) = :date)")
    List<Song> searchVisibleSongs(@Param("keyword") String keyword, @Param("date") LocalDate date);


    // --- CẬP NHẬT MỚI: TÌM KIẾM ĐA NĂNG ---
    // Sử dụng @Query để join bảng Artist và Album
    // LOWER() để tìm kiếm không phân biệt hoa thường
    @Query("SELECT s FROM Song s " +
            "LEFT JOIN s.artist a " +
            "LEFT JOIN s.album al " +
            "WHERE (LOWER(s.title) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "OR LOWER(a.name) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "OR LOWER(al.title) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
            "AND s.isHidden = false")
    List<Song> searchComplex(@Param("keyword") String keyword);
}
