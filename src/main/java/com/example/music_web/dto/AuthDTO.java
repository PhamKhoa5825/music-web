package com.example.music_web.dto;

import com.example.music_web.enums.Role;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

public class AuthDTO {
    @Data
    public static class RegisterRequest {
        @NotBlank(message = "Username không được để trống")
        @Size(min = 3, message = "Username phải có ít nhất 3 ký tự")
        private String username;

        @NotBlank(message = "Password không được để trống")
        @Size(min = 6, message = "Password phải có ít nhất 6 ký tự")
        private String password;

        private Role role;
    }

    @Data
    public static class LoginRequest {
        private String username;
        private String password;
    }

    @Data
    @AllArgsConstructor // <-- Thêm cái này (cần thư viện Lombok)
    @NoArgsConstructor
    public static class AuthResponse {
        private String token;
        private String username;
        private Role role;
    }
}

