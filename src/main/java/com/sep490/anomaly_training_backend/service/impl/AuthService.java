package com.sep490.anomaly_training_backend.service.impl;

import com.sep490.anomaly_training_backend.dto.request.*;
import com.sep490.anomaly_training_backend.dto.response.AuthResponse;
import com.sep490.anomaly_training_backend.dto.response.UserDashboard;
import com.sep490.anomaly_training_backend.dto.response.UserResponse;
import com.sep490.anomaly_training_backend.dto.response.UserRoleDTO;
import com.sep490.anomaly_training_backend.enums.OAuthProvider;
import com.sep490.anomaly_training_backend.exception.AuthException;
import com.sep490.anomaly_training_backend.exception.BusinessException;
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
import org.springframework.security.authentication.BadCredentialsException;
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
        // Check if username already exists
        if (userRepository.existsByUsernameAndDeleteFlagFalse(request.getUsername())) {
            throw new AuthException("Username already exists");
        }

        // Check if email already exists
        if (userRepository.existsByEmailAndDeleteFlagFalse(request.getEmail())) {
            throw new AuthException("Email already exists");
        }

        // Create new user
        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .fullName(request.getFullName())
                .roles(request.getRoles())
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
                    .orElseThrow(() -> new AuthException("User not found"));

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

    @Transactional
    public UserDashboard createUser(UserCreateRequest request) {
        // 1. Kiểm tra trùng lặp tài khoản
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new BusinessException("Username '" + request.getUsername() + "' đã tồn tại.");
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BusinessException("Email '" + request.getEmail() + "' đã tồn tại.");
        }
        if (userRepository.existsByEmployeeCode(request.getEmployeeCode())) {
            throw new BusinessException("Nhân viên có mã " + request.getEmployeeCode() + " đã có tài khoản.");
        }

        Employee employee = employeeRepository.findByEmployeeCode(request.getEmployeeCode())
                .orElseThrow(() -> new BusinessException("Không tìm thấy nhân viên với mã: " + request.getEmployeeCode()));

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

        // 5. Gán Role từ List ID
        if (request.getRoleIds() != null && !request.getRoleIds().isEmpty()) {
            Set<Role> roles = new HashSet<>(roleRepository.findAllById(request.getRoleIds()));

            // Kiểm tra xem số lượng Role tìm thấy có khớp với số ID truyền lên không
            if (roles.size() != request.getRoleIds().size()) {
                throw new BusinessException("Một hoặc nhiều Role ID không hợp lệ.");
            }
            user.setRoles(roles);
        }

        // 6. Lưu User vào Database
        User savedUser = userRepository.save(user);

        // 7. Gửi email thông báo mật khẩu (Sử dụng nguyên bản hàm sendSimpleMail của bạn)
        String subject = "Thông tin tài khoản hệ thống Anomaly Training";
        String body = "Xin chào " + savedUser.getFullName() + ",\n\n"
                + "Tài khoản của bạn trên hệ thống đã được tạo thành công.\n"
                + "Đây là thông tin đăng nhập của bạn:\n"
                + "- Tên đăng nhập: " + savedUser.getUsername() + "\n"
                + "- Mật khẩu: " + rawPassword + "\n\n"
                + "Vui lòng đăng nhập và đổi mật khẩu ngay trong lần sử dụng đầu tiên để bảo mật tài khoản.\n\n"
                + "Trân trọng,\nBan Quản Trị Hệ Thống.";

        mailService.sendSimpleMail(savedUser.getEmail(), subject, body);
        return toUserDashboard(savedUser);
    }

    private String generateRandomPassword() {
        String randomStr = java.util.UUID.randomUUID().toString().replace("-", "").substring(0, 8);
        return randomStr + "X@1z";
    }

    @Transactional
    public UserDashboard updateUser(Long id, UserUpdateRequest request) {
        // 1. Tìm User
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng"));

        // 2. Kiểm tra trùng lặp nếu họ đổi Email hoặc EmployeeCode
        if (!user.getEmail().equals(request.getEmail()) && userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email đã tồn tại ở một tài khoản khác");
        }
        if (!user.getEmployeeCode().equals(request.getEmployeeCode()) && userRepository.existsByEmployeeCode(request.getEmployeeCode())) {
            throw new RuntimeException("Mã nhân viên đã được gán cho một tài khoản khác");
        }

        user.setFullName(request.getFullName());
        user.setEmail(request.getEmail());
        user.setEmployeeCode(request.getEmployeeCode());
        if (request.getIsActive() != null) {
            user.setIsActive(request.getIsActive());
        }

        // 4. Cập nhật Roles
        if (request.getRoleIds() != null) {
            // Lấy danh sách role mới từ DB và thay thế hoàn toàn danh sách cũ
            Set<Role> roles = new HashSet<>(roleRepository.findAllById(request.getRoleIds()));
            user.setRoles(roles);
        } else {
            // Nếu gửi lên mảng rỗng hoặc null, tức là muốn xóa hết quyền
            user.getRoles().clear();
        }

        return toUserDashboard(userRepository.save(user));
    }

    // Hàm mapper chuyển đổi từ Entity sang DTO
    private UserDashboard toUserDashboard(User user) {
        if (user == null) {
            return null;
        }

        // Chuyển đổi an toàn danh sách Role
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

        // Build và trả về DTO
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