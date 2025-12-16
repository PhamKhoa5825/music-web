package com.example.music_web.Service;

import com.example.music_web.Service.IStorageService;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service // Đánh dấu đây là Bean để Spring quản lý
public class LocalStorageService implements IStorageService {

    private final Path rootLocation = Paths.get("uploads"); // Folder lưu ảnh

    public LocalStorageService() {
        try {
            Files.createDirectories(rootLocation); // Tự tạo folder nếu chưa có
        } catch (IOException e) {
            throw new RuntimeException("Không thể khởi tạo thư mục lưu trữ!");
        }
    }

    @Override
    public String uploadFile(MultipartFile file) throws IOException {
        if (file.isEmpty()) {
            throw new RuntimeException("File rỗng");
        }

        // Tạo tên file ngẫu nhiên để tránh trùng
        String fileName = UUID.randomUUID().toString() + "_" + file.getOriginalFilename();
        Path destinationFile = this.rootLocation.resolve(fileName).normalize().toAbsolutePath();

        // Lưu file vào ổ cứng
        try (var inputStream = file.getInputStream()) {
            Files.copy(inputStream, destinationFile, StandardCopyOption.REPLACE_EXISTING);
        }

        // Trả về đường dẫn để lưu vào Database (Ví dụ: /images/ten-file.jpg)
        // Lưu ý: Cần cấu hình WebMvcConfig để map đường dẫn này ra folder thật
        return "/uploads/" + fileName;
    }
}