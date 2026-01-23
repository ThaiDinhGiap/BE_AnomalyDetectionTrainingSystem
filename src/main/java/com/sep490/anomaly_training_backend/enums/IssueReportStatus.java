package com.sep490.anomaly_training_backend.enums;

public enum IssueReportStatus {
    DRAFT,
    WAITING_SV,
    REJECTED_BY_SV,
    WAITING_MANAGER,
    REJECTED_BY_MANAGER,
    APPROVED,
    TRAINING_REQUIRED,
    TRAINING_CREATED,
    NEED_UPDATE
}
