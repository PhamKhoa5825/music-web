package com.example.music_web.Controller;

import com.example.music_web.Entity.Song;
import com.example.music_web.Repository.SongRepository;
import com.example.music_web.Service.RecommendationService;
import lombok.Builder;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    @Autowired private RecommendationService recommendationService;
    @Autowired private SongRepository songRepository;

    @GetMapping("/{userId}")
    public ResponseEntity<DashboardData> getDashboardData(@PathVariable Long userId) {
        // 1. Lấy tất cả bài hát khả dụng
        List<Song> allSongs = songRepository.findByIsHiddenFalse();

        // 2. Logic phân loại
        // A. Gợi ý (Logic AI/Random cũ)
        List<Song> recommended = recommendationService.getRecommendationsForUser(userId);

        // B. Mới phát hành: Sắp xếp theo ID giảm dần (hoặc ngày tạo) -> Lấy 10 bài
        List<Song> newReleases = allSongs.stream()
                .sorted(Comparator.comparing(Song::getSongId).reversed())
                .limit(10)
                .collect(Collectors.toList());

        // C. Thịnh hành: Sắp xếp theo Views giảm dần -> Lấy 10 bài
        List<Song> trending = allSongs.stream()
                .sorted(Comparator.comparingInt(Song::getViews).reversed())
                .limit(10)
                .collect(Collectors.toList());

        return ResponseEntity.ok(DashboardData.builder()
                .recommendedSongs(recommended)
                .newReleases(newReleases)
                .trendingSongs(trending)
                .build());
    }

    @Data
    @Builder
    public static class DashboardData {
        private List<Song> recommendedSongs;
        private List<Song> newReleases;
        private List<Song> trendingSongs;
    }
}