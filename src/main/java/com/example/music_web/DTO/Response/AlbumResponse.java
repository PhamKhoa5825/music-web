package com.example.music_web.DTO.Response;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AlbumResponse {
    private Long albumId;
    private String title;
    private String description;
    private String coverUrl;
    private Integer releaseYear;

    private Long artistId;
    private String artistName;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}