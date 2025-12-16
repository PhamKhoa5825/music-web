package com.example.music_web.Controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class ViewController {

    @GetMapping("/home")
    public String home() {
        return "index";
    }

    @GetMapping("/my-music")
    public String library() {
        return "my-music";
    }

    @GetMapping("/ranking")
    public String ranking() {
        return "ranking";
    }

    @GetMapping("/song/{id}")
    public String songDetail(@PathVariable Long id, Model model) {
        // Truyền ID bài hát sang view để JS sử dụng fetch dữ liệu
        model.addAttribute("songId", id);
        return "song-detail"; // Trả về file song-detail.html
    }
}