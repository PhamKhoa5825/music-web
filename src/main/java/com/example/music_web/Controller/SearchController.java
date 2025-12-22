package com.example.music_web.Controller;

import com.example.music_web.Entity.Playlist;
import com.example.music_web.Entity.Song;
import com.example.music_web.Repository.PlaylistRepository;
import com.example.music_web.Repository.SongRepository;
import lombok.Builder;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/search")
public class SearchController {

    @Autowired private SongRepository songRepository;
    @Autowired private PlaylistRepository playlistRepository;

    @GetMapping
    public ResponseEntity<SearchResult> searchGlobal(@RequestParam String keyword) {
        String likeKey = "%" + keyword + "%";

        // 1. Tìm bài hát (Theo tên, Tên ca sĩ, Album)
        // Lưu ý: Cần viết Query trong Repository hoặc dùng findByTitleContaining v.v.
        // Ở đây giả định dùng method custom hoặc JPA cơ bản
        List<Song> songs = songRepository.searchVisibleSongs(keyword, null);

        // 2. Tìm Playlist công khai
        List<Playlist> playlists = playlistRepository.findByNameContainingAndIsPublicTrue(keyword);

        return ResponseEntity.ok(SearchResult.builder()
                .songs(songs)
                .playlists(playlists)
                .build());
    }

    @Data
    @Builder
    public static class SearchResult {
        private List<Song> songs;
        private List<Playlist> playlists;
    }
}