package com.example.music_web.Entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long userId;

    @Column(nullable = false, unique = true, length = 50)
    private String username;

    @Column(nullable = false)
    private String password; // Nên được hash

    @Enumerated(EnumType.STRING) // Sử dụng Enum để quản lý Role
    @Column(nullable = false)
    private Role role; // USER, ADMIN

    @CreationTimestamp
    private LocalDateTime createdAt; // Dùng CreationTimestamp từ Hibernate
}

