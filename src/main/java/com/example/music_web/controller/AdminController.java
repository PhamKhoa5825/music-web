package com.example.music_web.controller;

import com.example.music_web.Entity.User;
import com.example.music_web.repository.SystemLogRepository;
import com.example.music_web.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {

    private final UserService userService;
    private final SystemLogRepository logRepository; //

    @GetMapping("/dashboard")
    public String showDashboard(Model model) {
        model.addAttribute("users", userService.getAllUsers());
        model.addAttribute("logs", logRepository.findAllByOrderByTimeDesc());
        return "admin/dashboard";
    }

    // Xử lý Khóa/Mở khóa
    @PostMapping("/user/toggle-lock/{id}")
    public String toggleUserLock(@PathVariable Long id, @AuthenticationPrincipal User admin) {
        userService.toggleLock(id, admin);
        return "redirect:/admin/dashboard?message=StatusChanged";
    }

    // Xử lý Xóa user
    @PostMapping("/user/delete/{id}")
    public String deleteUser(@PathVariable Long id, @AuthenticationPrincipal User admin) {
        userService.deleteUser(id, admin);
        return "redirect:/admin/dashboard?message=UserDeleted";
    }


}