package com.example.music_web.Controller;

import com.example.music_web.DTO.AdvancedAiRequest;
import com.example.music_web.DTO.ComparativeAnalysisRequest;
import com.example.music_web.DTO.SongAnalysisRequest;
import com.example.music_web.Entity.Song;
import com.example.music_web.Repository.SongRepository;
import com.example.music_web.Service.GeminiService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/gemini")
public class GeminiController {

    @Autowired private GeminiService geminiService;
    @Autowired private SongRepository songRepository;

    // API giải thích bài hát (giữ nguyên)
    @GetMapping("/explain-song/{songId}")
    public ResponseEntity<String> explainSong(@PathVariable Long songId) {
        Song song = songRepository.findById(songId).orElseThrow();

        String prompt = "Hãy phân tích sâu bài hát '" + song.getTitle() + "' " +
                "của nghệ sĩ " + (song.getArtist() != null ? song.getArtist().getName() : "không rõ") + ". " +
                "Phân tích các khía cạnh: \n" +
                "1. Nội dung và ý nghĩa lời bài hát\n" +
                "2. Cảm xúc chủ đạo và thông điệp\n" +
                "3. Phong cách âm nhạc và thể loại\n" +
                "4. Điểm đặc biệt trong giai điệu/ca từ\n" +
                "5. Ai nên nghe bài hát này\n" +
                "Trả lời bằng tiếng Việt, khoảng 5-7 câu, tự nhiên và chuyên nghiệp.";

        String aiResponse = geminiService.callGemini(prompt);
        return ResponseEntity.ok(aiResponse);
    }

    // API phân tích sâu (nâng cao)
    @PostMapping("/deep-analyze-song/{songId}")
    public ResponseEntity<String> deepAnalyzeSong(@PathVariable Long songId,
                                                  @RequestBody SongAnalysisRequest request) {
        Song song = songRepository.findById(songId).orElseThrow();

        // Xây dựng prompt động dựa trên request
        StringBuilder promptBuilder = new StringBuilder();

        promptBuilder.append(String.format(
                "HÃY PHÂN TÍCH CHUYÊN SÂU BÀI HÁT '%s' CỦA %s.\n\n",
                song.getTitle(),
                song.getArtist() != null ? song.getArtist().getName() : "không rõ"
        ));

        // Thêm phần phân tích dựa trên focusArea
        promptBuilder.append("YÊU CẦU PHÂN TÍCH:\n");

        if ("all".equals(request.getFocusArea()) || "technical".equals(request.getFocusArea())) {
            promptBuilder.append("1. PHÂN TÍCH KỸ THUẬT ÂM NHẠC:\n");
            promptBuilder.append("   - Thể loại và phong cách âm nhạc\n");
            promptBuilder.append("   - Cấu trúc bài hát (intro, verse, chorus, bridge, outro)\n");
            promptBuilder.append("   - Hòa âm và tiến trình hợp âm\n");
            promptBuilder.append("   - Nhịp điệu (BPM, time signature)\n");
            promptBuilder.append("   - Kỹ thuật thanh nhạc và biểu cảm\n\n");
        }

        if ("all".equals(request.getFocusArea()) || "lyrical".equals(request.getFocusArea())) {
            promptBuilder.append("2. PHÂN TÍCH CA TỪ VÀ NỘI DUNG:\n");
            promptBuilder.append("   - Chủ đề chính và thông điệp\n");
            promptBuilder.append("   - Biểu tượng, ẩn dụ và hình ảnh sử dụng\n");
            promptBuilder.append("   - Cảm xúc xuyên suốt bài hát\n");
            promptBuilder.append("   - Cấu trúc lời và vần điệu\n");
            promptBuilder.append("   - Mối liên hệ giữa lời và giai điệu\n\n");
        }

        if (request.getIncludeCultural() != null && request.getIncludeCultural()) {
            promptBuilder.append("3. BỐI CẢNH VĂN HÓA - XÃ HỘI:\n");
            promptBuilder.append("   - Bối cảnh sáng tác và phát hành\n");
            promptBuilder.append("   - Ảnh hưởng văn hóa đương thời\n");
            promptBuilder.append("   - Thông điệp xã hội (nếu có)\n");
            promptBuilder.append("   - Vị trí trong sự nghiệp nghệ sĩ\n");
            promptBuilder.append("   - Tác động đến công chúng\n\n");
        }

        if ("all".equals(request.getFocusArea()) || "artistic".equals(request.getFocusArea())) {
            promptBuilder.append("4. PHÂN TÍCH NGHỆ THUẬT:\n");
            promptBuilder.append("   - Tính sáng tạo và độc đáo\n");
            promptBuilder.append("   - Kỹ thuật sản xuất và mixing\n");
            promptBuilder.append("   - Chất lượng trình diễn\n");
            promptBuilder.append("   - Sự phát triển phong cách nghệ sĩ\n");
            promptBuilder.append("   - Đóng góp cho thể loại\n\n");
        }

        if (request.getIncludeSimilar() != null && request.getIncludeSimilar()) {
            promptBuilder.append("5. ĐỀ XUẤT VÀ SO SÁNH:\n");
            promptBuilder.append("   - Bài hát tương tự về phong cách\n");
            promptBuilder.append("   - Nghệ sĩ cùng trường phái\n");
            promptBuilder.append("   - Hoàn cảnh nghe phù hợp\n");
            promptBuilder.append("   - Đối tượng nghe lý tưởng\n\n");
        }

        // Thêm thông tin độ sâu
        promptBuilder.append("MỨC ĐỘ CHI TIẾT: ").append(request.getDepthLevel()).append("/5\n");

        // Yêu cầu ngôn ngữ
        promptBuilder.append("TRẢ LỜI BẰNG TIẾNG ").append("vi".equals(request.getLanguage()) ? "VIỆT" : "ANH").append(".\n");

        // Yêu cầu format
        promptBuilder.append("SỬ DỤNG ĐỊNH DẠNG MARKDOWN VỚI CÁC TIÊU ĐỀ RÕ RÀNG.\n");
        promptBuilder.append("GIỌNG VĂN CHUYÊN NGHIỆP NHƯNG DỄ HIỂU.");

        String aiResponse = geminiService.callGemini(promptBuilder.toString());
        return ResponseEntity.ok(aiResponse);
    }

