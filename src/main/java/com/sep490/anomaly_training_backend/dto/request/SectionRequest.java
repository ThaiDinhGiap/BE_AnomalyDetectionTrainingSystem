package com.sep490.anomaly_training_backend.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class SectionRequest {
    private String name;
    private Long managerId;
}