package com.sep490.anomaly_training_backend.dto.response;

import com.sep490.anomaly_training_backend.enums.EmployeeStatus;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class EmployeeResponse {
    private Long id;
    private String employeeCode;
    private String fullName;
    private EmployeeStatus status;

    private Long teamId;
    private String teamName;

    private String groupName;
    private String sectionName;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}