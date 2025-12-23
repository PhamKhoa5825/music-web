package com.example.music_web.Entity;

import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PlaylistSongId implements Serializable {
    private Long playlistId;
    private Long songId;

    // Getters, setters, equals, and hashCode...
}
