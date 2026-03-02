package com.sep490.anomaly_training_backend.controller;

import com.sep490.anomaly_training_backend.dto.request.RolePermissionRequest;
import com.sep490.anomaly_training_backend.dto.request.RoleRequest;
import com.sep490.anomaly_training_backend.dto.response.ApiResponse;
import com.sep490.anomaly_training_backend.dto.response.PermissionResponse;
import com.sep490.anomaly_training_backend.dto.response.RoleDetailResponse;
import com.sep490.anomaly_training_backend.dto.response.RoleResponse;
import com.sep490.anomaly_training_backend.service.RoleManagementService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/roles")
@RequiredArgsConstructor
@Tag(name = "Role Management", description = "API quản lý vai trò và phân quyền cho vai trò")
public class RoleController {

    private final RoleManagementService roleManagementService;

    @Operation(summary = "Lấy danh sách tất cả vai trò")
    @GetMapping
    @PreAuthorize("hasAuthority('role.view')")
    public ResponseEntity<ApiResponse<List<RoleResponse>>> getAllRoles() {
        return ResponseEntity.ok(ApiResponse.success(roleManagementService.getAllRoles()));
    }

    @Operation(summary = "Lấy chi tiết vai trò theo ID")
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('role.view')")
    public ResponseEntity<ApiResponse<RoleDetailResponse>> getRoleById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(roleManagementService.getRoleById(id)));
    }

    @Operation(summary = "Tạo mới vai trò")
    @PostMapping
    @PreAuthorize("hasAuthority('role.create')")
    public ResponseEntity<ApiResponse<RoleResponse>> createRole(@Valid @RequestBody RoleRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(roleManagementService.createRole(request)));
    }

    @Operation(summary = "Cập nhật vai trò")
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('role.edit')")
    public ResponseEntity<ApiResponse<RoleResponse>> updateRole(
            @PathVariable Long id,
            @Valid @RequestBody RoleRequest request) {
        return ResponseEntity.ok(ApiResponse.success(roleManagementService.updateRole(id, request)));
    }

    @Operation(summary = "Xoá mềm vai trò (chỉ vai trò không phải hệ thống)")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('role.delete')")
    public ResponseEntity<ApiResponse<Void>> deleteRole(@PathVariable Long id) {
        roleManagementService.deleteRole(id);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @Operation(summary = "Gán quyền cho vai trò (thay thế toàn bộ)")
    @PutMapping("/{id}/permissions")
    @PreAuthorize("hasAuthority('role.assign_permission')")
    public ResponseEntity<ApiResponse<RoleDetailResponse>> assignPermissions(
            @PathVariable Long id,
            @Valid @RequestBody RolePermissionRequest request) {
        return ResponseEntity.ok(ApiResponse.success(roleManagementService.assignPermissions(id, request)));
    }

    @Operation(summary = "Lấy danh sách quyền của vai trò")
    @GetMapping("/{id}/permissions")
    @PreAuthorize("hasAuthority('role.view')")
    public ResponseEntity<ApiResponse<List<PermissionResponse>>> getRolePermissions(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(roleManagementService.getRolePermissions(id)));
    }
}
