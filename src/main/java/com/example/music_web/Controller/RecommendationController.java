package com.example.music_web.Controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/recommend")
public class RecommendationController {

    // @Autowired private RecommendationService recommendationService;

    @GetMapping("/{userId}")
    public ResponseEntity<String> getRecommendations(@PathVariable Long userId) {
        // TODO: Triển khai logic AI Recommendation ở đây
        return ResponseEntity.ok("List of recommended songs for user " + userId);
    }
}
