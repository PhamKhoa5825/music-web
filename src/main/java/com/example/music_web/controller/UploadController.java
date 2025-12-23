package com.example.music_web.controller;

import com.example.music_web.dto.response.UploadResponse;
import com.example.music_web.service.CloudinaryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Controller
@RequestMapping("/upload")
public class UploadController {

    @Autowired
    private CloudinaryService cloudinaryService;

    // ===== Trang Upload (giữ nguyên nếu bạn có trang riêng) =====
    @GetMapping
    public String showUploadPage() {
        return "upload/index";
    }

    // ===== REST API cho AJAX Upload =====

    @PostMapping("/song")
    @ResponseBody
    public ResponseEntity<?> uploadSong(@RequestParam("file") MultipartFile file) {
        try {
            if (file.isEmpty()) {
                return ResponseEntity.badRequest().body(new ErrorResponse("Please select a file to upload"));
            }

            UploadResponse response = cloudinaryService.uploadSong(file);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }

    @PostMapping("/cover")
    @ResponseBody
    public ResponseEntity<?> uploadCover(@RequestParam("file") MultipartFile file) {
        try {
            if (file.isEmpty()) {
                return ResponseEntity.badRequest().body(new ErrorResponse("Please select a file to upload"));
            }

            UploadResponse response = cloudinaryService.uploadCover(file);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }

    // Inner class cho error response
    private static class ErrorResponse {
        private String message;

        public ErrorResponse(String message) {
            this.message = message;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }
    }
}