package com.example.music_web.Controller;

import com.example.music_web.DTO.RatingRequest;
import com.example.music_web.Entity.SongRating;
import com.example.music_web.Service.RatingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/ratings")
public class RatingController {

    @Autowired private RatingService ratingService;

    @PostMapping
    public ResponseEntity<SongRating> addRating(@RequestBody RatingRequest request) {
        return ResponseEntity.ok(ratingService.addRating(request));
    }
}
