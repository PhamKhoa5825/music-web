package com.example.music_web.Entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "playlists")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Playlist {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long playlistId;

    @ManyToOne
    @JoinColumn(name = "user_id")
    @ToString.Exclude
    private User user;

    private String name;

    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    // Mapping ngược để lấy danh sách bài hát trong playlist
    // mappedBy trỏ tới tên biến "playlist" trong class PlaylistSong
    @OneToMany(mappedBy = "playlist", cascade = CascadeType.ALL)
    @ToString.Exclude
    private List<PlaylistSong> playlistSongs;
}

