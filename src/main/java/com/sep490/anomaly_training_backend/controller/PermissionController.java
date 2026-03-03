package com.sep490.anomaly_training_backend.controller;

import com.sep490.anomaly_training_backend.dto.response.ApiResponse;
import com.sep490.anomaly_training_backend.dto.response.ModulePermissionResponse;
import com.sep490.anomaly_training_backend.dto.response.UserPermissionResponse;
import com.sep490.anomaly_training_backend.model.User;
import com.sep490.anomaly_training_backend.service.PermissionService;
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
@Tag(name = "Permission Management", description = "API quản lý phân quyền và kiểm tra quyền người dùng")
public class PermissionController {

    private final PermissionService permissionService;

    @Operation(summary = "Lấy quyền của người dùng hiện tại theo module")
    @GetMapping("/me")
    public ResponseEntity<Map<String, List<String>>> getMyPermissions(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(permissionService.getUserPermissions(user.getId()));
    }

    @Operation(summary = "Lấy tất cả module cùng quyền (dùng cho UI cấu hình vai trò)")
    @GetMapping("/modules")
    @PreAuthorize("hasAuthority('role.view')")
    public ResponseEntity<List<ModulePermissionResponse>> getAllModulesWithPermissions() {
        return ResponseEntity.ok(permissionService.getAllModulesWithPermissions());
    }

    @Operation(summary = "Lấy quyền của người dùng cụ thể theo ID")
    @GetMapping("/users/{userId}")
    @PreAuthorize("hasAuthority('user.view')")
    public ResponseEntity<ApiResponse<UserPermissionResponse>> getUserPermissions(@PathVariable Long userId) {
        return ResponseEntity.ok(ApiResponse.success(permissionService.getUserPermissionDetail(userId)));
    }
}

