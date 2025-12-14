package com.example.music_web.service;

import com.example.music_web.Entity.*;
import com.example.music_web.dto.UserPreferenceDTO;
import com.example.music_web.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserPreferenceService {
    private final UserPreferenceRepository preferenceRepository;
    private final GenreRepository genreRepository;
    private final ArtistRepository artistRepository;
    private final SystemLogService logService;

    public void updateUserPreference(User user, UserPreferenceDTO dto) {
        UserPreference pref = preferenceRepository.findByUser_UserId(user.getUserId())
                .orElse(new UserPreference()); // Nếu chưa có thì tạo mới

        pref.setUser(user);

        // Cập nhật Genres
        if (dto.getFavoriteGenreIds() != null) {
            List<Genre> genres = genreRepository.findAllById(dto.getFavoriteGenreIds());
            pref.setFavoriteGenres(genres);
        }

        // Cập nhật Artists
        if (dto.getFavoriteArtistIds() != null) {
            List<Artist> artists = artistRepository.findAllById(dto.getFavoriteArtistIds());
            pref.setFavoriteArtists(artists);
        }

        pref.setListeningPattern(dto.getListeningPattern());

        preferenceRepository.save(pref);
        logService.log(user, "UPDATE_PREF", "Updated music preferences");
    }
}