package com.example.music_web.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final UserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        // 1. Lấy Header Authorization từ request
        final String authHeader = request.getHeader("Authorization");
        final String jwt;
        final String username;

        // 2. Kiểm tra xem Header có hợp lệ không (phải bắt đầu bằng "Bearer ")
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response); // Nếu không có token, cứ cho đi tiếp (để SecurityConfig chặn sau)
            return;
        }

        // 3. Lấy token ra (bỏ chữ "Bearer " ở đầu)
        jwt = authHeader.substring(7);

        // 4. Trích xuất username từ token
        try {
            username = jwtUtil.extractUsername(jwt);
        } catch (Exception e) {
            // Token lỗi hoặc hết hạn -> không làm gì, cho filter chạy tiếp
            filterChain.doFilter(request, response);
            return;
        }

        // 5. Nếu có username và chưa được xác thực trong Context hiện tại
        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {

            // Lấy thông tin user từ DB
            UserDetails userDetails = this.userDetailsService.loadUserByUsername(username);

            // 6. Kiểm tra tính hợp lệ của token so với user lấy từ DB
            if (jwtUtil.validateToken(jwt, userDetails.getUsername())) {

                // Tạo object xác thực
                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        userDetails,
                        null,
                        userDetails.getAuthorities()
                );

                // Gắn thêm thông tin request (IP, session ID...)
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                // 7. CẬP NHẬT CONTEXT: Báo cho Spring Security biết user này đã đăng nhập thành công
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        }

        // 8. Cho phép request đi tiếp đến các filter sau hoặc controller
        filterChain.doFilter(request, response);
    }
}