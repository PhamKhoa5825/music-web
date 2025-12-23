package com.example.music_web.controller;

import com.example.music_web.Entity.Song;
import com.example.music_web.service.InteractionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequestMapping("/library")
public class LibraryController {

    @Autowired
    private InteractionService interactionService; // Service lấy history/favorites

    // Trang chính (Mặc định load tab Favorites)
    @GetMapping
    public String myMusicPage(Model model) {
        // Load data mặc định
        model.addAttribute("activeTab", "favorites");
        return "for-you"; // Trả về khung trang
    }

    // Fragment: Tab Bài hát yêu thích
    @GetMapping("/favorites")
    public String getFavoritesFragment(Model model) {
        Long userId = 1L;
        List<Song> songs = interactionService.getLikedSongs(userId);
        model.addAttribute("songs", songs);
        return "for-you :: song-list-fragment"; // Tái sử dụng fragment hiển thị list
    }

    // Fragment: Tab Lịch sử
    @GetMapping("/history")
    public String getHistoryFragment(Model model) {
        Long userId = 1L;
        List<Song> songs = interactionService.getHistorySongs(userId);
        model.addAttribute("songs", songs);
        model.addAttribute("isHistory", true); // Để hiện nút xóa history
        return "history :: song-list-fragment";
    }
}
