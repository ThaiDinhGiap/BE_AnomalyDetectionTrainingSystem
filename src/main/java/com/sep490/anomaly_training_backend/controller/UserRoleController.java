package com.sep490.anomaly_training_backend.controller;

import com.sep490.anomaly_training_backend.dto.request.UserRoleRequest;
import com.sep490.anomaly_training_backend.dto.response.ApiResponse;
import com.sep490.anomaly_training_backend.dto.response.RoleResponse;
import com.sep490.anomaly_training_backend.service.account.UserRoleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@Tag(name = "User Role Management", description = "API for assigning roles to users")
public class UserRoleController {

    private final UserRoleService userRoleService;

    @Operation(summary = "Get user roles")
    @GetMapping("/{userId}/roles")
    @PreAuthorize("hasAuthority('user.manage')")
    public ResponseEntity<ApiResponse<List<RoleResponse>>> getUserRoles(@PathVariable Long userId) {
        return ResponseEntity.ok(ApiResponse.success(userRoleService.getUserRoles(userId)));
    }

    @Operation(summary = "Assign roles to user (replace all)")
    @PutMapping("/{userId}/roles")
    @PreAuthorize("hasAuthority('user.manage')")
    public ResponseEntity<ApiResponse<List<RoleResponse>>> assignRoles(
            @PathVariable Long userId,
            @Valid @RequestBody UserRoleRequest request) {
        return ResponseEntity.ok(ApiResponse.success(userRoleService.assignRoles(userId, request)));
    }
}
