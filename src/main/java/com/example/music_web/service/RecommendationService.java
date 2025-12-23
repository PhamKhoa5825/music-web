package com.example.music_web.service;

import com.example.music_web.Entity.*;
import com.example.music_web.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
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

    // --- CONFIGURATION CONSTANTS (Trọng số thuật toán) ---
    private static final double W_ARTIST = 15.0;
    private static final double W_GENRE = 10.0;
    private static final double W_RATING = 2.0;
    private static final double W_TREND = 5.0;
    private static final double PENALTY_LISTENED = 0.5; // Giảm điểm nếu đã nghe
    private static final double BONUS_CONTEXT = 5.0;    // Điểm thưởng ngữ cảnh (Artist đang nghe)

    private static final int TOP_LIMIT_KEY = 3;         // Lấy top 3 Artist/Genre
    private static final int RECOMMENDATION_LIMIT = 10; // Số bài gợi ý tối đa

    /**
     * 1. GỢI Ý CHÍNH (FOR YOU - DAILY MIX)
     */
    public List<Song> getRecommendationsForUser(Long userId) {
        User user = getUser(userId);
        if (user == null) return getTrendingSongs();

        // A. Thu thập dữ liệu
        List<ListeningHistory> history = historyRepository.findByUserOrderByListenedAtDesc(user);
        List<Favorite> favorites = favoriteRepository.findByUser(user);

        if (history.isEmpty() && favorites.isEmpty()) return getTrendingSongs();

        // B. Phân tích hành vi (Profiling)
        Set<Long> topArtists = analyzeTopArtists(history, favorites);
        Set<Long> topGenres = analyzeTopGenres(history, favorites);
        Set<Long> listenedIds = getListenedSongIds(history);
        Set<Long> trendingIds = getTrendingSongIds();

        // C. Lọc ứng viên (Candidate Generation)
        // Tìm bài hát theo Genre yêu thích (nếu có), ngược lại lấy tất cả bài chưa ẩn
        Set<Genre> genreEntities = topGenres.stream()
                .map(id -> Genre.builder().genreId(id).build())
                .collect(Collectors.toSet());

        List<Song> candidates = genreEntities.isEmpty() ?
                songRepository.findAll() :
                songRepository.findByGenres(genreEntities);

        // D. Chấm điểm & Xếp hạng (Scoring & Ranking)
        return rankSongs(candidates, topArtists, topGenres, trendingIds, listenedIds, null);
    }

    /**
     * 2. GỢI Ý BÀI LIÊN QUAN (SONG DETAIL - RELATED)
     */
    public List<Song> getRelatedSongs(Long currentSongId, Long userId) {
        Song currentSong = songRepository.findById(currentSongId).orElse(null);
        if (currentSong == null) return Collections.emptyList();

        // A. Lấy ứng viên (Cùng thể loại, khác bài hiện tại)
        List<Song> candidates = songRepository.findRelatedSongs(
                currentSong.getGenres(),
                currentSongId,
                PageRequest.of(0, 20) // Lấy pool 20 bài
        );

        // B. Nếu chưa đăng nhập -> Trả về danh sách gốc
        if (userId == null) return candidates.stream().limit(6).collect(Collectors.toList());

        // C. Nếu đã đăng nhập -> Re-rank theo sở thích cá nhân
        User user = getUser(userId);
        List<ListeningHistory> history = historyRepository.findByUserOrderByListenedAtDesc(user);
        List<Favorite> favorites = favoriteRepository.findByUser(user);

        Set<Long> topArtists = analyzeTopArtists(history, favorites);
        Set<Long> topGenres = analyzeTopGenres(history, favorites);
        Set<Long> listenedIds = getListenedSongIds(history);
        Set<Long> trendingIds = getTrendingSongIds();

        // Context: Bài đang xem thuộc Artist nào -> Ưu tiên Artist đó
        Long contextArtistId = currentSong.getArtist().getArtistId();

        List<Song> ranked = rankSongs(candidates, topArtists, topGenres, trendingIds, listenedIds, contextArtistId);
        return ranked.stream().limit(6).collect(Collectors.toList());
    }

    /**
     * 3. KHÁM PHÁ (DISCOVERY - RATING CAO & CHƯA NGHE)
     */
    public List<Song> getDiscoverySongs(Long userId) {
        User user = getUser(userId);
        if (user == null) return Collections.emptyList();

        Set<Long> listenedIds = getListenedSongIds(historyRepository.findByUserOrderByListenedAtDesc(user));
        List<Song> allSongs = songRepository.findAll();

        List<Song> discovery = allSongs.stream()
                .filter(s -> {
                    Double rating = s.getAverageRating();
                    return rating != null && rating >= 4.0; // Chỉ lấy bài hay (> 4 sao)
                })
                .filter(s -> !listenedIds.contains(s.getSongId())) // Chưa nghe bao giờ
                .collect(Collectors.toList());

        Collections.shuffle(discovery); // Trộn ngẫu nhiên
        return discovery.stream().limit(RECOMMENDATION_LIMIT).collect(Collectors.toList());
    }

    // --- CORE ALGORITHMS (THUẬT TOÁN CỐT LÕI) ---

    // Hàm chấm điểm và sắp xếp chung cho cả Recommendation và Related
    private List<Song> rankSongs(List<Song> candidates,
                                 Set<Long> topArtists,
                                 Set<Long> topGenres,
                                 Set<Long> trendingIds,
                                 Set<Long> listenedIds,
                                 Long contextArtistId) {
        List<Map.Entry<Song, Double>> scoredSongs = new ArrayList<>();

        for (Song song : candidates) {
            double score = 0.0;

            // 1. Artist Match
            if (topArtists.contains(song.getArtist().getArtistId())) score += W_ARTIST;

            // 2. Genre Match
            if (song.getGenres().stream().anyMatch(g -> topGenres.contains(g.getGenreId()))) score += W_GENRE;

            // 3. Rating Weight
            double avgRating = (song.getAverageRating() != null) ? song.getAverageRating() : 0.0;
            score += (avgRating * W_RATING);

            // 4. Trending Bonus
            if (trendingIds.contains(song.getSongId())) score += W_TREND;

            // 5. Context Bonus (Cho Related Songs: Cùng Artist bài đang xem)
            if (contextArtistId != null && song.getArtist().getArtistId().equals(contextArtistId)) {
                score += BONUS_CONTEXT;
            }

            // 6. Listened Penalty (Trừ điểm nếu đã nghe, ưu tiên cái mới)
            if (listenedIds.contains(song.getSongId())) score *= PENALTY_LISTENED;

            scoredSongs.add(new AbstractMap.SimpleEntry<>(song, score));
        }

        return scoredSongs.stream()
                .sorted((e1, e2) -> e2.getValue().compareTo(e1.getValue())) // Điểm cao xếp trước
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }

    // --- HELPER METHODS (PHÂN TÍCH DỮ LIỆU) ---

    private Set<Long> analyzeTopArtists(List<ListeningHistory> history, List<Favorite> favorites) {
        Map<Long, Integer> frequency = new HashMap<>();
        // Favorite: 5 điểm
        favorites.forEach(f -> frequency.merge(f.getSong().getArtist().getArtistId(), 5, Integer::sum));
        // History: 1 điểm
        history.forEach(h -> frequency.merge(h.getSong().getArtist().getArtistId(), 1, Integer::sum));
        return getTopKeys(frequency, TOP_LIMIT_KEY);
    }

    private Set<Long> analyzeTopGenres(List<ListeningHistory> history, List<Favorite> favorites) {
        Map<Long, Integer> frequency = new HashMap<>();
        favorites.forEach(f -> f.getSong().getGenres().forEach(g -> frequency.merge(g.getGenreId(), 5, Integer::sum)));
        history.forEach(h -> h.getSong().getGenres().forEach(g -> frequency.merge(g.getGenreId(), 1, Integer::sum)));
        return getTopKeys(frequency, TOP_LIMIT_KEY);
    }

    private Set<Long> getTopKeys(Map<Long, Integer> map, int limit) {
        return map.entrySet().stream()
                .sorted(Map.Entry.<Long, Integer>comparingByValue().reversed())
                .limit(limit)
                .map(Map.Entry::getKey)
                .collect(Collectors.toSet());
    }

    private Set<Long> getListenedSongIds(List<ListeningHistory> history) {
        return history.stream().map(h -> h.getSong().getSongId()).collect(Collectors.toSet());
    }

    private Set<Long> getTrendingSongIds() {
        return rankingRepository.findTop10ByRankingDateOrderByRankAsc(LocalDate.now())
                .stream().map(r -> r.getSong().getSongId()).collect(Collectors.toSet());
    }

    private List<Song> getTrendingSongs() {
        return songRepository.findAll().stream()
                .sorted(Comparator.comparingInt(Song::getViews).reversed())
                .limit(RECOMMENDATION_LIMIT)
                .collect(Collectors.toList());
    }

    private User getUser(Long userId) {
        if (userId == null) return null;
        return userRepository.findById(userId).orElse(null);
    }
}