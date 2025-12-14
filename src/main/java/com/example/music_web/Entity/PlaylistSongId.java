package com.example.music_web.Entity;

import jakarta.persistence.Embeddable;

import java.io.Serializable;

@Embeddable
public class PlaylistSongId implements Serializable {
    private Long playlistId;
    private Long songId;

    // Getters, setters, equals, and hashCode...
}
