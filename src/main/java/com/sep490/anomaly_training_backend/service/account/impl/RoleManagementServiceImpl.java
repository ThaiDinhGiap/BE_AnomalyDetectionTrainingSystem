package com.sep490.anomaly_training_backend.service.account.impl;

import com.sep490.anomaly_training_backend.dto.request.RolePermissionRequest;
import com.sep490.anomaly_training_backend.dto.request.RoleRequest;
import com.sep490.anomaly_training_backend.dto.response.PermissionResponse;
import com.sep490.anomaly_training_backend.dto.response.RoleDetailResponse;
import com.sep490.anomaly_training_backend.dto.response.RoleResponse;
import com.sep490.anomaly_training_backend.exception.AppException;
import com.sep490.anomaly_training_backend.exception.ErrorCode;
import com.sep490.anomaly_training_backend.model.Permission;
import com.sep490.anomaly_training_backend.model.Role;
import com.sep490.anomaly_training_backend.repository.PermissionRepository;
import com.sep490.anomaly_training_backend.repository.RoleRepository;
import com.sep490.anomaly_training_backend.service.account.RoleManagementService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RoleManagementServiceImpl implements RoleManagementService {

    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;

    @Override
    @Transactional(readOnly = true)
    public List<RoleResponse> getAllRoles() {
        return roleRepository.findByDeleteFlagFalse().stream()
                .map(this::toRoleResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public RoleDetailResponse getRoleById(Long id) {
        Role role = roleRepository.findByIdWithPermissions(id)
                .orElseThrow(() -> new AppException(ErrorCode.ROLE_NOT_FOUND));
        return toRoleDetailResponse(role);
    }

    @Override
    @Transactional
    public RoleResponse createRole(RoleRequest request) {
        if (roleRepository.existsByRoleCode(request.getRoleCode())) {
            throw new AppException(ErrorCode.ROLE_CODE_ALREADY_EXISTS);
        }
        Role role = Role.builder()
                .roleCode(request.getRoleCode())
                .displayName(request.getDisplayName())
                .description(request.getDescription())
                .isSystem(false)
                .isActive(true)
                .build();
        return toRoleResponse(roleRepository.save(role));
    }

    @Override
    @Transactional
    public RoleResponse updateRole(Long id, RoleRequest request) {
        Role role = roleRepository.findById(id)
                .filter(r -> !r.isDeleteFlag())
                .orElseThrow(() -> new AppException(ErrorCode.ROLE_NOT_FOUND));

        if (Boolean.TRUE.equals(role.getIsSystem())) {
            throw new AppException(ErrorCode.SYSTEM_ROLE_MODIFICATION_NOT_ALLOWED);
        }

        if (!role.getRoleCode().equals(request.getRoleCode())
                && roleRepository.existsByRoleCode(request.getRoleCode())) {
            throw new AppException(ErrorCode.ROLE_CODE_ALREADY_EXISTS);
        }

        role.setRoleCode(request.getRoleCode());
        role.setDisplayName(request.getDisplayName());
        role.setDescription(request.getDescription());
        return toRoleResponse(roleRepository.save(role));
    }

    @Override
    @Transactional
    public void deleteRole(Long id) {
        Role role = roleRepository.findById(id)
                .filter(r -> !r.isDeleteFlag())
                .orElseThrow(() -> new AppException(ErrorCode.ROLE_NOT_FOUND));

        if (Boolean.TRUE.equals(role.getIsSystem())) {
            throw new AppException(ErrorCode.SYSTEM_ROLE_DELETION_NOT_ALLOWED);
        }

        role.setDeleteFlag(true);
        roleRepository.save(role);
    }

    @Override
    @Transactional
    public RoleDetailResponse assignPermissions(Long id, RolePermissionRequest request) {
        Role role = roleRepository.findByIdWithPermissions(id)
                .orElseThrow(() -> new AppException(ErrorCode.ROLE_NOT_FOUND));
        List<Permission> permissions = permissionRepository.findByIdInAndDeleteFlagFalse(request.getPermissionIds());
        role.setPermissions(new HashSet<>(permissions));
        return toRoleDetailResponse(roleRepository.save(role));
    }

    @Override
    @Transactional(readOnly = true)
    public List<PermissionResponse> getRolePermissions(Long id) {
        Role role = roleRepository.findByIdWithPermissions(id)
                .orElseThrow(() -> new AppException(ErrorCode.ROLE_NOT_FOUND));
        return role.getPermissions().stream()
                .map(this::toPermissionResponse)
                .collect(Collectors.toList());
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

    private RoleDetailResponse toRoleDetailResponse(Role role) {
        List<PermissionResponse> permResponses = role.getPermissions() == null ? List.of() :
                role.getPermissions().stream()
                        .map(this::toPermissionResponse)
                        .collect(Collectors.toList());
        return RoleDetailResponse.builder()
                .id(role.getId())
                .roleCode(role.getRoleCode())
                .displayName(role.getDisplayName())
                .description(role.getDescription())
                .isSystem(role.getIsSystem())
                .isActive(role.getIsActive())
                .permissionCount(permResponses.size())
                .userCount(role.getUsers() != null ? role.getUsers().size() : 0)
                .permissions(permResponses)
                .build();
    }

    private PermissionResponse toPermissionResponse(Permission p) {
        return PermissionResponse.builder()
                .id(p.getId())
                .permissionCode(p.getPermissionCode())
                .displayName(p.getDisplayName())
                .description(p.getDescription())
                .action(p.getAction())
                .sortOrder(p.getSortOrder())
                .build();
    }
}