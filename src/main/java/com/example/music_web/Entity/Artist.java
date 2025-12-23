package com.example.music_web.Entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@Table(name = "artists")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Artist {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long artistId;

    @Column(nullable = false)
    private String name;

    @Column
    private String description;
    private String coverImage;

    // Bổ sung quan hệ ngược (nếu bạn muốn lấy list bài hát từ Artist)
    @OneToMany(mappedBy = "artist")
    @JsonIgnore //Khi lấy Artist, không lôi hết bài hát ra
    @ToString.Exclude
    private List<Song> songs;

    // Bổ sung quan hệ ngược (lấy list album)
    @OneToMany(mappedBy = "artist")
    @ToString.Exclude
    @JsonIgnore
    private List<Album> albums;
}

