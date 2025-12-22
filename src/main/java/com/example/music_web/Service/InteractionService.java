package com.example.music_web.Service;

import com.example.music_web.Entity.*;
import com.example.music_web.Repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

@Service
public class InteractionService {

    @Autowired private ListeningHistoryRepository historyRepo;
    @Autowired private FavoriteRepository favoriteRepository;
    @Autowired private UserRepository userRepo;
    @Autowired private SongRepository songRepository; // Đã xoá songRepo dư thừa
    @Autowired private SongRatingRepository ratingRepo;
    @Autowired private PlaylistRepository playlistRepo;

    /**
     * Ghi nhận lịch sử nghe & Tăng view (Chống spam 5 phút)
     */
    @Transactional
    public void logListeningHistory(Long userId, Long songId) {
        // 1. Chống Spam View
        LocalDateTime fiveMinutesAgo = LocalDateTime.now().minusMinutes(5);
        if (historyRepo.existsByUserAndSongAndListenedAtAfter(userId, songId, fiveMinutesAgo)) {
            return;
        }

        User user = userRepo.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));
        Song song = songRepository.findById(songId).orElseThrow(() -> new RuntimeException("Song not found"));

        // 2. Lưu History
        ListeningHistory history = ListeningHistory.builder().user(user).song(song).build();
        historyRepo.save(history);

        // 3. Tăng View
        song.setViews(song.getViews() == null ? 1 : song.getViews() + 1);
        songRepository.save(song);
    }

    public List<ListeningHistory> getUserHistory(Long userId) {
        User user = userRepo.findById(userId).orElseThrow();
        return historyRepo.findByUserOrderByListenedAtDesc(user);
    }

    /**
     * Toggle Like/Unlike
     */
    @Transactional
    public String toggleFavorite(Long userId, Long songId) {
        User user = userRepo.findById(userId).orElseThrow();
        Song song = songRepository.findById(songId).orElseThrow();

        Optional<Favorite> existing = favoriteRepository.findByUserAndSong(user, song);

        if (existing.isPresent()) {
            favoriteRepository.delete(existing.get());
            return "Unliked";
        } else {
            Favorite favorite = Favorite.builder().user(user).song(song).build();
            favoriteRepository.save(favorite);
            return "Liked";
        }
    }

    /**
     * Lấy Bảng Xếp Hạng (Ranking)
     */
    public List<Map<String, Object>> getTopCharts(String mode, Long genreId) {
        LocalDateTime endTime = LocalDateTime.now();
        LocalDateTime startTime = switch (mode.toUpperCase()) {
            case "DAY" -> endTime.minusDays(1);
            case "WEEK" -> endTime.minusWeeks(1);
            case "MONTH" -> endTime.minusMonths(1);
            case "YEAR" -> endTime.minusYears(1);
            default -> endTime.minusYears(100); // ALL TIME
        };

        // Lấy Top 10
        List<Object[]> results = historyRepo.findTopSongsByTimeAndGenre(
                startTime, endTime, genreId, PageRequest.of(0, 10));

        List<Map<String, Object>> rankingList = new ArrayList<>();
        for (Object[] row : results) {
            Map<String, Object> map = new HashMap<>();
            map.put("song", row[0]);  // Song object
            map.put("plays", row[1]); // Play count
            rankingList.add(map);
        }
        return rankingList;
    }

    @Transactional
    public void deleteHistoryById(Long historyId) {
        if (historyRepo.existsById(historyId)) {
            historyRepo.deleteById(historyId);
        }
    }
}