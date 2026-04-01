package com.sep490.anomaly_training_backend.service.impl;

import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Collections;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class JwtServiceImplTest {

    @InjectMocks
    private JwtServiceImpl jwtService;

    private UserDetails userDetails;

    @BeforeEach
    void setUp() {
        // Example base64 encoded strong key for HMAC-SHA256 (requires at least 256 bits)
        String secretKey = "4c6d62785042637a726678796859553755324558503831776566416664536676";
        ReflectionTestUtils.setField(jwtService, "secretKey", secretKey);
        ReflectionTestUtils.setField(jwtService, "accessTokenExpiration", 3600000L); // 1 hour
        ReflectionTestUtils.setField(jwtService, "refreshTokenExpiration", 86400000L); // 1 day

        userDetails = new User("testusr", "password", Collections.emptyList());
    }

    @Test
    void generateAccessToken_ShouldReturnValidToken() {
        String token = jwtService.generateAccessToken(userDetails);

        assertThat(token).isNotBlank();
        assertThat(jwtService.isTokenValid(token, userDetails)).isTrue();
        assertThat(jwtService.extractUsername(token)).isEqualTo("testusr");
    }

    @Test
    void generateAccessToken_WithExtraClaims_ShouldIncludeClaims() {
        String token = jwtService.generateAccessToken(Map.of("role", "ADMIN"), userDetails);

        assertThat(token).isNotBlank();
        assertThat(jwtService.isTokenValid(token, userDetails)).isTrue();
        
        String role = jwtService.extractClaim(token, claims -> claims.get("role", String.class));
        assertThat(role).isEqualTo("ADMIN");
    }

    @Test
    void generateRefreshToken_ShouldReturnValidToken() {
        String token = jwtService.generateRefreshToken(userDetails);

        assertThat(token).isNotBlank();
        assertThat(jwtService.isTokenValid(token, userDetails)).isTrue();
    }

    @Test
    void validateToken_WhenValid_ShouldReturnTrue() {
        String token = jwtService.generateAccessToken(userDetails);
        assertThat(jwtService.validateToken(token)).isTrue();
    }

    @Test
    void validateToken_WhenInvalidFormat_ShouldReturnFalse() {
        assertThat(jwtService.validateToken("invalid.jwt.token")).isFalse();
    }

    @Test
    void isTokenValid_WhenWrongUser_ShouldReturnFalse() {
        String token = jwtService.generateAccessToken(userDetails);
        UserDetails wrongUser = new User("wrongusr", "password", Collections.emptyList());

        assertThat(jwtService.isTokenValid(token, wrongUser)).isFalse();
    }
}
