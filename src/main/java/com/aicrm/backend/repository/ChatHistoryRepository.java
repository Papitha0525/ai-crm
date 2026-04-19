package com.aicrm.backend.repository;

import com.aicrm.backend.model.ChatHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ChatHistoryRepository extends JpaRepository<ChatHistory, Long> {
    List<ChatHistory> findByUserId(Long userId);
    List<ChatHistory> findBySessionId(String sessionId);
}