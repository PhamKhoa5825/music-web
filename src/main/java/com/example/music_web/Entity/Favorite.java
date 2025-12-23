package com.example.music_web.Entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "favorites", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"user_id", "song_id"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Favorite {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id") // Sửa từ userId thành User entity
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "song_id")
    private Song song;

    @CreationTimestamp
    private LocalDateTime createdAt;

    // --- BỔ SUNG TRƯỜNG NÀY ĐỂ SỬA LỖI ---
    @Column(name = "liked_at")
    private LocalDateTime likedAt;

    // Tự động gán thời gian khi tạo mới
    @PrePersist
    protected void onCreate() {
        this.likedAt = LocalDateTime.now();
    }
}