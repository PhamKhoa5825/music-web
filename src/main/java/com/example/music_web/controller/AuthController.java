package com.example.music_web.controller;

import com.example.music_web.Entity.User;
import com.example.music_web.dto.AuthDTO;
import com.example.music_web.enums.Provider;
import com.example.music_web.enums.Role;
import com.example.music_web.repository.UserRepository;
import com.example.music_web.service.SystemLogService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

@Controller // Trả về View (HTML)
@RequiredArgsConstructor
public class AuthController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final SystemLogService logService;

    // Hiển thị form đăng nhập
    @GetMapping("/login")
    public String showLoginForm() {
        return "login"; // Trỏ đến file login.html
    }

    // Hiển thị form đăng ký
    @GetMapping("/register")
    public String showRegisterForm(Model model) {
        model.addAttribute("user", new AuthDTO.RegisterRequest());
        return "register"; // Trỏ đến file register.html
    }

    // Xử lý đăng ký
    @PostMapping("/register")
    public String registerUser(@Valid @ModelAttribute("user") AuthDTO.RegisterRequest request,
                               BindingResult bindingResult, // Chứa kết quả lỗi nếu có
                               Model model) {
        if (bindingResult.hasErrors()) {
            return "register"; // Trả về trang register kèm lỗi hiển thị
        }
        if (userRepository.existsByUsername(request.getUsername())) {
            model.addAttribute("error", "Username đã tồn tại!");
            return "register";
        }

        // SỬA ĐOẠN NÀY:
        User user = new User();
        user.setUsername(request.getUsername());
        //user.setEmail(request.getEmail()); // Nếu bạn có dùng email
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(Role.USER);
        user.setProvider(Provider.LOCAL);

        // --- LOGIC MỚI: FullName mặc định bằng Username ---
        user.setFullName(request.getUsername());

        userRepository.save(user);
        logService.log(user, "REGISTER", "New account registered: " + user.getUsername());
        return "redirect:/login?success"; // Chuyển về trang login
    }
}