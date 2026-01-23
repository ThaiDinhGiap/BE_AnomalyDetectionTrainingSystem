package com.sep490.anomaly_training_backend.service.impl;

import com.sep490.anomaly_training_backend.dto.request.LoginRequest;
import com.sep490.anomaly_training_backend.dto.request.RefreshTokenRequest;
import com.sep490.anomaly_training_backend.dto.request.RegisterRequest;
import com.sep490.anomaly_training_backend.dto.response.AuthResponse;
import com.sep490.anomaly_training_backend.dto.response.UserResponse;
import com.sep490.anomaly_training_backend.enums.OAuthProvider;
import com.sep490.anomaly_training_backend.exception.AuthException;
import com.sep490.anomaly_training_backend.model.RefreshToken;
import com.sep490.anomaly_training_backend.model.User;
import com.sep490.anomaly_training_backend.repository.RefreshTokenRepository;
import com.sep490.anomaly_training_backend.repository.UserRepository;
import com.sep490.anomaly_training_backend.service.JwtService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework. security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final CustomUserDetailsService userDetailsService;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        // Check if username already exists
        if (userRepository.existsByUsernameAndDeleteFlagFalse(request.getUsername())) {
            throw new AuthException("Username already exists");
        }

        // Check if email already exists
        if (userRepository.existsByEmailAndDeleteFlagFalse(request. getEmail())) {
            throw new AuthException("Email already exists");
        }

        // Create new user
        User user = User. builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .fullName(request.getFullName())
                .role(request.getRole())
                .oauthProvider(OAuthProvider.LOCAL)
                .isActive(true)
                .build();

        user = userRepository.save(user);
        log.info("New user registered: {}", user.getUsername());

        return generateAuthResponse(user);
    }

    @Transactional
    public AuthResponse login(LoginRequest request) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getUsername(),
                            request.getPassword()
                    )
            );

            User user = userRepository.findByUsernameAndDeleteFlagFalse(request.getUsername())
                    . orElseThrow(() -> new AuthException("User not found"));

            if (!user.getIsActive()) {
                throw new AuthException("User account is inactive");
            }

            log.info("User logged in: {}", user.getUsername());

            return generateAuthResponse(user);

        } catch (BadCredentialsException e) {
            throw new AuthException("Invalid username or password");
        }
    }

    @Transactional
    public AuthResponse refreshToken(RefreshTokenRequest request) {
        String refreshTokenStr = request.getRefreshToken();

        // Find refresh token in database
        RefreshToken refreshToken = refreshTokenRepository
                .findByTokenAndRevokedFalseAndDeleteFlagFalse(refreshTokenStr)
                .orElseThrow(() -> new AuthException("Invalid refresh token"));

        // Check if token is expired
        if (refreshToken.getExpiresAt().isBefore(LocalDateTime.now())) {
            refreshTokenRepository.revokeByToken(refreshTokenStr);
            throw new AuthException("Refresh token has expired");
        }

        User user = refreshToken.getUser();

        if (!user.getIsActive()) {
            throw new AuthException("User account is inactive");
        }

        // Revoke old refresh token
        refreshTokenRepository.revokeByToken(refreshTokenStr);

        log.info("Token refreshed for user: {}", user.getUsername());

        return generateAuthResponse(user);
    }

    @Transactional
    public void logout(String refreshTokenStr) {
        refreshTokenRepository.findByTokenAndRevokedFalseAndDeleteFlagFalse(refreshTokenStr)
                .ifPresent(token -> {
                    refreshTokenRepository.revokeByToken(refreshTokenStr);
                    log.info("User logged out: {}", token.getUser().getUsername());
                });
    }

    @Transactional
    public void logoutAll(String username) {
        User user = userRepository.findByUsernameAndDeleteFlagFalse(username)
                .orElseThrow(() -> new AuthException("User not found"));

        refreshTokenRepository.revokeAllByUser(user);
        log.info("All sessions revoked for user: {}", username);
    }

    @Transactional
    public AuthResponse generateAuthResponseForOAuth2User(User user) {
        return generateAuthResponse(user);
    }

    private AuthResponse generateAuthResponse(User user) {
        UserDetails userDetails = userDetailsService.loadUserByUsername(user.getUsername());

        String accessToken = jwtService.generateAccessToken(userDetails);
        String refreshTokenStr = jwtService.generateRefreshToken(userDetails);

        // Save refresh token to database
        RefreshToken refreshToken = RefreshToken.builder()
                .user(user)
                .token(refreshTokenStr)
                .expiresAt(LocalDateTime.now().plusSeconds(jwtService.getRefreshTokenExpiration() / 1000))
                .revoked(false)
                .build();

        refreshTokenRepository.save(refreshToken);

        return AuthResponse.of(
                accessToken,
                refreshTokenStr,
                jwtService.getAccessTokenExpiration(),
                UserResponse.fromEntity(user)
        );
    }
}