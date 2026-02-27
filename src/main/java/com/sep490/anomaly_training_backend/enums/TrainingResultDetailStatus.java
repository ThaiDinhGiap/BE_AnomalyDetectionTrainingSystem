package com.sep490.anomaly_training_backend.enums;

/**
 * Status for training result details
 */
public enum TrainingResultDetailStatus {
    PENDING,
    DONE,
    NEED_SIGN,
    WAITING_SV,
    REJECTED_BY_SV,
    APPROVED
}
