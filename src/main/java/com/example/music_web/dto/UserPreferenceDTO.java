package com.example.music_web.dto;

import lombok.Data;

import java.util.List;

@Data
public class UserPreferenceDTO {
    private List<Long> favoriteGenreIds;
    private List<Long> favoriteArtistIds;
    private String listeningPattern;
}