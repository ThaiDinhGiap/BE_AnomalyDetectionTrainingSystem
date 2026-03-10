package com.sep490.anomaly_training_backend.dto.request;

import lombok.Data;

import java.util.List;


@Data
public class UserCreateRequest {
    private String username;
    private String password;
    private String fullName;
    private String email;
    private String employeeCode;
    private List<Long> roleIds; 
    private Boolean isActive;
}
