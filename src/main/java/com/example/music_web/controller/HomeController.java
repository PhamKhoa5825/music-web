package com.example.music_web.controller;

import com.example.music_web.Entity.User;
import com.example.music_web.enums.Role;
import com.example.music_web.repository.UserRepository;
import com.example.music_web.service.GenreService;
import com.example.music_web.service.SongService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequiredArgsConstructor
public class HomeController {

    private final SongService songService;
    private final UserRepository userRepository; // <-- Inject thêm cái này để tìm User

    @GetMapping("/")
    public String home(Model model, Authentication authentication) {
        // 1. Load nhạc (Giữ nguyên logic cũ của bạn)
        //model.addAttribute("topCharts", songService.getTopCharts());
        // model.addAttribute("recommendations", ...); // Nếu bạn có hàm gợi ý

        // 2. Xử lý thông tin người dùng (SỬA ĐOẠN NÀY)
        if (authentication != null && authentication.isAuthenticated()) {
            model.addAttribute("isLoggedIn", true);

            User user = null;
            Object principal = authentication.getPrincipal();

            // Case A: Đăng nhập bằng Google
            if (principal instanceof OAuth2User oauth2User) {
                String email = oauth2User.getAttribute("email");
                user = userRepository.findByEmail(email).orElse(null);
            }
            // Case B: Đăng nhập thường
            else if (principal instanceof User userDetails) {
                user = userDetails;
            }

            if (user != null) {
                // Logic chọn tên hiển thị: FullName > Username/Email
                String displayName = (user.getFullName() != null && !user.getFullName().isEmpty())
                        ? user.getFullName()
                        : user.getUsername();

                model.addAttribute("username", displayName); // Gửi tên đẹp sang View

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
                model.addAttribute("avatarUrl", avatarUrl); // Gửi avatar sang View



                // Kiểm tra quyền Admin
                model.addAttribute("isAdmin", user.getRole() == Role.ADMIN);
            }
        } else {
            model.addAttribute("isLoggedIn", false);
        }
        model.addAttribute("activeTab", "home");
        return "index";




    }
    @GetMapping("/for-you")
    public String forYouPage(Model model) {
        model.addAttribute("activeTab", "for-you");
        return "for-you";
    }

    @Autowired
    private GenreService genreService;

    @GetMapping("/gemini")
    public String aiDjPage(Model model) {
        // activeTab dùng để highlight menu bên trái (nếu layout hỗ trợ)
        model.addAttribute("activeTab", "ai-dj");
        // Lấy danh sách thể loại để hiển thị trong select box
        model.addAttribute("genres", genreService.getAllGenres(null, PageRequest.of(0, 100, Sort.by("name"))).getContent());
        return "ai-dj";
    }

    @GetMapping("/history")
    public String historyPage(Model model, Authentication authentication) { // Thêm Authentication
        model.addAttribute("activeTab", "history");

        // Lấy ID người dùng đang đăng nhập
        if (authentication != null && authentication.getPrincipal() instanceof User) {
            User user = (User) authentication.getPrincipal();
            model.addAttribute("currentUserId", user.getUserId()); // Gửi ID sang HTML
        } else if (authentication != null && authentication.getPrincipal() instanceof org.springframework.security.oauth2.core.user.OAuth2User) {
            // Logic lấy ID cho Google User (như bạn đã làm ở các bước trước)
            String email = ((org.springframework.security.oauth2.core.user.OAuth2User) authentication.getPrincipal()).getAttribute("email");
            User user = userRepository.findByEmail(email).orElse(null);
            if(user != null) model.addAttribute("currentUserId", user.getUserId());
        }
        return "history";
    }


    // Trong hàm xử lý GET /favorites
    @GetMapping("/favorites")
    public String favoritesPage(Model model, Authentication auth) {
        User user = null;
        if (auth.getPrincipal() instanceof User) {
            user = (User) auth.getPrincipal();
        } else if (auth.getPrincipal() instanceof OAuth2User) {
            String email = ((OAuth2User) auth.getPrincipal()).getAttribute("email");
            user = userRepository.findByEmail(email).orElse(null);
        }

        if (user != null) {
            model.addAttribute("currentUserId", user.getUserId());
            // Lấy FullName nếu có, không thì lấy Username
            String displayTitle = (user.getFullName() != null && !user.getFullName().isEmpty())
                    ? user.getFullName() : user.getUsername();
            model.addAttribute("username", displayTitle);
        }
        return "favorites";
    }

}