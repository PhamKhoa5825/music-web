package com.example.music_web.security;

import com.example.music_web.Entity.User;
import com.example.music_web.enums.Role;
import com.example.music_web.repository.UserRepository;
import com.example.music_web.service.SystemLogService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class LoginSuccessHandler implements AuthenticationSuccessHandler {

    private final SystemLogService logService;
    private final UserRepository userRepository; // Cần Repo để tìm user

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {

        User user = null;
        Object principal = authentication.getPrincipal();

        // 1. Nếu đăng nhập bằng Username/Pass
        if (principal instanceof User) {
            user = (User) principal;
        }
        // 2. Nếu đăng nhập bằng Google
        else if (principal instanceof OAuth2User) {
            String email = ((OAuth2User) principal).getAttribute("email");
            // Tìm user trong DB dựa trên email Google trả về
            user = userRepository.findByEmail(email).orElse(null);
        }

        // Ghi log và Redirect
        if (user != null) {
            logService.log(user, "LOGIN", "Logged in via " + (principal instanceof OAuth2User ? "Google" : "Form"));

            // Nếu là Admin -> vào trang Admin, User -> vào trang chủ
            if (user.getRole() == Role.ADMIN) {
                response.sendRedirect("/");
            } else {
                response.sendRedirect("/");
            }
        } else {
            response.sendRedirect("/");
        }
    }
}
