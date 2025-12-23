package com.example.music_web.Entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "comments")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Comment {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne @JoinColumn(name = "song_id")
    private Song song;

    @Column(columnDefinition = "TEXT")
    private String content;

    @Builder.Default
    private Integer likeCount = 0;    // Số lượt thích

    @Builder.Default
    private Integer dislikeCount = 0; // Số lượt không thích

    @CreationTimestamp
    private LocalDateTime createdAt;

    // Nếu muốn kỹ hơn: Quan hệ để biết ai đã like comment này (để chặn spam like)
    @OneToMany(mappedBy = "comment", cascade = CascadeType.ALL)
    @JsonIgnore
    @ToString.Exclude
    private List<CommentReaction> reactions;
}