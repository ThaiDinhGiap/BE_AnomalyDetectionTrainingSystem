package com.sep490.anomaly_training_backend.dto.response;

import com.sep490.anomaly_training_backend.enums.OAuthProvider;
import com.sep490.anomaly_training_backend.enums.UserRole;
import com.sep490.anomaly_training_backend.model.Permission;
import com.sep490.anomaly_training_backend.model.Role;
import com.sep490.anomaly_training_backend.model.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.stream.Collectors;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {

    private Long id;
    private String username;
    private String email;
    private String fullName;
    private UserRole role;
    private OAuthProvider oauthProvider;
    private Boolean isActive;
    private List<String> roles;
    private List<String> permissions;

    public static UserResponse fromEntity(User user) {
        List<String> roleCodes = user.getRoles() != null
                ? user.getRoles().stream()
                        .filter(r -> Boolean.TRUE.equals(r.getIsActive()))
                        .map(Role::getRoleCode)
                        .collect(Collectors.toList())
                : List.of();

        List<String> permissionCodes = user.getRoles() != null
                ? user.getRoles().stream()
                        .filter(r -> Boolean.TRUE.equals(r.getIsActive()) && r.getPermissions() != null)
                        .flatMap(r -> r.getPermissions().stream())
                        .map(Permission::getPermissionCode)
                        .distinct()
                        .collect(Collectors.toList())
                : List.of();

        return UserResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .role(user.getRole())
                .oauthProvider(user.getOauthProvider())
                .isActive(user.getIsActive())
                .roles(roleCodes)
                .permissions(permissionCodes)
                .build();
    }
}