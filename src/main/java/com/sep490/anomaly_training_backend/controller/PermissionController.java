package com.sep490.anomaly_training_backend.controller;

import com.sep490.anomaly_training_backend.dto.response.ModulePermissionResponse;
import com.sep490.anomaly_training_backend.model.User;
import com.sep490.anomaly_training_backend.service.PermissionService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
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

    @GetMapping("/me")
    public ResponseEntity<Map<String, List<String>>> getMyPermissions(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(permissionService.getUserPermissions(user.getId()));
    }

    @GetMapping("/modules")
    @PreAuthorize("hasAuthority('role.view')")
    public ResponseEntity<List<ModulePermissionResponse>> getAllModulesWithPermissions() {
        return ResponseEntity.ok(permissionService.getAllModulesWithPermissions());
    }
}
