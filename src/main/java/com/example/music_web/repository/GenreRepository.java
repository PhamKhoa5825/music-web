package com.example.music_web.repository;

import com.example.music_web.Entity.Genre;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface GenreRepository extends JpaRepository<Genre, Long> {

    @Query("SELECT g FROM Genre g " +
            "WHERE (:name IS NULL OR LOWER(g.name) LIKE LOWER(CONCAT('%', :name, '%')))")
    Page<Genre> searchGenres(@Param("name") String name, Pageable pageable);

    @Query("SELECT CASE WHEN COUNT(sg) > 0 THEN true ELSE false END " +
            "FROM Song s JOIN s.genres sg WHERE sg.genreId = :genreId")
    boolean hasSongs(@Param("genreId") Long genreId);
}