package com.example.music_web.dto.request;
import lombok.Data;

@Data
public class ReportRequest {
    private Long reporterId;
    private Long targetId; // ID của bài hát hoặc comment
    private String type;   // "SONG" hoặc "COMMENT"
    private String reason;
}