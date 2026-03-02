package com.sep490.anomaly_training_backend.service;

import com.sep490.anomaly_training_backend.dto.request.UserRoleRequest;
import com.sep490.anomaly_training_backend.dto.response.RoleResponse;

import java.util.List;

public interface UserRoleService {

    List<RoleResponse> getUserRoles(Long userId);

    List<RoleResponse> assignRoles(Long userId, UserRoleRequest request);
}
