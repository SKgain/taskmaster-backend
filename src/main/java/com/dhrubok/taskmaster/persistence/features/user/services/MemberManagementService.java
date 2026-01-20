package com.dhrubok.taskmaster.persistence.features.user.services;

import com.dhrubok.taskmaster.auth.constants.SecurityConstant;
import com.dhrubok.taskmaster.common.exceptions.ApplicationException;
import com.dhrubok.taskmaster.common.exceptions.DuplicateResourceException;
import com.dhrubok.taskmaster.common.exceptions.ResourceNotFoundException;
import com.dhrubok.taskmaster.common.services.EmailService;
import com.dhrubok.taskmaster.persistence.auth.entities.User;
import com.dhrubok.taskmaster.persistence.auth.enums.RoleType;
import com.dhrubok.taskmaster.persistence.auth.repositories.UserRepository;
import com.dhrubok.taskmaster.persistence.features.user.models.CreateMemberRequest;
import com.dhrubok.taskmaster.persistence.features.user.models.UserResponse;
import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class MemberManagementService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

    @Value("${app.frontend-url}")
    private String frontendUrl;

    private static final String UPPERCASE = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final String LOWERCASE = "abcdefghijklmnopqrstuvwxyz";
    private static final String DIGITS = "0123456789";
    private static final String SPECIAL = "!@#$%";
    private static final String ALLOWED_CHARS = UPPERCASE + LOWERCASE + DIGITS + SPECIAL;
    private static final int PASSWORD_LENGTH = 12;

    @Transactional
    public UserResponse createMember(String managerEmail, CreateMemberRequest request) {

        User manager = userRepository.findByEmail(managerEmail.toLowerCase())
                .orElseThrow(() -> new ResourceNotFoundException("Manager not found with email: " + managerEmail));

        if (manager.getRole() != RoleType.MANAGER) {

            throw new ApplicationException("Only MANAGER can create members. Your role: " + manager.getRole());
        }

        String memberEmail = request.getEmail().toLowerCase();
        if (userRepository.existsByEmail(memberEmail)) {

            throw new DuplicateResourceException("Member with email " + memberEmail + " already exists");
        }

        String generatedPassword = generateSecurePassword();

        String username = request.getUsername();
        if (username == null || username.isBlank()) {
            username = memberEmail.split("@")[0];
        }

        User member = User.builder()
                .username(username)
                .fullName(request.getFullName())
                .email(memberEmail)
                .password(passwordEncoder.encode(generatedPassword))
                .role(RoleType.MEMBER)
                .isActive(true)
                .isEnabled(false)
                .createdBy(managerEmail)
                .isEmailVerified(false)
                .verificationToken(UUID.randomUUID().toString())
                .tokenExpiryDate(Instant.now().plusSeconds(7 * 24 * 3600))
                .build();

        userRepository.save(member);

        String verificationUrl = frontendUrl + SecurityConstant.VERIFICATION_URL + member.getVerificationToken();

        try {
            emailService.sendMemberWelcomeEmail(
                    member.getEmail(),
                    member.getFullName(),
                    generatedPassword,
                    verificationUrl
            );

        } catch (Exception e) {
            log.error("Failed to send welcome email to {}: {}", memberEmail, e.getMessage());
        }

        return mapToUserResponse(member);
    }

    @Transactional(readOnly = true)
    public List<UserResponse> getAllMembers(String managerEmail) {

        User manager = userRepository.findByEmail(managerEmail.toLowerCase())
                .orElseThrow(() -> new ResourceNotFoundException("Manager not found"));

        if (manager.getRole() != RoleType.MANAGER) {
            throw new ApplicationException("Only MANAGER can view members");
        }

        List<User> members = userRepository.findByRoleAndCreatedBy(RoleType.MEMBER, managerEmail);

        return members.stream()
                .map(this::mapToUserResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<UserResponse> getActiveMembers(String managerEmail) {

        User manager = userRepository.findByEmail(managerEmail.toLowerCase())
                .orElseThrow(() -> new ResourceNotFoundException("Manager not found"));

        if (manager.getRole() != RoleType.MANAGER) {
            throw new ApplicationException("Only MANAGER can view members");
        }

        List<User> activeMembers = userRepository.findByRoleAndIsActiveTrueAndCreatedBy(RoleType.MEMBER, managerEmail);

        return activeMembers.stream()
                .map(this::mapToUserResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public UserResponse getMemberById(String managerEmail, String memberId) {

        User manager = userRepository.findByEmail(managerEmail.toLowerCase())
                .orElseThrow(() -> new ResourceNotFoundException("Manager not found"));

        if (manager.getRole() != RoleType.MANAGER) {
            throw new ApplicationException("Only MANAGER can view member details");
        }

        User member = userRepository.findById(memberId)
                .orElseThrow(() -> new ResourceNotFoundException("Member not found with ID: " + memberId));

        if (member.getRole() != RoleType.MEMBER) {
            throw new ApplicationException("User is not a MEMBER");
        }

        return mapToUserResponse(member);
    }

    @Transactional
    public void deactivateMember(String managerEmail, String memberId) {

        User manager = userRepository.findByEmail(managerEmail.toLowerCase())
                .orElseThrow(() -> new ResourceNotFoundException("Manager not found"));

        if (manager.getRole() != RoleType.MANAGER) {
            throw new ApplicationException("Only MANAGER can deactivate members");
        }

        User member = userRepository.findById(memberId)
                .orElseThrow(() -> new ResourceNotFoundException("Member not found with ID: " + memberId));

        if (member.getRole() != RoleType.MEMBER) {
            throw new ApplicationException("Can only deactivate MEMBER accounts. Found role: " + member.getRole());
        }

        if (!member.getIsActive()) {
            throw new ApplicationException("Member is already deactivated");
        }

        member.setIsActive(false);
        userRepository.save(member);
    }

    @Transactional
    public void reactivateMember(String managerEmail, String memberId) {

        User manager = userRepository.findByEmail(managerEmail.toLowerCase())
                .orElseThrow(() -> new ResourceNotFoundException("Manager not found"));

        if (manager.getRole() != RoleType.MANAGER) {
            throw new ApplicationException("Only MANAGER can reactivate members");
        }

        User member = userRepository.findById(memberId)
                .orElseThrow(() -> new ResourceNotFoundException("Member not found with ID: " + memberId));

        if (member.getRole() != RoleType.MEMBER) {
            throw new ApplicationException("Can only reactivate MEMBER accounts");
        }

        member.setIsActive(true);
        userRepository.save(member);
    }

    @Transactional
    public void resendMemberVerification(String managerEmail, String memberId)
            throws MessagingException, IOException {

        User manager = userRepository.findByEmail(managerEmail.toLowerCase())
                .orElseThrow(() -> new ResourceNotFoundException("Manager not found"));

        if (manager.getRole() != RoleType.MANAGER) {
            throw new ApplicationException("Only MANAGER can resend verification");
        }

        User member = userRepository.findById(memberId)
                .orElseThrow(() -> new ResourceNotFoundException("Member not found with ID: " + memberId));

        if (member.getIsEnabled() && member.getIsEmailVerified()) {
            throw new ApplicationException("Member is already verified");
        }

        member.setVerificationToken(UUID.randomUUID().toString());
        member.setTokenExpiryDate(Instant.now().plusSeconds(7 * 24 * 3600));
        userRepository.save(member);

        String verificationUrl = frontendUrl + SecurityConstant.VERIFICATION_URL + member.getVerificationToken();
        emailService.sendVerificationEmail(member.getEmail(), verificationUrl);
    }

    @Transactional(readOnly = true)
    public MemberStats getMemberStats(String managerEmail) {
        User manager = userRepository.findByEmail(managerEmail.toLowerCase())
                .orElseThrow(() -> new ResourceNotFoundException("Manager not found"));

        if (manager.getRole() != RoleType.MANAGER) {
            throw new ApplicationException("Only MANAGER can view statistics");
        }

        Long totalMembers = userRepository.countByRole(RoleType.MEMBER);

        List<User> allMembers = userRepository.findByRole(RoleType.MEMBER);
        long activeMembers = allMembers.stream().filter(User::getIsActive).count();
        long verifiedMembers = allMembers.stream().filter(User::getIsEmailVerified).count();
        long pendingMembers = totalMembers - verifiedMembers;

        return new MemberStats(totalMembers, activeMembers, verifiedMembers, pendingMembers);
    }

    private String generateSecurePassword() {
        SecureRandom random = new SecureRandom();
        StringBuilder password = new StringBuilder(PASSWORD_LENGTH);

        password.append(UPPERCASE.charAt(random.nextInt(UPPERCASE.length())));
        password.append(LOWERCASE.charAt(random.nextInt(LOWERCASE.length())));
        password.append(DIGITS.charAt(random.nextInt(DIGITS.length())));
        password.append(SPECIAL.charAt(random.nextInt(SPECIAL.length())));

        for (int i = 4; i < PASSWORD_LENGTH; i++) {
            int index = random.nextInt(ALLOWED_CHARS.length());
            password.append(ALLOWED_CHARS.charAt(index));
        }

        char[] passwordArray = password.toString().toCharArray();
        for (int i = passwordArray.length - 1; i > 0; i--) {
            int j = random.nextInt(i + 1);
            char temp = passwordArray[i];
            passwordArray[i] = passwordArray[j];
            passwordArray[j] = temp;
        }

        return new String(passwordArray);
    }

    private UserResponse mapToUserResponse(User user) {
        return UserResponse.builder()
                .userId(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .role(user.getRole().name())
                .profileImage(user.getProfileImage())
                .isActive(user.getIsActive())
                .isEmailVerified(user.getIsEmailVerified())
                .build();
    }

    public static class MemberStats {
        public final Long totalMembers;
        public final Long activeMembers;
        public final Long verifiedMembers;
        public final Long pendingMembers;

        public MemberStats(Long total, Long active, Long verified, Long pending) {
            this.totalMembers = total;
            this.activeMembers = active;
            this.verifiedMembers = verified;
            this.pendingMembers = pending;
        }
    }
}