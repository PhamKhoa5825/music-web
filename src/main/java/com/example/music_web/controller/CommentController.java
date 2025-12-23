package com.example.music_web.controller;

import com.example.music_web.Entity.Comment;
import com.example.music_web.dto.request.CommentRequest;
import com.example.music_web.service.CommentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/comments")
public class CommentController {

    @Autowired
    private CommentService commentService;

    // GET: Lấy comment của bài hát (kèm sắp xếp)
    // URL ví dụ: /api/comments/song/1?sort=likes
    @GetMapping("/song/{songId}")
    public ResponseEntity<List<Comment>> getSongComments(
            @PathVariable Long songId,
            @RequestParam(defaultValue = "newest") String sort) {
        return ResponseEntity.ok(commentService.getComments(songId, sort));
    }

    // POST: Viết bình luận
    @PostMapping("/add")
    public ResponseEntity<Comment> addComment(@RequestBody CommentRequest req) {
        return ResponseEntity.ok(commentService.addComment(req.getUserId(), req.getSongId(), req.getContent()));
    }

    // POST: Like/Dislike comment
    // URL ví dụ: /api/comments/react?userId=1&commentId=5&isLike=true
    @PostMapping("/react")
    public ResponseEntity<String> react(@RequestParam Long userId,
                                        @RequestParam Long commentId,
                                        @RequestParam Boolean isLike) {
        commentService.reactToComment(userId, commentId, isLike);
        return ResponseEntity.ok("Success");
    }
}
