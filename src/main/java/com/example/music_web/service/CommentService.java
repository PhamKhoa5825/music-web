package com.example.music_web.service;

import com.example.music_web.Entity.Comment;
import com.example.music_web.Entity.CommentReaction;
import com.example.music_web.Entity.Song;
import com.example.music_web.Entity.User;
import com.example.music_web.repository.CommentReactionRepository;
import com.example.music_web.repository.CommentRepository;
import com.example.music_web.repository.SongRepository;
import com.example.music_web.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class CommentService {

    @Autowired private CommentRepository commentRepository;
    @Autowired private CommentReactionRepository reactionRepository; // Repo mới tạo ở bước trước
    @Autowired private SongRepository songRepository;
    @Autowired private UserRepository userRepository;

    /**
     * 1. Lấy danh sách bình luận theo bài hát + Sắp xếp
     */
    public List<Comment> getComments(Long songId, String sortBy) {
        Song song = songRepository.findById(songId)
                .orElseThrow(() -> new RuntimeException("Song not found"));

        // Tạo đối tượng sắp xếp dựa trên tham số frontend gửi lên
        Pageable pageable;

        // Lưu ý: sortBy nên check null
        if (sortBy == null) sortBy = "newest";

        switch (sortBy) {
            case "likes":
                // Sắp xếp nhiều like nhất
                pageable = PageRequest.of(0, 20, Sort.by("likeCount").descending());
                break;
            case "oldest":
                // Cũ nhất trước
                pageable = PageRequest.of(0, 20, Sort.by("createdAt").ascending());
                break;
            default: // "newest"
                // Mặc định: Mới nhất trước
                pageable = PageRequest.of(0, 20, Sort.by("createdAt").descending());
                break;
        }

        return commentRepository.findBySong(song, pageable).getContent();
    }

    /**
     * 2. Thêm bình luận mới (Cần thiết để có dữ liệu mà test)
     */
    public Comment addComment(Long userId, Long songId, String content) {
        User user = userRepository.findById(userId).orElseThrow();
        Song song = songRepository.findById(songId).orElseThrow();

        Comment comment = new Comment();
        comment.setUser(user);
        comment.setSong(song);
        comment.setContent(content);
        // likeCount, dislikeCount mặc định là 0 do @Builder.Default hoặc khởi tạo

        return commentRepository.save(comment);
    }

    /**
     * 3. Logic Like/Dislike Comment (Toggle)
     */
    @Transactional // Quan trọng để đảm bảo tính nhất quán dữ liệu
    public void reactToComment(Long userId, Long commentId, boolean isLike) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new RuntimeException("Comment not found"));

        Optional<CommentReaction> existing = reactionRepository.findByUserAndComment(user, comment);

        if (existing.isPresent()) {
            // Nếu đã react rồi -> Xóa (Toggle - Bấm lần nữa để hủy)
            CommentReaction reaction = existing.get();

            // Nếu user đang like mà bấm like lần nữa -> Hủy like -> Giảm likeCount
            if (reaction.getIsLike()) {
                comment.setLikeCount(Math.max(0, comment.getLikeCount() - 1));
            } else {
                comment.setDislikeCount(Math.max(0, comment.getDislikeCount() - 1));
            }

            // Xóa record reaction
            reactionRepository.delete(reaction);

            // LƯU Ý NÂNG CAO: Nếu bạn muốn logic "Đang Like chuyển sang Dislike",
            // code sẽ phức tạp hơn chút. Ở đây ta dùng logic Toggle đơn giản.

        } else {
            // Chưa react bao giờ -> Tạo mới
            CommentReaction reaction = new CommentReaction();
            reaction.setUser(user);
            reaction.setComment(comment);
            reaction.setIsLike(isLike);
            reactionRepository.save(reaction);

            // Tăng count
            if (isLike) {
                comment.setLikeCount(comment.getLikeCount() + 1);
            } else {
                comment.setDislikeCount(comment.getDislikeCount() + 1);
            }
        }

        // Lưu thay đổi số lượng like/dislike vào bảng Comment
        commentRepository.save(comment);
    }
}