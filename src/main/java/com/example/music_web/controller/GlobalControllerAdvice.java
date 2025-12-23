package com.example.music_web.controller;

import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
public class GlobalControllerAdvice {

    @ModelAttribute
    public void addGlobalAttributes(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        // Kiểm tra xem user có thực sự đăng nhập không (khác null và không phải user ẩn danh)
        boolean isLoggedIn = auth != null
                && auth.isAuthenticated()
                && !(auth instanceof AnonymousAuthenticationToken);

        // Luôn luôn thêm biến này vào Model để Thymeleaf không bị lỗi null
        model.addAttribute("isLoggedIn", isLoggedIn);

        if (isLoggedIn) {
            model.addAttribute("username", auth.getName());

            // Thêm check Admin nếu layout cần
            boolean isAdmin = auth.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
            model.addAttribute("isAdmin", isAdmin);
        }
    }
}