package com.example.music_web.Entity;

import com.example.music_web.enums.Provider;
import com.example.music_web.enums.Role;
import jakarta.persistence.*;
import lombok.*; // Khuyên dùng Lombok cho gọn
import org.hibernate.annotations.CreationTimestamp;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import jakarta.persistence.Column;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

@Entity
@Table(name = "users")
@Data // Lombok: Getter, Setter, ToString...
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User implements UserDetails { // Implements UserDetails là bắt buộc cho Spring Security

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long userId;

    // --- THÊM MỚI ---
    @Column(unique = true)
    private String email; // Dùng để định danh khi login bằng Google

    private String fullName; // Tên hiển thị (VD: Nguyen Van A)

    @Enumerated(EnumType.STRING)
    private Provider provider;
    @Column(unique = true)
    private String username;

    @Column(nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    @Column(columnDefinition = "boolean default false")
    private boolean locked = false;


    @Column
    private String avatar;

    @CreationTimestamp
    private LocalDateTime createdAt;

    // --- CẬP NHẬT LOGIC SPRING SECURITY ---

    // Spring Security mặc định hỏi "Username là gì?".
    // Chúng ta trả về EMAIL để nó dùng Email xác thực.
    @Override
    public String getUsername() {
        return username;
    }

    // Các hàm khác giữ nguyên
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + role.name()));
    }
    @Override
    public boolean isAccountNonExpired() { return true; }
    @Override
    public boolean isAccountNonLocked() { return !locked; }
    @Override
    public boolean isCredentialsNonExpired() { return true; }
    @Override
    public boolean isEnabled() { return true; }
}