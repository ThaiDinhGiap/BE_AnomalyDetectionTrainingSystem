package com.sep490.anomaly_training_backend.service.impl;

import com.sep490.anomaly_training_backend.dto.response.ModulePermissionResponse;
import com.sep490.anomaly_training_backend.dto.response.PermissionResponse;
import com.sep490.anomaly_training_backend.model.Module;
import com.sep490.anomaly_training_backend.model.Permission;
import com.sep490.anomaly_training_backend.model.Role;
import com.sep490.anomaly_training_backend.model.User;
import com.sep490.anomaly_training_backend.repository.ModuleRepository;
import com.sep490.anomaly_training_backend.repository.UserRepository;
import com.sep490.anomaly_training_backend.service.PermissionService;
import com.sep490.anomaly_training_backend.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PermissionServiceImpl implements PermissionService {

    private final UserRepository userRepository;
    private final ModuleRepository moduleRepository;

    @Override
    @Transactional(readOnly = true)
    public Map<String, List<String>> getUserPermissions(Long userId) {
        User user = userRepository.findByIdWithRolesAndPermissions(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));
        return buildPermissionMap(user);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean hasPermission(Long userId, String permissionCode) {
        Map<String, List<String>> permissions = getUserPermissions(userId);
        return permissions.values().stream()
                .anyMatch(codes -> codes.contains(permissionCode));
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, List<String>> getCurrentUserPermissions() {
        User currentUser = SecurityUtils.getCurrentUserOrThrow();
        return buildPermissionMap(currentUser);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ModulePermissionResponse> getAllModulesWithPermissions() {
        List<Module> modules = moduleRepository.findAll();
        List<ModulePermissionResponse> result = new ArrayList<>();
        for (Module module : modules) {
            List<PermissionResponse> permResponses = module.getPermissions().stream()
                    .map(p -> PermissionResponse.builder()
                            .id(p.getId())
                            .permissionCode(p.getPermissionCode())
                            .displayName(p.getDisplayName())
                            .description(p.getDescription())
                            .action(p.getAction())
                            .build())
                    .collect(Collectors.toList());
            result.add(ModulePermissionResponse.builder()
                    .moduleId(module.getId())
                    .moduleCode(module.getModuleCode())
                    .displayName(module.getDisplayName())
                    .permissions(permResponses)
                    .build());
        }
        return result;
    }

    private Map<String, List<String>> buildPermissionMap(User user) {
        Map<String, List<String>> permissionsByModule = new HashMap<>();
        if (user.getRoles() == null) {
            return permissionsByModule;
        }
        for (Role role : user.getRoles()) {
            if (!Boolean.TRUE.equals(role.getIsActive()) || role.getPermissions() == null) {
                continue;
            }
            for (Permission permission : role.getPermissions()) {
                String moduleCode = permission.getModule() != null
                        ? permission.getModule().getModuleCode()
                        : "general";
                permissionsByModule
                        .computeIfAbsent(moduleCode, k -> new ArrayList<>())
                        .add(permission.getPermissionCode());
            }
        }
        // Deduplicate
        permissionsByModule.replaceAll((k, v) -> v.stream().distinct().collect(Collectors.toList()));
        return permissionsByModule;
    }
}
