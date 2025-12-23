package com.example.music_web.repository;

import com.example.music_web.Entity.Playlist;
import com.example.music_web.Entity.User;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PlaylistRepository extends JpaRepository<Playlist, Long> {
    List<Playlist> findByUser(User user);

    List<Playlist> findByIsPublicTrue();

    List<Playlist> findByNameContainingAndIsPublicTrue(String keyword);


}
