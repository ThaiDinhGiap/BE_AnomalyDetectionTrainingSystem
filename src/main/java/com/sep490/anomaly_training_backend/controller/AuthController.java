package com.sep490.anomaly_training_backend.controller;

import com.sep490.anomaly_training_backend.dto.request.LoginRequest;
import com.sep490.anomaly_training_backend.dto.request.RefreshTokenRequest;
import com.sep490.anomaly_training_backend.dto.request.RegisterRequest;
import com.sep490.anomaly_training_backend.dto.response.ApiResponse;
import com.sep490.anomaly_training_backend.dto.response.AuthResponse;
import com.sep490.anomaly_training_backend.model.User;
import com.sep490.anomaly_training_backend.service.impl.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Authentication", description = "APIs for user authentication and authorization")
public class AuthController {

    private final AuthService authService;

    @Operation(
            summary = "Register new user",
            description = "Register a new local user account. Email and username must be unique."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "201",
                    description = "Registration successful",
                    content = @Content(schema = @Schema(implementation = AuthResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "Invalid input or duplicate username/email",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))
            )
    })
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<AuthResponse>> register(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Registration details",
                    required = true,
                    content = @Content(
                            schema = @Schema(implementation = RegisterRequest.class),
                            examples = @ExampleObject(
                                    name = "Example",
                                    value = """
                                            {
                                                "username":  "john_doe",
                                                "email": "john.doe@sep490.com",
                                                "password": "SecurePass123",
                                                "fullName": "John Doe",
                                                "role": "TEAM_LEADER"
                                            }
                                            """
                            )
                    )
            )
            @Valid @RequestBody RegisterRequest request) {

        log.info("Register request for username: {}", request.getUsername());
        AuthResponse response = authService.register(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Registration successful", response));
    }

    @Operation(
            summary = "Login",
            description = "Authenticate user with username and password. Returns JWT tokens."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Login successful"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "Invalid credentials"
            )
    })
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Login credentials",
                    required = true,
                    content = @Content(
                            examples = @ExampleObject(
                                    name = "Example",
                                    value = """
                                            {
                                                "username": "admin",
                                                "password":  "Password@123"
                                            }
                                            """
                            )
                    )
            )
            @Valid @RequestBody LoginRequest request) {

        log.info("Login request for username: {}", request.getUsername());
        AuthResponse response = authService.login(request);

        return ResponseEntity.ok(ApiResponse.success("Login successful", response));
    }

    @Operation(
            summary = "Refresh token",
            description = "Get new access token using refresh token"
    )
    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<AuthResponse>> refreshToken(
            @Valid @RequestBody RefreshTokenRequest request) {

        log.info("Token refresh request");
        AuthResponse response = authService.refreshToken(request);

        return ResponseEntity.ok(ApiResponse.success("Token refreshed successfully", response));
    }

    @Operation(
            summary = "Logout",
            description = "Revoke refresh token to logout user"
    )
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(
            @Valid @RequestBody RefreshTokenRequest request) {

        log.info("Logout request");
        authService.logout(request.getRefreshToken());

        return ResponseEntity.ok(ApiResponse.success("Logout successful", null));
    }

    @Operation(
            summary = "Logout from all devices",
            description = "Revoke all refresh tokens for the current user"
    )
    @SecurityRequirement(name = "bearerAuth")
    @PostMapping("/logout-all")
    public ResponseEntity<ApiResponse<Void>> logoutAll(
            @AuthenticationPrincipal UserDetails userDetails) {

        log.info("Logout all request for user: {}", userDetails.getUsername());
        authService.logoutAll(userDetails.getUsername());

        return ResponseEntity.ok(ApiResponse.success("Logged out from all devices", null));
    }

    @Operation(
            summary = "Change Password",
            description = "Change the password for the currently authenticated user"
    )
    @SecurityRequirement(name = "bearerAuth")
    @PostMapping("/change-password")
    public ResponseEntity<ApiResponse<Void>> changePassword(
            @AuthenticationPrincipal User userDetails,
            @Valid @RequestBody com.sep490.anomaly_training_backend.dto.request.ChangePasswordRequest request) {

        log.info("Change password request for user: {}", userDetails.getUsername());
        authService.changePassword(userDetails.getUsername(), request);

        return ResponseEntity.ok(ApiResponse.success("Password changed successfully", null));
    }

    @Operation(
            summary = "Forgot Password",
            description = "Send a new temporary password to the user's email if they have one linked."
    )
    @PostMapping("/forgot-password")
    public ResponseEntity<ApiResponse<Void>> forgotPassword(
            @Valid @RequestBody com.sep490.anomaly_training_backend.dto.request.ForgotPasswordRequest request) {

        log.info("Forgot password request for username: {}", request.getUsername());
        authService.forgotPassword(request);

        return ResponseEntity.ok(ApiResponse.success("A temporary password has been sent to your email", null));
    }

}