package com.sep490.anomaly_training_backend.dto.response;

import lombok.Data;
import java.time.Instant;
import java.time.LocalDateTime;

@Data
public class GroupResponse {
    private Long id;
    private String name;

    private Long sectionId;
    private String sectionName;

    private Long supervisorId;
    private String supervisorName;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}