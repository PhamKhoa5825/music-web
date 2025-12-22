package com.example.music_web.repository;

import com.example.music_web.Entity.Song;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface SongRepository extends JpaRepository<Song, Long> {
    @Query("SELECT DISTINCT s FROM Song s " +
            "LEFT JOIN s.genres g " +
            "WHERE (:title IS NULL OR LOWER(s.title) LIKE LOWER(CONCAT('%', :title, '%'))) " +
            "AND (:artistId IS NULL OR s.artist.artistId = :artistId) " +
            "AND (:albumId IS NULL OR s.album.albumId = :albumId) " +
            "AND (:genreId IS NULL OR g.genreId = :genreId)")
    Page<Song> searchSongs(
            @Param("title") String title,
            @Param("artistId") Long artistId,
            @Param("albumId") Long albumId,
            @Param("genreId") Long genreId,
            Pageable pageable
    );
    @Query("SELECT s FROM Song s WHERE s.artist.artistId = :artistId")
    Page<Song> findByArtistId(@Param("artistId") Long artistId, Pageable pageable);

    @Query("SELECT s FROM Song s WHERE s.album.albumId = :albumId")
    Page<Song> findByAlbumId(@Param("albumId") Long albumId, Pageable pageable);

    @Query("SELECT s FROM Song s JOIN s.genres g WHERE g.genreId = :genreId")
    Page<Song> findByGenreId(@Param("genreId") Long genreId, Pageable pageable);

}