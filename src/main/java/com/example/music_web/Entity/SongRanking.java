package com.example.music_web.Entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "song_rankings", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"ranking_date", "song_id"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SongRanking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "song_id")
    private Song song;

    private LocalDate rankingDate;

    @Column(nullable = false)
    private Integer rank;

    private Integer totalViews; // Số lượt xem trong ngày/chu kỳ này
}

