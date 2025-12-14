package com.example.music_web.config;

import com.example.music_web.Entity.User;
import com.example.music_web.enums.Role;
import com.example.music_web.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        // Kiểm tra: Nếu chưa có tài khoản tên "admin" thì mới tạo
        if (!userRepository.existsByUsername("admin")) {
            User admin = User.builder()
                    .username("admin")
                    .password(passwordEncoder.encode("123456")) // Mật khẩu mặc định
                    .role(Role.ADMIN)
                    .build();

            userRepository.save(admin);
            System.out.println("---------------------------------------------");
            System.out.println("ADMIN ACCOUNT CREATED: Username: admin | Password: 123456");
            System.out.println("---------------------------------------------");
        }
    }
}