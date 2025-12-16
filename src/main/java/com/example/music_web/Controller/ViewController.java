package com.example.music_web.Controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ViewController {
    // Mọi đường dẫn UI đều trả về 'my-music' (Shell)
    // Spring Boot sẽ map file templates/my-music.html
    @GetMapping({
            "/",
            "/home",
            "/ranking",
            "/my-music",
            "/playlist/**",  // Quan trọng: Bắt tất cả playlist/1, playlist/2...
            "/song/**"       // Nếu sau này làm chi tiết bài hát
    })
    public String spaIndex() {
        return "my-music";
    }
}