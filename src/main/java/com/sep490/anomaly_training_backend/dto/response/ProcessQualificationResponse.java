package com.sep490.anomaly_training_backend.dto.response;

import lombok.Data;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class ProcessQualificationResponse {
    private Long id;

    private Long employeeId;
    private String employeeName;
    private String employeeCode;

    private Long processId;
    private String processName;
    private String processCode;

    private LocalDate certifiedDate;
    private LocalDate expiryDate;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}