package com.example.music_web.Entity;

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

    @Column(nullable = false, unique = true, length = 50)
    private String username;

    @Column(nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    @Column
    private String avatar;

    @CreationTimestamp
    private LocalDateTime createdAt;

    // --- THÊM MỚI: Trạng thái khóa tài khoản ---
    @Column(columnDefinition = "boolean default false")
    private boolean locked = false;

    // --- SỬA LẠI Logic Spring Security ---
    @Override
    public boolean isAccountNonLocked() {
        return !locked; // Nếu locked = false thì tài khoản không bị khóa (true)
    }

    // --- Logic của Spring Security ---
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + role.name()));
    }


    @Override
    public boolean isAccountNonExpired() { return true; }


    @Override
    public boolean isCredentialsNonExpired() { return true; }
    @Override
    public boolean isEnabled() { return true; }
}