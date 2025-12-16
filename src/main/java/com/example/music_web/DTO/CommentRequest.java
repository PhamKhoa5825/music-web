package com.example.music_web.DTO;

import lombok.Data; // Thư viện Lombok để tự sinh getter/setter

@Data
public class CommentRequest {
    private Long userId;
    private Long songId;
    private String content;
}