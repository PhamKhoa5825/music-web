package com.example.music_web.repository;

import com.example.music_web.Entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    // Dùng cho Form Login (Username)
    Optional<User> findByUsername(String username);
    boolean existsByUsername(String username);

    // Dùng cho Google Login (Email) -> THÊM DÒNG NÀY
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);

}
