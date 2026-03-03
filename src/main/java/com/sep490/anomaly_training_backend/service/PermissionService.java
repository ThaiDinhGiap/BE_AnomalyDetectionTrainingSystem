package com.sep490.anomaly_training_backend.service;

import com.sep490.anomaly_training_backend.dto.response.ModulePermissionResponse;
import com.sep490.anomaly_training_backend.dto.response.UserPermissionResponse;

import java.util.List;
import java.util.Map;

public interface PermissionService {

    /** Get all permissions grouped by module for a given user */
    Map<String, List<String>> getUserPermissions(Long userId);

    /** Check if user has a specific permission */
    boolean hasPermission(Long userId, String permissionCode);

    /** Get all permissions for the current authenticated user (for FE to build menus) */
    Map<String, List<String>> getCurrentUserPermissions();

    /** Get all modules with their permissions (for role management UI) */
    List<ModulePermissionResponse> getAllModulesWithPermissions();

    /** Get detailed permissions for a specific user including roles */
    UserPermissionResponse getUserPermissionDetail(Long userId);
}
