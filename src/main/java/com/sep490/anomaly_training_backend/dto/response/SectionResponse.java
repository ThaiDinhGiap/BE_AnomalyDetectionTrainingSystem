package com.sep490.anomaly_training_backend.dto.response;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class SectionResponse {
    private Long id;
    private String code;
    private String name;

    private Long managerId;
    private String managerName;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}