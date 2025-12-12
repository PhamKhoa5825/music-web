package com.example.music_web.Controller;

import com.example.music_web.DTO.AddSongRequest;
import com.example.music_web.DTO.PlaylistRequest;
import com.example.music_web.Entity.Playlist;
import com.example.music_web.Service.PlaylistService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/playlists")
public class PlaylistController {

    @Autowired private PlaylistService playlistService;

    @PostMapping
    public ResponseEntity<Playlist> createPlaylist(@RequestBody PlaylistRequest request) {
        return ResponseEntity.ok(playlistService.createPlaylist(request));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Playlist>> getUserPlaylists(@PathVariable Long userId) {
        return ResponseEntity.ok(playlistService.getUserPlaylists(userId));
    }

    @PostMapping("/{playlistId}/songs")
    public ResponseEntity<String> addSong(@PathVariable Long playlistId, @RequestBody AddSongRequest request) {
        playlistService.addSongToPlaylist(playlistId, request.getSongId());
        return ResponseEntity.ok("Song added to playlist");
    }

    @DeleteMapping("/{playlistId}/songs/{songId}")
    public ResponseEntity<String> removeSong(@PathVariable Long playlistId, @PathVariable Long songId) {
        playlistService.removeSongFromPlaylist(playlistId, songId);
        return ResponseEntity.ok("Song removed from playlist");
    }
}
