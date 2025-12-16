package com.example.music_web.Entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "songs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Song {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long songId;

    @Column(nullable = false)
    private String title;

    @ManyToOne
    @JoinColumn(name = "artist_id")
    @ToString.Exclude
    private Artist artist;

    // Quan hệ Many-to-Many cho Genre (Một bài hát có thể có nhiều thể loại)
    @ManyToMany
    @JoinTable(
            name = "song_genres",
            joinColumns = @JoinColumn(name = "song_id"),
            inverseJoinColumns = @JoinColumn(name = "genre_id")
    )
    @ToString.Exclude
    private List<Genre> genres;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "album_id") // Thêm Album vào Song
    @ToString.Exclude // Quan hệ Lazy nên exclude để tránh lỗi query ngầm
    private Album album;

    private String filePath;
    private String coverImage;

    @Builder.Default // Nếu dùng @Builder, cần dòng này để set giá trị mặc định
    @Column(nullable = false)
    private Integer views = 0;

    @Builder.Default
    private LocalDateTime uploadDate = LocalDateTime.now();

    // Các trường phục vụ AI từ Set 1
    @Column(name = "average_rating")
    private Double averageRating = 0.0; // Thêm giá trị mặc định 0.0

    @Column(name = "total_ratings")
    private Integer totalRatings = 0;   // Thêm giá trị mặc định 0
    private String audioFeatures; // JSON/TEXT

    @Column(columnDefinition = "TEXT")
    private String lyrics; // Lời bài hát

    private String backgroundImage;

    @Builder.Default
    @Column(name = "is_hidden", nullable = false, columnDefinition = "boolean default false")
    private Boolean isHidden = false; // false = Hiện (Visible), true = Ẩn (Hidden)
}

