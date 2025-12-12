package com.example.music_web.Service;

import com.example.music_web.DTO.PlaylistRequest;
import com.example.music_web.Entity.*;
import com.example.music_web.Repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class PlaylistService {

    @Autowired private PlaylistRepository playlistRepository;
    @Autowired private PlaylistSongRepository playlistSongRepository;
    @Autowired private UserRepository userRepository; // Của Người 1
    @Autowired private SongRepository songRepository; // Của Người 2

    public Playlist createPlaylist(PlaylistRequest request) {
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        Playlist playlist = new Playlist();
        playlist.setName(request.getName());
        playlist.setUser(user);
        return playlistRepository.save(playlist);
    }

    public List<Playlist> getUserPlaylists(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return playlistRepository.findByUser(user);
    }

    public void addSongToPlaylist(Long playlistId, Long songId) {
        Playlist playlist = playlistRepository.findById(playlistId)
                .orElseThrow(() -> new RuntimeException("Playlist not found"));
        Song song = songRepository.findById(songId)
                .orElseThrow(() -> new RuntimeException("Song not found"));

        // Tạo khóa phức hợp
        PlaylistSongId id = new PlaylistSongId();
        id.setPlaylistId(playlistId);
        id.setSongId(songId);

        PlaylistSong playlistSong = new PlaylistSong();
        playlistSong.setId(id);
        playlistSong.setPlaylist(playlist);
        playlistSong.setSong(song);
        // Tự động tính thứ tự
        playlistSong.setTrackOrder(playlistSongRepository.countByPlaylist(playlist) + 1);

        playlistSongRepository.save(playlistSong);
    }

    public void removeSongFromPlaylist(Long playlistId, Long songId) {
        PlaylistSong ps = playlistSongRepository.findByPlaylist_PlaylistIdAndSong_SongId(playlistId, songId)
                .orElseThrow(() -> new RuntimeException("Song not in playlist"));
        playlistSongRepository.delete(ps);
    }
}
