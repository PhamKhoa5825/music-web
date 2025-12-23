package com.example.music_web.controller;

import com.example.music_web.service.InteractionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/ranking")
public class RankingController {

    @Autowired private InteractionService interactionService;

    @GetMapping
    public String rankingPage(Model model) {
        // Chỉ cần trả về view, việc load dữ liệu hãy để JavaScript lo
        model.addAttribute("activeTab", "ranking");
        model.addAttribute("pageTitle", "Bảng Xếp Hạng");
        return "ranking"; // Trả về file ranking.html
    }

    // Hàm này trả về DỮ LIỆU (JSON) khi Javascript gọi
    // Đường dẫn thực tế sẽ là: /ranking/data
    @GetMapping("/data")
    @ResponseBody // <-- Annotation này biến hàm này thành API trả về JSON
    public ResponseEntity<?> getRankingApi(
            @RequestParam(defaultValue = "DAY") String mode,
            @RequestParam(required = false) Long genreId) {

        try {
            List<Map<String, Object>> data = interactionService.getTopCharts(mode, genreId);
            return ResponseEntity.ok(data);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Lỗi server: " + e.getMessage());
        }
    }
}
