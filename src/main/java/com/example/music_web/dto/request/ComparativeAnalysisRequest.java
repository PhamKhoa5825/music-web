package com.example.music_web.dto.request;

import lombok.Data;

import java.util.List;

@Data
public class ComparativeAnalysisRequest {
    private Long songId1;
    private Long songId2;
    private List<String> comparisonPoints; // ["lyrics", "melody", "production", "impact"]
    private String analysisPerspective; // "technical", "artistic", "cultural"
    private Integer detailLevel; // 1-5

    public ComparativeAnalysisRequest() {
        this.comparisonPoints = List.of("lyrics", "melody", "production");
        this.analysisPerspective = "technical";
        this.detailLevel = 3;
    }
}