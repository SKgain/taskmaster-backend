package com.dhrubok.taskmaster.core.controllers.features.user;

import com.dhrubok.taskmaster.common.models.Response;
import com.dhrubok.taskmaster.persistence.features.user.models.CreateMemberRequest;
import com.dhrubok.taskmaster.persistence.features.user.services.MemberManagementService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.mail.MessagingException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

import static com.dhrubok.taskmaster.auth.constants.SecurityConstant.JWT;

@RestController
@RequestMapping("/api/manager")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Manager Operations", description = "Manager-only endpoints for member management")
@SecurityRequirement(name = JWT)
public class ManagerController {

    private final MemberManagementService memberService;

    @Operation(summary = "Create a new MEMBER")
    @ApiResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = Response.class)))
    @PreAuthorize("hasRole('MANAGER')")
    @PostMapping("/members")
    public ResponseEntity<Response> createMember(Authentication authentication,
                                                 @Valid @RequestBody CreateMemberRequest request) throws MessagingException, IOException {

        return ResponseEntity.ok(
                Response.getResponseEntity(
                        true,
                        "Member created successfully. Verification email sent to " + request.getEmail(),
                        memberService.createMember(authentication.getName(), request)
                )
        );
    }

    @Operation(summary = "Get all members")
    @ApiResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = Response.class)))
    @PreAuthorize("hasRole('MANAGER')")
    @GetMapping("/members")
    public ResponseEntity<Response> getAllMembers(Authentication authentication) {

        return ResponseEntity.ok(
                Response.getResponseEntity(
                        true,
                        "Members retrieved successfully",
                        memberService.getAllMembers(authentication.getName())
                )
        );
    }

    @Operation(summary = "Get active members")
    @ApiResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = Response.class)))
    @PreAuthorize("hasRole('MANAGER')")
    @GetMapping("/members/active")
    public ResponseEntity<Response> getActiveMembers(Authentication authentication) {

        return ResponseEntity.ok(
                Response.getResponseEntity(
                        true,
                        "Active members retrieved successfully",
                        memberService.getActiveMembers(authentication.getName())
                )
        );
    }

    @Operation(summary = "Get member by ID")
    @ApiResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = Response.class)))
    @PreAuthorize("hasRole('MANAGER')")
    @GetMapping("/members/{memberId}")
    public ResponseEntity<Response> getMemberById(Authentication authentication,
                                                  @PathVariable String memberId) {

        return ResponseEntity.ok(
                Response.getResponseEntity(
                        true,
                        "Member details retrieved",
                        memberService.getMemberById(authentication.getName(), memberId)
                )
        );
    }

    @Operation(summary = "Deactivate a member")
    @ApiResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = Response.class)))
    @PreAuthorize("hasRole('MANAGER')")
    @DeleteMapping("/members/{memberId}")
    public ResponseEntity<Response> deactivateMember(Authentication authentication,
                                                     @PathVariable String memberId) {

        memberService.deactivateMember(authentication.getName(), memberId);

        return ResponseEntity.ok(
                Response.getResponseEntity(
                        true,
                        "Member deactivated successfully",
                        null)
        );
    }

    @Operation(summary = "Reactivate a member")
    @ApiResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = Response.class)))
    @PreAuthorize("hasRole('MANAGER')")
    @PutMapping("/members/{memberId}")
    public ResponseEntity<Response> reactivateMember(Authentication authentication,
                                                     @PathVariable String memberId) {

        memberService.reactivateMember(authentication.getName(), memberId);

        return ResponseEntity.ok(
                Response.getResponseEntity(
                        true,
                        "Member reactivated successfully",
                        null)
        );
    }

    @Operation(summary = "Resend verification email")
    @ApiResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = Response.class)))
    @PreAuthorize("hasRole('MANAGER')")
    @PostMapping("/members/{memberId}/resend-verification")
    public ResponseEntity<Response> resendVerification(Authentication authentication,
                                                       @PathVariable String memberId) throws MessagingException, IOException {

        memberService.resendMemberVerification(authentication.getName(), memberId);

        return ResponseEntity.ok(
                Response.getResponseEntity(
                        true,
                        "Verification email resent successfully",
                        null)
        );
    }

    @Operation(summary = "Get member statistics")
    @ApiResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = Response.class)))
    @PreAuthorize("hasRole('MANAGER')")
    @GetMapping("/members/stats")
    public ResponseEntity<Response> getMemberStats(Authentication authentication) {

        return ResponseEntity.ok(
                Response.getResponseEntity(
                        true,
                        "Member statistics retrieved",
                        memberService.getMemberStats(authentication.getName())
                )
        );
    }
}