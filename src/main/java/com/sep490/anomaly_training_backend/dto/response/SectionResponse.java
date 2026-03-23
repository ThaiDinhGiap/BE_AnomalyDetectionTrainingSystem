package com.sep490.anomaly_training_backend.dto.response;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class SectionResponse {
    private Long id;
    private String code;
    private String name;

    private Long managerId;
    private String managerName;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    List<GroupResponse> groups;
    List<ProductLineResponse> productLines;
}