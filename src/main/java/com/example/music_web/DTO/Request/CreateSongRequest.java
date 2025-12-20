package com.example.music_web.DTO.Request;


import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateSongRequest {
    @NotBlank(message = "Song title is required")
    private String title;
    @NotBlank(message = "Artist is required")
    private Long artistId;
    @NotBlank(message = "Genre is required")
    private List<Long> genreId;
    private Long albumId;
    @NotBlank(message = "Link song is required")
    private MultipartFile filePath;
    private MultipartFile coverImage;
    @NotBlank(message = "Lyric is required")
    private String lyric;

}
