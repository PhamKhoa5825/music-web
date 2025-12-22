package com.example.music_web.Controller;

import com.example.music_web.DTO.AdvancedAiRequest;
import com.example.music_web.Entity.Song;
import com.example.music_web.Repository.SongRepository;
import com.example.music_web.Service.GeminiService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/gemini")
public class GeminiController {

    @Autowired private GeminiService geminiService;
    @Autowired private SongRepository songRepository;

    // 1. API Gợi ý bài hát nâng cao (Tâm điểm chính)
    // Tìm nhạc dựa trên: Mood, Genre, Activity, BPM, Rating...
    @PostMapping("/advanced-recommend")
    public ResponseEntity<?> advancedRecommend(@RequestBody AdvancedAiRequest request) {
        List<Song> allSongs = songRepository.findAll();
        // Logic lọc Java + AI chọn lọc
        List<Long> selectedIds = geminiService.advancedSongRecommendation(request, allSongs);
        return ResponseEntity.ok(songRepository.findAllById(selectedIds));
    }

    // 2. API Gợi ý nhanh theo cảm xúc (Mood đơn giản)
    @PostMapping("/recommend-by-mood")
    public ResponseEntity<?> recommendByMood(@RequestBody Map<String, Object> payload) {
        String mood = (String) payload.get("mood");

        // Chuyển đổi thành request nâng cao để tái sử dụng logic
        AdvancedAiRequest req = new AdvancedAiRequest();
        req.setMoods(List.of(mood));
        req.setSongCount(10); // Mặc định 10 bài

        List<Song> allSongs = songRepository.findAll();
        List<Long> selectedIds = geminiService.advancedSongRecommendation(req, allSongs);
        return ResponseEntity.ok(songRepository.findAllById(selectedIds));
    }

    // 3. API Chat Assistant
    @PostMapping("/chat")
    public ResponseEntity<String> chatWithAi(@RequestBody Map<String, Object> request) {
        String message = (String) request.get("message");
        List<Map<String, String>> history = (List<Map<String, String>>) request.get("history");

        Integer userId = null;
        if (request.get("userId") != null) {
            userId = Integer.valueOf(request.get("userId").toString());
        }

        String response = geminiService.chatAssistant(message, history, userId);
        return ResponseEntity.ok(response);
    }

    // 4. API So sánh bài hát
    @PostMapping("/compare-songs")
    public ResponseEntity<String> compareSongs(@RequestBody Map<String, Object> request) {
        List<Integer> songIds = (List<Integer>) request.get("songIds");

        Integer userId = null;
        if (request.get("userId") != null) {
            userId = Integer.valueOf(request.get("userId").toString());
        }

        String comparison = geminiService.compareSongs(songIds, userId);
        return ResponseEntity.ok(comparison);
    }

    // 5. API Phân tích thói quen nghe nhạc (Dựa trên lịch sử)
    @PostMapping("/analyze-listening-habits/{userId}")
    public ResponseEntity<String> analyzeListeningHabits(@PathVariable Long userId) {
        String analysis = geminiService.analyzeUserHabits(userId);
        return ResponseEntity.ok(analysis);
    }
}