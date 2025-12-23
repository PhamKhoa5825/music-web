package com.example.music_web.service;

import com.example.music_web.Entity.SystemLog;
import com.example.music_web.Entity.User;
import com.example.music_web.repository.SystemLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class SystemLogService {
    private final SystemLogRepository logRepository;

    public void log(User user, String action, String description) {
        SystemLog log = new SystemLog();
        log.setUser(user);
        log.setAction(action);
        log.setDescription(description);
        log.setTime(LocalDateTime.now()); // Đảm bảo set thời gian
        logRepository.save(log);
    }
}