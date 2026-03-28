package com.sep490.anomaly_training_backend.service.account;

import com.sep490.anomaly_training_backend.dto.response.ModulePermissionResponse;
import com.sep490.anomaly_training_backend.dto.response.UserPermissionResponse;

import java.util.List;
import java.util.Map;

public interface PermissionService {

    Map<String, List<String>> getUserPermissions(Long userId);

    boolean hasPermission(Long userId, String permissionCode);

    Map<String, List<String>> getCurrentUserPermissions();

    List<ModulePermissionResponse> getAllModulesWithPermissions();

    UserPermissionResponse getUserPermissionDetail(Long userId);
}
