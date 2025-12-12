package com.example.music_web.Entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

@Entity
@Table(name = "system_logs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SystemLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long logId;

    private String action;
    private String description;

    @CreationTimestamp
    private LocalDateTime time;

    @ManyToOne(fetch = FetchType.LAZY) // Có thể liên kết với User thực hiện hành động
    @JoinColumn(name = "user_id")
    private User user;
}

