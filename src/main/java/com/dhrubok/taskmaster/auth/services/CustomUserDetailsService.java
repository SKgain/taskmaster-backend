package com.dhrubok.taskmaster.auth.services;

import com.dhrubok.taskmaster.auth.principles.UserDetailsPrinciple;
import com.dhrubok.taskmaster.common.exceptions.ResourceNotFoundException;
import com.dhrubok.taskmaster.persistence.auth.entities.User;
import com.dhrubok.taskmaster.persistence.auth.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {
    private final UserRepository userRepository;
    @Override
    public UserDetails loadUserByUsername(String username) throws ResourceNotFoundException {

        User user = userRepository
                .findByEmail(username).orElseThrow(() -> new ResourceNotFoundException(username));

        return new UserDetailsPrinciple(user);
    }
}