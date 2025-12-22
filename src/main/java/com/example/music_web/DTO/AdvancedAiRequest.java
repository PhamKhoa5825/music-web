package com.example.music_web.DTO;

import lombok.Data;

import java.util.List;

@Data
public class AdvancedAiRequest {
    private String description; // Mô tả của người dùng
    private List<String> moods; // Danh sách tâm trạng
    private List<String> genres; // Thể loại yêu thích
    private List<String> artists; // Nghệ sĩ yêu thích
    private String activity; // Hoạt động hiện tại
    private String timeOfDay; // Thời gian trong ngày
    private Integer duration; // Thời lượng mong muốn (phút)
    private Integer songCount; // Số lượng bài hát muốn
    private Double minRating; // Rating tối thiểu
    private Integer minYear; // Năm phát hành tối thiểu
    private Integer maxYear;
    private Boolean excludeListened; // Loại trừ bài đã nghe
    private Boolean onlyLikedArtists; // Chỉ nghệ sĩ đã thích
    private String language;
    private Boolean excludeExplicit; // Loại trừ bài explicit

    // Thêm các tham số âm nhạc
    private Integer minBpm;
    private Integer maxBpm;
    private Integer minEnergy;
    private Integer minDanceability;

    public AdvancedAiRequest() {
        this.duration = 60;
        this.songCount = 10;
        this.minRating = 3.5;
        this.excludeExplicit = false;
        this.excludeListened = false;
    }
}