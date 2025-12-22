package com.example.music_web.Service;

import com.example.music_web.DTO.AdvancedAiRequest;
import com.example.music_web.Entity.Genre;
import com.example.music_web.Entity.ListeningHistory;
import com.example.music_web.Entity.Song;
import com.example.music_web.Repository.ListeningHistoryRepository;
import com.example.music_web.Repository.SongRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class GeminiService {

    @Value("${google.gemini.api-key}")
    private String apiKey;

    @Value("${google.gemini.url}")
    private String apiUrl;

    @Autowired
    private SongRepository songRepository;

    @Autowired
    private ListeningHistoryRepository historyRepository;

    private final RestTemplate restTemplate = new RestTemplate();

    // --- CORE: GỌI GEMINI API ---
    public String callGemini(String prompt) {
        String finalUrl = apiUrl + "?key=" + apiKey;
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("contents", List.of(Map.of(
                "parts", List.of(Map.of("text", prompt))
        )));

        try {
            ResponseEntity<Map> response = restTemplate.postForEntity(finalUrl, new HttpEntity<>(requestBody, headers), Map.class);
            return extractTextFromResponse(response.getBody());
        } catch (Exception e) {
            e.printStackTrace();
            return "Xin lỗi, AI đang bận. Vui lòng thử lại sau.";
        }
    }

    // --- LOGIC 1: GỢI Ý BÀI HÁT NÂNG CAO ---
    public List<Long> advancedSongRecommendation(AdvancedAiRequest request, List<Song> allSongs) {
        // B1. Lọc sơ bộ bằng Java (Hard filters) để giảm tải cho AI
        List<Song> filteredSongs = filterSongsByCriteria(request, allSongs);

        // B2. Nếu danh sách quá ít (< 5 bài), lấy thêm bài random để AI có cái để chọn
        if (filteredSongs.size() < 5) {
            filteredSongs = allSongs.stream().limit(50).collect(Collectors.toList());
        }

        // B3. Chuẩn bị dữ liệu gửi AI
        String songData = prepareSongData(filteredSongs);
        String prompt = buildAdvancedPrompt(request, songData);

        // B4. Gọi AI và Parse kết quả
        String jsonResponse = callGemini(prompt);
        return extractIdsFromResponse(jsonResponse);
    }

    // --- LOGIC 2: CHAT ASSISTANT ---
    public String chatAssistant(String message, List<Map<String, String>> history, Integer userId) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("Bạn là trợ lý âm nhạc ảo. Hãy trả lời ngắn gọn, thân thiện.\n\n");

        if (history != null) {
            for (Map<String, String> msg : history) {
                prompt.append(msg.get("role").equals("user") ? "User: " : "AI: ")
                        .append(msg.get("content")).append("\n");
            }
        }
        prompt.append("User: ").append(message).append("\nAI:");
        return callGemini(prompt.toString());
    }

    // --- LOGIC 3: SO SÁNH BÀI HÁT ---
    public String compareSongs(List<Integer> songIds, Integer userId) {
        List<Long> longIds = songIds.stream().map(Long::valueOf).collect(Collectors.toList());
        List<Song> songs = songRepository.findAllById(longIds);

        if (songs.size() < 2) return "Chọn ít nhất 2 bài hát để so sánh.";

        StringBuilder prompt = new StringBuilder("SO SÁNH CÁC BÀI HÁT SAU:\n");
        for (Song s : songs) {
            prompt.append("- ").append(s.getTitle()).append(" (").append(s.getArtistName()).append(")\n");
        }
        prompt.append("\nTiêu chí: Giai điệu, Ca từ, Cảm xúc. Trả lời bằng Markdown.");
        return callGemini(prompt.toString());
    }

    // --- LOGIC 4: PHÂN TÍCH THÓI QUEN NGHE ---
    public String analyzeUserHabits(Long userId) {
        // Sửa query repository cho đúng chuẩn JPA
        List<ListeningHistory> histories = historyRepository.findTop50ByUser_UserIdOrderByListenedAtDesc(userId);

        if (histories.isEmpty()) return "Bạn chưa nghe bài nào gần đây.";

        StringBuilder data = new StringBuilder();
        histories.stream().map(ListeningHistory::getSong).distinct().limit(20).forEach(s ->
                data.append("- ").append(s.getTitle()).append(" (").append(s.getArtistName()).append(")\n")
        );

        String prompt = "Dựa trên lịch sử nghe:\n" + data +
                "\n\nPhân tích gu âm nhạc, tâm trạng và gợi ý 3 bài hát mới. Dùng Markdown.";
        return callGemini(prompt);
    }

    // --- HELPER METHODS ---

    private List<Song> filterSongsByCriteria(AdvancedAiRequest request, List<Song> allSongs) {
        return allSongs.stream().filter(song -> {
            // Lọc Rating
            if (request.getMinRating() != null && song.getAverageRating() != null && song.getAverageRating() < request.getMinRating()) return false;
            // Lọc Năm
            if (request.getMinYear() != null && song.getReleaseYear() != null && song.getReleaseYear() < request.getMinYear()) return false;
            // Lọc Thể loại (Genre)
            if (request.getGenres() != null && !request.getGenres().isEmpty()) {
                boolean match = song.getGenres().stream().anyMatch(g -> request.getGenres().contains(g.getName()));
                if (!match) return false;
            }
            // Lọc BPM
            if (request.getMinBpm() != null && song.getBpm() != null && song.getBpm() < request.getMinBpm()) return false;
            if (request.getMaxBpm() != null && song.getBpm() != null && song.getBpm() > request.getMaxBpm()) return false;

            return true;
        }).limit(100).collect(Collectors.toList());
    }

    private String prepareSongData(List<Song> songs) {
        StringBuilder sb = new StringBuilder();
        for (Song s : songs) {
            String genres = s.getGenres().stream().map(Genre::getName).collect(Collectors.joining(","));
            sb.append(String.format("{id:%d, title:\"%s\", artist:\"%s\", genre:\"%s\", bpm:%d, rating:%.1f}\n",
                    s.getSongId(), s.getTitle(), s.getArtistName(), genres,
                    s.getBpm() != null ? s.getBpm() : 0,
                    s.getAverageRating() != null ? s.getAverageRating() : 0.0));
        }
        return sb.toString();
    }

    private String buildAdvancedPrompt(AdvancedAiRequest request, String songData) {
        return String.format(
                "Đóng vai DJ chuyên nghiệp. Hãy chọn ra %d bài hát phù hợp nhất từ danh sách dưới đây cho người dùng:\n" +
                        "YÊU CẦU: Mô tả: '%s', Mood: %s, Activity: %s.\n\n" +
                        "DANH SÁCH:\n%s\n\n" +
                        "CHỈ TRẢ VỀ JSON MẢNG ID BÀI HÁT. Ví dụ: [1, 5, 10]",
                request.getSongCount() != null ? request.getSongCount() : 10,
                request.getDescription(),
                request.getMoods(),
                request.getActivity(),
                songData
        );
    }

    private String extractTextFromResponse(Map body) {
        try {
            List candidates = (List) body.get("candidates");
            Map first = (Map) candidates.get(0);
            Map content = (Map) first.get("content");
            List parts = (List) content.get("parts");
            return (String) ((Map) parts.get(0)).get("text");
        } catch (Exception e) { return "[]"; }
    }

    private List<Long> extractIdsFromResponse(String text) {
        List<Long> ids = new ArrayList<>();
        try {
            Matcher m = Pattern.compile("\\[.*?\\]").matcher(text);
            if (m.find()) {
                ids = new ObjectMapper().readValue(m.group(), new ObjectMapper().getTypeFactory().constructCollectionType(List.class, Long.class));
            }
        } catch (Exception e) { /* Ignore */ }
        return ids;
    }
}