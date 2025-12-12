package com.example.music_web.Repository;

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
}
