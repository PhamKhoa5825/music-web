package com.example.music_web.Entity;

import jakarta.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;

@Entity
@Table(name = "playlist_songs")
public class PlaylistSong {

    @EmbeddedId
    private PlaylistSongId id;

    @ManyToOne
    @MapsId("playlistId")
    @JoinColumn(name = "playlist_id")
    private Playlist playlist;

    @ManyToOne
    @MapsId("songId")
    @JoinColumn(name = "song_id")
    private Song song;

    private Integer trackOrder; // Thêm trường thứ tự bài hát trong playlist

    private LocalDateTime addedAt = LocalDateTime.now(); // Thời điểm thêm vào

    // Getters and setters...
}
@Embeddable
public class PlaylistSongId implements Serializable {
    private Long playlistId;
    private Long songId;

    // Getters, setters, equals, and hashCode...
}


