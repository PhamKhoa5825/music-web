package com.example.music_web.Controller;

import com.example.music_web.Entity.Song;
import com.example.music_web.Repository.SongRepository;
import com.example.music_web.Service.RecommendationService;
import lombok.Builder;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    @Autowired private RecommendationService recommendationService;
    @Autowired private SongRepository songRepository;

    @GetMapping("/{userId}")
    public ResponseEntity<DashboardData> getDashboardData(@PathVariable Long userId) {
        // 1. Lấy danh sách gợi ý
        List<Song> recommended = recommendationService.getRecommendationsForUser(userId);

        // 2. Lấy danh sách bài hát cho Dashboard
        // ❌ CŨ: List<Song> allSongs = songRepository.findAll();
        // ✅ MỚI: Chỉ lấy bài chưa ẩn
        List<Song> allSongs = songRepository.findByIsHiddenFalse();

        return ResponseEntity.ok(DashboardData.builder()
                .recommendedSongs(recommended)
                .allSongs(allSongs)
                .build());
    }

    // DTO trả về cho Dashboard
    @Data
    @Builder
    public static class DashboardData {
        private List<Song> recommendedSongs;
        private List<Song> allSongs;
    }
}