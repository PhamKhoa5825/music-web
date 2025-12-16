package com.example.music_web.Controller;

import com.example.music_web.Entity.*;
import com.example.music_web.Repository.FavoriteRepository;
import com.example.music_web.Repository.SongRankingRepository;
import com.example.music_web.Repository.SongRepository;
import com.example.music_web.Repository.UserRepository;
import com.example.music_web.Service.InteractionService;
import com.example.music_web.Service.RecommendationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
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

    @GetMapping("/songs/{id}/detail")
    public ResponseEntity<?> getSongDetailFull(
            @PathVariable Long id,
            @RequestParam(required = false) Long userId // Thêm param optional userId
    ) {
        Song song = songRepository.findById(id).orElseThrow(() -> new RuntimeException("Song not found"));

        // 1. [NÂNG CẤP] Lấy bài hát liên quan thông minh (AI Hybrid)
        // Thay vì gọi repo trực tiếp, ta gọi qua Service để tính điểm
        List<Song> related = recommendationService.getRelatedSongs(id, userId);

        // 2. Lấy thống kê view/like
        long totalLikes = favoriteRepository.countBySong(song);

        // Check xem user hiện tại đã like bài này chưa (để tô đỏ trái tim)
        boolean isLiked = false;
        if (userId != null) {
            User currentUser = userRepository.findById(userId).orElse(null);
            if (currentUser != null) {
                isLiked = favoriteRepository.existsByUserAndSong(currentUser, song);
            }
        }

        // 3. Đóng gói trả về
        Map<String, Object> response = new HashMap<>();
        response.put("song", song);
        response.put("relatedSongs", related);
        response.put("totalLikes", totalLikes);
        response.put("isLiked", isLiked); // Frontend dùng biến này để hiển thị nút like

        return ResponseEntity.ok(response);
    }


    // --- API DÀNH CHO BẠN (FOR YOU) ---
    // --- [MỚI] API TỔNG HỢP CHO TRANG DÀNH CHO BẠN ---
    // GET /api/foryou/1
    @GetMapping("/foryou/{userId}")
    public ResponseEntity<Map<String, Object>> getForYouData(@PathVariable Long userId) {
        Map<String, Object> response = new HashMap<>();

        // 1. Daily Mix: Logic Gợi ý AI cũ (Dựa trên lịch sử)
        List<Song> dailyMix = recommendationService.getRecommendationsForUser(userId);
        response.put("dailyMix", dailyMix);

        // 2. Trending: Lấy Top 10 BXH ngày hôm nay (Dùng logic của Ranking)
        List<Song> trending = rankingRepository.findTop10ByRankingDateOrderByRanksAsc(LocalDate.now())
                .stream()
                .map(SongRanking::getSong)
                .collect(Collectors.toList());

        // Fallback: Nếu hôm nay chưa chạy batch ranking thì lấy hôm qua (tránh lỗi list rỗng)
        if (trending.isEmpty()) {
            trending = rankingRepository.findTop10ByRankingDateOrderByRanksAsc(LocalDate.now().minusDays(1))
                    .stream().map(SongRanking::getSong).collect(Collectors.toList());
        }
        response.put("trending", trending);

        // 3. Discovery: Bài hát hay chưa nghe bao giờ
        List<Song> discovery = recommendationService.getDiscoverySongs(userId);
        response.put("discovery", discovery);

        return ResponseEntity.ok(response);
    }
}
