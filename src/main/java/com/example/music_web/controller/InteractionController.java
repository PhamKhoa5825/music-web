package com.example.music_web.controller;

import com.example.music_web.Entity.Favorite;
import com.example.music_web.Entity.ListeningHistory;
import com.example.music_web.Entity.Song;
import com.example.music_web.Entity.User;
import com.example.music_web.repository.FavoriteRepository;
import com.example.music_web.repository.SongRankingRepository;
import com.example.music_web.repository.SongRepository;
import com.example.music_web.repository.UserRepository;
import com.example.music_web.service.InteractionService;
import com.example.music_web.service.RecommendationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
public class InteractionController {

    @Autowired private InteractionService interactionService;
    @Autowired private UserRepository userRepository;
    @Autowired private SongRepository songRepository;
    @Autowired private FavoriteRepository favoriteRepository;
    @Autowired private RecommendationService recommendationService;
    @Autowired private SongRankingRepository rankingRepository;


    // Thêm API này để frontend check quyền
    @GetMapping("/users/{id}")
    public ResponseEntity<User> getUserInfo(@PathVariable Long id) {
        return ResponseEntity.ok(userRepository.findById(id).orElseThrow());
    }

    // --- 1. API LỊCH SỬ NGHE (History) ---
    @GetMapping("/history")
    public ResponseEntity<List<ListeningHistory>> getUserHistory(@RequestParam Long userId) {
        return ResponseEntity.ok(interactionService.getUserHistory(userId));
    }

    @DeleteMapping("/history/{historyId}")
    public ResponseEntity<?> deleteHistoryItem(@PathVariable Long historyId) {
        interactionService.deleteHistoryById(historyId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/history")
    public ResponseEntity<String> logListen(@RequestParam Long songId) {
        // 1. Lấy thông tin người dùng đang đăng nhập từ Security Context
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        // Kiểm tra nếu chưa đăng nhập (anonymousUser) thì bỏ qua
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            return ResponseEntity.ok("Guest user - History skipped");
        }

        // 2. Tìm User trong DB dựa vào username
        String username = auth.getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // 3. Ghi lịch sử
        interactionService.logListeningHistory(user.getUserId(), songId);
        return ResponseEntity.ok("Logged listening history for: " + username);
    }

    // --- FAVORITE APIs ---
    @GetMapping("/favorites/user/{userId}")
    public ResponseEntity<List<SongResponse>> getUserFavorites(@PathVariable Long userId) {
        User user = userRepository.findById(userId).orElseThrow();
        List<Favorite> favorites = favoriteRepository.findByUser(user);

        List<SongResponse> songs = favorites.stream().map(fav -> {
            Song s = fav.getSong();
            SongResponse dto = new SongResponse();
            dto.setSongId(s.getSongId());
            dto.setTitle(s.getTitle());
            dto.setArtistName(s.getArtist().getName());
            dto.setImageUrl(s.getCoverImage());
            dto.setMusicUrl(s.getFilePath());
            dto.setAlbumTitle(s.getAlbum() != null ? s.getAlbum().getTitle() : "");
            return dto;
        }).collect(Collectors.toList());

        return ResponseEntity.ok(songs);
    }

    @PostMapping("/favorites/toggle")
    public ResponseEntity<String> toggleFavorite(@RequestParam Long userId, @RequestParam Long songId) {
        String result = interactionService.toggleFavorite(userId, songId);
        return ResponseEntity.ok(result);
    }

    // --- FOR YOU APIs ---
    @GetMapping("/foryou/{userId}")
    public ResponseEntity<Map<String, Object>> getForYouData(@PathVariable Long userId) {
        Map<String, Object> response = new HashMap<>();
        List<Song> dailyMix = recommendationService.getRecommendationsForUser(userId);
        response.put("dailyMix", dailyMix);
        // Có thể thêm trending, discovery nếu service hỗ trợ
        return ResponseEntity.ok(response);
    }

    static class SongResponse {
        public Long songId;
        public String title;
        public String artistName;
        public String imageUrl;
        public String musicUrl;
        public String albumTitle;
        // Getters Setters...
        public void setSongId(Long id) { this.songId = id; }
        public void setTitle(String t) { this.title = t; }
        public void setArtistName(String a) { this.artistName = a; }
        public void setImageUrl(String i) { this.imageUrl = i; }
        public void setMusicUrl(String m) { this.musicUrl = m; }
        public void setAlbumTitle(String a) { this.albumTitle = a; }
    }
}
