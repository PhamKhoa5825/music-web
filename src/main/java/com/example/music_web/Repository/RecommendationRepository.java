package com.example.music_web.Repository;

import com.example.music_web.Entity.Recommendation;
import com.example.music_web.Entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface RecommendationRepository extends JpaRepository<Recommendation, Long> {
    List<Recommendation> findByUserOrderByConfidenceScoreDesc(User user);
}
