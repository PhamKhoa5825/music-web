package com.example.music_web.Service;

import com.example.music_web.Entity.*;
import com.example.music_web.Repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public class InteractionService {

    @Autowired private ListeningHistoryRepository historyRepository;
    @Autowired private FavoriteRepository favoriteRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private SongRepository songRepository;

    public void logHistory(Long userId, Long songId) {
        User user = userRepository.findById(userId).orElseThrow();
        Song song = songRepository.findById(songId).orElseThrow();

        ListeningHistory history = new ListeningHistory();
        history.setUser(user);
        history.setSong(song);
        historyRepository.save(history);

        // TODO: Có thể gọi SongRepository để tăng viewCount ở đây
    }

    public List<ListeningHistory> getUserHistory(Long userId) {
        User user = userRepository.findById(userId).orElseThrow();
        return historyRepository.findByUserOrderByListenedAtDesc(user);
    }

    public String toggleFavorite(Long userId, Long songId) {
        User user = userRepository.findById(userId).orElseThrow();
        Song song = songRepository.findById(songId).orElseThrow();

        Optional<Favorite> existing = favoriteRepository.findByUserAndSong(user, song);
        if (existing.isPresent()) {
            favoriteRepository.delete(existing.get());
            return "Unliked";
        } else {
            Favorite favorite = new Favorite();
            favorite.setUser(user);
            favorite.setSong(song);
            favoriteRepository.save(favorite);
            return "Liked";
        }
    }
}
