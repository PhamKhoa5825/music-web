package com.example.music_web.Controller;

import com.example.music_web.DTO.AddSongRequest;
import com.example.music_web.DTO.PlaylistRequest;
import com.example.music_web.Entity.Playlist;
import com.example.music_web.Entity.Song;
import com.example.music_web.Repository.SongRepository;
import com.example.music_web.Service.IStorageService;
import com.example.music_web.Service.PlaylistService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

@RestController
@RequestMapping("/api/playlists")
public class PlaylistController {

    @Autowired private PlaylistService playlistService;
    @Autowired private SongRepository songRepository;
    @Autowired private IStorageService storageService;


    @GetMapping("/{playlistId}")
    public ResponseEntity<Playlist> getPlaylistDetail(@PathVariable Long playlistId) {
        // Lưu ý: Đảm bảo Playlist Entity không bị vòng lặp vô tận (Infinite Recursion) khi serialize JSON
        // Nếu bị lỗi StackOverflow, hãy dùng DTO hoặc @JsonIgnore ở Entity
        return ResponseEntity.ok(playlistService.getPlaylistById(playlistId));
    }

    @PostMapping
    public ResponseEntity<Playlist> createPlaylist(@RequestBody PlaylistRequest request) {
        return ResponseEntity.ok(playlistService.createPlaylist(request));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Playlist>> getUserPlaylists(@PathVariable Long userId) {
        return ResponseEntity.ok(playlistService.getUserPlaylists(userId));
    }

    // API Mới: Cập nhật thông tin Playlist (Patch)
    @PatchMapping("/{playlistId}")
    public ResponseEntity<Playlist> updatePlaylist(@PathVariable Long playlistId, @RequestBody Map<String, Object> body) {
        String name = (String) body.get("name");
        String description = (String) body.get("description");

        // Xử lý an toàn cho Boolean (tránh NullPointer)
        Boolean isPublic = null;
        if (body.get("isPublic") != null) {
            isPublic = Boolean.valueOf(body.get("isPublic").toString());
        }

        // Gọi Service (Bạn cần cập nhật Service để nhận thêm isPublic nhé)
        return ResponseEntity.ok(playlistService.updatePlaylistInfo(playlistId, name, description, isPublic));
    }

    // API Mới: Tìm kiếm bài hát để thêm vào Playlist (Spotify Style)
    @GetMapping("/search-songs")
    public ResponseEntity<List<Song>> searchSongsForAdd(@RequestParam String keyword) {
        // Tận dụng hàm searchVisibleSongs đã có trong SongRepository
        // Chỉ trả về tối đa 10 kết quả để UI gọn nhẹ
        List<Song> songs = songRepository.searchVisibleSongs(keyword, null);
        return ResponseEntity.ok(songs.size() > 10 ? songs.subList(0, 10) : songs);
    }

    @PostMapping("/{playlistId}/songs")
    public ResponseEntity<?> addSong(@PathVariable Long playlistId, @RequestBody AddSongRequest request) {
        try {
            playlistService.addSongToPlaylist(playlistId, request.getSongId());
            return ResponseEntity.ok(Map.of("message", "Đã thêm bài hát thành công!"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @DeleteMapping("/{playlistId}/songs/{songId}")
    public ResponseEntity<?> removeSong(@PathVariable Long playlistId, @PathVariable Long songId) {
        playlistService.removeSongFromPlaylist(playlistId, songId);
        return ResponseEntity.ok(Map.of("message", "Đã xóa bài hát"));
    }

    @PostMapping(value = "/{playlistId}/images", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> uploadPlaylistImages(
            @PathVariable Long playlistId,
            @RequestParam(value = "cover", required = false) MultipartFile cover,
            @RequestParam(value = "background", required = false) MultipartFile background) {

        try {
            String coverUrl = null;
            String bgUrl = null;

            // Upload nếu có file
            if (cover != null && !cover.isEmpty()) {
                coverUrl = storageService.uploadFile(cover);
            }

            if (background != null && !background.isEmpty()) {
                bgUrl = storageService.uploadFile(background);
            }

            // Update Database
            playlistService.updatePlaylistImages(playlistId, coverUrl, bgUrl);

            // --- [FIX LỖI TẠI ĐÂY] ---
            // Thay Map.of bằng HashMap để chấp nhận giá trị null
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Upload thành công");
            response.put("coverUrl", coverUrl);
            response.put("bgUrl", bgUrl);

            return ResponseEntity.ok(response);

        } catch (IOException e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Lỗi upload file: " + e.getMessage());
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }
}
