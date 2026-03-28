package com.sep490.anomaly_training_backend.dto.response;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class TrainingResultListResponse {
    private Long id;
    private String title;
    private Long lineId;
    private String lineName;
    private String monthList;
    private LocalDateTime createdAt;
    private String status;
    private String createdBy;

    private long totalItems;
    private long totalPass;
    private long totalFail;
    private long totalNotYetTrained;
    private BigDecimal passRate;
}
