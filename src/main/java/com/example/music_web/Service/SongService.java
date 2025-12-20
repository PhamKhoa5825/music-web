package com.example.music_web.Service;


import com.example.music_web.DTO.Request.CreateSongRequest;
import com.example.music_web.DTO.Request.UpdateSongRequest;
import com.example.music_web.DTO.Response.SongResponse;
import com.example.music_web.DTO.Response.UploadResponse;
import com.example.music_web.Entity.Album;
import com.example.music_web.Entity.Artist;
import com.example.music_web.Entity.Genre;
import com.example.music_web.Entity.Song;
import com.example.music_web.ExceptionHandler.AppException;
import com.example.music_web.ExceptionHandler.ErrorCode;
import com.example.music_web.Mapper.SongMapper;
import com.example.music_web.Repository.AlbumRepo;
import com.example.music_web.Repository.ArtistRepo;
import com.example.music_web.Repository.GenreRepo;
import com.example.music_web.Repository.SongRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SongService {
    @Autowired
    SongRepo songRepo;
    @Autowired
    ArtistRepo artistRepo;
    @Autowired
    AlbumRepo albumRepo;
    @Autowired
    GenreRepo genreRepo;
    @Autowired
    SongMapper songMapper;
    @Autowired
    CloudinaryService cloudinaryService;


    public Page<SongResponse> getAllSongs(String title,
                                          Long artistId,
                                          Long albumId,
                                          Long genreId,
                                          Pageable pageable) {
        Page<Song> songPage = songRepo.searchSongs(title, artistId, albumId, genreId, pageable);


        return songPage.map(songMapper::toSongResponse);
    }

    public SongResponse getSongById(Long id) {
        return songMapper.toSongResponse(songRepo.findById(id).orElseThrow(
                () -> new AppException(ErrorCode.SONG_NOT_EXISTED)));
    }

    public Page<SongResponse> getSongsByArtistId(Long artistId, Pageable pageable) {
        // Gọi Repo để tìm bài hát theo artistId
        Page<Song> songPage = songRepo.findByArtistId(artistId, pageable);
        // Map từ Entity sang DTO
        return songPage.map(songMapper::toSongResponse);
    }

    public Page<SongResponse> getSongsByAlbumId(Long albumId, Pageable pageable) {

        Page<Song> songPage = songRepo.findByAlbumId(albumId, pageable);

        return songPage.map(songMapper::toSongResponse);
    }

    public Page<SongResponse> getSongsByGenreId(Long genreId, Pageable pageable) {

        Page<Song> songPage = songRepo.findByGenreId(genreId, pageable);

        return songPage.map(songMapper::toSongResponse);
    }

    public SongResponse createNewSong(CreateSongRequest request) {

        Song song = songMapper.toSong(request);
        Artist artist = artistRepo.findById(request.getArtistId())
                .orElseThrow(() -> new AppException(ErrorCode.ARTIST_NOT_EXISTED));
        song.setArtist(artist);
        if (request.getAlbumId() != null) {
            Album album = albumRepo.findById(request.getAlbumId())
                    .orElseThrow(() -> new AppException(ErrorCode.ALBUM_NOT_EXISTED));
            song.setAlbum(album);
        }

        List<Genre> genre = genreRepo.findAllById(request.getGenreId());
        song.setGenres(genre);

        if (request.getFilePath() != null && !request.getFilePath().isEmpty()) {
            UploadResponse response = cloudinaryService.uploadSong(request.getFilePath());
            song.setFilePath(response.getUrl()); // Lưu URL mới vào DB
        }
        // Nếu không vào if, song.setFilePath vẫn giữ giá trị cũ.

        // 4. Xử lý Ảnh bìa (Tương tự)
        if (request.getCoverImage() != null && !request.getCoverImage().isEmpty()) {
            UploadResponse response = cloudinaryService.uploadCover(request.getCoverImage());
            song.setCoverImage(response.getUrl());
        }

        return songMapper.toSongResponse(songRepo.save(song));
    }

    public SongResponse updateSong(UpdateSongRequest request, Long songId) {
        Song song = songRepo.findById(songId)
                .orElseThrow(() -> new AppException(ErrorCode.SONG_NOT_EXISTED));
        songMapper.updateSong(song, request);
        if (request.getAlbumId() == -1) { // go khoi album
            song.setAlbum(null);
        }
            Artist artist = artistRepo.findById(request.getArtistId())
                    .orElseThrow(() -> new AppException(ErrorCode.ARTIST_NOT_EXISTED));
            song.setArtist(artist);

        if (request.getAlbumId() != null) {
            Album album = albumRepo.findById(request.getAlbumId())
                    .orElseThrow(() -> new AppException(ErrorCode.ALBUM_NOT_EXISTED));
            song.setAlbum(album);
        }

        List<Genre> genre = genreRepo.findAllById(request.getGenreId());
        song.setGenres(genre);

        if (request.getFilePath() != null && !request.getFilePath().isEmpty()) {
            UploadResponse response = cloudinaryService.uploadSong(request.getFilePath());
            song.setFilePath(response.getUrl()); // Lưu URL mới vào DB
        }
        // Nếu không vào if, song.setFilePath vẫn giữ giá trị cũ.

        // 4. Xử lý Ảnh bìa (Tương tự)
        if (request.getCoverImage() != null && !request.getCoverImage().isEmpty()) {
            UploadResponse response = cloudinaryService.uploadCover(request.getCoverImage());
            song.setCoverImage(response.getUrl());
        }

        return songMapper.toSongResponse(songRepo.save(song));
    }

    public void deleteSong(Long songId) {
        if (!songRepo.existsById(songId)) {
            throw new AppException(ErrorCode.SONG_NOT_EXISTED);

        }
        songRepo.deleteById(songId);
    }


    public void incrementView(Long songId) {
        Song song = songRepo.findById(songId)
                .orElseThrow(() -> new AppException(ErrorCode.SONG_NOT_EXISTED));
        song.setViews(song.getViews() + 1);
        songRepo.save(song);
    }

    public List<Long> getGenreIdsBySongId(Long songId) {
        Song song = songRepo.findById(songId)
                .orElseThrow(() -> new AppException(ErrorCode.SONG_NOT_EXISTED));

        return song.getGenres().stream()
                .map(Genre::getGenreId)
                .collect(java.util.stream.Collectors.toList());
    }


}
