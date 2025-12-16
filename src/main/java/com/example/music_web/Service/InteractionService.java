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
    @Autowired private SongRepository songRepository;
    @Autowired private SongRepository songRepo;
    @Autowired private SongRatingRepository ratingRepo;
    @Autowired private PlaylistRepository playlistRepo;

    /**
     * Ghi nhận lịch sử nghe nhạc và tăng view cho bài hát
     */
    @Transactional // Đảm bảo cả 2 hành động (lưu history + tăng view) cùng thành công hoặc cùng thất bại
    public void logHistory(Long userId, Long songId) {
        // 1. Kiểm tra tồn tại
        User user = userRepo.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));
        Song song = songRepository.findById(songId)
                .orElseThrow(() -> new RuntimeException("Song not found with id: " + songId));

        // 2. Lưu lịch sử nghe
        ListeningHistory history = new ListeningHistory();
        history.setUser(user);
        history.setSong(song);
        // listenedAt sẽ được tự động tạo bởi @CreationTimestamp trong Entity
        historyRepo.save(history);

        // 3. Tăng lượt nghe (Views) cho bài hát
        // Logic: Lấy view hiện tại + 1. Nếu null thì gán bằng 1.
        int currentViews = (song.getViews() == null) ? 0 : song.getViews();
        song.setViews(currentViews + 1);

        songRepository.save(song);
    }

    // Lấy danh sách lịch sử nghe của user
    public List<ListeningHistory> getUserHistory(Long userId) {
        User user = userRepo.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));

        return historyRepo.findByUserOrderByListenedAtDesc(user);
    }

    // Chức năng Like / Unlike (Toggle)
    public String toggleFavorite(Long userId, Long songId) {
        User user = userRepo.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));
        Song song = songRepository.findById(songId)
                .orElseThrow(() -> new RuntimeException("Song not found with id: " + songId));

        Optional<Favorite> existing = favoriteRepository.findByUserAndSong(user, song);

        if (existing.isPresent()) {
            // Nếu đã like rồi -> Xóa (Unlike)
            favoriteRepository.delete(existing.get());
            return "Unliked";
        } else {
            // Nếu chưa like -> Tạo mới (Like)
            Favorite favorite = new Favorite();
            favorite.setUser(user);
            favorite.setSong(song);
            // createdAt tự động
            favoriteRepository.save(favorite);
            return "Liked";
        }
    }




    // --- 1. CHỨC NĂNG LỊCH SỬ & TĂNG VIEW ---
    @Transactional
    public void logListeningHistory(Long userId, Long songId) {
        // Logic chặn spam: Nếu nghe lại bài đó trong vòng 5 phút thì không tính view mới
        LocalDateTime fiveMinutesAgo = LocalDateTime.now().minusMinutes(5);
        if (historyRepo.existsByUserAndSongAndListenedAtAfter(userId, songId, fiveMinutesAgo)) {
            return;
        }

        User user = userRepo.findById(userId).orElseThrow();
        Song song = songRepo.findById(songId).orElseThrow();

        // 1. Lưu lịch sử
        ListeningHistory history = ListeningHistory.builder()
                .user(user)
                .song(song)
                .build();
        historyRepo.save(history);

        // 2. Tăng view trực tiếp vào bảng Song (Người 2 quản lý, nhưng Người 3 gọi hàm)
        song.setViews(song.getViews() + 1);
        songRepo.save(song);
    }


    // --- 2. CHỨC NĂNG RATING (ĐÁNH GIÁ SAO) ---
    @Transactional
    public SongRating rateSong(Long userId, Long songId, Integer stars, String review) {
        User user = userRepo.findById(userId).orElseThrow();
        Song song = songRepo.findById(songId).orElseThrow();

        // Kiểm tra xem đã rate chưa, nếu rồi thì update
        SongRating rating = ratingRepo.findByUserAndSong(user, song)
                .orElse(SongRating.builder().user(user).song(song).build());

        rating.setRating(stars);
        rating.setReview(review);
        rating.setCreatedAt(LocalDateTime.now());

        SongRating savedRating = ratingRepo.save(rating);

        // Update lại điểm trung bình vào bảng Song để query cho nhanh
        Double avg = ratingRepo.getAverageRating(songId);
        song.setAverageRating(avg);
        song.setTotalRatings(Math.toIntExact(ratingRepo.countBySongSongId(songId)));
        songRepo.save(song);

        return savedRating;
    }

    // --- 3. CHỨC NĂNG BẢNG XẾP HẠNG (RANKING) ---
    // Mode: DAY, MONTH, YEAR, ALL
    public List<Map<String, Object>> getTopCharts(String mode, Long genreId) {
        LocalDateTime endTime = LocalDateTime.now();
        LocalDateTime startTime;

        switch (mode.toUpperCase()) {
            case "DAY": startTime = endTime.minusDays(1); break;
            case "WEEK": startTime = endTime.minusWeeks(1); break;
            case "MONTH": startTime = endTime.minusMonths(1); break;
            case "YEAR": startTime = endTime.minusYears(1); break;
            default: startTime = endTime.minusYears(100); // All time
        }

        // Lấy Top 10 bài hát
        List<Object[]> results = historyRepo.findTopSongsByTimeAndGenre(
                startTime, endTime, genreId, PageRequest.of(0, 10));

        List<Map<String, Object>> rankingList = new ArrayList<>();
        for (Object[] row : results) {
            Song song = (Song) row[0];
            Long plays = (Long) row[1];

            Map<String, Object> map = new HashMap<>();
            map.put("song", song);
            map.put("plays", plays);
            rankingList.add(map);
        }
        return rankingList;
    }

    // --- 4. PLAYLIST CƠ BẢN ---
    public Playlist createPlaylist(Long userId, String name) {
        User user = userRepo.findById(userId).orElseThrow();
        Playlist p = Playlist.builder().user(user).name(name).build();
        return playlistRepo.save(p);
    }

    @Transactional // Quan trọng: Để đảm bảo việc xóa diễn ra an toàn trong DB
    public void deleteHistoryById(Long historyId) {
        // 1. Kiểm tra xem dòng lịch sử này có tồn tại không
        if (historyRepo.existsById(historyId)) {
            // 2. Thực hiện xóa theo ID (Dùng hàm có sẵn của JPA)
            historyRepo.deleteById(historyId);
        } else {
            // (Tuỳ chọn) Ném lỗi hoặc log ra console nếu ID không tồn tại
            System.out.println("History ID " + historyId + " không tồn tại, bỏ qua.");
        }
    }
}
