package com.example.music_web.Entity;

import jakarta.persistence.*;
import lombok.*;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserPreference {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "user_id")
    @ToString.Exclude // Tránh in ngược lại user
    private User user;

    @ManyToMany
    @JoinTable(name = "preferred_genres")
    @ToString.Exclude // BẮT BUỘC
    private List<Genre> favoriteGenres;

    @ManyToMany
    @JoinTable(name = "preferred_artists")
    @ToString.Exclude // BẮT BUỘC
    private List<Artist> favoriteArtists;

    @Column(columnDefinition = "JSON") // hoặc TEXT
    private String listeningPattern; // thời gian nghe, tần suất
}