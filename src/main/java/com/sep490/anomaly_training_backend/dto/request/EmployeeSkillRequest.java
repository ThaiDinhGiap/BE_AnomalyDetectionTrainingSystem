package com.sep490.anomaly_training_backend.dto.request;

import com.sep490.anomaly_training_backend.enums.EmployeeSkillStatus;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class EmployeeSkillRequest {
    Long id;
    Long employeeId;
    Long processId;
    EmployeeSkillStatus status;
}
