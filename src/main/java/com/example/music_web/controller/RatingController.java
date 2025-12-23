package com.example.music_web.controller;

import com.example.music_web.Entity.SongRating;
import com.example.music_web.dto.request.RatingRequest;
import com.example.music_web.service.RatingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/ratings")
public class RatingController {

    @Autowired private RatingService ratingService;

    @PostMapping
    public ResponseEntity<SongRating> addRating(@RequestBody RatingRequest request) {
        return ResponseEntity.ok(ratingService.addRating(request));
    }
}
