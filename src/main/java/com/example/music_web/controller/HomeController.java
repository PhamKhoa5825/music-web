package com.example.music_web.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.security.core.Authentication;

@Controller
public class HomeController {

    @GetMapping("/")
    public String home(Model model, Authentication authentication) {
        // Kiểm tra xem người dùng đã đăng nhập chưa để đổi nút "Đăng nhập" thành Avatar/Tên
        if (authentication != null && authentication.isAuthenticated()) {
            model.addAttribute("isLoggedIn", true);
            model.addAttribute("username", authentication.getName());
            // Lấy role để nếu là Admin thì hiện nút Dashboard (xử lý ở view)
            model.addAttribute("isAdmin", authentication.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN")));
        } else {
            model.addAttribute("isLoggedIn", false);
        }
        return "index"; // Trả về file index.html
    }
}