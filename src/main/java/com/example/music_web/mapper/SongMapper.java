package com.example.music_web.mapper;

import com.example.music_web.Entity.Song;
import com.example.music_web.dto.request.CreateSongRequest;
import com.example.music_web.dto.request.UpdateSongRequest;
import com.example.music_web.dto.response.SongResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface SongMapper {


    @Mapping(target = "artist", ignore = true)
    @Mapping(target = "album", ignore = true)
    @Mapping(target = "genres", ignore = true)
    @Mapping(target = "coverImage", ignore = true) // MapStruct đừng đụng vào coverImage
    @Mapping(target = "filePath", ignore = true)   // MapStruct đừng đụng vào filePath (hoặc audioFile)
    Song toSong (CreateSongRequest request);
    @Mapping(source = "filePath", target = "musicUrl")
    @Mapping(source = "coverImage", target = "imageUrl")

    @Mapping(source = "artist.artistId", target = "artistId")
    @Mapping(source = "artist.name", target = "artistName")
    @Mapping(source = "artist.coverImage", target = "coverImage")

    @Mapping(source = "album.albumId", target = "albumId")
    @Mapping(source = "album.title", target = "albumTitle")


    @Mapping(source = "genres", target = "genres")
    SongResponse toSongResponse (Song song);

    @Mapping(target = "artist", ignore = true)
    @Mapping(target = "album", ignore = true)
    @Mapping(target = "genres", ignore = true)
    @Mapping(target = "coverImage", ignore = true) // MapStruct đừng đụng vào coverImage
    @Mapping(target = "filePath", ignore = true)   // MapStruct đừng đụng vào filePath (hoặc audioFile)
    void updateSong(@MappingTarget Song song, UpdateSongRequest request);


}
