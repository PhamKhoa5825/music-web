package com.example.music_web.Entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

@Entity
@Table(name = "recommendations")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Recommendation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne
    @JoinColumn(name = "recommended_song_id")
    private Song recommendedSong;

    private Double confidenceScore;

    private String reason; // Lý do gợi ý: "Based on genre", "Similar to..."

    @CreationTimestamp
    private LocalDateTime recommendedAt;

    private Boolean clicked = false; // Theo dõi hành vi sau gợi ý
    private Boolean liked = false;
}

