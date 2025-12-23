package com.example.music_web.Entity;

import com.example.music_web.enums.ReportStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "reports")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Report {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne @JoinColumn(name = "reporter_id")
    private User reporter; // Người báo cáo

    // Có thể report bài hát hoặc report comment. Ở đây demo report bài hát vi phạm
    @ManyToOne @JoinColumn(name = "song_id")
    private Song song;

    @ManyToOne @JoinColumn(name = "comment_id")
    private Comment comment;

    private String reason; // Lý do: "Nội dung xấu", "Vi phạm bản quyền"

    @Enumerated(EnumType.STRING)
    private ReportStatus status; // PENDING, RESOLVED, DISMISSED

    @CreationTimestamp
    private LocalDateTime reportedAt;
}

