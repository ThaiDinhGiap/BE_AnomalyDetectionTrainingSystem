package com.sep490.anomaly_training_backend.dto.request;

import com.sep490.anomaly_training_backend.enums.UserRole;
import lombok.Data;


@Data
public class UserRequest {

    private String username;
    private String password;
    private String fullName;
    private String email;
    private UserRole role;
    private Boolean isActive;

}