package com.example.music_web.Entity;

import com.fasterxml.jackson.annotation.JsonFormat; // 1. Import cái này
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
    @JsonIgnore
    private Playlist playlist;

    @ManyToOne
    @MapsId("songId")
    @JoinColumn(name = "song_id")
    private Song song;

    private Integer trackOrder;

    // 2. Sửa phần hiển thị ngày tháng (Để trả về JSON đẹp: "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss") // <-- THÊM DÒNG NÀY
    @Builder.Default
    private LocalDateTime addedAt = LocalDateTime.now();

    // 3. Đảm bảo khi lưu xuống DB không bao giờ bị NULL
    @PrePersist // <-- THÊM HÀM NÀY
    protected void onCreate() {
        if (this.addedAt == null) {
            this.addedAt = LocalDateTime.now();
        }
    }
}