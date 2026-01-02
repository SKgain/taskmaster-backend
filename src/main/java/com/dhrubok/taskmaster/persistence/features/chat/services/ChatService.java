package com.dhrubok.taskmaster.persistence.features.chat.services;

import com.dhrubok.taskmaster.persistence.features.chat.entities.ChatMessage;
import com.dhrubok.taskmaster.persistence.features.chat.repositories.ChatMessageRepository;
import com.dhrubok.taskmaster.persistence.features.projectmember.repositories.ProjectMemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ChatService {

    private final ChatMessageRepository chatMessageRepository;
    private final ProjectMemberRepository projectMemberRepository;

    /**
     * Check if a user is a member of a project
     *
     * ✅ FIXED: Removed incorrect negation operator
     */
    public boolean isUserProjectMember(String userId, String projectId) {
        boolean isMember = projectMemberRepository.existsByUserIdAndProjectId(userId, projectId);
        log.debug("Checking membership - User: {}, Project: {}, IsMember: {}",
                userId, projectId, isMember);
        return isMember;
    }

    /**
     * Get all messages for a project (only if user is a member)
     *
     * ✅ FIXED: Corrected condition check
     */
    @Transactional(readOnly = true)
    public List<ChatMessage> getProjectMessages(String projectId, String userId) {
        // Verify user is a member
        if (!isUserProjectMember(userId, projectId)) {  // Now correctly checks if NOT a member
            log.warn("Access denied - User {} is not a member of project {}", userId, projectId);
            throw new SecurityException("You are not a member of this project");
        }

        log.info("Loading chat history for project {} requested by user {}", projectId, userId);
        return chatMessageRepository.findByProjectIdOrderByTimestampAsc(projectId);
    }

    /**
     * Update a message (only sender can edit)
     */
    @Transactional
    public ChatMessage updateMessage(String messageId, String userId, String newContent) {
        ChatMessage message = chatMessageRepository.findById(messageId)
                .orElseThrow(() -> new IllegalArgumentException("Message not found"));

        // Only the sender can edit their message
        if (!message.getSenderId().equals(userId)) {
            log.warn("Edit denied - User {} attempted to edit message {} owned by {}",
                    userId, messageId, message.getSenderId());
            throw new SecurityException("You can only edit your own messages");
        }

        log.info("User {} editing message {}", userId, messageId);
        message.setContent(newContent);
        return chatMessageRepository.save(message);
    }

    /**
     * Delete a message (sender or manager can delete)
     */
    @Transactional
    public void deleteMessage(String messageId, String userId, boolean isManager) {
        ChatMessage message = chatMessageRepository.findById(messageId)
                .orElseThrow(() -> new IllegalArgumentException("Message not found"));

        // Only sender or manager can delete
        if (!message.getSenderId().equals(userId) && !isManager) {
            log.warn("Delete denied - User {} attempted to delete message {} without permission",
                    userId, messageId);
            throw new SecurityException("You don't have permission to delete this message");
        }

        log.info("User {} (Manager: {}) deleting message {}", userId, isManager, messageId);
        chatMessageRepository.delete(message);
    }
}