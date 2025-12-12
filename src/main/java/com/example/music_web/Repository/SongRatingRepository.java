package com.example.music_web.Repository;

import com.example.music_web.Entity.SongRating;
import com.example.music_web.Entity.Song;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface SongRatingRepository extends JpaRepository<SongRating, Long> {
    List<SongRating> findBySong(Song song);
}
