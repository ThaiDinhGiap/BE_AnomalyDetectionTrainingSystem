package com.sep490.anomaly_training_backend.dto.response;

import com.sep490.anomaly_training_backend.enums.ApprovalEntityType;
import com.sep490.anomaly_training_backend.enums.ReportStatus;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;

@Data
@Builder
public class PendingApprovalResponse {
    private ApprovalEntityType entityType;
    private Long entityId;
    private String entityName;
    private ReportStatus status;
    private Integer currentVersion;

    // Submitter info
    private String submittedByUsername;
    private String submittedByFullName;
    private Instant submittedAt;

    // Production Line info
    private Long lineId;
    private String lineName;

    // Summary
    private Integer detailCount;
}