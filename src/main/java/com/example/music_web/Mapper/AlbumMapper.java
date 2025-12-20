package com.example.music_web.Mapper;

import com.example.music_web.DTO.Request.CreateAlbumRequest;
import com.example.music_web.DTO.Request.UpdateAlbumRequest;
import com.example.music_web.DTO.Response.AlbumResponse;
import com.example.music_web.Entity.Album;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface AlbumMapper {

    @Mapping(target = "artist", ignore = true)
    @Mapping(target = "songs", ignore = true)
    @Mapping(target = "coverUrl", ignore = true)
    Album toAlbum(CreateAlbumRequest request);

    @Mapping(source = "artist.artistId", target = "artistId")
    @Mapping(source = "artist.name", target = "artistName")
    AlbumResponse toAlbumResponse(Album album);

    @Mapping(target = "artist", ignore = true)
    @Mapping(target = "songs", ignore = true)
    @Mapping(target = "coverUrl", ignore = true) // MapStruct đừng đụng vào coverImage
    void updateAlbum(@MappingTarget Album album, UpdateAlbumRequest request);
}