package com.example.music_web.Repository;

import com.example.music_web.Entity.Playlist;
import com.example.music_web.Entity.Song;
import com.example.music_web.Entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface PlaylistRepository extends JpaRepository<Playlist, Long> {
    List<Playlist> findByUser(User user);

    List<Playlist> findByNameContainingAndIsPublicTrue(String keyword);
}
