package com.sep490.anomaly_training_backend.enums;

public enum TrainingPlanStatus {
    DRAFT,
    WAITING_SV,
    REJECTED_BY_SV,
    WAITING_MANAGER,
    REJECTED_BY_MANAGER,
    APPROVED_BY_SV,
    APPROVED_BY_MANAGER,
    NEED_UPDATE,
    APPROVED
}
