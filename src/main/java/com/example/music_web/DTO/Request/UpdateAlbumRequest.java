package com.example.music_web.DTO.Request;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateAlbumRequest {

    @NotBlank(message = "Album title is required")
    private String title;
    private String description;
    private MultipartFile coverUrl;
    private Integer releaseYear;
    private Long artistId;
}