package com.dhrubok.taskmaster.auth.services;

import com.dhrubok.taskmaster.auth.constants.SecurityConstant;
import com.dhrubok.taskmaster.persistence.auth.models.*;
import com.dhrubok.taskmaster.common.constants.ErrorCode;
import com.dhrubok.taskmaster.common.constants.SuccessCode;
import com.dhrubok.taskmaster.common.exceptions.ApplicationException;
import com.dhrubok.taskmaster.common.exceptions.DuplicateResourceException;
import com.dhrubok.taskmaster.common.exceptions.ResourceNotFoundException;
import com.dhrubok.taskmaster.common.models.Response;
import com.dhrubok.taskmaster.common.services.EmailService;
import com.dhrubok.taskmaster.persistence.auth.entities.User;
import com.dhrubok.taskmaster.persistence.auth.enums.RoleType;
import com.dhrubok.taskmaster.persistence.auth.repositories.UserRepository;
import com.dhrubok.taskmaster.persistence.system.entities.SystemConfig;
import com.dhrubok.taskmaster.persistence.system.repositories.SystemConfigRepository;
import jakarta.mail.MessagingException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JWTService jwtService;
    private final ModelMapper mapper;
    private final AuthenticationManager authenticationManager;
    private final EmailService emailService;
    private final SystemConfigRepository systemConfigRepository;

    @Value("${app.frontend-url}")
    private String frontendUrl;

    @Value("${app.base-url}")
    private String backendUrl;

    @Transactional
    public Response signUp(@Valid SignUpRequest request) throws MessagingException, IOException {
        Optional<User> existUser = userRepository.findByEmail(request.getEmail().toLowerCase());

        if (existUser.isPresent()) {
            throw new DuplicateResourceException(ErrorCode.ERROR_USER_ALREADY_EXISTS);
        }

        SystemConfig systemConfig = systemConfigRepository.findFirstByOrderByIdDesc();

        if(!systemConfig.isAllowRegistration()){
            throw new ApplicationException(ErrorCode.ERROR_USER_REGISTRATION_OFF);
        }

        User user = User.builder()
                .username(request.getUsername())
                .fullName(request.getFullName())
                .email(request.getEmail().toLowerCase())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(RoleType.MANAGER)
                .isActive(true)
                .isEnabled(false)
                .isEmailVerified(false)
                .verificationToken(UUID.randomUUID().toString())
                .tokenExpiryDate(Instant.now().plusSeconds(24 * 3600))
                .build();

        userRepository.save(user);

        String verificationUrl = frontendUrl + SecurityConstant.VERIFICATION_URL + user.getVerificationToken();
        emailService.sendVerificationEmail(user.getEmail(), verificationUrl);

        return Response.getResponseEntity(
                true,
                SuccessCode.SUCCESS_USER_SIGN_UP,
                mapper.map(user, AuthUserResponse.class));
    }

    @Transactional
    public void verifyUser(String token) throws MessagingException, IOException {
        User user = userRepository.findByVerificationToken(token)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.ERROR_VERIFICATION_TOKEN_INVALID));

        if (user.getTokenExpiryDate().isBefore(Instant.now())) {
            throw new ApplicationException(ErrorCode.ERROR_VERIFICATION_TOKEN_EXPIRED);
        }

        user.setIsEnabled(true);
        user.setIsEmailVerified(true);
        user.setVerificationToken(null);
        user.setTokenExpiryDate(null);
        userRepository.save(user);

        String dashboardUrl = SecurityConstant.DASHBOARD_URL;
        emailService.sendWelcomeEmail(user.getEmail(), dashboardUrl);

        Response.getResponseEntity(
                true,
                SuccessCode.SUCCESS_USER_VERIFIED,
                mapper.map(user, AuthUserResponse.class));
    }

    @Transactional
    public Response resendVerificationCode(String email) throws MessagingException, IOException {
        User user = userRepository.findByEmail(email.toLowerCase())
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.ERROR_USER_NOT_FOUND));

        if (user.getIsEnabled()) {
            throw new ApplicationException(ErrorCode.ERROR_ACCOUNT_ALREADY_VERIFIED);
        }

        user.setVerificationToken(UUID.randomUUID().toString());
        user.setTokenExpiryDate(Instant.now().plusSeconds(24 * 3600)); // 24 hours expiry
        userRepository.save(user);

        String verificationUrl = frontendUrl + SecurityConstant.VERIFICATION_URL + user.getVerificationToken();
        emailService.sendVerificationEmail(user.getEmail(), verificationUrl);

        return Response.getResponseEntity(
                true,
                SuccessCode.SUCCESS_EMAIL_RESENT,
                null
        );
    }

    @Transactional
    public Response forgotPassword(String email) throws MessagingException, IOException {
        User user = userRepository.findByEmail(email.toLowerCase())
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.ERROR_USER_NOT_FOUND));

        user.setVerificationToken(UUID.randomUUID().toString());
        user.setTokenExpiryDate(Instant.now().plusSeconds(15 * 60)); // 15 minutes expiry
        userRepository.save(user);

        String resetUrl = frontendUrl + SecurityConstant.RESET_PASSWORD_URL + user.getVerificationToken();
        emailService.sendPasswordResetEmail(user.getEmail(), resetUrl);

        return Response.getResponseEntity(
                true,
                SuccessCode.SUCCESS_FORGET_PASSWORD_EMAIL_SENT,
                null
        );
    }

    @Transactional
    public Response resetPassword(ResetPasswordRequest request) {
        User user = userRepository.findByVerificationToken(request.getToken())
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.ERROR_VERIFICATION_TOKEN_INVALID));

        if (user.getTokenExpiryDate().isBefore(Instant.now())) {
            throw new ApplicationException(ErrorCode.ERROR_VERIFICATION_TOKEN_EXPIRED);
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));

        user.setVerificationToken(null);
        user.setTokenExpiryDate(null);

        if (!user.getIsEnabled()) {
            user.setIsEnabled(true);
        }

        userRepository.save(user);

        return Response.getResponseEntity(
                true,
                SuccessCode.SUCCESS_PASSWORD_RESET,
                null
        );
    }

    @Transactional
    public Response signIn(@Valid SignInRequest request) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getEmail().toLowerCase(),
                            request.getPassword()
                    )
            );
            if (authentication.isAuthenticated()) {

                User user = userRepository.findByEmail(request.getEmail()).orElseThrow(() -> new ApplicationException(
                        ErrorCode.ERROR_USER_NOT_FOUND));

                if (!user.getIsActive()) {
                    throw new ApplicationException(ErrorCode.ERROR_ACCOUNT_IS_DISABLED);
                }

                if (!user.getIsEmailVerified()) {
                    throw new ApplicationException(ErrorCode.ERROR_ACCOUNT_IS_NOT_VERIFIED);
                }

                if (!user.getIsEnabled()) {
                    throw new ApplicationException(ErrorCode.ERROR_ACCOUNT_IS_NOT_VERIFIED);
                }

                SystemConfig systemConfig = systemConfigRepository.findFirstByOrderByIdDesc();

                if(systemConfig.isMaintenanceMode() && !(user.getRole().equals(RoleType.ADMIN))){
                    throw new ApplicationException(ErrorCode.ERROR_SYSTEM_UNDER_MAINTENANCE);
                }


                String accessToken = jwtService.generateAccessToken(request.getEmail());

                String refreshToken = jwtService.generateRefreshToken(user.getEmail());
                user.setRefreshToken(refreshToken);
                user.setLastLogin(Instant.now());

                userRepository.save(user);

                AuthUserResponse userDto = mapper.map(user, AuthUserResponse.class);
                return Response.getResponseEntity(
                        true,
                        SuccessCode.SUCCESS_USER_SIGN_IN,
                        Map.of(
                                "accessToken", accessToken,
                                "refreshToken", refreshToken,
                                "user", userDto
                        )
                );
            }

            throw new ApplicationException(ErrorCode.ERROR_EMAIL_OR_PASSWORD_INCORRECT);
        } catch (BadCredentialsException ex) {
            throw new BadCredentialsException(ErrorCode.ERROR_EMAIL_OR_PASSWORD_INCORRECT);

        } catch (UsernameNotFoundException ex) {
            throw new ResourceNotFoundException(ErrorCode.ERROR_EMAIL_OR_PASSWORD_INCORRECT);
        }
    }

    @Transactional
    public Response refreshToken(RefreshTokenRequest request) {
        String refreshToken = request.getRefreshToken();

        String email = jwtService.extractUserName(refreshToken);

        if (email == null || !jwtService.validateToken(refreshToken, email)) {
            throw new ApplicationException(ErrorCode.ERROR_VERIFICATION_REFRESH_TOKEN_INVALID);
        }

        String newAccessToken = jwtService.generateAccessToken(email);

        return Response.getResponseEntity(
                HttpStatus.OK,
                "Token refreshed successfully",
                Map.of("accessToken", newAccessToken)
        );
    }

    @Transactional
    public void logout(String username) {

        var user = userRepository.findByEmail(username)
                .orElseThrow(() -> new UsernameNotFoundException(ErrorCode.ERROR_USER_NOT_FOUND));

         user.setRefreshToken(null);
         userRepository.save(user);;
    }
}