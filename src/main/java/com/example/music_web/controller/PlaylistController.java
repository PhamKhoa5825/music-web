package com.example.music_web.controller;

import com.example.music_web.Entity.Playlist;
import com.example.music_web.Entity.PlaylistSong;
import com.example.music_web.Entity.Song;
import com.example.music_web.Entity.User;
import com.example.music_web.dto.response.UploadResponse;
import com.example.music_web.repository.PlaylistRepository;
import com.example.music_web.repository.PlaylistSongRepository;
import com.example.music_web.repository.SongRepository;
import com.example.music_web.repository.UserRepository;
import com.example.music_web.service.CloudinaryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/playlists")
public class PlaylistController {

    @Autowired private PlaylistRepository playlistRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private SongRepository songRepository;
    @Autowired private CloudinaryService cloudinaryService; // Service upload ảnh
    @Autowired private PlaylistSongRepository playlistSongRepository;

    // 1. Danh sách Playlist
    @GetMapping
    public String myPlaylists(Model model, Authentication auth) {
        User user = getAuthenticatedUser(auth);
        List<Playlist> playlists = playlistRepository.findByUser(user);
        model.addAttribute("playlists", playlists);
        model.addAttribute("activeTab", "playlists");
        return "playlists/list";
    }

    // 2. Form Tạo mới
    @GetMapping("/create")
    public String showCreateForm(Model model) {
        model.addAttribute("playlist", new Playlist());
        model.addAttribute("pageTitle", "Tạo Playlist Mới");
        return "playlists/upload";
    }

    // 3. Form Chỉnh sửa
    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model) {
        Playlist playlist = playlistRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Playlist không tồn tại"));
        model.addAttribute("playlist", playlist);
        model.addAttribute("pageTitle", "Chỉnh Sửa Playlist");
        return "playlists/upload";
    }

    // 4. Lưu (Tạo mới hoặc Cập nhật)
    @PostMapping("/save")
    public String savePlaylist(@ModelAttribute Playlist playlist,
                               @RequestParam(value = "coverFile", required = false) MultipartFile coverFile,
                               @RequestParam(value = "backgroundFile", required = false) MultipartFile bgFile,
                               Authentication auth) throws IOException {
        User user = getAuthenticatedUser(auth);

        Playlist savedPlaylist;
        if (playlist.getPlaylistId() != null) {
            // Update
            savedPlaylist = playlistRepository.findById(playlist.getPlaylistId()).orElseThrow();
            savedPlaylist.setName(playlist.getName());
            savedPlaylist.setDescription(playlist.getDescription());
            savedPlaylist.setIsPublic(playlist.getIsPublic());
        } else {
            // Create
            savedPlaylist = playlist;
            savedPlaylist.setUser(user);
            savedPlaylist.setCreatedAt(LocalDateTime.now());
            savedPlaylist.setPlaylistSongs(new ArrayList<>());
        }

        // Upload ảnh nếu có
        if (coverFile != null && !coverFile.isEmpty()) {
            UploadResponse response = cloudinaryService.uploadCover(coverFile);
            savedPlaylist.setImageUrl(response.getUrl()); // Lấy URL string từ response
        }
        if (bgFile != null && !bgFile.isEmpty()) {
            UploadResponse response = cloudinaryService.uploadCover(bgFile);
            savedPlaylist.setBackgroundImage(response.getUrl());
        }

        playlistRepository.save(savedPlaylist);
        return "redirect:/playlists/" + savedPlaylist.getPlaylistId(); // Về trang chi tiết
    }

    @GetMapping("/delete/{id}")
    public String deletePlaylist(@PathVariable Long id, Authentication auth) {
        // Kiểm tra quyền sở hữu nếu cần
        playlistRepository.deleteById(id);
        return "redirect:/playlists";
    }

    // 5. Xem chi tiết
    @GetMapping("/{id}")
    public String viewPlaylist(@PathVariable Long id, Model model) {
        Playlist playlist = playlistRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid Id"));

        // Fix lỗi lazy loading nếu cần
        playlist.getPlaylistSongs().size();

        model.addAttribute("playlist", playlist);
        model.addAttribute("activeTab", "playlists");
        return "playlists/detail";
    }

    // --- API JSON CHO JAVASCRIPT ---

    // Tìm bài hát để thêm
    @GetMapping("/search-songs")
    @ResponseBody
    public List<Map<String, Object>> searchSongs(@RequestParam String keyword) {
        if (keyword == null || keyword.trim().length() < 2) {
            return new ArrayList<>();
        }
        // Tìm 10 bài hát theo tên
        List<Song> songs = songRepository.findByTitleContainingIgnoreCase(keyword);
        // Chuyển đổi sang Map để tránh lỗi JSON đệ quy và khớp với JS
        return songs.stream().limit(10).map(s -> {
            String img = s.getCoverImage();
            if (img == null || img.isEmpty()) img = "/images/default.png";

            return Map.of(
                    "songId", (Object)s.getSongId(),
                    "title", s.getTitle(),
                    "artistName", s.getArtist().getName(), // Lưu ý: Lấy tên nghệ sĩ
                    "imageUrl", img
            );
        }).toList();
    }

    // Thêm bài hát vào playlist
    @PostMapping("/{id}/add-song")
    @ResponseBody
    public ResponseEntity<?> addSong(@PathVariable Long id, @RequestParam Long songId) {
        Playlist pl = playlistRepository.findById(id).orElseThrow();
        Song song = songRepository.findById(songId).orElseThrow();

        // Kiểm tra xem bài hát đã có trong playlist chưa (Dựa trên PlaylistSong)
        // Giả sử getter của list là getPlaylistSongs() hoặc getSongs() trả về List<PlaylistSong>
        boolean exists = pl.getPlaylistSongs().stream()
                .anyMatch(ps -> ps.getSong().getSongId().equals(songId));

        if (!exists) {
            // SỬA LỖI 1: Tạo đối tượng PlaylistSong thay vì add Song trực tiếp
            PlaylistSong newItem = new PlaylistSong();
            newItem.setPlaylist(pl);
            newItem.setSong(song);
            newItem.setAddedAt(LocalDateTime.now());

            // Lưu đối tượng trung gian
            playlistSongRepository.save(newItem);
        }
        return ResponseEntity.ok().build();
    }

    // Xóa bài hát khỏi playlist
    @PostMapping("/{id}/remove-song")
    @ResponseBody
    public ResponseEntity<?> removeSong(@PathVariable Long id, @RequestParam Long songId) {
        Playlist pl = playlistRepository.findById(id).orElseThrow();
        // Tìm PlaylistSong cần xóa
        PlaylistSong itemToDelete = pl.getPlaylistSongs().stream()
                .filter(ps -> ps.getSong().getSongId().equals(songId))
                .findFirst()
                .orElse(null);

        if (itemToDelete != null) {
            // Xóa đối tượng trung gian
            playlistSongRepository.delete(itemToDelete);
        }
        return ResponseEntity.ok().build();
    }

    private User getAuthenticatedUser(Authentication auth) {
        return userRepository.findByUsername(auth.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));
    }
}