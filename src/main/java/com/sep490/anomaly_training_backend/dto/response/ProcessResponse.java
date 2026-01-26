package com.sep490.anomaly_training_backend.dto.response;

import com.sep490.anomaly_training_backend.enums.ProcessClassification;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class ProcessResponse {
    private Long id;
    private String code;
    private String name;
    private String description;

    private ProcessClassification classification;

    private BigDecimal standardTimeJt;

    private Long groupId;
    private String groupName;

    private LocalDateTime createdAt;
}