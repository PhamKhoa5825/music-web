package com.example.music_web.Entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.ToString;

import java.util.List;
@Data
@Entity
public class UserPreference {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "user_id")
    @ToString.Exclude
    private User user;

    @ManyToMany
    @JoinTable(
            name = "preferred_genres", // Tên bảng trung gian
            joinColumns = @JoinColumn(name = "user_preference_id"), // Khóa ngoại trỏ về bảng này (UserPreference)
            inverseJoinColumns = @JoinColumn(name = "genre_id")     // Khóa ngoại trỏ về bảng kia (Genre)
    )
    @ToString.Exclude
    private List<Genre> favoriteGenres;

    @ManyToMany
    @JoinTable(
            name = "preferred_artists",
            joinColumns = @JoinColumn(name = "user_preference_id"),
            inverseJoinColumns = @JoinColumn(name = "artist_id")
    )
    @ToString.Exclude
    private List<Artist> favoriteArtists;

    @Column(columnDefinition = "JSON") // hoặc TEXT
    private String listeningPattern; // thời gian nghe, tần suất


}