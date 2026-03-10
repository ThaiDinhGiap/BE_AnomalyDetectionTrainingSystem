package com.sep490.anomaly_training_backend.dto.response;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class UserDashboard {
    Long id;
    String fullName;
    List<String> roles;
    String employeeCode;
    String email;
    String username;
    boolean isActive;
}
