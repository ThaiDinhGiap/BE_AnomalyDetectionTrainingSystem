package com.sep490.anomaly_training_backend.dto.response;

import com.sep490.anomaly_training_backend.enums.EmployeeSkillStatus;
import lombok.Builder;
import lombok.Data;
import java.time.LocalDate;

@Data
@Builder
public class EmployeeSkillResponse {
    Long id;
    String employeeName;
    String employeeCode;
    ProcessResponse process;
    LocalDate certifiedDate;
    LocalDate expiryDate;
    EmployeeSkillStatus status;
}
