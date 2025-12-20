package com.example.music_web.DTO.Request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateGenreRequest {

    @NotBlank(message = "Genre name is required")
    private String name;
    private MultipartFile coverImage;
}