package com.example.music_web.dto;

import com.example.music_web.enums.Role;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

public class AuthDTO {
    @Data
    public static class RegisterRequest {
        private String username;
        private String password;
        private Role role; // Optional, default USER
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

