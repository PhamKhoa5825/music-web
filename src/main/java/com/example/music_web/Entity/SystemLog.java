package com.example.music_web.Entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
@Data
@Entity
@Table(name = "system_logs")
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

