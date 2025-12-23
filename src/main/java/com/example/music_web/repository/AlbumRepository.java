package com.example.music_web.repository;

import com.example.music_web.Entity.Album;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface AlbumRepository extends JpaRepository<Album, Long> {

    @Query("SELECT a FROM Album a " +
            "WHERE (:title IS NULL OR LOWER(a.title) LIKE LOWER(CONCAT('%', :title, '%')))")
    Page<Album> searchAlbums(@Param("title") String title, Pageable pageable);

    @Query("SELECT CASE WHEN COUNT(s) > 0 THEN true ELSE false END " +
            "FROM Song s WHERE s.album.albumId = :albumId")
    boolean hasSongs(@Param("albumId") Long albumId);
}