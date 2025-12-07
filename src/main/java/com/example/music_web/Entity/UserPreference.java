package com.example.music_web.Entity;

import jakarta.persistence.*;

import java.util.List;

@Entity
public class UserPreference {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToMany
    @JoinTable(name = "preferred_genres")
    private List<Genre> favoriteGenres;

    @ManyToMany
    @JoinTable(name = "preferred_artists")
    private List<Artist> favoriteArtists;

    @Column(columnDefinition = "JSON") // hoặc TEXT
    private String listeningPattern; // thời gian nghe, tần suất
}