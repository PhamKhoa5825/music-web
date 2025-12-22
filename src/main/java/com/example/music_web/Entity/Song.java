package com.example.music_web.Entity;

import jakarta.persistence.*;
import lombok.*; // Thêm Lombok để code gọn hơn
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "songs")
@Data // Thêm @Data để tự sinh Getter/Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Song {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long songId;

    @Column(nullable = false)
    private String title;

    // --- THÊM TRƯỜNG LỜI BÀI HÁT (MỚI) ---
    @Column(columnDefinition = "TEXT") // Dùng TEXT để lưu lời bài hát dài
    private String lyric;
    // -------------------------------------

    @ManyToOne
    @JoinColumn(name = "artist_id")
    private Artist artist;

    @ManyToMany
    @JoinTable(
            name = "song_genres",
            joinColumns = @JoinColumn(name = "song_id"),
            inverseJoinColumns = @JoinColumn(name = "genre_id")
    )
    private List<Genre> genres;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "album_id")
    private Album album;

    private String filePath;   // Link file nhạc (Cloudinary)
    private String coverImage; // Link ảnh bìa (Cloudinary)

    @Column(nullable = false)
    private Integer views = 0;

    private LocalDateTime uploadDate = LocalDateTime.now();

    // Các trường AI cũ
    private Double averageRating;
    private Integer totalRatings;
    private String audioFeatures;
}

