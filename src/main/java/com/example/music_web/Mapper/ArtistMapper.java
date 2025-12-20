package com.example.music_web.Mapper;

import com.example.music_web.DTO.Request.CreateArtistRequest;
import com.example.music_web.DTO.Request.UpdateArtistRequest;
import com.example.music_web.DTO.Response.ArtistResponse;
import com.example.music_web.Entity.Artist;
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
