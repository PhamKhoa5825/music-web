package com.example.music_web.Controller;

import com.example.music_web.Entity.SongRanking;
import com.example.music_web.Repository.SongRankingRepository;
import com.example.music_web.Service.InteractionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/ranking")
public class RankingController {

    @Autowired private SongRankingRepository rankingRepository;
    @Autowired private InteractionService interactionService;
    // --- 3. API BẢNG XẾP HẠNG (Ranking) ---

    // GET /api/ranking?mode=MONTH&genreId=2
    // mode: DAY, WEEK, MONTH, YEAR, ALL
    // genreId: null (lấy tất cả) hoặc ID thể loại
    @GetMapping
    public ResponseEntity<List<Map<String, Object>>> getRanking(
            @RequestParam(defaultValue = "WEEK") String mode,
            @RequestParam(required = false) Long genreId) {
        return ResponseEntity.ok(interactionService.getTopCharts(mode, genreId));
    }
}
