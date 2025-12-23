package com.example.music_web.Entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(uniqueConstraints = {
        @UniqueConstraint(columnNames = {"user_id", "song_id"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SongRating {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne
    @JoinColumn(name = "song_id")
    private Song song;

    @Column(nullable = false)
    private Integer rating; // 1-5 sao

    @Column(columnDefinition = "TEXT")
    private String review;

    @CreationTimestamp
    private LocalDateTime createdAt;
}
