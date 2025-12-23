package com.example.music_web.dto.response;

import lombok.Data;

@Data
public class GenreResponse {
    private Long genreId;
    private String name;
    private String coverImage;
}