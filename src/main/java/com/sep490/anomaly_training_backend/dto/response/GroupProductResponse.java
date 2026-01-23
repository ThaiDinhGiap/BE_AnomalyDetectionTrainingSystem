package com.sep490.anomaly_training_backend.dto.response;

import lombok.Data;
import java.time.Instant;
import java.time.LocalDateTime;

@Data
public class GroupProductResponse {
    private Long id;
    private String productCode;

    private Long groupId;
    private String groupName;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}