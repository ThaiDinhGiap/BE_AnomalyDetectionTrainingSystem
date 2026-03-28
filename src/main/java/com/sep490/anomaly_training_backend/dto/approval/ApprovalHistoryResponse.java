package com.sep490.anomaly_training_backend.dto.approval;

import com.sep490.anomaly_training_backend.enums.ApprovalAction;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.Set;

@Data
@Builder
public class ApprovalHistoryResponse {
    private Long id;
    private Integer entityVersion;
    private Integer stepOrder;
    private String stepName;
    private String requiredPermission;
    private ApprovalAction action;
    private String performedByUsername;
    private String performedByFullName;
    private String performedByRole;
    private String comment;
    private Set<RejectReasonResponse> rejectReasons;
    private Set<RequiredActionResponse> requiredActions;
    private Instant performedAt;
    private String ipAddress;

    @Data
    @Builder
    public static class RejectReasonResponse {
        private Long id;
        private String categoryName;
        private String reasonName;
    }

    @Data
    @Builder
    public static class RequiredActionResponse {
        private Long id;
        private String actionName;
    }
}