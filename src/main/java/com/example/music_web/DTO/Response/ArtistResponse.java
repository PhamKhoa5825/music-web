package com.example.music_web.DTO.Response;

import lombok.Data;

@Data
public class ArtistResponse {
    private Long artistId;
    private String name;
    private String description;
    private String coverImage;
}
