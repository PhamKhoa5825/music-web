package com.example.music_web.controller;

import com.example.music_web.service.GenreService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {
    @Autowired
    private GenreService genreService;

    @GetMapping("/")
    public String home(Model model, Authentication authentication) {
        model.addAttribute("activeTab", "home");
        return "index";
    }

    @GetMapping("/for-you")
    public String forYouPage(Model model) {
        model.addAttribute("activeTab", "for-you");
        return "for-you";
    }

    @GetMapping("/gemini")
    public String aiDjPage(Model model) {
        // activeTab dùng để highlight menu bên trái (nếu layout hỗ trợ)
        model.addAttribute("activeTab", "ai-dj");
        // Lấy danh sách thể loại để hiển thị trong select box
        model.addAttribute("genres", genreService.getAllGenres(null, PageRequest.of(0, 100, Sort.by("name"))).getContent());
        return "ai-dj";
    }

    // Thêm mới
    @GetMapping("/history")
    public String historyPage(Model model) {
        model.addAttribute("activeTab", "history");
        return "history";
    }

    // Thêm mới
    @GetMapping("/favorites")
    public String favoritesPage(Model model) {
        model.addAttribute("activeTab", "favorites");
        return "favorites";
    }
}