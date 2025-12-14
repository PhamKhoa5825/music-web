package com.example.music_web.Controller;

import com.example.music_web.Entity.ListeningHistory;
import com.example.music_web.Entity.Playlist;
import com.example.music_web.Entity.SongRating;
import com.example.music_web.Entity.User;
import com.example.music_web.Repository.UserRepository;
import com.example.music_web.Service.InteractionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class InteractionController {

    @Autowired private InteractionService interactionService;
    @Autowired private UserRepository userRepository;

    // Thêm API này để frontend check quyền
    @GetMapping("/users/{id}")
    public ResponseEntity<User> getUserInfo(@PathVariable Long id) {
        return ResponseEntity.ok(userRepository.findById(id).orElseThrow());
    }

    // --- 1. API LỊCH SỬ NGHE (History) ---

    // POST /api/history?userId=1&songId=5 -> Ghi nhận lượt nghe (gọi khi bấm Play)
    @PostMapping("/history")
    public ResponseEntity<String> logListen(@RequestParam Long userId, @RequestParam Long songId) {
        interactionService.logListeningHistory(userId, songId);
        return ResponseEntity.ok("Logged listening history");
    }

    // GET /api/history/user/1 -> Lấy danh sách đã nghe
    @GetMapping("/history/user/{userId}")
    public ResponseEntity<List<ListeningHistory>> getUserHistory(@PathVariable Long userId) {
        return ResponseEntity.ok(interactionService.getUserHistory(userId));
    }

    // --- Favorite APIs ---
    @PostMapping("/favorites/toggle")
    public ResponseEntity<String> toggleFavorite(@RequestParam Long userId, @RequestParam Long songId) {
        String result = interactionService.toggleFavorite(userId, songId);
        return ResponseEntity.ok(result);
    }
}
