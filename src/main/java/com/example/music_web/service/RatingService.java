package com.example.music_web.service;

import com.example.music_web.Entity.Song;
import com.example.music_web.Entity.SongRating;
import com.example.music_web.Entity.User;
import com.example.music_web.dto.request.RatingRequest;
import com.example.music_web.repository.SongRatingRepository;
import com.example.music_web.repository.SongRepository;
import com.example.music_web.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RatingService {

    @Autowired private SongRatingRepository ratingRepository;
    @Autowired private SongRepository songRepository;
    @Autowired private UserRepository userRepository;

    @Transactional
    public SongRating addRating(RatingRequest req) {
        User user = userRepository.findById(req.getUserId()).orElseThrow();
        Song song = songRepository.findById(req.getSongId()).orElseThrow();

        // 1. Lưu hoặc Cập nhật Rating của User
        SongRating rating = ratingRepository.findByUserAndSong(user, song)
                .orElse(SongRating.builder().user(user).song(song).build());

        rating.setRating(req.getRating());
        rating.setReview(req.getReview());
        SongRating savedRating = ratingRepository.save(rating);

        // --- 2. TÍNH TOÁN LẠI TRUNG BÌNH NGAY LẬP TỨC ---
        // Gọi hàm tính trung bình từ Repository (đã khai báo ở các bước trước)
        Double newAverage = ratingRepository.getAverageRating(song.getSongId());
        Long totalCount = ratingRepository.countBySongSongId(song.getSongId());

        // Nếu chưa có ai đánh giá thì set mặc định
        if (newAverage == null) newAverage = 0.0;

        // 3. Cập nhật ngược lại vào bảng Song để Frontend hiển thị
        song.setAverageRating(newAverage);
        song.setTotalRatings(totalCount.intValue());
        songRepository.save(song);

        return savedRating;
    }
}