package com.dhrubok.taskmaster.persistence.features.user.services;

import com.dhrubok.taskmaster.persistence.auth.entities.User;
import com.dhrubok.taskmaster.persistence.auth.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class BaseUserService {
    private final UserRepository userRepository;

    public User getAuthenticatedUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            return null;
        }

        String identifier = auth.getName();

        Optional<User> user = userRepository.findById(identifier);
        if (user.isPresent()) return user.get();

        user = userRepository.findByUsername(identifier);
        if (user.isPresent()) return user.get();

        user = userRepository.findByEmail(identifier);
        return user.orElse(null);
    }
}
