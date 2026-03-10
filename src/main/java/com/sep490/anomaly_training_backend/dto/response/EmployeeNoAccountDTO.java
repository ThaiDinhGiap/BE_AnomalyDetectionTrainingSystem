package com.sep490.anomaly_training_backend.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class EmployeeNoAccountDTO {
    private Long id;
    private String employeeCode;
    private String fullName;
    private String teamName;
    private String status;
}
