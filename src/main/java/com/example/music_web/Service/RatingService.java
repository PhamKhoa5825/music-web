package com.example.music_web.Service;

import com.example.music_web.DTO.RatingRequest;
import com.example.music_web.Entity.*;
import com.example.music_web.Repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class RatingService {
    @Autowired private SongRatingRepository ratingRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private SongRepository songRepository;

    public SongRating addRating(RatingRequest request) {
        User user = userRepository.findById(request.getUserId()).orElseThrow();
        Song song = songRepository.findById(request.getSongId()).orElseThrow();

        SongRating rating = new SongRating();
        rating.setUser(user);
        rating.setSong(song);
        rating.setRating(request.getRating());
        rating.setReview(request.getReview());

        return ratingRepository.save(rating);
    }
}
