package com.dhrubok.taskmaster.core.controllers.features.chat;

import com.dhrubok.taskmaster.common.models.Response;
import com.dhrubok.taskmaster.persistence.auth.entities.User;
import com.dhrubok.taskmaster.persistence.auth.repositories.UserRepository;
import com.dhrubok.taskmaster.persistence.features.chat.entities.ChatMessage;
import com.dhrubok.taskmaster.persistence.features.chat.repositories.ChatMessageRepository;
import com.dhrubok.taskmaster.persistence.features.chat.services.ChatService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static com.dhrubok.taskmaster.auth.constants.SecurityConstant.JWT_TOKEN;

@Controller
@RequestMapping("/api/chat")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Chat Management", description = "Real-time chat operations for projects")
@SecurityRequirement(name = JWT_TOKEN)
public class ChatController {

    private final ChatMessageRepository chatRepository;
    private final SimpMessagingTemplate messagingTemplate;
    private final ChatService chatService;
    private final UserRepository userRepository;

    /**
     * 💪 ROBUST USER LOOKUP
     * Checks SecurityContext directly and tries ID, Username, and Email.
     */
    private User getAuthenticatedUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            log.error("❌ No Authentication found in SecurityContext");
            return null;
        }

        String identifier = auth.getName(); // This is what is inside your JWT (sub)
        log.info("🔍 Lookup User for Identifier: '{}'", identifier);

        // 1. Try finding by ID (in case sub is UUID)
        Optional<User> user = userRepository.findById(identifier);
        if (user.isPresent()) return user.get();

        // 2. Try finding by Username
        user = userRepository.findByUsername(identifier);
        if (user.isPresent()) return user.get();

        // 3. Try finding by Email
        user = userRepository.findByEmail(identifier);
        if (user.isPresent()) return user.get();

        log.error("❌ User NOT found in DB for identifier: '{}'", identifier);
        return null;
    }

    // --- REST API ENDPOINTS ---

    @Operation(summary = "Get chat history for a project")
    @GetMapping("/history/{projectId}")
    @ResponseBody
    public ResponseEntity<Response> getChatHistory(@PathVariable String projectId) {

        User user = getAuthenticatedUser();

        if (user == null) {
            return ResponseEntity.status(401).body(Response.getResponseEntity(false, "Unauthorized: User not found", null));
        }

        log.info("✅ Authorized GET History for Project: {}", projectId);

        try {
            // Since your Entity uses Strings now, we return the list directly.
            // NO MAPPING NEEDED -> NO CRASHES.
            List<ChatMessage> messages = chatService.getProjectMessages(projectId, user.getId());

            return ResponseEntity.ok(Response.getResponseEntity(true, "History retrieved", messages));

        } catch (SecurityException e) {
            return ResponseEntity.status(403).body(Response.getResponseEntity(false, e.getMessage(), null));
        } catch (Exception e) {
            log.error("Error loading chat history", e);
            return ResponseEntity.internalServerError().body(Response.getResponseEntity(false, "Server Error: " + e.getMessage(), null));
        }
    }

    // --- WEBSOCKET HANDLERS ---

    @MessageMapping("/chat.sendMessage")
    public void sendMessage(@Payload ChatMessage chatMessage, SimpMessageHeaderAccessor headerAccessor) {
        try {
            // WebSocket authentication is handled differently (via ChannelInterceptor)
            // But for now, we rely on the message content itself or the header accessor
            if (headerAccessor.getUser() == null) {
                log.error("Websocket: No User in Header");
                return;
            }

            if (chatMessage.getSenderId() == null || chatMessage.getProjectId() == null) return;

            if (!chatService.isUserProjectMember(chatMessage.getSenderId(), chatMessage.getProjectId())) {
                log.error("Websocket: User not member");
                return;
            }

            chatMessage.setTimestamp(LocalDateTime.now());
            ChatMessage savedMessage = chatRepository.save(chatMessage);

            messagingTemplate.convertAndSend("/topic/project/" + chatMessage.getProjectId(), savedMessage);

        } catch (Exception e) {
            log.error("Error processing chat message", e);
        }
    }

    @MessageMapping("/chat.typing")
    public void handleTyping(@Payload Map<String, Object> typingData) {
        try {
            String projectId = (String) typingData.get("projectId");
            if (projectId != null) {
                messagingTemplate.convertAndSend("/topic/project/" + projectId + "/typing", Optional.of(typingData));
            }
        } catch (Exception e) {
            log.error("Error processing typing", e);
        }
    }

    @PutMapping("/messages/{messageId}")
    @ResponseBody
    public ResponseEntity<Response> updateMessage(@PathVariable String messageId, @RequestBody Map<String, String> payload) {
        User user = getAuthenticatedUser();
        if (user == null) return ResponseEntity.status(401).build();

        try {
            String newContent = payload.get("content");
            ChatMessage updatedMessage = chatService.updateMessage(messageId, user.getId(), newContent);

            Map<String, Object> broadcastMessage = new HashMap<>();
            broadcastMessage.put("type", "UPDATE");
            broadcastMessage.put("message", updatedMessage);
            messagingTemplate.convertAndSend("/topic/project/" + updatedMessage.getProjectId(), Optional.of(broadcastMessage));

            return ResponseEntity.ok(Response.getResponseEntity(true, "Updated", updatedMessage));
        } catch (Exception e) {
            return ResponseEntity.status(403).body(Response.getResponseEntity(false, e.getMessage(), null));
        }
    }

    @DeleteMapping("/messages/{messageId}")
    @ResponseBody
    public ResponseEntity<Response> deleteMessage(@PathVariable String messageId) {
        User user = getAuthenticatedUser();
        if (user == null) return ResponseEntity.status(401).build();

        try {
            boolean isManager = user.getRole().name().equals("MANAGER");
            ChatMessage message = chatRepository.findById(messageId).orElseThrow();
            chatService.deleteMessage(messageId, user.getId(), isManager);

            Map<String, Object> broadcastMessage = new HashMap<>();
            broadcastMessage.put("type", "DELETE");
            broadcastMessage.put("messageId", messageId);
            messagingTemplate.convertAndSend("/topic/project/" + message.getProjectId(), Optional.of(broadcastMessage));

            return ResponseEntity.ok(Response.getResponseEntity(true, "Deleted", null));
        } catch (Exception e) {
            return ResponseEntity.status(403).body(Response.getResponseEntity(false, e.getMessage(), null));
        }
    }
}