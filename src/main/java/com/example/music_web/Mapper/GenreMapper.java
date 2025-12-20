package com.example.music_web.Mapper;

import com.example.music_web.DTO.Request.CreateGenreRequest;
import com.example.music_web.DTO.Request.UpdateGenreRequest;
import com.example.music_web.DTO.Response.GenreResponse;
import com.example.music_web.Entity.Genre;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface GenreMapper {

    @Mapping(target = "coverImage", ignore = true) // MapStruct đừng đụng vào coverImage

    Genre toGenre(CreateGenreRequest request);

    GenreResponse toGenreResponse(Genre genre);

    @Mapping(target = "coverImage", ignore = true) // MapStruct đừng đụng vào coverImage
    void updateGenre(@MappingTarget Genre genre, UpdateGenreRequest request);
}