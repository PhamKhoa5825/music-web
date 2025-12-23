package com.example.music_web.Entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "listening_history")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ListeningHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long historyId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id") // Sửa từ userId thành User entity
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "song_id")
    private Song song;

    @CreationTimestamp
    private LocalDateTime listenedAt;
}

