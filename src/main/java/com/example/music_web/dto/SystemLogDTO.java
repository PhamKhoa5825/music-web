package com.example.music_web.dto;

import lombok.Data;

@Data
public class SystemLogDTO {
    private String action;
    private String description;
    private String username;
    private String time;
}
