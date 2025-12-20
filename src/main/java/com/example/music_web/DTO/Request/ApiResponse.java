package com.example.music_web.DTO.Request;


import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
public class ApiResponse <T> {
    private int code = 1000;
    private String message;
    private T result;
}
