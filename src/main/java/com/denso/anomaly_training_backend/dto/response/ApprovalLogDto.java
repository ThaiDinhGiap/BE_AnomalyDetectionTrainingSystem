package com.denso.anomaly_training_backend.dto.response;

import com.denso.anomaly_training_backend.enums.ApprovalAction;
import com.denso.anomaly_training_backend.enums.TrainingPlanStatus;
import lombok.Data;
import java.time.Instant;
import java.time.LocalDateTime;

@Data
public class ApprovalLogDto {
    private Long id;
    private String processedByName;
    private String processedRole;
    private ApprovalAction action;
    private TrainingPlanStatus resultingStatus;
    private String comment;
    private LocalDateTime createdAt;
}