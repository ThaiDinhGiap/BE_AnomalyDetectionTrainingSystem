package com.sep490.anomaly_training_backend.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

@Data
public class ProcessQualificationRequest {
    private Long employeeId;

    private Long processId;

    private LocalDate certifiedDate;

    private LocalDate expiryDate; // Có thể null (vô thời hạn)
}