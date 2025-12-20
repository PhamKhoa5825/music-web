package com.example.music_web.Service;

import com.example.music_web.DTO.Request.CreateAlbumRequest;
import com.example.music_web.DTO.Request.UpdateAlbumRequest;
import com.example.music_web.DTO.Response.AlbumResponse;
import com.example.music_web.DTO.Response.UploadResponse;
import com.example.music_web.Entity.Album;
import com.example.music_web.Entity.Artist;
import com.example.music_web.ExceptionHandler.AppException;
import com.example.music_web.ExceptionHandler.ErrorCode;
import com.example.music_web.Mapper.AlbumMapper;
import com.example.music_web.Repository.AlbumRepo;
import com.example.music_web.Repository.ArtistRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class AlbumService {

    @Autowired
    private AlbumRepo albumRepo;

    @Autowired
    private ArtistRepo artistRepo;

    @Autowired
    private AlbumMapper albumMapper;
    @Autowired
    CloudinaryService cloudinaryService;

    public Page<AlbumResponse> getAllAlbums(String title, Pageable pageable) {
        Page<Album> albumPage = albumRepo.searchAlbums(title, pageable);
        return albumPage.map(albumMapper::toAlbumResponse);
    }



    public AlbumResponse getAlbumById(Long id) {
        Album album = albumRepo.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.ALBUM_NOT_EXISTED));
        return albumMapper.toAlbumResponse(album);
    }

    public AlbumResponse createAlbum(CreateAlbumRequest request) {
        Album album = albumMapper.toAlbum(request);

        Artist artist = artistRepo.findById(request.getArtistId())
                .orElseThrow(() -> new AppException(ErrorCode.ARTIST_NOT_EXISTED));
        album.setArtist(artist);

        if (request.getCoverUrl() != null && !request.getCoverUrl().isEmpty()) {
            UploadResponse response = cloudinaryService.uploadCover(request.getCoverUrl()); // Nhớ dùng hàm upload ảnh riêng
            album.setCoverUrl(response.getUrl());
        }

        Album savedAlbum = albumRepo.save(album);
        return albumMapper.toAlbumResponse(savedAlbum);
    }

    public AlbumResponse updateAlbum(Long id, UpdateAlbumRequest request) {
        Album album = albumRepo.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.ALBUM_NOT_EXISTED));

        albumMapper.updateAlbum(album, request);

        Artist artist = artistRepo.findById(request.getArtistId())
                .orElseThrow(() -> new AppException(ErrorCode.ARTIST_NOT_EXISTED));
        album.setArtist(artist);

        if (request.getCoverUrl() != null && !request.getCoverUrl().isEmpty()) {
            UploadResponse response = cloudinaryService.uploadCover(request.getCoverUrl()); // Nhớ dùng hàm upload ảnh riêng
            album.setCoverUrl(response.getUrl());
        }

        Album updatedAlbum = albumRepo.save(album);
        return albumMapper.toAlbumResponse(updatedAlbum);
    }

    public void deleteAlbum(Long id) {
        if (!albumRepo.existsById(id)) {
            throw new AppException(ErrorCode.ALBUM_NOT_EXISTED);
        }

        if (albumRepo.hasSongs(id)) {
            throw new AppException(ErrorCode.ALBUM_HAS_SONGS);
        }

        albumRepo.deleteById(id);
    }
}