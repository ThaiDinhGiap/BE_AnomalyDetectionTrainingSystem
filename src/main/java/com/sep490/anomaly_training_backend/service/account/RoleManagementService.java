package com.sep490.anomaly_training_backend.service.account;

import com.sep490.anomaly_training_backend.dto.request.RolePermissionRequest;
import com.sep490.anomaly_training_backend.dto.request.RoleRequest;
import com.sep490.anomaly_training_backend.dto.response.PermissionResponse;
import com.sep490.anomaly_training_backend.dto.response.RoleDetailResponse;
import com.sep490.anomaly_training_backend.dto.response.RoleResponse;

import java.util.List;

public interface RoleManagementService {

    List<RoleResponse> getAllRoles();

    RoleDetailResponse getRoleById(Long id);

    RoleResponse createRole(RoleRequest request);

    RoleResponse updateRole(Long id, RoleRequest request);

    void deleteRole(Long id);

    RoleDetailResponse assignPermissions(Long id, RolePermissionRequest request);

    List<PermissionResponse> getRolePermissions(Long id);
}
