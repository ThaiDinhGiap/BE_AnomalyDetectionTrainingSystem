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
    
    // Group info
    private Long groupId;
    private String groupName;
    
    // Summary
    private Integer detailCount;
}