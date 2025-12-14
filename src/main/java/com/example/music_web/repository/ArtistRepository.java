package com.example.music_web.repository;

import com.example.music_web.Entity.Artist;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ArtistRepository extends JpaRepository<Artist, Long> {
    // Tương tự, đã có sẵn findAllById để service sử dụng
}
