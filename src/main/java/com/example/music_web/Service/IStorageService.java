package com.example.music_web.Service;

import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;

public interface IStorageService {
    // Hàm nhận file và trả về đường dẫn URL (String)
    String uploadFile(MultipartFile file) throws IOException;
}