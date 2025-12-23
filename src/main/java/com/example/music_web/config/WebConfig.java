package com.example.music_web.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**") // Áp dụng cho tất cả API
                .allowedOrigins("http://localhost:3000", "http://127.0.0.1:5500") // Cho phép frontend gọi vào
                .allowedMethods("GET", "POST", "PUT", "DELETE")
                .allowedHeaders("*")
                .allowCredentials(true);
    }

    // --- THÊM MỚI: Cấu hình Resource Handler cho ảnh upload ---
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Khi gọi đường dẫn http://localhost:8080/uploads/...
        // nó sẽ tìm file trong thư mục 'uploads' ở gốc dự án
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:uploads/");
    }
}