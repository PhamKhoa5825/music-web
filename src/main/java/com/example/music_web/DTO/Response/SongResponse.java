package com.example.music_web.DTO.Response;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class SongResponse {
    private Long songId;
    private String title;
    private String lyric;
    private String musicUrl;     // Map từ song.filePath (Link nhạc từ Cloudinary)
    private String imageUrl;    // Map từ song.coverImage
    // --- 3. Thông tin Nghệ sĩ (Flatten từ quan hệ ManyToOne) ---
    // Frontend cần ID để click vào xem profile nghệ sĩ, cần Name để hiển thị
    private Long artistId;
    private String artistName;
    private String coverImage;

    // --- 4. Thông tin Album (Flatten từ quan hệ ManyToOne) ---
    // Album có thể null (bài hát lẻ), nên cần xử lý null khi map
    private Long albumId;
    private String albumTitle;

    // --- 5. Thông tin Thể loại (Flatten từ quan hệ ManyToMany) ---
    // Trả về danh sách tên thể loại để hiển thị tags (VD: "Pop", "Ballad")
    private List<GenreResponse> genres;

    // --- 6. Thống kê & Metadata (Phục vụ hiển thị & Sort) ---
    private Integer views;          // Map từ song.views
    private LocalDateTime uploadDate; // Map từ song.uploadDate

    // --- 7. Dữ liệu đánh giá (Từ yêu cầu AI/Rating) ---
    private Double averageRating;   // Map từ song.averageRating
    private Integer totalRatings;   // Để hiển thị số lượng đánh giá (VD: 4.5 sao / 100 lượt)
    private String audioFeatures; // JSON/TEXT

}
