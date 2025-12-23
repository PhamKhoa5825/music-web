package com.example.music_web.mapper;

import com.example.music_web.Entity.Artist;
import com.example.music_web.dto.request.CreateArtistRequest;
import com.example.music_web.dto.request.UpdateArtistRequest;
import com.example.music_web.dto.response.ArtistResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface ArtistMapper {

    @Mapping(target = "coverImage", ignore = true) // MapStruct đừng đụng vào coverImage
    Artist toArtist(CreateArtistRequest request);

    ArtistResponse toArtistResponse(Artist artist);

    @Mapping(target = "coverImage", ignore = true) // MapStruct đừng đụng vào coverImage
    void updateArtist(@MappingTarget Artist artist, UpdateArtistRequest request);
}
