package com.example.music_web.Controller;

import com.example.music_web.Entity.Playlist;
import com.example.music_web.Repository.PlaylistRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller // Lưu ý: Dùng @Controller, không phải @RestController
public class PlaylistViewController {

    @Autowired
    private PlaylistRepository playlistRepository;

    // API này trả về trang HTML chi tiết playlist
    @GetMapping("/playlist/{id}")
    public String viewPlaylistDetail(@PathVariable Long id, Model model) {
        Playlist playlist = playlistRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Playlist not found"));

        // Truyền dữ liệu playlist vào HTML
        model.addAttribute("playlist", playlist);

        // Trả về file playlist-detail.html trong thư mục templates
        return "playlist-detail";
    }
}