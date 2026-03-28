package com.sep490.anomaly_training_backend.controller;

import com.sep490.anomaly_training_backend.dto.request.RolePermissionRequest;
import com.sep490.anomaly_training_backend.dto.request.RoleRequest;
import com.sep490.anomaly_training_backend.dto.response.ApiResponse;
import com.sep490.anomaly_training_backend.dto.response.PermissionResponse;
import com.sep490.anomaly_training_backend.dto.response.RoleDetailResponse;
import com.sep490.anomaly_training_backend.dto.response.RoleResponse;
import com.sep490.anomaly_training_backend.service.account.RoleService;
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
@Tag(name = "Role Management", description = "API for managing roles and role permissions")
public class RoleController {

    private final RoleService roleService;

    @Operation(summary = "Get all roles")
    @GetMapping
    @PreAuthorize("hasAuthority('role.manage')")
    public ResponseEntity<ApiResponse<List<RoleResponse>>> getAllRoles() {
        return ResponseEntity.ok(ApiResponse.success(roleService.getAllRoles()));
    }

    @Operation(summary = "Get role details by ID")
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('role.manage')")
    public ResponseEntity<ApiResponse<RoleDetailResponse>> getRoleById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(roleService.getRoleById(id)));
    }

    @Operation(summary = "Create new role")
    @PostMapping
    @PreAuthorize("hasAuthority('role.manage')")
    public ResponseEntity<ApiResponse<RoleResponse>> createRole(@Valid @RequestBody RoleRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(roleService.createRole(request)));
    }

    @Operation(summary = "Update role")
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('role.manage')")
    public ResponseEntity<ApiResponse<RoleResponse>> updateRole(
            @PathVariable Long id,
            @Valid @RequestBody RoleRequest request) {
        return ResponseEntity.ok(ApiResponse.success(roleService.updateRole(id, request)));
    }

    @Operation(summary = "Soft delete role (non-system roles only)")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('role.manage')")
    public ResponseEntity<ApiResponse<Void>> deleteRole(@PathVariable Long id) {
        roleService.deleteRole(id);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @Operation(summary = "Assign permissions to role (replace all)")
    @PutMapping("/{id}/permissions")
    @PreAuthorize("hasAuthority('role.manage')")
    public ResponseEntity<ApiResponse<RoleDetailResponse>> assignPermissions(
            @PathVariable Long id,
            @Valid @RequestBody RolePermissionRequest request) {
        return ResponseEntity.ok(ApiResponse.success(roleService.assignPermissions(id, request)));
    }

    @Operation(summary = "Get role permissions")
    @GetMapping("/{id}/permissions")
    @PreAuthorize("hasAuthority('role.manage')")
    public ResponseEntity<ApiResponse<List<PermissionResponse>>> getRolePermissions(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(roleService.getRolePermissions(id)));
    }
}
