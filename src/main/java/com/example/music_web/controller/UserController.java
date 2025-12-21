package com.example.music_web.controller;

import com.example.music_web.Entity.User;
import com.example.music_web.Entity.UserPreference;
import com.example.music_web.dto.UserPreferenceDTO;
import com.example.music_web.repository.UserPreferenceRepository;
import com.example.music_web.service.SystemLogService;
import com.example.music_web.service.UserPreferenceService;
import com.example.music_web.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

@Controller
@RequiredArgsConstructor
public class UserController {

    private final UserPreferenceService preferenceService;
    private final UserPreferenceRepository preferenceRepository;
    private final SystemLogService logService;



    // Sửa lại hàm showProfile để truyền thêm thông tin avatar
    @GetMapping("/profile")
    public String showProfile(@AuthenticationPrincipal User user, Model model) {
        // ... code cũ lấy preferences ...

        // Thêm avatar vào model (Nếu null thì dùng ảnh mặc định)
        String avatarUrl = (user.getAvatar() == null || user.getAvatar().isEmpty())
                ? "/images/default-avatar.png" // Bạn nhớ tạo ảnh này trong static/images
                : "/uploads/avatars/" + user.getAvatar();

        model.addAttribute("avatarUrl", avatarUrl);
        model.addAttribute("username", user.getUsername());

        // Lấy thông tin preference hiện tại để hiển thị lên form
        UserPreference pref = preferenceRepository.findByUser_UserId(user.getUserId())
                .orElse(new UserPreference());

        UserPreferenceDTO dto = new UserPreferenceDTO();
        dto.setListeningPattern(pref.getListeningPattern());
        // (Bạn có thể map thêm genres/artists vào dto nếu muốn hiển thị)

        model.addAttribute("username", user.getUsername());
        model.addAttribute("role", user.getRole());
        model.addAttribute("preference", dto);

        return "profile";
    }

    @PostMapping("/profile/update")
    public String updateProfile(@AuthenticationPrincipal User user,
                                @ModelAttribute UserPreferenceDTO dto) {
        preferenceService.updateUserPreference(user, dto);

        logService.log(user, "UPDATE_PREF", "User updated music preferences");
        return "redirect:/profile?updated";
    }


    private final UserService userService; // Inject thêm UserService
    // ... các service cũ (preferenceService, logService...)

    // API Đổi mật khẩu
    @PostMapping("/profile/change-password")
    public String changePassword(@AuthenticationPrincipal User user,
                                 @RequestParam("oldPassword") String oldPassword,
                                 @RequestParam("newPassword") String newPassword,
                                 Model model) {
        try {
            userService.changePassword(user, oldPassword, newPassword);
            return "redirect:/profile?success=PasswordChanged";
        } catch (RuntimeException e) {
            return "redirect:/profile?error=" + e.getMessage();
        }
    }

    // API Upload Avatar
    @PostMapping("/profile/upload-avatar")
    public String uploadAvatar(@AuthenticationPrincipal User user,
                               @RequestParam("avatarFile") MultipartFile file) {
        try {
            userService.updateAvatar(user, file);
            return "redirect:/profile?success=AvatarUpdated";
        } catch (Exception e) {
            return "redirect:/profile?error=UploadFailed";
        }
    }


}