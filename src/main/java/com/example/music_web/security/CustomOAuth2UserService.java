package com.example.music_web.security;

import com.example.music_web.Entity.User;
import com.example.music_web.enums.Provider;
import com.example.music_web.enums.Role;
import com.example.music_web.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error; // <-- Nhớ import cái này
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        // 1. Lấy user từ Google về
        OAuth2User oAuth2User = super.loadUser(userRequest);

        String email = oAuth2User.getAttribute("email");
        String name = oAuth2User.getAttribute("name");
        String avatar = oAuth2User.getAttribute("picture");

        User user = userRepository.findByEmail(email).orElse(null);

        if (user != null) {
            // == TRƯỜNG HỢP: NGƯỜI DÙNG ĐÃ TỒN TẠI ==

            // --- [QUAN TRỌNG] KIỂM TRA KHÓA ---
            if (user.isLocked()) {
                // Nếu bị khóa, ném lỗi ngay lập tức để Spring Security chặn lại
                // Mã lỗi "account_locked" này sẽ được hứng ở SecurityConfig
                throw new OAuth2AuthenticationException(new OAuth2Error("account_locked"), "Tài khoản của bạn đã bị khóa.");
            }
            // ----------------------------------

            // Nếu không bị khóa thì cập nhật thông tin mới nhất từ Google
            user.setFullName(name);
            user.setAvatar(avatar);
            user.setProvider(Provider.GOOGLE);
            userRepository.save(user);

        } else {
            // == TRƯỜNG HỢP: NGƯỜI DÙNG MỚI (CHƯA CÓ TRONG DB) ==
            user = new User();
            user.setEmail(email);
            user.setFullName(name);
            user.setAvatar(avatar);
            user.setRole(Role.USER);
            user.setProvider(Provider.GOOGLE);
            user.setPassword(UUID.randomUUID().toString()); // Password ngẫu nhiên
            user.setUsername(email); // Hoặc logic tạo username riêng

            userRepository.save(user);
        }

        return oAuth2User;
    }
}