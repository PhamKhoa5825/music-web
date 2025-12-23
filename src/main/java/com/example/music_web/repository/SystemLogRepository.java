package com.example.music_web.repository;

import com.example.music_web.Entity.SystemLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SystemLogRepository extends JpaRepository<SystemLog, Long> {
    List<SystemLog> findByUser_UserIdOrderByTimeDesc(Long userId);
    List<SystemLog> findAllByOrderByTimeDesc();
}
