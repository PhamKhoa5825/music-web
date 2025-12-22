package com.example.music_web.DTO.Request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateSongRequest {
    @NotBlank(message = "Song title is required")
    private String title;
    @NotEmpty(message = "Artist is required")
    private Long artistId;
    @NotEmpty(message = "Genre is required")
    @Size(max = 3, message = "You can only select up to 3 genres")
    private List<Long> genreId;


    private Long albumId;
    @NotNull(message = "Link song is required")
    private MultipartFile filePath;
    private MultipartFile coverImage;
    @NotBlank(message = "Lyric is required")
    private String lyric;

}
