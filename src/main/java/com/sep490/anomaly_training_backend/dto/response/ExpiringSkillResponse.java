package com.sep490.anomaly_training_backend.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

@Data
@Builder
public class ExpiringSkillResponse {
    private Long skillId;
    private String employeeName;
    private String employeeCode;
    private String processName;
    private LocalDate expiryDate;
}
