package com.sep490.anomaly_training_backend.dto.request;

import lombok.Data;

@Data
public class SectionRequest {
    private String name;
    private String code;
    private Long managerId;
}