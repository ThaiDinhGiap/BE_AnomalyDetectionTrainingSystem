package com.sep490.anomaly_training_backend.service.impl;

import com.sep490.anomaly_training_backend.dto.request.UserRoleRequest;
import com.sep490.anomaly_training_backend.dto.response.RoleResponse;
import com.sep490.anomaly_training_backend.exception.BusinessException;
import com.sep490.anomaly_training_backend.model.Role;
import com.sep490.anomaly_training_backend.model.User;
import com.sep490.anomaly_training_backend.repository.RoleRepository;
import com.sep490.anomaly_training_backend.repository.UserRepository;
import com.sep490.anomaly_training_backend.service.UserRoleService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserRoleServiceImpl implements UserRoleService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;

    @Override
    @Transactional(readOnly = true)
    public List<RoleResponse> getUserRoles(Long userId) {
        User user = userRepository.findByIdWithRolesAndPermissions(userId)
                .orElseThrow(() -> new BusinessException("User not found: " + userId, HttpStatus.NOT_FOUND));
        return user.getRoles() == null ? List.of() :
                user.getRoles().stream()
                        .filter(r -> !r.isDeleteFlag())
                        .map(this::toRoleResponse)
                        .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public List<RoleResponse> assignRoles(Long userId, UserRoleRequest request) {
        User user = userRepository.findByIdWithRolesAndPermissions(userId)
                .orElseThrow(() -> new BusinessException("User not found: " + userId, HttpStatus.NOT_FOUND));
        List<Role> roles = roleRepository.findByDeleteFlagFalse().stream()
                .filter(r -> request.getRoleIds().contains(r.getId()))
                .collect(Collectors.toList());
        if (roles.size() != request.getRoleIds().size()) {
            throw new BusinessException("One or more role IDs not found or have been deleted");
        }
        user.setRoles(new HashSet<>(roles));
        userRepository.save(user);
        return roles.stream().map(this::toRoleResponse).collect(Collectors.toList());
    }

    private RoleResponse toRoleResponse(Role role) {
        return RoleResponse.builder()
                .id(role.getId())
                .roleCode(role.getRoleCode())
                .displayName(role.getDisplayName())
                .description(role.getDescription())
                .isSystem(role.getIsSystem())
                .isActive(role.getIsActive())
                .permissionCount(role.getPermissions() != null ? role.getPermissions().size() : 0)
                .userCount(role.getUsers() != null ? role.getUsers().size() : 0)
                .build();
    }
}
