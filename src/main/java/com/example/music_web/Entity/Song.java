package com.example.music_web.Entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDate;
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
    @Column(nullable = false)
    private String artistName;

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

    @Column(name = "likes", columnDefinition = "int default 0")
    private Integer likes = 0;

    @Builder.Default
    private LocalDateTime uploadDate = LocalDateTime.now();

    // Các trường phục vụ AI từ Set 1
    @Column(name = "average_rating")
    private Double averageRating = 0.0; // Thêm giá trị mặc định 0.0

    @Column(name = "total_ratings")
    private Integer totalRatings = 0;   // Thêm giá trị mặc định 0
    private String audioFeatures; // JSON/TEXT

    @CreationTimestamp
    private LocalDate createdAt;

    @Column(columnDefinition = "TEXT")
    private String lyrics; // Lời bài hát

    private String backgroundImage;

    @Builder.Default
    @Column(name = "is_hidden", nullable = false, columnDefinition = "boolean default false")
    private Boolean isHidden = false; // false = Hiện (Visible), true = Ẩn (Hidden)

    private Integer duration; // Thời lượng tính bằng giây (seconds)

    // Thêm vào Song.java
    @Column(name = "bpm")
    private Integer bpm; // Nhịp đập mỗi phút

    @Column(name = "energy_level")
    private Integer energyLevel; // Mức độ năng lượng 1-10

    @Column(name = "danceability")
    private Integer danceability; // Khả năng nhảy múa 1-10

    @Column(name = "valence")
    private Integer valence; // Mức độ tích cực 1-10

    @Column(name = "release_year")
    private Integer releaseYear; // Năm phát hành

    private String language; // Ngôn ngữ bài hát (vi, en, ko, etc.)
    private Boolean explicit; // Có chứa nội dung nhạy cảm không

    // Thêm trường duration tính bằng phút:giây (derived)
    @Transient
    public String getFormattedDuration() {
        if (duration == null) return "0:00";
        int minutes = duration / 60;
        int seconds = duration % 60;
        return String.format("%d:%02d", minutes, seconds);
    }

    @Transient
    public Integer getReleaseYear() {
        return releaseYear;
    }

    @Transient
    public String getLanguageDisplay() {
        if (language == null) return "Không xác định";
        return switch (language.toLowerCase()) {
            case "vi" -> "Tiếng Việt";
            case "en" -> "Tiếng Anh";
            case "ko" -> "Tiếng Hàn";
            case "jp" -> "Tiếng Nhật";
            case "cn" -> "Tiếng Trung";
            default -> language.toUpperCase();
        };
    }

    @Transient
    public String getExplicitDisplay() {
        return Boolean.TRUE.equals(explicit) ? "Có" : "Không";
    }
}

