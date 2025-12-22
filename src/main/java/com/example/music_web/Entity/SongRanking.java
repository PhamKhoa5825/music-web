package com.example.music_web.Entity;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "song_rankings", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"ranking_date", "song_id"})
})
public class SongRanking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "song_id")
    private Song song;

    private LocalDate rankingDate;

    // Đổi tên cột trong Database thành "ranking_position" để tránh trùng từ khóa
    @Column(name = "ranking_position", nullable = false)
    private Integer rank;

    private Integer totalViews; // Số lượt xem trong ngày/chu kỳ này
}

