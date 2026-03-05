package com.sep490.anomaly_training_backend.dto.response;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class TrainingResultListResponse {
    private Long id;
    private String title;
    private Long lineId;
    private String lineName;
    private String monthList;
    private LocalDateTime createdAt;
    private LocalDateTime approvedAt;
    private String status;
    private String createdBy;
}