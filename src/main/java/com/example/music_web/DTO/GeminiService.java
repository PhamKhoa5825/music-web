package com.example.music_web.DTO;

import com.example.music_web.Entity.Genre;
import com.example.music_web.Entity.Song;
import com.fasterxml.jackson.databind.ObjectMapper;
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

    private final RestTemplate restTemplate = new RestTemplate();

    // Phương thức call Gemini cơ bản (giữ nguyên)
    public String callGemini(String prompt) {
        String finalUrl = apiUrl + "?key=" + apiKey;

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("contents", List.of(Map.of(
                "parts", List.of(Map.of("text", prompt))
        )));

        try {
            ResponseEntity<Map> response = restTemplate.postForEntity(finalUrl,
                    new HttpEntity<>(requestBody, headers), Map.class);

            return extractTextFromResponse(response.getBody());

        } catch (Exception e) {
            e.printStackTrace();
            return "Xin lỗi, AI đang bận. Vui lòng thử lại sau.";
        }
    }

    // Phương thức gợi ý nâng cao
    public List<Long> advancedSongRecommendation(AdvancedAiRequest request, List<Song> allSongs) {
        // 1. Lọc sơ bộ bài hát dựa trên tiêu chí
        List<Song> filteredSongs = filterSongsByCriteria(request, allSongs);

        // 2. Chuẩn bị dữ liệu bài hát cho prompt
        String songData = prepareSongData(filteredSongs);

        // 3. Tạo prompt nâng cao
        String prompt = buildAdvancedPrompt(request, songData);

        // 4. Gọi AI
        String jsonResponse = callGemini(prompt);

        // 5. Xử lý kết quả
        return extractIdsFromResponse(jsonResponse);
    }

    // Phương thức tạo playlist thông minh
    public Map<String, Object> createSmartPlaylist(AdvancedAiRequest request, List<Song> allSongs) {
        // Lọc bài hát
        List<Song> filteredSongs = filterSongsByCriteria(request, allSongs);
        String songData = prepareSongData(filteredSongs);

        // Tạo prompt đặc biệt cho playlist
        String prompt = String.format(
                "TÔI CẦN TẠO MỘT PLAYLIST THÔNG MINH VỚI THÔNG TIN SAU:\n" +
                        "MÔ TẢ: %s\n" +
                        "TÂM TRẠNG: %s\n" +
                        "THỂ LOẠI: %s\n" +
                        "HOẠT ĐỘNG: %s\n" +
                        "THỜI GIAN: %s\n" +
                        "SỐ BÀI: %d\n" +
                        "THỜI LƯỢNG: %d phút\n\n" +
                        "DANH SÁCH BÀI HÁT KHẢ THI:\n%s\n\n" +
                        "YÊU CẦU:\n" +
                        "1. Chọn đúng số lượng bài hát theo yêu cầu\n" +
                        "2. Sắp xếp theo trình tự hợp lý (khởi đầu, phát triển, cao trào, kết thúc)\n" +
                        "3. Đảm bảo chuyển tiếp mượt mà giữa các bài\n" +
                        "4. Cân bằng giữa bài mới và bài quen thuộc\n" +
                        "5. Tạo tên playlist sáng tạo và mô tả hấp dẫn\n\n" +
                        "TRẢ LỜI THEO ĐỊNH DẠNG JSON:\n" +
                        "{\n" +
                        "  \"playlistName\": \"Tên playlist\",\n" +
                        "  \"description\": \"Mô tả playlist\",\n" +
                        "  \"songIds\": [id1, id2, ...],\n" +
                        "  \"totalDuration\": \"tổng thời lượng\",\n" +
                        "  \"moodSummary\": \"tóm tắt tâm trạng\",\n" +
                        "  \"flowDescription\": \"mô tả trình tự\"\n" +
                        "}",
                request.getDescription(),
                request.getMoods() != null ? String.join(", ", request.getMoods()) : "không xác định",
                request.getGenres() != null ? String.join(", ", request.getGenres()) : "đa dạng",
                request.getActivity() != null ? request.getActivity() : "nghe nhạc thông thường",
                request.getTimeOfDay() != null ? request.getTimeOfDay() : "bất kỳ",
                request.getSongCount() != null ? request.getSongCount() : 10,
                request.getDuration() != null ? request.getDuration() : 60,
                songData
        );

        String jsonResponse = callGemini(prompt);

        try {
            ObjectMapper mapper = new ObjectMapper();
            return mapper.readValue(jsonResponse, Map.class);
        } catch (Exception e) {
            // Fallback: trả về bài hát đơn giản
            List<Long> ids = extractIdsFromResponse(jsonResponse);
            return Map.of(
                    "playlistName", "Playlist AI tạo",
                    "description", "Được tạo tự động bởi AI",
                    "songIds", ids,
                    "totalDuration", "~" + (ids.size() * 4) + " phút"
            );
        }
    }

    // Hàm lọc bài hát theo tiêu chí
    private List<Song> filterSongsByCriteria(AdvancedAiRequest request, List<Song> allSongs) {
        return allSongs.stream()
                .filter(song -> {
                    // Lọc theo rating tối thiểu
                    if (request.getMinRating() != null && song.getAverageRating() != null) {
                        if (song.getAverageRating() < request.getMinRating()) {
                            return false;
                        }
                    }

                    // Lọc theo năm phát hành
                    if (request.getMinYear() != null && song.getReleaseYear() != null) {
                        if (song.getReleaseYear() < request.getMinYear()) {
                            return false;
                        }
                    }

                    // Lọc theo thể loại nếu có
                    if (request.getGenres() != null && !request.getGenres().isEmpty()) {
                        boolean hasMatchingGenre = song.getGenres().stream()
                                .anyMatch(genre -> request.getGenres().contains(genre.getName()));
                        if (!hasMatchingGenre) {
                            return false;
                        }
                    }

                    // Lọc theo nghệ sĩ nếu có
                    if (request.getArtists() != null && !request.getArtists().isEmpty()) {
                        if (song.getArtist() == null ||
                                !request.getArtists().contains(song.getArtist().getName())) {
                            return false;
                        }
                    }

                    // Thêm filter theo BPM
                    if (request.getMinBpm() != null && song.getBpm() != null) {
                        if (song.getBpm() < request.getMinBpm()) return false;
                    }
                    if (request.getMaxBpm() != null && song.getBpm() != null) {
                        if (song.getBpm() > request.getMaxBpm()) return false;
                    }

                    // Filter theo mức năng lượng
                    if (request.getMinEnergy() != null && song.getEnergyLevel() != null) {
                        if (song.getEnergyLevel() < request.getMinEnergy()) return false;
                    }

                    // Filter theo danceability
                    if (request.getMinDanceability() != null && song.getDanceability() != null) {
                        if (song.getDanceability() < request.getMinDanceability()) return false;
                    }

                    // Filter theo ngôn ngữ
                    if (request.getLanguage() != null && song.getLanguage() != null) {
                        if (!song.getLanguage().equalsIgnoreCase(request.getLanguage())) return false;
                    }

                    // Filter explicit
                    if (request.getExcludeExplicit() != null && request.getExcludeExplicit()) {
                        if (Boolean.TRUE.equals(song.getExplicit())) return false;
                    }

                    // Filter năm phát hành (có thêm maxYear)
                    if (request.getMinYear() != null && song.getReleaseYear() != null) {
                        if (song.getReleaseYear() < request.getMinYear()) return false;
                    }
                    if (request.getMaxYear() != null && song.getReleaseYear() != null) {
                        if (song.getReleaseYear() > request.getMaxYear()) return false;
                    }

                    return true;
                })
                .limit(100) // Giới hạn 100 bài để gửi cho AI
                .collect(Collectors.toList());
    }

    // Chuẩn bị dữ liệu bài hát cho prompt
    private String prepareSongData(List<Song> songs) {
        StringBuilder sb = new StringBuilder();

        for (Song s : songs) {
            String genres = s.getGenres().stream()
                    .map(Genre::getName)
                    .collect(Collectors.joining(", "));

            String artists = s.getArtist() != null ? s.getArtist().getName() :
                    s.getArtistName() != null ? s.getArtistName() : "Không rõ";

            // Sử dụng formattedDuration
            String durationStr = s.getFormattedDuration();
            Double rating = s.getAverageRating() != null ? s.getAverageRating() : 0.0;
            Integer year = s.getReleaseYear() != null ? s.getReleaseYear() : 0;
            Integer views = s.getViews() != null ? s.getViews() : 0;
            Integer bpm = s.getBpm() != null ? s.getBpm() : 120;
            Integer energy = s.getEnergyLevel() != null ? s.getEnergyLevel() : 5;
            Integer dance = s.getDanceability() != null ? s.getDanceability() : 5;
            String language = s.getLanguage() != null ? s.getLanguage() : "vi";
            Boolean explicit = s.getExplicit() != null ? s.getExplicit() : false;

            sb.append(String.format(
                    "{id: %d, title: \"%s\", artist: \"%s\", genres: \"%s\", " +
                            "duration: \"%s\", rating: %.1f, year: %d, views: %d, " +
                            "bpm: %d, energy: %d/10, dance: %d/10, language: \"%s\", explicit: %s},\n",
                    s.getSongId(), s.getTitle(), artists, genres,
                    durationStr, rating, year, views,
                    bpm, energy, dance, language, explicit ? "có" : "không"
            ));
        }

        return sb.toString();
    }

    // Xây dựng prompt nâng cao
    private String buildAdvancedPrompt(AdvancedAiRequest request, String songData) {
        return String.format(
                "TÔI CẦN GỢI Ý BÀI HÁT VỚI THÔNG TIN CHI TIẾT SAU:\n\n" +
                        "THÔNG TIN NGƯỜI DÙNG:\n" +
                        "• Mô tả: %s\n" +
                        "• Tâm trạng: %s\n" +
                        "• Thể loại ưa thích: %s\n" +
                        "• Nghệ sĩ yêu thích: %s\n" +
                        "• Hoạt động hiện tại: %s\n" +
                        "• Thời gian trong ngày: %s\n" +
                        "• Số bài mong muốn: %d\n" +
                        "• Thời lượng: %d phút\n\n" +
                        "DANH SÁCH BÀI HÁT KHẢ THI:\n%s\n\n" +
                        "YÊU CẦU GỢI Ý:\n" +
                        "1. Phân tích kỹ yêu cầu người dùng\n" +
                        "2. Xem xét các yếu tố: tâm trạng, hoạt động, thời gian, thể loại\n" +
                        "3. Cân bằng giữa bài phổ biến và bài ít nghe biết\n" +
                        "4. Đảm bảo đa dạng nhưng vẫn tập trung vào chủ đề\n" +
                        "5. Ưu tiên bài có rating cao và phù hợp ngữ cảnh\n\n" +
                        "QUAN TRỌNG: Chỉ trả về mảng JSON chứa ID bài hát, không giải thích thêm.\n" +
                        "Ví dụ: [1, 5, 12, 23, 45]",

                request.getDescription(),
                request.getMoods() != null ? String.join(", ", request.getMoods()) : "không xác định",
                request.getGenres() != null ? String.join(", ", request.getGenres()) : "đa dạng",
                request.getArtists() != null ? String.join(", ", request.getArtists()) : "không xác định",
                request.getActivity() != null ? request.getActivity() : "nghe nhạc thông thường",
                request.getTimeOfDay() != null ? request.getTimeOfDay() : "bất kỳ",
                request.getSongCount() != null ? request.getSongCount() : 10,
                request.getDuration() != null ? request.getDuration() : 60,
                songData
        );
    }

    // Trích xuất văn bản từ response (giữ nguyên)
    private String extractTextFromResponse(Map body) {
        try {
            List candidates = (List) body.get("candidates");
            Map firstCandidate = (Map) candidates.get(0);
            Map contentResp = (Map) firstCandidate.get("content");
            List partsResp = (List) contentResp.get("parts");
            Map firstPart = (Map) partsResp.get(0);
            return (String) firstPart.get("text");
        } catch (Exception e) {
            return "Không thể phân tích phản hồi từ AI.";
        }
    }

    // Trích xuất ID từ response (giữ nguyên)
    private List<Long> extractIdsFromResponse(String text) {
        List<Long> ids = new ArrayList<>();
        try {
            Pattern pattern = Pattern.compile("\\[.*?\\]");
            Matcher matcher = pattern.matcher(text);
            if (matcher.find()) {
                String jsonArray = matcher.group();
                ObjectMapper mapper = new ObjectMapper();
                ids = mapper.readValue(jsonArray,
                        mapper.getTypeFactory().constructCollectionType(List.class, Long.class));
            }
        } catch (Exception e) {
            System.out.println("Lỗi parse ID từ AI: " + e.getMessage());
        }
        return ids;
    }
}