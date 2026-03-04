package com.dhrubok.taskmaster.core.controllers.admin;

import com.dhrubok.taskmaster.auth.constants.SecurityConstant;
import com.dhrubok.taskmaster.common.annotations.ApiLog;
import com.dhrubok.taskmaster.common.constants.SuccessCode;
import com.dhrubok.taskmaster.persistence.auth.models.RefreshTokenRequest;
import com.dhrubok.taskmaster.persistence.auth.models.ResetPasswordRequest;
import com.dhrubok.taskmaster.persistence.auth.models.SignInRequest;
import com.dhrubok.taskmaster.persistence.auth.models.SignUpRequest;
import com.dhrubok.taskmaster.auth.services.UserService;
import com.dhrubok.taskmaster.common.models.Response;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.mail.MessagingException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Auth Operations", description = "Auth related operations")
@Slf4j
public class AuthController {
    private final UserService userService;

    @Operation(summary = "Sign-up")
    @ApiResponse(content = @Content(schema = @Schema(implementation = Response.class)), responseCode = "200")
    @ApiLog
    @PostMapping("/sign-up")
    public ResponseEntity<Response> signUp(@Valid @RequestBody SignUpRequest request) throws MessagingException, IOException {

        return ResponseEntity.ok(
                Response.getResponseEntity(
                        true,
                        SuccessCode.SUCCESS_USER_SIGN_UP,
                        userService.signUp(request)
                )
        );
    }

    @Operation(summary = "Sign-up-Verify")
    @ApiResponse(content = @Content(schema = @Schema(implementation = Response.class)), responseCode = "200")
    @ApiLog
    @GetMapping("/verify")
    public ResponseEntity<Response> verify(@RequestParam String token) throws MessagingException, IOException {

        userService.verifyUser(token);
        return ResponseEntity.ok(
                Response.getResponseEntity(
                        true,
                        SuccessCode.SUCCESS_EMAIL_VERIFY,
                        null
                )
        );
    }

    @Operation(summary = "Sign-In")
    @ApiResponse(content = @Content(schema = @Schema(implementation = Response.class)), responseCode = "200")
    @ApiLog
    @PostMapping("/sign-in")
    public ResponseEntity<Response> signIn(@Valid @RequestBody SignInRequest request) {
        Response response = userService.signIn(request);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Resend Verification Email")
    @ApiResponse(content = @Content(schema = @Schema(implementation = Response.class)), responseCode = "200")
    @ApiLog
    @PostMapping("/resend-verification")
    public ResponseEntity<Response> resendVerification(@RequestParam String email) throws MessagingException, IOException {

        return ResponseEntity.ok(
                Response.getResponseEntity(
                        true,
                        SuccessCode.SUCCESS_EMAIL_RESENT ,
                        userService.resendVerificationCode(email)
                )
        );
    }

    @Operation(summary = "Forgot Password Request")
    @ApiResponse(content = @Content(schema = @Schema(implementation = Response.class)), responseCode = "200")
    @ApiLog
    @PostMapping("/forgot-password")
    public ResponseEntity<Response> forgotPassword(@RequestParam String email) throws MessagingException, IOException {

        return ResponseEntity.ok(
                Response.getResponseEntity(
                        true,
                        SuccessCode.SUCCESS_FORGET_PASSWORD_EMAIL_SENT,
                        userService.forgotPassword(email)
                )
        );
    }

    @Operation(summary = "Reset Password")
    @ApiResponse(content = @Content(schema = @Schema(implementation = Response.class)), responseCode = "200")
    @ApiLog
    @PostMapping("/reset-password")
    public ResponseEntity<Response> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {

        return ResponseEntity.ok(
                Response.getResponseEntity(
                        true,
                        SuccessCode.SUCCESS_FORGET_PASSWORD_EMAIL_RESENT,
                        userService.resetPassword(request)
                )
        );
    }

    @Operation(summary = "Refresh Token")
    @ApiResponse(content = @Content(schema = @Schema(implementation = Response.class)), responseCode = "200")
    @ApiLog
    @PostMapping("/refresh-token")
    public ResponseEntity<Response> refreshToken(@Valid @RequestBody RefreshTokenRequest request) {
        return ResponseEntity.ok(
                Response.getResponseEntity(
                        true,
                        SuccessCode.SUCCESS,
                        userService.refreshToken(request)
                )
        );
    }

    @Operation(summary = "User Logout", security = @SecurityRequirement(name = SecurityConstant.JWT))
    @ApiResponse(content = @Content(schema = @Schema(implementation = Response.class)), responseCode = "200")
    @ApiLog
    @PostMapping("/logout")
    public ResponseEntity<Response> logout(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication
    ) {
        if (authentication != null) {
            userService.logout(authentication.getName());
        }

        SecurityContextLogoutHandler logoutHandler = new SecurityContextLogoutHandler();
        logoutHandler.logout(request, response, authentication);

        return ResponseEntity.ok(
                Response.getResponseEntity(true,
                        SuccessCode.SUCCESS_USER_SIGN_OUT,
                        null)
        );
    }
}
