package com.example.music_web.Entity;

import jakarta.persistence.*;
import lombok.*;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Table(name = "artists")
@Getter
@Setter
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

