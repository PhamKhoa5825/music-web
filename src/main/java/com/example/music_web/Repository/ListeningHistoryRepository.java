package com.example.music_web.Repository;

import com.example.music_web.Entity.ListeningHistory;
import com.example.music_web.Entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ListeningHistoryRepository extends JpaRepository<ListeningHistory, Long> {
    List<ListeningHistory> findByUserOrderByListenedAtDesc(User user);
}
