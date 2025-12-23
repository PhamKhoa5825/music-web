package com.example.music_web.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL) // Bỏ qua các trường null khi trả về JSON
public class ApiResponse<T> {
    private int code;      // Ví dụ: 1000 (Thành công), 1001 (Lỗi)
    private String message; // Thông báo
    private T result;       // Dữ liệu trả về (User, Token, List...)
}