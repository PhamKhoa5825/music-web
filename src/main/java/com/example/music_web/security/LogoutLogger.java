package com.example.music_web.security;

import com.example.music_web.Entity.User;
import com.example.music_web.service.SystemLogService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class LogoutLogger implements LogoutSuccessHandler {

    private final SystemLogService logService;

    @Override
    public void onLogoutSuccess(HttpServletRequest request,
                                HttpServletResponse response,
                                Authentication authentication) throws IOException {

        // Kiểm tra xem người dùng có đăng nhập không (đề phòng null)
        if (authentication != null && authentication.getPrincipal() instanceof User) {
            User user = (User) authentication.getPrincipal();
            logService.log(user, "LOGOUT", "User logged out manually");
        }

        // Sau khi ghi log xong thì chuyển hướng về trang chủ
        response.sendRedirect("/?logout");
    }
}