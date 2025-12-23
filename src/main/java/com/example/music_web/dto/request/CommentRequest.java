package com.example.music_web.dto.request;

import lombok.Data;

@Data
public class CommentRequest {
    private Long userId;
    private Long songId;
    private String content;
}