package com.example.music_web.Entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "comment_reactions", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"user_id", "comment_id"}) // 1 user chỉ reaction 1 lần/comment
})
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class CommentReaction {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne @JoinColumn(name = "comment_id")
    @JsonIgnore
    private Comment comment;

    private Boolean isLike; // true = Like, false = Dislike
}