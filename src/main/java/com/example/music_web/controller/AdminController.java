package com.example.music_web.controller;

import com.example.music_web.repository.SystemLogRepository;
import com.example.music_web.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {

    private final UserRepository userRepository;
    private final SystemLogRepository logRepository;

    @GetMapping("/dashboard")
    public String showDashboard(Model model) {
        // 1. Lấy danh sách Users
        model.addAttribute("users", userRepository.findAll());

        // 2. Lấy danh sách Logs (Mới nhất lên đầu)
        model.addAttribute("logs", logRepository.findAllByOrderByTimeDesc());

        return "admin"; // Trả về file admin.html
    }
}