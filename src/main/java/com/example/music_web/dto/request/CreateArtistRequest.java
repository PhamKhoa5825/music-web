package com.example.music_web.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateArtistRequest {


    @NotBlank(message = "Artist name is required")
    private String name;
    private String description;
    private MultipartFile coverImage;
}
