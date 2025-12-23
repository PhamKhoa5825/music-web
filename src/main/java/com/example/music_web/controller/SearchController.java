package com.example.music_web.controller;

import com.example.music_web.Entity.Playlist;
import com.example.music_web.Entity.Song;
import com.example.music_web.repository.PlaylistRepository;
import com.example.music_web.repository.SongRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
@RequestMapping("/search")
public class SearchController {

    @Autowired private SongRepository songRepository;
    @Autowired private PlaylistRepository playlistRepository;

    @GetMapping("/fragment")
    public String searchFragment(@RequestParam String keyword, Model model) {
        // Logic tìm kiếm cũ
        List<Song> songs = songRepository.searchVisibleSongs(keyword, null);
        List<Playlist> playlists = playlistRepository.findByNameContainingAndIsPublicTrue(keyword);

        model.addAttribute("songs", songs);
        model.addAttribute("playlists", playlists);

        // Trả về đoạn HTML kết quả tìm kiếm (dropdown)
        return "fragments/search_results :: dropdown";
    }
}