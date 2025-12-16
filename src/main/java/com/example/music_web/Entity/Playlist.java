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

    @Column(nullable = false)
    private String name;

    @Column(columnDefinition = "TEXT") // Mới: Mô tả playlist
    private String description;

    @Column(name = "image_url") // Mới: Ảnh bìa playlist
    private String imageUrl;

    @Column(name = "background_image")
    private String backgroundImage;

    @Column(name = "is_public")
    private Boolean isPublic = true;

    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    // Mapping ngược để lấy danh sách bài hát trong playlist
    // mappedBy trỏ tới tên biến "playlist" trong class PlaylistSong
    @OneToMany(mappedBy = "playlist", cascade = CascadeType.ALL, orphanRemoval = true) // Thêm orphanRemoval để xóa sạch khi xóa playlist
    @ToString.Exclude
    private List<PlaylistSong> playlistSongs;
}

