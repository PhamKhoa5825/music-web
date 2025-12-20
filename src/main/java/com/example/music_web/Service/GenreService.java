package com.example.music_web.Service;

import com.example.music_web.DTO.Request.CreateGenreRequest;
import com.example.music_web.DTO.Request.UpdateGenreRequest;
import com.example.music_web.DTO.Response.GenreResponse;
import com.example.music_web.DTO.Response.UploadResponse;
import com.example.music_web.Entity.Genre;
import com.example.music_web.ExceptionHandler.AppException;
import com.example.music_web.ExceptionHandler.ErrorCode;
import com.example.music_web.Mapper.GenreMapper;
import com.example.music_web.Repository.GenreRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class GenreService {

    @Autowired
    private GenreRepo genreRepo;

    @Autowired
    private GenreMapper genreMapper;
    @Autowired
    CloudinaryService cloudinaryService;

    public Page<GenreResponse> getAllGenres(String name, Pageable pageable) {
        Page<Genre> genrePage = genreRepo.searchGenres(name, pageable);
        return genrePage.map(genreMapper::toGenreResponse);
    }


    public GenreResponse getGenreById(Long id) {
        Genre genre = genreRepo.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.GENRE_NOT_EXISTED));
        return genreMapper.toGenreResponse(genre);
    }

    public GenreResponse createGenre(CreateGenreRequest request) {
        Genre genre = genreMapper.toGenre(request);

        if (request.getCoverImage() != null && !request.getCoverImage().isEmpty()) {
            UploadResponse response = cloudinaryService.uploadCover(request.getCoverImage()); // Nhớ dùng hàm upload ảnh riêng
            genre.setCoverImage(response.getUrl());
        }
        Genre savedGenre = genreRepo.save(genre);

        return genreMapper.toGenreResponse(savedGenre);
    }

    public GenreResponse updateGenre(Long id, UpdateGenreRequest request) {
        Genre genre = genreRepo.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.GENRE_NOT_EXISTED));

        if (request.getCoverImage() != null && !request.getCoverImage().isEmpty()) {
            UploadResponse response = cloudinaryService.uploadCover(request.getCoverImage()); // Nhớ dùng hàm upload ảnh riêng
            genre.setCoverImage(response.getUrl());
        }

        genreMapper.updateGenre(genre, request);
        Genre updatedGenre = genreRepo.save(genre);
        return genreMapper.toGenreResponse(updatedGenre);
    }

    public void deleteGenre(Long id) {
        if (!genreRepo.existsById(id)) {
            throw new AppException(ErrorCode.GENRE_NOT_EXISTED);
        }

        if (genreRepo.hasSongs(id)) {
            throw new AppException(ErrorCode.GENRE_HAS_SONGS);
        }

        genreRepo.deleteById(id);
    }
}