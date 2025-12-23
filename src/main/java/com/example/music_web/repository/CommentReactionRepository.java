package com.example.music_web.repository;

import com.example.music_web.Entity.Comment;
import com.example.music_web.Entity.CommentReaction;
import com.example.music_web.Entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CommentReactionRepository extends JpaRepository<CommentReaction, Long> {
    // Kiểm tra xem user này đã react comment này chưa
    Optional<CommentReaction> findByUserAndComment(User user, Comment comment);
}