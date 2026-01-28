package com.sep490.anomaly_training_backend.dto.response;

import com.sep490.anomaly_training_backend.enums.ApprovalAction;
import com.sep490.anomaly_training_backend.enums.UserRole;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;

@Data
@Builder
public class ApprovalHistoryResponse {
    private Long id;
    private Integer entityVersion;
    private Integer stepOrder;
    private String stepName;
    private UserRole requiredRole;
    private ApprovalAction action;
    private String performedByUsername;
    private String performedByFullName;
    private UserRole performedByRole;
    private String comment;
    private String rejectReason;
    private Instant performedAt;
    private String ipAddress;
}