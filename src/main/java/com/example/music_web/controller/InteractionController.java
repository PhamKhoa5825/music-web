package com.example.music_web.controller;

import com.example.music_web.Entity.Favorite;
import com.example.music_web.Entity.ListeningHistory;
import com.example.music_web.Entity.Song;
import com.example.music_web.Entity.User;
import com.example.music_web.repository.*;
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

    // ... các autowired cũ ...
    @Autowired private ListeningHistoryRepository historyRepository;

    // API trả về danh sách lịch sử nghe nhạc dạng JSON
    @GetMapping("/history/{userId}")
    public ResponseEntity<?> getUserHistory(@PathVariable Long userId) {
        // 1. Kiểm tra User có tồn tại không
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // 2. Lấy danh sách từ DB (Sắp xếp mới nhất trước)
        // Hàm findByUserOrderByListenedAtDesc đã có trong Repository của bạn
        List<ListeningHistory> historyList = historyRepository.findByUserOrderByListenedAtDesc(user);

        // 3. Trả về kết quả
        return ResponseEntity.ok(historyList);
    }

    // API xóa một dòng lịch sử (HTML của bạn có nút xóa)
    @DeleteMapping("/history/{historyId}")
    public ResponseEntity<?> deleteHistoryItem(@PathVariable Long historyId) {
        historyRepository.deleteById(historyId);
        return ResponseEntity.ok("Deleted");
    }

    @PostMapping("/history/log")
    public ResponseEntity<?> logHistory(@RequestParam Long songId) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        // 1. Kiểm tra xem có đăng nhập chưa
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            return ResponseEntity.status(401).body("User not logged in");
        }

        User user = null;
        Object principal = auth.getPrincipal();

        // 2. LOGIC LẤY USER (QUAN TRỌNG)
        if (principal instanceof User) {
            // ==> TRƯỜNG HỢP 1: Tài khoản Local
            // Vì class User của bạn implements UserDetails, nên ép kiểu trực tiếp được
            user = (User) principal;
        }
        else if (principal instanceof org.springframework.security.oauth2.core.user.OAuth2User) {
            // ==> TRƯỜNG HỢP 2: Tài khoản Google
            // Phải lấy email từ Google, sau đó tìm User tương ứng trong Database
            org.springframework.security.oauth2.core.user.OAuth2User oauth2User =
                    (org.springframework.security.oauth2.core.user.OAuth2User) principal;

            String email = oauth2User.getAttribute("email");

            // Tìm user trong DB theo email
            user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("Google User not found in DB"));
        }

        // 3. Nếu tìm được User thì lưu lịch sử
        if (user != null) {
            interactionService.logListeningHistory(user.getUserId(), songId);
            return ResponseEntity.ok("History logged for user: " + user.getEmail());
        }

        return ResponseEntity.badRequest().body("Cannot identify user");
    }

    // --- FAVORITE APIs ---
    @GetMapping("/favorites/{userId}")
    public ResponseEntity<?> getUserFavorites(@PathVariable Long userId) {
        // 1. Kiểm tra User tồn tại (Logic lấy user an toàn)
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) {
            return ResponseEntity.badRequest().body("User not found");
        }

        // 2. Lấy danh sách từ Service (Hàm getLikedSongs bạn đã có trong InteractionService)
        List<Song> likedSongs = interactionService.getLikedSongs(userId);

        // 3. Trả về JSON
        return ResponseEntity.ok(likedSongs);
    }

    @PostMapping("/favorites/toggle")
    public ResponseEntity<?> toggleFavorite(@RequestParam Long songId) {
        // 1. Lấy User hiện tại (Code chuẩn lấy từ SecurityContext)
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            return ResponseEntity.status(401).body("Unauthorized");
        }

        Long userId;
        // Kiểm tra User Local hay Google để lấy ID đúng
        if (auth.getPrincipal() instanceof User) {
            userId = ((User) auth.getPrincipal()).getUserId();
        } else {
            // Google User
            String email = ((org.springframework.security.oauth2.core.user.OAuth2User) auth.getPrincipal()).getAttribute("email");
            userId = userRepository.findByEmail(email).orElseThrow().getUserId();
        }

        // 2. Gọi Service xử lý (Bạn đã có hàm này trong InteractionService)
        // Hàm này sẽ trả về String: "Added to favorites" hoặc "Removed from favorites"
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
