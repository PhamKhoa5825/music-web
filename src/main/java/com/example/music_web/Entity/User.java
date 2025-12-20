package com.example.music_web.Entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "users")
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