    // API gợi ý bài hát nâng cao
    @PostMapping("/advanced-recommend")
    public ResponseEntity<?> advancedRecommend(@RequestBody AdvancedAiRequest request) {
        // Lấy tất cả bài hát
        List<Song> allSongs = songRepository.findAll();

        // Gọi service với request nâng cao
        List<Long> selectedIds = geminiService.advancedSongRecommendation(request, allSongs);

        // Lấy thông tin chi tiết bài hát
        List<Song> recommendedSongs = songRepository.findAllById(selectedIds);

        return ResponseEntity.ok(recommendedSongs);
    }

    // API tạo playlist thông minh
    @PostMapping("/create-smart-playlist")
    public ResponseEntity<?> createSmartPlaylist(@RequestBody AdvancedAiRequest request) {
        List<Song> allSongs = songRepository.findAll();

        Map<String, Object> result = geminiService.createSmartPlaylist(request, allSongs);

        return ResponseEntity.ok(result);
    }

    // API phân tích thói quen nghe nhạc
    @PostMapping("/analyze-listening-habits/{userId}")
    public ResponseEntity<String> analyzeListeningHabits(@PathVariable Long userId) {
        // Lấy dữ liệu lịch sử nghe của user
        // (Cần thêm repository và service để lấy dữ liệu)

        String prompt = "Tôi có dữ liệu thói quen nghe nhạc của một người dùng. " +
                "Hãy phân tích và đưa ra nhận xét về: \n" +
                "1. Thể loại âm nhạc ưa thích\n" +
                "2. Khung giờ nghe nhạc thường xuyên\n" +
                "3. Xu hướng tâm trạng qua các bài hát\n" +
                "4. Đề xuất cải thiện trải nghiệm nghe nhạc\n" +
                "5. Nghệ sĩ nên khám phá thêm\n" +
                "Trả lời bằng tiếng Việt, thân thiện và mang tính xây dựng.";

        String aiResponse = geminiService.callGemini(prompt);
        return ResponseEntity.ok(aiResponse);
    }

    // API chat âm nhạc thông minh
    @PostMapping("/music-chat")
    public ResponseEntity<Map<String, Object>> musicChat(@RequestBody Map<String, Object> payload) {
        String userMessage = (String) payload.get("message");
        Long userId = payload.get("userId") != null ? Long.valueOf(payload.get("userId").toString()) : null;

        // Lấy context từ dữ liệu người dùng nếu có userId
        String context = "";
        if (userId != null) {
            // Có thể thêm logic lấy thông tin user, lịch sử, sở thích
            context = "Người dùng có ID: " + userId + ". ";
        }

        String prompt = context + "Câu hỏi về âm nhạc: " + userMessage +
                "\nHãy trả lời như một chuyên gia âm nhạc, am hiểu về các thể loại, " +
                "nghệ sĩ, xu hướng âm nhạc. Trả lời bằng tiếng Việt.";

        String aiResponse = geminiService.callGemini(prompt);

        return ResponseEntity.ok(Map.of(
                "response", aiResponse,
                "suggestedActions", List.of("play_song", "create_playlist", "explore_genre"),
                "confidence", 0.9
        ));
    }

    @PostMapping("/compare-songs")
    public ResponseEntity<String> compareSongs(@RequestBody ComparativeAnalysisRequest request) {
        Song song1 = songRepository.findById(request.getSongId1()).orElseThrow();
        Song song2 = songRepository.findById(request.getSongId2()).orElseThrow();

        String prompt = String.format(
                "HÃY SO SÁNH CHI TIẾT HAI BÀI HÁT:\n" +
                        "1. '%s' - %s\n" +
                        "2. '%s' - %s\n\n" +
                        "YÊU CẦU SO SÁNH THEO CÁC KHÍA CẠNH:\n%s\n\n" +
                        "GÓC ĐỘ PHÂN TÍCH: %s\n" +
                        "MỨC ĐỘ CHI TIẾT: %d/5\n\n" +
                        "TRÌNH BÀY DƯỚI DẠNG BẢNG SO SÁNH, SAU ĐÓ ĐƯA RA NHẬN XÉT TỔNG QUAN.\n" +
                        "TRẢ LỜI BẰNG TIẾNG VIỆT, SỬ DỤNG MARKDOWN.",
                song1.getTitle(),
                song1.getArtist() != null ? song1.getArtist().getName() : "Không rõ",
                song2.getTitle(),
                song2.getArtist() != null ? song2.getArtist().getName() : "Không rõ",
                String.join(", ", request.getComparisonPoints()),
                request.getAnalysisPerspective(),
                request.getDetailLevel()
        );

        String aiResponse = geminiService.callGemini(prompt);
        return ResponseEntity.ok(aiResponse);
    }
}

