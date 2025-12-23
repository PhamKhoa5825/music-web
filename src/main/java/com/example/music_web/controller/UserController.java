package com.example.music_web.controller;

import com.example.music_web.Entity.User;
import com.example.music_web.Entity.UserPreference;
import com.example.music_web.dto.UserPreferenceDTO;
import com.example.music_web.repository.UserPreferenceRepository;
import com.example.music_web.repository.UserRepository;
import com.example.music_web.service.SystemLogService;
import com.example.music_web.service.UserPreferenceService;
import com.example.music_web.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
public class UserController {

    private final UserPreferenceService preferenceService;
    private final UserPreferenceRepository preferenceRepository;
    private final SystemLogService logService;



    private final UserRepository userRepository; // Inject thêm repository

    @GetMapping("/profile")
    public String showProfile(Authentication authentication, Model model) {
        User user = null;

        // 1. Kiểm tra xem User đăng nhập từ nguồn nào
        Object principal = authentication.getPrincipal();

        if (principal instanceof User) {
            // Case 1: Đăng nhập thường (Form) -> Lấy trực tiếp
            user = (User) principal;
        } else if (principal instanceof OAuth2User) {
            // Case 2: Đăng nhập Google -> Lấy email từ Google rồi tìm trong DB
            OAuth2User oauth2User = (OAuth2User) principal;
            String email = oauth2User.getAttribute("email");
            user = userRepository.findByEmail(email).orElse(null);
        }

        // 2. Nếu vẫn không tìm thấy user (lỗi lạ) -> Đá về login
        if (user == null) {
            return "redirect:/login";
        }

        // 3. Logic hiển thị Profile (như cũ)
        // --- XỬ LÝ AVATAR ---
        String avatarUrl;
        if (user.getAvatar() != null && !user.getAvatar().isEmpty()) {
            // Nếu là link ảnh Google (bắt đầu bằng http) thì dùng luôn
            if (user.getAvatar().startsWith("http")) {
                avatarUrl = user.getAvatar();
            } else {
                // Nếu là ảnh upload local
                avatarUrl = "/uploads/avatars/" + user.getAvatar();
            }
        } else {
            avatarUrl = "/images/default-avatar.png";
        }

        model.addAttribute("avatarUrl", avatarUrl);
        model.addAttribute("username", user.getFullName() != null ? user.getFullName() : user.getUsername());
        model.addAttribute("role", user.getRole());
        model.addAttribute("email", user.getEmail()); // Hiển thị thêm email cho đẹp

        // --- XỬ LÝ PREFERENCE ---
        UserPreference pref = preferenceRepository.findByUser_UserId(user.getUserId())
                .orElse(new UserPreference());

        UserPreferenceDTO dto = new UserPreferenceDTO();
        dto.setListeningPattern(pref.getListeningPattern());
        model.addAttribute("preference", dto);

        model.addAttribute("provider", user.getProvider());
        System.out.println("========== DEBUG PROVIDER: " + user.getProvider() + " ==========");


        model.addAttribute("activeTab", "profile");

        return "profile";
    }

    /*@PostMapping("/profile/update")
    public String updateProfile(@AuthenticationPrincipal User user,
                                @ModelAttribute UserPreferenceDTO dto) {
        preferenceService.updateUserPreference(user, dto);

        logService.log(user, "UPDATE_PREF", "User updated music preferences");
        return "redirect:/profile?updated";
    }*/

    @PostMapping("/profile/update")
    public String updateProfile(
            @AuthenticationPrincipal Object principal, // Nhận Object để xử lý cả Google lẫn Local
            @RequestParam("fullName") String newFullName, // Nhận tên mới từ form
            RedirectAttributes redirectAttributes
    ) {
        User user = null;

        // 1. Lấy User hiện tại từ Database
        if (principal instanceof User) {
            user = (User) principal;
        } else if (principal instanceof OAuth2User) {
            String email = ((OAuth2User) principal).getAttribute("email");
            user = userRepository.findByEmail(email).orElse(null);
        }

        if (user != null) {
            // 2. Cập nhật tên mới
            user.setFullName(newFullName);
            userRepository.save(user); // Lưu vào DB

            // Cập nhật lại session (để hiển thị ngay trên thanh menu mà không cần login lại)
            // (Phần này hơi phức tạp, tạm thời lưu DB là được, F5 sẽ thấy đổi)

            redirectAttributes.addFlashAttribute("successMessage", "Cập nhật thông tin thành công!");
        }

        return "redirect:/profile";
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