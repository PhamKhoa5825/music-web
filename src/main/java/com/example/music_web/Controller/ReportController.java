package com.example.music_web.Controller;

import com.example.music_web.DTO.ReportRequest;
import com.example.music_web.Entity.*;
import com.example.music_web.Repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/reports")
public class ReportController {

    @Autowired private ReportRepository reportRepo;
    @Autowired private UserRepository userRepo;
    @Autowired private SongRepository songRepo;
    @Autowired private CommentRepository commentRepo;

    @PostMapping("/create")
    public ResponseEntity<String> createReport(@RequestBody ReportRequest req) {
        User reporter = userRepo.findById(req.getReporterId()).orElseThrow();

        Report report = new Report();
        report.setReporter(reporter);
        report.setReason(req.getReason());
        report.setStatus(ReportStatus.PENDING);

        if ("SONG".equalsIgnoreCase(req.getType())) {
            Song song = songRepo.findById(req.getTargetId()).orElseThrow();
            report.setSong(song);
        } else if ("COMMENT".equalsIgnoreCase(req.getType())) {
            Comment comment = commentRepo.findById(req.getTargetId()).orElseThrow();
            report.setComment(comment);
            // Có thể lưu thêm Song của comment đó nếu muốn dễ truy vết
            report.setSong(comment.getSong());
        }

        reportRepo.save(report);
        return ResponseEntity.ok("Report submitted successfully");
    }
}