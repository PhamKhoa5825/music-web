package com.example.music_web.Controller;

import com.example.music_web.Entity.ListeningHistory;
import com.example.music_web.Service.InteractionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api")
public class InteractionController {

    @Autowired private InteractionService interactionService;

    // --- History APIs ---
    @GetMapping("/history/{userId}")
    public ResponseEntity<List<ListeningHistory>> getHistory(@PathVariable Long userId) {
        return ResponseEntity.ok(interactionService.getUserHistory(userId));
    }

    @PostMapping("/history")
    public ResponseEntity<String> logHistory(@RequestParam Long userId, @RequestParam Long songId) {
        interactionService.logHistory(userId, songId);
        return ResponseEntity.ok("History logged");
    }

    // --- Favorite APIs ---
    @PostMapping("/favorites/toggle")
    public ResponseEntity<String> toggleFavorite(@RequestParam Long userId, @RequestParam Long songId) {
        String result = interactionService.toggleFavorite(userId, songId);
        return ResponseEntity.ok(result);
    }
}
