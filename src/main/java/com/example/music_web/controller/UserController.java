package com.example.music_web.controller;

import com.example.music_web.Entity.User;
import com.example.music_web.Entity.UserPreference;
import com.example.music_web.dto.UserPreferenceDTO;
import com.example.music_web.repository.UserPreferenceRepository;
import com.example.music_web.service.SystemLogService;
import com.example.music_web.service.UserPreferenceService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
@RequiredArgsConstructor
public class UserController {

    private final UserPreferenceService preferenceService;
    private final UserPreferenceRepository preferenceRepository;
    private final SystemLogService logService;

    @GetMapping("/profile")
    public String showProfile(@AuthenticationPrincipal User user, Model model) {

        // --- THÊM DÒNG NÀY ĐỂ DEBUG ---
        System.out.println("DEBUG CHECK - Username: " + user.getUsername());
        System.out.println("DEBUG CHECK - Role trong DB: " + user.getRole());
        System.out.println("DEBUG CHECK - Quyền thực tế (Authorities): " + user.getAuthorities());
        // -----------------------------
        // Lấy thông tin preference hiện tại để hiển thị lên form
        UserPreference pref = preferenceRepository.findByUser_UserId(user.getUserId())
                .orElse(new UserPreference());

        UserPreferenceDTO dto = new UserPreferenceDTO();
        dto.setListeningPattern(pref.getListeningPattern());
        // (Bạn có thể map thêm genres/artists vào dto nếu muốn hiển thị)

        model.addAttribute("username", user.getUsername());
        model.addAttribute("role", user.getRole());
        model.addAttribute("preference", dto);

        return "profile"; // Trỏ đến profile.html
    }

    @PostMapping("/profile/update")
    public String updateProfile(@AuthenticationPrincipal User user,
                                @ModelAttribute UserPreferenceDTO dto) {
        preferenceService.updateUserPreference(user, dto);

        logService.log(user, "UPDATE_PREF", "User updated music preferences");
        return "redirect:/profile?updated";
    }
}