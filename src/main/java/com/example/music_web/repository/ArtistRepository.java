package com.example.music_web.repository;

import com.example.music_web.Entity.Artist;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ArtistRepository extends JpaRepository<Artist, Long> {

    // Search artist theo tên (không phân biệt hoa thường)
    @Query("SELECT a FROM Artist a " +
            "WHERE (:name IS NULL OR LOWER(a.name) LIKE LOWER(CONCAT('%', :name, '%')))")
    Page<Artist> searchArtists(@Param("name") String name, Pageable pageable);

    // Kiểm tra xem artist có song hoặc album liên quan không (dùng để check trước khi xóa)
    @Query("SELECT CASE WHEN COUNT(s) > 0 THEN true ELSE false END " +
            "FROM Song s WHERE s.artist.artistId = :artistId")
    boolean hasSongs(@Param("artistId") Long artistId);

    @Query("SELECT CASE WHEN COUNT(a) > 0 THEN true ELSE false END " +
            "FROM Album a WHERE a.artist.artistId = :artistId")
    boolean hasAlbums(@Param("artistId") Long artistId);
}
