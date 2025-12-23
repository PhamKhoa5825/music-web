package com.example.music_web.mapper;

import com.example.music_web.Entity.Genre;
import com.example.music_web.dto.request.CreateGenreRequest;
import com.example.music_web.dto.request.UpdateGenreRequest;
import com.example.music_web.dto.response.GenreResponse;
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