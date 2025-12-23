package com.example.music_web.repository;

import com.example.music_web.Entity.Playlist;
import com.example.music_web.Entity.PlaylistSong;
import com.example.music_web.Entity.PlaylistSongId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PlaylistSongRepository extends JpaRepository<PlaylistSong, PlaylistSongId> {
    Integer countByPlaylist(Playlist playlist);
    Optional<PlaylistSong> findByPlaylist_PlaylistIdAndSong_SongId(Long playlistId, Long songId);

    // Kiểm tra bài hát đã có trong playlist chưa (trả về true/false)
    boolean existsByPlaylist_PlaylistIdAndSong_SongId(Long playlistId, Long songId);
}
