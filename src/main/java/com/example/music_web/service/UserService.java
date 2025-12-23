package com.example.music_web.service;

import com.example.music_web.Entity.User;
import com.example.music_web.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final SystemLogService logService; //

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    // Chức năng: Khóa / Mở khóa tài khoản
    public void toggleLock(Long userId, User adminUser) {
        User targetUser = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Đảo ngược trạng thái: Đang khóa -> Mở, Đang mở -> Khóa
        boolean newStatus = !targetUser.isLocked();
        targetUser.setLocked(newStatus);
        userRepository.save(targetUser);

        // Ghi log hành động của Admin
        String action = newStatus ? "LOCK_USER" : "UNLOCK_USER";
        logService.log(adminUser, action, "Admin changed status for user: " + targetUser.getUsername());
    }

    // Chức năng: Xóa tài khoản
    public void deleteUser(Long userId, User adminUser) {
        User targetUser = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        String deletedUsername = targetUser.getUsername();
        userRepository.delete(targetUser);

        logService.log(adminUser, "DELETE_USER", "Admin deleted user: " + deletedUsername);
    }

    private final PasswordEncoder passwordEncoder;

    // 1. Chức năng Đổi mật khẩu
    public void changePassword(User user, String oldPassword, String newPassword) {
        // Kiểm tra mật khẩu cũ có đúng không
        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new RuntimeException("Mật khẩu cũ không chính xác!");
        }
        // Mã hóa mật khẩu mới và lưu
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        // Ghi log
        logService.log(user, "CHANGE_PASS", "User changed password");
    }

    // 2. Chức năng Upload Avatar
    public void updateAvatar(User user, MultipartFile file) throws IOException {
        if (file.isEmpty()) return;

        // Đường dẫn thư mục lưu ảnh (Lưu trong dự án hoặc thư mục ngoài)
        // Để đơn giản, ta lưu vào thư mục 'uploads' ở gốc dự án
        String uploadDir = "uploads/avatars/";
        Path uploadPath = Paths.get(uploadDir);

        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        // Tạo tên file duy nhất để tránh trùng (VD: user_1_avatar.jpg)
        String fileName = "user_" + user.getUserId() + "_" + Objects.requireNonNull(file.getOriginalFilename());
        Path filePath = uploadPath.resolve(fileName);

        // Lưu file
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

        // Cập nhật đường dẫn vào DB
        user.setAvatar(fileName);
        userRepository.save(user);

        logService.log(user, "UPDATE_AVATAR", "User updated profile picture");
    }

    
}