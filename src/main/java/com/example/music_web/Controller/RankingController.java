package com.example.music_web.Controller;

import com.example.music_web.Entity.SongRanking;
import com.example.music_web.Repository.SongRankingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/ranking")
public class RankingController {

    @Autowired private SongRankingRepository rankingRepository;

    @GetMapping("/top")
    public ResponseEntity<List<SongRanking>> getTopCharts() {
        // Lấy BXH của ngày hôm nay
        return ResponseEntity.ok(rankingRepository.findTop10ByRankingDateOrderByRankAsc(LocalDate.now()));
    }
}
