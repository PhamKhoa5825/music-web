package com.example.music_web.service;

import com.example.music_web.Entity.Genre;
import com.example.music_web.Entity.ListeningHistory;
import com.example.music_web.Entity.Song;
import com.example.music_web.dto.request.AdvancedAiRequest;
import com.example.music_web.repository.ListeningHistoryRepository;
import com.example.music_web.repository.SongRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class GeminiService {

    @Value("${google.gemini.api-key}")
    private String apiKey;

    @Value("${google.gemini.url}")
    private String apiUrl;

    @Autowired
    private ListeningHistoryRepository historyRepository;
    @Autowired
    private SongRepository songRepository;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    // --- CORE: GỌI GEMINI API ---
    public String callGemini(String prompt) {
        String finalUrl = apiUrl + "?key=" + apiKey;
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> requestBody = Map.of(
                "contents", List.of(Map.of("parts", List.of(Map.of("text", prompt))))
        );

        try {
            ResponseEntity<String> response = restTemplate.postForEntity(finalUrl, new HttpEntity<>(requestBody, headers), String.class);
            return extractTextFromResponse(response.getBody());
        } catch (Exception e) {
            e.printStackTrace();
            return "[]"; // Trả về mảng rỗng nếu lỗi
        }
    }

    // --- LOGIC 1: GỢI Ý BÀI HÁT NÂNG CAO ---
    public List<Long> advancedSongRecommendation(AdvancedAiRequest request, List<Song> allSongs) {
        // 1. Lọc cứng (Java Filter)
        List<Song> filteredSongs = filterSongsByCriteria(request, allSongs);
        if (filteredSongs.size() < 5) {
            // Fallback: Lấy thêm bài random nếu danh sách quá ít
            filteredSongs = allSongs.stream().limit(50).collect(Collectors.toList());
        }

        // 2. Chuẩn bị Prompt
        String songData = prepareSongData(filteredSongs);
        String prompt = buildAdvancedPrompt(request, songData);

        // 3. Gọi AI & Parse JSON
        String jsonResponse = callGemini(prompt);
        return parseIdsFromJson(jsonResponse);
    }

    // --- HELPER METHODS ---
    private String extractTextFromResponse(String rawJson) {
        try {
            JsonNode root = objectMapper.readTree(rawJson);
            return root.path("candidates").get(0).path("content").path("parts").get(0).path("text").asText();
        } catch (Exception e) {
            return "[]";
        }
    }

    // Parse JSON mảng ID an toàn bằng Jackson
    private List<Long> parseIdsFromJson(String text) {
        try {
            // Tìm đoạn bắt đầu bằng [ và kết thúc bằng ] để lọc bớt text thừa của AI
            int start = text.indexOf("[");
            int end = text.lastIndexOf("]");
            if (start != -1 && end != -1) {
                String jsonArray = text.substring(start, end + 1);
                return objectMapper.readValue(jsonArray, new TypeReference<List<Long>>() {});
            }
        } catch (JsonProcessingException e) {
            System.err.println("Lỗi parse JSON từ AI: " + e.getMessage());
        }
        return Collections.emptyList();
    }

    private List<Song> filterSongsByCriteria(AdvancedAiRequest request, List<Song> allSongs) {
        return allSongs.stream().filter(s -> {
            if (request.getMinRating() != null && (s.getAverageRating() == null || s.getAverageRating() < request.getMinRating())) return false;
            if (request.getMinBpm() != null && (s.getBpm() == null || s.getBpm() < request.getMinBpm())) return false;
            // Thêm các logic lọc khác nếu cần...
            return true;
        }).limit(100).collect(Collectors.toList());
    }

    private String prepareSongData(List<Song> songs) {
        StringBuilder sb = new StringBuilder();
        for (Song s : songs) {
            String genres = s.getGenres().stream().map(Genre::getName).collect(Collectors.joining(","));
            sb.append(String.format("{\"id\":%d, \"title\":\"%s\", \"artist\":\"%s\", \"genre\":\"%s\", \"rating\":%.1f},",
                    s.getSongId(), s.getTitle(), s.getArtist().getName(), genres, s.getAverageRating()));
        }
        // Xóa dấu phẩy cuối
        if (sb.length() > 0) sb.setLength(sb.length() - 1);
        return "[" + sb.toString() + "]";
    }

    private String buildAdvancedPrompt(AdvancedAiRequest req, String songData) {
        return String.format(
                "Bạn là DJ. Hãy chọn %d bài hát phù hợp nhất.\n" +
                        "Yêu cầu: %s. Mood: %s.\n" +
                        "Dữ liệu bài hát: %s\n" +
                        "CHỈ TRẢ VỀ MẢNG JSON ID. Ví dụ: [1, 5, 9]",
                req.getSongCount(), req.getDescription(), req.getMoods(), songData
        );
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
        return callGemini("User: " + message);
    }

    // --- LOGIC 3: SO SÁNH BÀI HÁT ---
    public String compareSongs(List<Integer> songIds, Integer userId) {
        List<Long> longIds = songIds.stream().map(Long::valueOf).collect(Collectors.toList());
        List<Song> songs = songRepository.findAllById(longIds);

        if (songs.size() < 2) return "Chọn ít nhất 2 bài hát để so sánh.";

        StringBuilder prompt = new StringBuilder("SO SÁNH CÁC BÀI HÁT SAU:\n");
        for (Song s : songs) {
            prompt.append("- ").append(s.getTitle()).append(" (").append(s.getArtist().getName()).append(")\n");
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
                data.append("- ").append(s.getTitle()).append(" (").append(s.getArtist().getName()).append(")\n")
        );

        String prompt = "Dựa trên lịch sử nghe:\n" + data +
                "\n\nPhân tích gu âm nhạc, tâm trạng và gợi ý 3 bài hát mới. Dùng Markdown.";
        return callGemini(prompt);
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
}