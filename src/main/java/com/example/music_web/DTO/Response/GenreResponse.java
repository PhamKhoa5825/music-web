package com.example.music_web.DTO.Response;

import lombok.Data;

@Data
public class GenreResponse {
    private Long genreId;
    private String name;
    private String coverImage;
}