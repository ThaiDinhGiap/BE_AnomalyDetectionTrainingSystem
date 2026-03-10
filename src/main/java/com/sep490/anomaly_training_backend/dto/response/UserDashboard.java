package com.sep490.anomaly_training_backend.dto.response;

import com.sep490.anomaly_training_backend.enums.UserRole;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class UserDashboard {
    Long id;
    String fullName;
    String employeeCode;
    String email;
    String username;
    boolean isActive;
    private List<UserRoleDTO> roles;
}
