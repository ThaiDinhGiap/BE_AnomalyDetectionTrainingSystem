package com.sep490.anomaly_training_backend.dto.response;

import com.sep490.anomaly_training_backend.enums.EmployeeStatus;
import lombok.Builder;
import lombok.Data;


import java.time.LocalDate;

@Data
@Builder
public class EmployeeSkillResponse {
    Long id;
    String employeeName;
    String employeeCode;
    Long processId;
    Boolean isQualified;
    LocalDate certifiedDate;
    LocalDate expiryDate;
    EmployeeStatus status;
}
