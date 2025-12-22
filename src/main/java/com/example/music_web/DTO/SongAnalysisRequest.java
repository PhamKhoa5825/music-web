package com.example.music_web.DTO;

import lombok.Data;

@Data
public class SongAnalysisRequest {
    private Long userId; // ID người dùng (tùy chọn)
    private String focusArea; // Lĩnh vực tập trung phân tích
    private Boolean includeTechnical; // Bao gồm phân tích kỹ thuật
    private Boolean includeCultural; // Bao gồm bối cảnh văn hóa
    private Boolean includeSimilar; // Bao gồm bài hát tương tự
    private Integer depthLevel; // Mức độ chi tiết (1-5)
    private String language; // Ngôn ngữ trả lời (vi/en)

    // Constructor mặc định
    public SongAnalysisRequest() {
        this.focusArea = "all";
        this.includeTechnical = true;
        this.includeCultural = true;
        this.includeSimilar = true;
        this.depthLevel = 3;
        this.language = "vi";
    }
}