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

    public boolean isUserProjectMember(String userId, String projectId) {
        boolean isMember = projectMemberRepository.existsByUserIdAndProjectId(userId, projectId);

        return isMember;
    }

    @Transactional(readOnly = true)
    public List<ChatMessage> getProjectMessages(String projectId, String userId) {
        if (!isUserProjectMember(userId, projectId)) {

            throw new SecurityException("You are not a member of this project");
        }

        return chatMessageRepository.findByProjectIdOrderByTimestampAsc(projectId);
    }

    @Transactional
    public ChatMessage updateMessage(String messageId, String userId, String newContent) {
        ChatMessage message = chatMessageRepository.findById(messageId)
                .orElseThrow(() -> new IllegalArgumentException("Message not found"));

        if (!message.getSenderId().equals(userId)) {

            throw new SecurityException("You can only edit your own messages");
        }

        message.setContent(newContent);
        return chatMessageRepository.save(message);
    }

    @Transactional
    public void deleteMessage(String messageId, String userId, boolean isManager) {
        ChatMessage message = chatMessageRepository.findById(messageId)
                .orElseThrow(() -> new IllegalArgumentException("Message not found"));

        if (!message.getSenderId().equals(userId) && !isManager) {

            throw new SecurityException("You don't have permission to delete this message");
        }

        chatMessageRepository.delete(message);
    }
}