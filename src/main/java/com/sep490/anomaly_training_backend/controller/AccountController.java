package com.sep490.anomaly_training_backend.controller;

import com.sep490.anomaly_training_backend.dto.response.ApiResponse;
import com.sep490.anomaly_training_backend.dto.response.AuthResponse;
import com.sep490.anomaly_training_backend.dto.response.UserResponse;
import com.sep490.anomaly_training_backend.exception.AppException;
import com.sep490.anomaly_training_backend.exception.ErrorCode;
import com.sep490.anomaly_training_backend.model.User;
import com.sep490.anomaly_training_backend.repository.UserRepository;
import com.sep490.anomaly_training_backend.service.impl.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Account", description = "APIs for user profile")
public class AccountController {

    private final UserRepository userRepository;
    private final AuthService authService;

    @Operation(
            summary = "Get current user",
            description = "Get information of the currently authenticated user"
    )
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserResponse>> getCurrentUser(
            @AuthenticationPrincipal User userDetails) {

        User user = userRepository.findByUsernameAndDeleteFlagFalse(userDetails.getUsername())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        return ResponseEntity.ok(ApiResponse.success(UserResponse.fromEntity(user)));
    }

    @Operation(
            summary = "Change Password",
            description = "Change the password for the currently authenticated user"
    )
    @SecurityRequirement(name = "bearerAuth")
    @PostMapping("/change-password")
    public ResponseEntity<ApiResponse<AuthResponse>> changePassword(
            @AuthenticationPrincipal User userDetails,
            @Valid @RequestBody com.sep490.anomaly_training_backend.dto.request.ChangePasswordRequest request) {

        log.info("Change password request for user: {}", userDetails.getUsername());
        AuthResponse authResponse = authService.changePassword(userDetails.getUsername(), request);

        return ResponseEntity.ok(ApiResponse.success("Password changed successfully", authResponse));
    }
}
