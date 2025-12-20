package com.example.music_web.Entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "artists")
@Data
public class Artist {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long artistId;

    @Column(nullable = false)
    private String name;

    @Column
    private String description;
    private String coverImage;
}

