package com.example.music_web.Repository;

import com.example.music_web.Entity.Comment;
import com.example.music_web.Entity.Song;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {

    // 1. Tìm comment theo bài hát + Hỗ trợ Phân trang & Sắp xếp
    // Pageable sẽ giúp bạn sort theo: createdAt desc, likeCount desc,...
    Page<Comment> findBySong(Song song, Pageable pageable);

    // 2. Đếm tổng số comment của 1 bài (để hiển thị icon comment)
    Long countBySong(Song song);
}