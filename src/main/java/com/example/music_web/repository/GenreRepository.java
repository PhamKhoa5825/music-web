package com.example.music_web.repository;

import com.example.music_web.Entity.Genre;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GenreRepository extends JpaRepository<Genre, Long> {
    // JpaRepository đã có sẵn các hàm findAllById, findById, save, delete...
    // Bạn không cần viết thêm gì trừ khi muốn tìm theo tên cụ thể
}
