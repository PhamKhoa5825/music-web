package com.example.music_web.security;

import com.example.music_web.Entity.User;
import com.example.music_web.service.SystemLogService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class LoginSuccessHandler implements AuthenticationSuccessHandler {

    private final SystemLogService logService;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        // 1. Lấy thông tin user vừa đăng nhập
        User user = (User) authentication.getPrincipal();

        // 2. Ghi Log
        logService.log(user, "LOGIN", "User logged in successfully");

        // 3. Chuyển hướng người dùng (giống logic cũ)
        response.sendRedirect("/");
    }
}
