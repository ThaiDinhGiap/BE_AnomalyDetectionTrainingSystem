package com.sep490.anomaly_training_backend.controller;

import com.sep490.anomaly_training_backend.dto.response.ApiResponse;
import com.sep490.anomaly_training_backend.dto.response.ModulePermissionResponse;
import com.sep490.anomaly_training_backend.dto.response.UserPermissionResponse;
import com.sep490.anomaly_training_backend.model.User;
import com.sep490.anomaly_training_backend.service.account.PermissionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/permissions")
@RequiredArgsConstructor
@Tag(name = "Permission Management", description = "API for managing permissions and user authorization")
public class PermissionController {

    private final PermissionService permissionService;

    @Operation(summary = "Get current user permissions by module")
    @GetMapping("/me")
    public ResponseEntity<Map<String, List<String>>> getMyPermissions(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(permissionService.getUserPermissions(user.getId()));
    }

    @Operation(summary = "Get all modules with permissions (for role configuration UI)")
    @GetMapping("/modules")
    @PreAuthorize("hasAuthority('role.view')")
    public ResponseEntity<List<ModulePermissionResponse>> getAllModulesWithPermissions() {
        return ResponseEntity.ok(permissionService.getAllModulesWithPermissions());
    }

    @Operation(summary = "Get permissions of a specific user by ID")
    @GetMapping("/users/{userId}")
    @PreAuthorize("hasAuthority('user.view')")
    public ResponseEntity<ApiResponse<UserPermissionResponse>> getUserPermissions(@PathVariable Long userId) {
        return ResponseEntity.ok(ApiResponse.success(permissionService.getUserPermissionDetail(userId)));
    }
}

