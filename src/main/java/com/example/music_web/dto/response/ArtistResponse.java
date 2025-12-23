package com.example.music_web.dto.response;

import lombok.Data;

@Data
public class ArtistResponse {
    private Long artistId;
    private String name;
    private String description;
    private String coverImage;
}
