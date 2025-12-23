

package com.example.music_web.config;

import com.example.music_web.repository.UserRepository;
import com.example.music_web.security.CustomOAuth2UserService;
import com.example.music_web.security.LoginSuccessHandler;
import com.example.music_web.security.LogoutLogger;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {


    private final UserRepository userRepository;
    private final LoginSuccessHandler loginSuccessHandler; // 1. Tiêm Handler
    private final LogoutLogger logoutLogger;
    private final CustomOAuth2UserService customOAuth2UserService;

    @Bean
    public UserDetailsService userDetailsService() {
        return username -> userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
    }

    @Bean
    public AuthenticationProvider authenticationProvider(
            UserDetailsService userDetailsService,
            PasswordEncoder passwordEncoder) {

        DaoAuthenticationProvider authProvider =
                new DaoAuthenticationProvider(userDetailsService);

        authProvider.setPasswordEncoder(passwordEncoder);

        return authProvider;
    }

    // --- THÊM PHẦN NÀY ĐỂ SỬA LỖI STARTUP (Nếu có file cũ cần dùng) ---
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        // 1. CHO PHÉP TỰ DO (Public Access)
                        .requestMatchers("/", "/home", "/index", "/songs/**").permitAll() // <-- Trang chủ ai cũng xem được
                        .requestMatchers("/register", "/login", "/css/**", "/js/**", "/images/**").permitAll()

                        // 2. CÁC TRANG CẦN ĐĂNG NHẬP
                        .requestMatchers("/profile/**").authenticated() // Vào profile bắt buộc login
                        .requestMatchers("/favorites/**").authenticated() // Thêm bài yêu thích phải login

                        // 3. ADMIN
                        .requestMatchers("/admin/**").hasRole("ADMIN")

                        // 4. CÒN LẠI
                        .anyRequest().authenticated()
                )
                // 1. CẤU HÌNH ĐĂNG NHẬP THƯỜNG (Username)
                .formLogin(form -> form
                        .loginPage("/login")
                        .successHandler(loginSuccessHandler) // Dùng Handler chung
                        .failureUrl("/login?error")
                        .permitAll()
                )
                // 2. CẤU HÌNH ĐĂNG NHẬP GOOGLE
                .oauth2Login(oauth2 -> oauth2
                        .loginPage("/login")
                        .userInfoEndpoint(userInfo -> userInfo
                                .userService(customOAuth2UserService) // Logic lưu user Google
                        )
                        .successHandler(loginSuccessHandler) // Dùng chung Handler để ghi log/redirect
                )
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        // 2. THAY ĐỔI DÒNG NÀY: Dùng handler của mình
                        .logoutSuccessHandler(logoutLogger)
                        .permitAll()
                );

        return http.build();
    }
}