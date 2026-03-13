package com.sep490.anomaly_training_backend.service.impl;

import com.sep490.anomaly_training_backend.dto.request.*;
import com.sep490.anomaly_training_backend.dto.response.AuthResponse;
import com.sep490.anomaly_training_backend.dto.response.UserDashboard;
import com.sep490.anomaly_training_backend.dto.response.UserResponse;
import com.sep490.anomaly_training_backend.dto.response.UserRoleDTO;
import com.sep490.anomaly_training_backend.enums.OAuthProvider;
import com.sep490.anomaly_training_backend.exception.AppException;
import com.sep490.anomaly_training_backend.exception.ErrorCode;
import com.sep490.anomaly_training_backend.model.Employee;
import com.sep490.anomaly_training_backend.model.RefreshToken;
import com.sep490.anomaly_training_backend.model.Role;
import com.sep490.anomaly_training_backend.model.User;
import com.sep490.anomaly_training_backend.repository.EmployeeRepository;
import com.sep490.anomaly_training_backend.repository.RefreshTokenRepository;
import com.sep490.anomaly_training_backend.repository.RoleRepository;
import com.sep490.anomaly_training_backend.repository.UserRepository;
import com.sep490.anomaly_training_backend.service.JwtService;
import com.sep490.anomaly_training_backend.service.MailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

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
    private final RoleRepository roleRepository;
    private final EmployeeRepository employeeRepository;
    private final MailService mailService;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByUsernameAndDeleteFlagFalse(request.getUsername())) {
            throw new AppException(ErrorCode.USERNAME_ALREADY_EXISTS);
        }
        if (userRepository.existsByEmailAndDeleteFlagFalse(request.getEmail())) {
            throw new AppException(ErrorCode.EMAIL_ALREADY_EXISTS);
        }

        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .fullName(request.getFullName())
                .oauthProvider(OAuthProvider.LOCAL)
                .isActive(true)
                .build();

        user = userRepository.save(user);
        log.info("New user registered: {}", user.getUsername());

        return generateAuthResponse(user);
    }

    @Transactional
    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
        );

        User user = userRepository.findByUsernameAndDeleteFlagFalse(request.getUsername())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        if (!user.getIsActive()) {
            throw new AppException(ErrorCode.USER_ACCOUNT_INACTIVE);
        }

        log.info("User logged in: {}", user.getUsername());
        return generateAuthResponse(user);
    }

    @Transactional
    public AuthResponse refreshToken(RefreshTokenRequest request) {
        String refreshTokenStr = request.getRefreshToken();

        RefreshToken refreshToken = refreshTokenRepository
                .findByTokenAndRevokedFalseAndDeleteFlagFalse(refreshTokenStr)
                .orElseThrow(() -> new AppException(ErrorCode.INVALID_REFRESH_TOKEN));

        if (refreshToken.getExpiresAt().isBefore(LocalDateTime.now())) {
            refreshTokenRepository.revokeByToken(refreshTokenStr);
            throw new AppException(ErrorCode.REFRESH_TOKEN_EXPIRED);
        }

        User user = refreshToken.getUser();
        if (!user.getIsActive()) {
            throw new AppException(ErrorCode.USER_ACCOUNT_INACTIVE);
        }

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
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

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

    @Transactional
    public UserDashboard createUser(UserCreateRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new AppException(ErrorCode.USERNAME_ALREADY_EXISTS);
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new AppException(ErrorCode.EMAIL_ALREADY_EXISTS);
        }
        if (userRepository.existsByEmployeeCode(request.getEmployeeCode())) {
            throw new AppException(ErrorCode.EMPLOYEE_CODE_ALREADY_LINKED);
        }

        Employee employee = employeeRepository.findByEmployeeCode(request.getEmployeeCode())
                .orElseThrow(() -> new AppException(ErrorCode.EMPLOYEE_NOT_FOUND));

        String rawPassword = generateRandomPassword();
        String encodedPassword = passwordEncoder.encode(rawPassword);

        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .employeeCode(request.getEmployeeCode())
                .fullName(employee.getFullName())
                .passwordHash(encodedPassword)
                .oauthProvider(OAuthProvider.LOCAL)
                .build();

        if (request.getRoleIds() != null && !request.getRoleIds().isEmpty()) {
            Set<Role> roles = new HashSet<>(roleRepository.findAllById(request.getRoleIds()));
            if (roles.size() != request.getRoleIds().size()) {
                throw new AppException(ErrorCode.INVALID_ROLE_IDS);
            }
            user.setRoles(roles);
        }

        User savedUser = userRepository.save(user);

        String subject = "Your Anomaly Training System Account Information";
        String body = "Hello " + savedUser.getFullName() + ",\n\n"
                + "Your account on the system has been created successfully.\n"
                + "Here is your login information:\n"
                + "- Username: " + savedUser.getUsername() + "\n"
                + "- Password: " + rawPassword + "\n\n"
                + "Please log in and change your password immediately for security.\n\n"
                + "Best regards,\nThe System Administration Team.";
        mailService.sendSimpleMail(savedUser.getEmail(), subject, body);

        return toUserDashboard(savedUser);
    }

    private String generateRandomPassword() {
        return java.util.UUID.randomUUID().toString().replace("-", "").substring(0, 8) + "X@1z";
    }

    @Transactional
    public UserDashboard updateUser(Long id, UserUpdateRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        if (!user.getEmail().equals(request.getEmail()) && userRepository.existsByEmail(request.getEmail())) {
            throw new AppException(ErrorCode.EMAIL_ALREADY_EXISTS);
        }
        if (!user.getEmployeeCode().equals(request.getEmployeeCode()) && userRepository.existsByEmployeeCode(request.getEmployeeCode())) {
            throw new AppException(ErrorCode.EMPLOYEE_CODE_ALREADY_LINKED);
        }

        user.setFullName(request.getFullName());
        user.setEmail(request.getEmail());
        user.setEmployeeCode(request.getEmployeeCode());
        if (request.getIsActive() != null) {
            user.setIsActive(request.getIsActive());
        }

        if (request.getRoleIds() != null) {
            Set<Role> roles = new HashSet<>(roleRepository.findAllById(request.getRoleIds()));
            if (roles.size() != request.getRoleIds().size() && !request.getRoleIds().isEmpty()) {
                throw new AppException(ErrorCode.INVALID_ROLE_IDS);
            }
            user.setRoles(roles);
        } else {
            user.getRoles().clear();
        }

        return toUserDashboard(userRepository.save(user));
    }

    private UserDashboard toUserDashboard(User user) {
        if (user == null) {
            return null;
        }
        List<UserRoleDTO> roleDtoList = new ArrayList<>();
        if (user.getRoles() != null && !user.getRoles().isEmpty()) {
            roleDtoList = user.getRoles().stream()
                    .map(role -> UserRoleDTO.builder()
                            .id(role.getId())
                            .roleCode(role.getRoleCode())
                            .displayName(role.getDisplayName())
                            .isActive(role.getIsActive())
                            .build())
                    .collect(Collectors.toList());
        }
        return UserDashboard.builder()
                .id(user.getId())
                .fullName(user.getFullName())
                .employeeCode(user.getEmployeeCode())
                .email(user.getEmail())
                .username(user.getUsername())
                .isActive(user.getIsActive())
                .roles(roleDtoList)
                .build();
    }
}