package com.example.music_web.Repository;

import com.example.music_web.Entity.SongRanking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface SongRankingRepository extends JpaRepository<SongRanking, Long> {
    // Lấy top 10 bài hát theo ngày
    List<SongRanking> findTop10ByRankingDateOrderByRankAsc(LocalDate date);
}
