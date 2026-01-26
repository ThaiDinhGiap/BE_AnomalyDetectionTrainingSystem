package com.sep490.anomaly_training_backend.dto.request;

import com.sep490.anomaly_training_backend.enums.EmployeeStatus;
import lombok.Data;

@Data
public class EmployeeRequest {
    private String employeeCode;

    private String fullName;

    private Long teamId;

    private EmployeeStatus status;
}