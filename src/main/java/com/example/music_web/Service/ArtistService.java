package com.example.music_web.Service;

import com.example.music_web.DTO.Request.CreateArtistRequest;
import com.example.music_web.DTO.Request.UpdateArtistRequest;
import com.example.music_web.DTO.Response.ArtistResponse;
import com.example.music_web.DTO.Response.UploadResponse;
import com.example.music_web.Entity.Artist;
import com.example.music_web.ExceptionHandler.AppException;
import com.example.music_web.ExceptionHandler.ErrorCode;
import com.example.music_web.Mapper.ArtistMapper;
import com.example.music_web.Repository.ArtistRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class ArtistService {

    @Autowired
    private ArtistRepo artistRepo;

    @Autowired
    private ArtistMapper artistMapper;
    @Autowired
    CloudinaryService cloudinaryService;


    public Page<ArtistResponse> getAllArtists(String name, Pageable pageable) {
        Page<Artist> artistPage = artistRepo.searchArtists(name, pageable);
        return artistPage.map(artistMapper::toArtistResponse);
    }



    public ArtistResponse getArtistById(Long id) {
        Artist artist = artistRepo.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.ARTIST_NOT_EXISTED));
        return artistMapper.toArtistResponse(artist);
    }


    public ArtistResponse createArtist(CreateArtistRequest request) {
        Artist artist = artistMapper.toArtist(request);

        if (request.getCoverImage() != null && !request.getCoverImage().isEmpty()) {
            UploadResponse response = cloudinaryService.uploadCover(request.getCoverImage());
            artist.setCoverImage(response.getUrl());
        }
        Artist savedArtist = artistRepo.save(artist);
        return artistMapper.toArtistResponse(savedArtist);
    }


    public ArtistResponse updateArtist(Long id, UpdateArtistRequest request) {
        Artist artist = artistRepo.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.ARTIST_NOT_EXISTED));

        if (request.getCoverImage() != null && !request.getCoverImage().isEmpty()) {
            UploadResponse response = cloudinaryService.uploadCover(request.getCoverImage());
            artist.setCoverImage(response.getUrl());
        }

        artistMapper.updateArtist(artist, request);
        Artist updatedArtist = artistRepo.save(artist);
        return artistMapper.toArtistResponse(updatedArtist);
    }


    public void deleteArtist(Long id) {
        if (!artistRepo.existsById(id)) {
            throw new AppException(ErrorCode.ARTIST_NOT_EXISTED);
        }

        // Kiểm tra xem artist có song hoặc album liên quan không
        if (artistRepo.hasSongs(id)) {
            throw new AppException(ErrorCode.ARTIST_HAS_SONGS); // Bạn cần thêm ErrorCode này
        }

        if (artistRepo.hasAlbums(id)) {
            throw new AppException(ErrorCode.ARTIST_HAS_ALBUMS); // Bạn cần thêm ErrorCode này
        }

        artistRepo.deleteById(id);
    }
}
