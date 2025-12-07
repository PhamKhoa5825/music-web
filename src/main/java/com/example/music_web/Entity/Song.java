package com.example.music_web.Entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "songs")
public class Song {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long songId;

    @Column(nullable = false)
    private String title;

    @ManyToOne
    @JoinColumn(name = "artist_id")
    private Artist artist;

    // Quan hệ Many-to-Many cho Genre (Một bài hát có thể có nhiều thể loại)
    @ManyToMany
    @JoinTable(
            name = "song_genres",
            joinColumns = @JoinColumn(name = "song_id"),
            inverseJoinColumns = @JoinColumn(name = "genre_id")
    )
    private List<Genre> genres;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "album_id") // Thêm Album vào Song
    private Album album;

    private String filePath;
    private String coverImage;

    @Column(nullable = false)
    private Integer views = 0;

    private LocalDateTime uploadDate = LocalDateTime.now();

    // Các trường phục vụ AI từ Set 1
    private Double averageRating;
    private Integer totalRatings;
    private String audioFeatures; // JSON/TEXT
}

