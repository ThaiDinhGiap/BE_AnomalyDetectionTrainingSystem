package com.sep490.anomaly_training_backend.dto.request;

import com.sep490.anomaly_training_backend.enums.EmployeeSkillStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

@Data
@Builder
public class EmployeeSkillRequest {
    Long id;
    Long employeeId;
    String employeeCode;
    String fullName;
    Long processId;
    EmployeeSkillStatus status;
    LocalDate certifiedDate;
    LocalDate expiryDate;
}
