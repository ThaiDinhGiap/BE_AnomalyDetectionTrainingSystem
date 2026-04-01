package com.sep490.anomaly_training_backend.service.impl;

import com.sep490.anomaly_training_backend.dto.request.LoginRequest;
import com.sep490.anomaly_training_backend.dto.request.RegisterRequest;
import com.sep490.anomaly_training_backend.dto.response.AuthResponse;
import com.sep490.anomaly_training_backend.enums.OAuthProvider;
import com.sep490.anomaly_training_backend.exception.AppException;
import com.sep490.anomaly_training_backend.model.User;
import com.sep490.anomaly_training_backend.repository.EmployeeRepository;
import com.sep490.anomaly_training_backend.repository.RefreshTokenRepository;
import com.sep490.anomaly_training_backend.repository.RoleRepository;
import com.sep490.anomaly_training_backend.repository.UserRepository;
import com.sep490.anomaly_training_backend.service.JwtService;
import com.sep490.anomaly_training_backend.service.impl.AuthService;
import com.sep490.anomaly_training_backend.service.impl.CustomUserDetailsService;
import com.sep490.anomaly_training_backend.service.notification.impl.MailDispatcher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private RefreshTokenRepository refreshTokenRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private JwtService jwtService;
    @Mock
    private AuthenticationManager authenticationManager;
    @Mock
    private CustomUserDetailsService userDetailsService;
    @Mock
    private RoleRepository roleRepository;
    @Mock
    private EmployeeRepository employeeRepository;
    @Mock
    private MailDispatcher mailDispatcher;

    @InjectMocks
    private AuthService authService;

    private User user;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .id(1L)
                .username("testusr")
                .passwordHash("hash")
                .email("test@email.com")
                .isActive(true)
                .requirePasswordChange(false)
                .oauthProvider(OAuthProvider.LOCAL)
                .build();
    }

    @Test
    void register_WhenUsernameExists_ShouldThrow() {
        RegisterRequest req = new RegisterRequest();
        req.setUsername("testusr");

        when(userRepository.existsByUsernameAndDeleteFlagFalse("testusr")).thenReturn(true);

        assertThatThrownBy(() -> authService.register(req))
                .isInstanceOf(AppException.class)
                .hasMessageContaining("Username already exists");
    }

    @Test
    void login_Success() {
        LoginRequest req = new LoginRequest();
        req.setUsername("testusr");
        req.setPassword("pass");

        when(userRepository.findByUsernameAndDeleteFlagFalse("testusr")).thenReturn(Optional.of(user));
        UserDetails mockDetails = mock(UserDetails.class);
        when(userDetailsService.loadUserByUsername("testusr")).thenReturn(mockDetails);
        when(jwtService.generateAccessToken(mockDetails)).thenReturn("access_token");
        when(jwtService.generateRefreshToken(mockDetails)).thenReturn("refresh_token");
        when(jwtService.getAccessTokenExpiration()).thenReturn(3600L);

        AuthResponse resp = authService.login(req);

        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        assertThat(resp.getAccessToken()).isEqualTo("access_token");
        assertThat(resp.getRefreshToken()).isEqualTo("refresh_token");
    }

    @Test
    void login_WhenUserInactive_ShouldThrow() {
        user.setIsActive(false);
        LoginRequest req = new LoginRequest();
        req.setUsername("testusr");

        when(userRepository.findByUsernameAndDeleteFlagFalse("testusr")).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> authService.login(req))
                .isInstanceOf(AppException.class);
    }
}
