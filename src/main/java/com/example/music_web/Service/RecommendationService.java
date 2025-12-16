package com.example.music_web.Service;

import com.example.music_web.Entity.*;
import com.example.music_web.Repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class RecommendationService {

    @Autowired private ListeningHistoryRepository historyRepository;
    @Autowired private FavoriteRepository favoriteRepository;
    @Autowired private SongRepository songRepository;
    @Autowired private SongRatingRepository ratingRepository;
    @Autowired private SongRankingRepository rankingRepository;
    @Autowired private UserRepository userRepository;

    // --- TRỌNG SỐ (WEIGHTS) ---
    private static final double W_ARTIST = 15.0; // Điểm cộng nếu trùng Artist yêu thích
    private static final double W_GENRE = 10.0;  // Điểm cộng nếu trùng Genre hay nghe
    private static final double W_RATING = 2.0;  // Hệ số nhân với rating (VD: 4.5 sao * 2 = 9 điểm)
    private static final double W_TREND = 5.0;   // Điểm cộng nếu đang Top Trending
    private static final double PENALTY_LISTENED = 0.5; // Hệ số giảm điểm nếu đã nghe rồi (để ưu tiên bài mới)

    /**
     * Hàm chính: Gợi ý bài hát dựa trên phân tích dữ liệu
     */
    public List<Song> getRecommendationsForUser(Long userId) {
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) return Collections.emptyList();

        // 1. DATA GATHERING (Thu thập dữ liệu hành vi)
        List<ListeningHistory> history = historyRepository.findByUserOrderByListenedAtDesc(user);
        List<Favorite> favorites = favoriteRepository.findByUser(user);

        // Lấy danh sách bài hát user đánh giá thấp (1-2 sao) để blacklist
        List<Long> blacklistedSongIds = ratingRepository.findAll().stream()
                .filter(r -> r.getUser().getUserId().equals(userId) && r.getRating() <= 2)
                .map(r -> r.getSong().getSongId())
                .collect(Collectors.toList());

        // 2. USER PROFILING (Phân tích sở thích)
        // Nếu user mới tinh (chưa nghe/thích gì) -> Trả về Trending (Cold Start)
        if (history.isEmpty() && favorites.isEmpty()) {
            return getTrendingSongs();
        }

        // Tìm Top Artists và Top Genres từ lịch sử & favorite
        Set<Long> topArtistIds = analyzeTopArtists(history, favorites);
        Set<Long> topGenreIds = analyzeTopGenres(history, favorites);
        Set<Long> listenedSongIds = history.stream().map(h -> h.getSong().getSongId()).collect(Collectors.toSet());

        // Lấy danh sách Top Trending hôm nay để đối chiếu
        Set<Long> trendingSongIds = rankingRepository.findTop10ByRankingDateOrderByRanksAsc(LocalDate.now())
                .stream().map(r -> r.getSong().getSongId()).collect(Collectors.toSet());

        // 3. CANDIDATE GENERATION (Tạo tập ứng viên)
        // Thay vì quét toàn bộ DB, chỉ lấy những bài thuộc Genre user thích để tối ưu query
        // Đồng thời lọc luôn bài bị ẩn (isHidden = false) từ Repository
        Set<Genre> genreEntities = topGenreIds.stream()
                .map(id -> Genre.builder().genreId(id).build()) // Tạo dummy object để query
                .collect(Collectors.toSet());

        List<Song> candidates;
        if (genreEntities.isEmpty()) {
            candidates = songRepository.findByIsHiddenFalse(); // Fallback lấy hết nếu không xác định được genre
        } else {
            candidates = songRepository.findByGenresInAndIsHiddenFalse(genreEntities);
        }

        // 4. SCORING & RANKING (Tính điểm và Xếp hạng)
        List<Map.Entry<Song, Double>> scoredSongs = new ArrayList<>();

        for (Song song : candidates) {
            // Bỏ qua bài trong blacklist
            if (blacklistedSongIds.contains(song.getSongId())) continue;
            // Bỏ qua bài chính user đang nghe (tránh gợi ý lại bài vừa bật)
            if (!history.isEmpty() && history.get(0).getSong().getSongId().equals(song.getSongId())) continue;

            double score = calculateScore(song, topArtistIds, topGenreIds, trendingSongIds, listenedSongIds);
            scoredSongs.add(new AbstractMap.SimpleEntry<>(song, score));
        }

        // 5. SORTING & LIMIT (Sắp xếp giảm dần theo điểm)
        return scoredSongs.stream()
                .sorted((e1, e2) -> e2.getValue().compareTo(e1.getValue())) // Điểm cao xếp trước
                .map(Map.Entry::getKey)
                .limit(10) // Lấy Top 10
                .collect(Collectors.toList());
    }

    /**
     * Logic tính điểm chi tiết cho từng bài hát
     */
    private double calculateScore(Song song, Set<Long> topArtists, Set<Long> topGenres, Set<Long> trendingIds, Set<Long> listenedIds) {
        double score = 0.0;

        // Tiêu chí 1: Đúng Artist yêu thích
        if (topArtists.contains(song.getArtist().getArtistId())) {
            score += W_ARTIST;
        }

        // Tiêu chí 2: Đúng Genre yêu thích (Kiểm tra xem bài hát có chứa genre nào trong topGenres không)
        boolean isGenreMatch = song.getGenres().stream()
                .anyMatch(g -> topGenres.contains(g.getGenreId()));
        if (isGenreMatch) {
            score += W_GENRE;
        }

        // Tiêu chí 3: Điểm đánh giá trung bình (Global Rating)
        // Ví dụ: 4.5 sao -> cộng 9 điểm
        double avgRating = (song.getAverageRating() != null) ? song.getAverageRating() : 0.0;
        score += (avgRating * W_RATING);

        // Tiêu chí 4: Đang Trending
        if (trendingIds.contains(song.getSongId())) {
            score += W_TREND;
        }

        // Tiêu chí 5: Đã nghe hay chưa?
        // Nếu đã nghe rồi, giảm 50% số điểm để ưu tiên khám phá bài mới
        if (listenedIds.contains(song.getSongId())) {
            score *= PENALTY_LISTENED;
        }

        return score;
    }

    // --- CÁC HÀM HELPER PHÂN TÍCH DỮ LIỆU ---

    private Set<Long> analyzeTopArtists(List<ListeningHistory> history, List<Favorite> favorites) {
        // Đếm tần suất xuất hiện Artist
        Map<Long, Integer> frequency = new HashMap<>();

        // Ưu tiên Favorites (Mỗi like tính là 5 lượt nghe)
        favorites.forEach(f -> frequency.merge(f.getSong().getArtist().getArtistId(), 5, Integer::sum));
        // Lịch sử nghe (Mỗi lần nghe tính 1)
        history.forEach(h -> frequency.merge(h.getSong().getArtist().getArtistId(), 1, Integer::sum));

        // Lấy Top 3 Artist ID có điểm cao nhất
        return getTopKeys(frequency, 3);
    }

    private Set<Long> analyzeTopGenres(List<ListeningHistory> history, List<Favorite> favorites) {
        Map<Long, Integer> frequency = new HashMap<>();

        favorites.forEach(f -> f.getSong().getGenres().forEach(g ->
                frequency.merge(g.getGenreId(), 5, Integer::sum)
        ));
        history.forEach(h -> h.getSong().getGenres().forEach(g ->
                frequency.merge(g.getGenreId(), 1, Integer::sum)
        ));

        // Lấy Top 3 Genre ID
        return getTopKeys(frequency, 3);
    }

    private Set<Long> getTopKeys(Map<Long, Integer> map, int limit) {
        return map.entrySet().stream()
                .sorted(Map.Entry.<Long, Integer>comparingByValue().reversed())
                .limit(limit)
                .map(Map.Entry::getKey)
                .collect(Collectors.toSet());
    }

    private List<Song> getTrendingSongs() {
        // Fallback: Lấy bài nhiều view nhất chưa bị ẩn
        return songRepository.findByIsHiddenFalse().stream()
                .sorted(Comparator.comparingInt(Song::getViews).reversed()) // Giả sử Song có field views
                .limit(10)
                .collect(Collectors.toList());
    }



    /**
     * [MỚI] Lấy danh sách bài hát liên quan (cho trang Song Detail)
     * Logic: Lấy candidate theo Genre/Artist -> Re-rank bằng thuật toán AI theo user
     */
    public List<Song> getRelatedSongs(Long currentSongId, Long userId) {
        // 1. Lấy thông tin bài hát hiện tại
        Song currentSong = songRepository.findById(currentSongId).orElse(null);
        if (currentSong == null) return Collections.emptyList();

        // 2. Lấy tập ứng viên (Candidate Generation)
        // Lấy khoảng 15 bài cùng thể loại để lọc (Candidate Set)
        // Lưu ý: Reuse hàm findRelatedSongs của Repo hoặc viết query mới bao gồm cả Artist
        List<Song> candidates = songRepository.findRelatedSongs(
                new ArrayList<>(currentSong.getGenres()), // Chuyển Set sang List nếu cần
                currentSongId,
                org.springframework.data.domain.PageRequest.of(0, 15) // Lấy pool 15 bài để chấm điểm
        );

        // 3. Nếu User chưa đăng nhập (Guest) -> Trả về danh sách gốc (đã sort theo view hoặc mặc định)
        if (userId == null) {
            return candidates.stream().limit(6).collect(Collectors.toList());
        }

        // 4. Nếu User đã đăng nhập -> Chấm điểm (Scoring) để Re-rank
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) return candidates.stream().limit(6).collect(Collectors.toList());

        // Thu thập dữ liệu user (Reuse code logic cũ)
        List<ListeningHistory> history = historyRepository.findByUserOrderByListenedAtDesc(user);
        List<Favorite> favorites = favoriteRepository.findByUser(user);
        Set<Long> topArtists = analyzeTopArtists(history, favorites);
        Set<Long> topGenres = analyzeTopGenres(history, favorites);

        // Lấy trending để cộng điểm
        Set<Long> trendingSongIds = rankingRepository.findTop10ByRankingDateOrderByRanksAsc(LocalDate.now())
                .stream().map(r -> r.getSong().getSongId()).collect(Collectors.toSet());

        Set<Long> listenedSongIds = history.stream().map(h -> h.getSong().getSongId()).collect(Collectors.toSet());

        // 5. Chấm điểm từng bài candidate
        List<Map.Entry<Song, Double>> scoredCandidates = new ArrayList<>();
        for (Song s : candidates) {
            // Tận dụng lại hàm calculateScore đã viết ở bước trước
            // Lưu ý: Hàm calculateScore đang là private, hãy đảm bảo nó nằm cùng class này
            double score = calculateScore(s, topArtists, topGenres, trendingSongIds, listenedSongIds);

            // Bonus: Nếu cùng Artist với bài đang nghe -> Cộng thêm điểm cực lớn (Contextual Bonus)
            if (s.getArtist().getArtistId().equals(currentSong.getArtist().getArtistId())) {
                score += 5.0;
            }

            scoredCandidates.add(new AbstractMap.SimpleEntry<>(s, score));
        }

        // 6. Sắp xếp và trả về Top 6
        return scoredCandidates.stream()
                .sorted((e1, e2) -> e2.getValue().compareTo(e1.getValue()))
                .map(Map.Entry::getKey)
                .limit(6)
                .collect(Collectors.toList());
    }


    /**
     * [MỚI] Logic Khám Phá: Tìm bài hát Rating cao (>= 4.0) mà User CHƯA TỪNG nghe
     */
    public List<Song> getDiscoverySongs(Long userId) {
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) return Collections.emptyList();

        // 1. Lấy danh sách ID bài hát đã nghe
        List<ListeningHistory> history = historyRepository.findByUserOrderByListenedAtDesc(user);
        Set<Long> listenedIds = history.stream()
                .map(h -> h.getSong().getSongId())
                .collect(Collectors.toSet());

        // 2. Lấy tất cả bài hát chưa ẩn
        List<Song> allSongs = songRepository.findByIsHiddenFalse();

        // 3. Lọc: Rating cao (> 3.5) AND Chưa nghe bao giờ
        List<Song> discovery = allSongs.stream()
                .filter(s -> {
                    Double rating = s.getAverageRating();
                    return rating != null && rating >= 3.5; // Lấy bài rating khá trở lên
                })
                .filter(s -> !listenedIds.contains(s.getSongId())) // Chưa nghe
                .collect(Collectors.toList());

        // 4. Trộn ngẫu nhiên và lấy 10 bài
        Collections.shuffle(discovery);
        return discovery.stream().limit(10).collect(Collectors.toList());
    }
}