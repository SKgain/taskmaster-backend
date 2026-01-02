package com.dhrubok.taskmaster.persistence.features.chat.repositories;

import com.dhrubok.taskmaster.persistence.features.chat.entities.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, String> {
    /**
     * Find all messages for a project, ordered by timestamp
     */
    List<ChatMessage> findByProjectIdOrderByTimestampAsc(String projectId);

    /**
     * Delete all messages for a project
     */
    void deleteByProjectId(String projectId);
}
