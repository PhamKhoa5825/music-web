package com.example.music_web.Repository;

import com.example.music_web.Entity.Favorite;
import com.example.music_web.Entity.Song;
import com.example.music_web.Entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface FavoriteRepository extends JpaRepository<Favorite, Long> {
    Optional<Favorite> findByUserAndSong(User user, Song song);
    List<Favorite> findByUser(User user);
}
