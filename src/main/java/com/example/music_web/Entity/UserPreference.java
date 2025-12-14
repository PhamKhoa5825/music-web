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

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public List<Genre> getFavoriteGenres() {
        return favoriteGenres;
    }

    public void setFavoriteGenres(List<Genre> favoriteGenres) {
        this.favoriteGenres = favoriteGenres;
    }

    public List<Artist> getFavoriteArtists() {
        return favoriteArtists;
    }

    public void setFavoriteArtists(List<Artist> favoriteArtists) {
        this.favoriteArtists = favoriteArtists;
    }

    public String getListeningPattern() {
        return listeningPattern;
    }

    public void setListeningPattern(String listeningPattern) {
        this.listeningPattern = listeningPattern;
    }
}