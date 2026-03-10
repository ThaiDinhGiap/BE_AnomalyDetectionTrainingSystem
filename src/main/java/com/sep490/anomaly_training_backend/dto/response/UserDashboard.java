package com.sep490.anomaly_training_backend.dto.response;

import com.sep490.anomaly_training_backend.enums.UserRole;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserDashboard {
    Long id;
    String fullName;
    UserRole role;
    String employeeCode;
    String email;
    String username;
    boolean isActive;
}
