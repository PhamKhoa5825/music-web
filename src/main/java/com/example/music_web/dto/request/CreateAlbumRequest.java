package com.example.music_web.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateAlbumRequest {

    @NotBlank(message = "Album title is required")
    private String title;
    private String description;
    private MultipartFile coverUrl;
    private Integer releaseYear;
    @NotNull(message = "Artist ID is required")
    private Long artistId;
}