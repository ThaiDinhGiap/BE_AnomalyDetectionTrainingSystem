package com.sep490.anomaly_training_backend.controller;

import com.sep490.anomaly_training_backend.dto.response.ApiResponse;
import com.sep490.anomaly_training_backend.dto.response.SectionResponse;
import com.sep490.anomaly_training_backend.dto.response.UserResponse;
import com.sep490.anomaly_training_backend.dto.response.WorkingPosition;
import com.sep490.anomaly_training_backend.exception.AuthException;
import com.sep490.anomaly_training_backend.model.User;
import com.sep490.anomaly_training_backend.repository.UserRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Account", description = "APIs for user profile")
public class AccountController {

    private final UserRepository userRepository;
    @Operation(
            summary = "Get current user",
            description = "Get information of the currently authenticated user"
    )
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserResponse>> getCurrentUser(
            @AuthenticationPrincipal User userDetails) {

        User user = userRepository.findByUsernameAndDeleteFlagFalse(userDetails.getUsername())
                .orElseThrow(() -> new AuthException("User not found"));

        return ResponseEntity.ok(ApiResponse.success(UserResponse.fromEntity(user)));
    }

    @GetMapping("/working-position")
    @PreAuthorize("hasAuthority('staff_organization.view')")
    @Operation(summary = "Get summary information of current user to display shortly",
               description = "Get basic information for the current user, including sections and permissions"
    )
    public ResponseEntity<ApiResponse<WorkingPosition>> getWorkingPosition() {
        return null;
    }
}
