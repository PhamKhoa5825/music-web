package com.example.music_web.Entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;
import java.time.LocalDateTime;

@Entity
@Table(name = "playlist_songs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PlaylistSong {

    @EmbeddedId
    private PlaylistSongId id;

    @ManyToOne
    @MapsId("playlistId")
    @JoinColumn(name = "playlist_id")
    @JsonIgnore // Để khi xem bài hát trong playlist, nó không in ngược lại thông tin playlist nữa
    private Playlist playlist;

    @ManyToOne
    @MapsId("songId")
    @JoinColumn(name = "song_id")
    private Song song;

    private Integer trackOrder; // Thêm trường thứ tự bài hát trong playlist

    @Builder.Default
    private LocalDateTime addedAt = LocalDateTime.now(); // Thời điểm thêm vào
}



