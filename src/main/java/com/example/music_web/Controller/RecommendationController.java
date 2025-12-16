package com.example.music_web.Controller;

import com.example.music_web.Entity.Song;
import com.example.music_web.Service.RecommendationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/recommend")
public class RecommendationController {

    @Autowired
    private RecommendationService recommendationService;

    // API: Lấy danh sách gợi ý cho User cụ thể
    // URL: GET http://localhost:8080/api/recommend/1
    @GetMapping("/{userId}")
    public ResponseEntity<List<Song>> getRecommendations(@PathVariable Long userId) {
        List<Song> songs = recommendationService.getRecommendationsForUser(userId);
        return ResponseEntity.ok(songs);
    }
}